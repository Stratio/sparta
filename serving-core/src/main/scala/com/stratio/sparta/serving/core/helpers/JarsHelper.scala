/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.helpers

import java.io.File
import java.lang.reflect.Method
import java.net.{URL, URLClassLoader}

import scala.util.Try
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.utils.UserFirstURLClassLoader
import org.apache.commons.io.FileUtils
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.workflow.Workflow
import com.stratio.sparta.serving.core.services.{HdfsFilesService, HdfsService}

import scala.collection.mutable

object JarsHelper extends JarsHelper {

  private var pluginsClassLoader: ClassLoader = Thread.currentThread().getContextClassLoader
  private val pluginsModifiedRuntime: mutable.Set[URL] = mutable.Set.empty

  def getClassLoader: ClassLoader = synchronized {
    pluginsClassLoader
  }

  def addPlugins(pluginPath: Seq[URL]): ClassLoader = synchronized { // TODO replace Thread.currentThread() with initialClassLoader
    pluginPath.foreach(pluginsModifiedRuntime.add)
    pluginsClassLoader = UserFirstURLClassLoader(pluginsModifiedRuntime.toArray, Thread.currentThread().getContextClassLoader)
    pluginsClassLoader
  }

}

trait JarsHelper extends SLF4JLogging {

  protected lazy val hdfsService = HdfsService()
  protected lazy val hdfsFilesService = HdfsFilesService()

  /**
    * Finds files that are the driver application.
    *
    * @param path base path when it starts to scan in order to find plugins.
    * @return a list of jars.
    */
  def findDriverByPath(path: File): Seq[File] = {
    if (path.exists) {
      val these = path.listFiles()
      val good =
        these.filter(f => f.getName.toLowerCase.contains("driver") &&
          f.getName.toLowerCase.contains("sparta") &&
          f.getName.endsWith(".jar"))

      good ++ these.filter(_.isDirectory).flatMap(path => findDriverByPath(path))
    } else {
      log.warn(s"The file ${path.getName} not exists.")
      Seq()
    }
  }

  def localUserPluginJars(workflow: Workflow): Seq[String] =
    userJars(workflow)

  def clusterUserPluginJars(workflow: Workflow): Seq[String] =
    (userJars(workflow) ++ JarsHelper.getJdbcDriverPaths).distinct

  def getJdbcDriverPaths: Seq[String] = {
    val jdbcDrivers = new File("/jdbc-drivers")
    if (jdbcDrivers.exists && jdbcDrivers.isDirectory) {
      jdbcDrivers.listFiles()
        .filter(file => file.isFile && file.getName.endsWith("jar"))
        .map(file => file.getAbsolutePath)
    } else Seq.empty[String]
  }

  def addJdbcDriversToClassPath(): Unit =
    addJarsToClassPath(getJdbcDriverPaths)

  def addJarsToClassPath(jarFiles: Seq[String]): Unit =
    jarFiles.foreach { filePath =>
      log.info(s"Adding jar file to classpath: $filePath")
      if (filePath.startsWith("/") || filePath.startsWith("file://")) addFromLocal(filePath)
      if (filePath.startsWith("hdfs")) addFromHdfs(filePath)
      if (filePath.startsWith("http")) addFromHttp(filePath)
    }

  def getLocalPathFromJars(jarFiles: Seq[String]): Seq[String] =
    jarFiles.flatMap { filePath =>
      if (filePath.startsWith("/") || filePath.startsWith("file://"))
        Option(pathFromLocal(filePath))
      else if (filePath.startsWith("hdfs") || filePath.startsWith("http"))
        Option(pathFromURI(filePath))
      else None
    }

  def addDyplonCrossdataPluginsToClassPath(): Unit =
    addJarsToClassPath(getDyplonCrossdataPluginsPaths)

  def addDyplonSpartaPluginsToClassPath(): Unit =
    addJarsToClassPath(getDyplonSpartaPluginsPaths(SecurityManagerHelper.dyplonFacadeEnabled))

  def getDyplonCrossdataPluginsPaths: Seq[String] = {
    val xdDrivers = new File(Try(SpartaConfig.getCrossdataConfig().get.getString("session.sparkjars-path"))
      .getOrElse("/opt/sds/sparta/repo"))
    if (xdDrivers.exists && xdDrivers.isDirectory) {
      xdDrivers.listFiles()
        .filter(file => file.isFile && file.getName.startsWith("dyplon-crossdata") && file.getName.endsWith("jar")
          && file.getName.contains(SpartaConfig.getCrossdataConfig().get.getString("security.plugin.version")))
        .map(file => file.getAbsolutePath)
    } else Seq.empty[String]
  }

  def getDyplonSpartaPluginsPaths(facade: Boolean): Seq[String] = {
    val dyplonJars = new File("/opt/sds/sparta/dyplon-sparta")
    if (dyplonJars.exists && dyplonJars.isDirectory) {
      val filteredJars = {
        if(facade)
          dyplonJars.listFiles().filter(file => file.isFile && file.getName == "dyplon-sparta-facade.jar")
        else dyplonJars.listFiles().filter(file => file.isFile && file.getName == "dyplon-sparta.jar")
      }

      filteredJars.map(file => file.getAbsolutePath)
    } else Seq.empty[String]
  }

  private def pathFromLocal(filePath: String): String = filePath.replace("file://", "")

  private def addJarToClasspath(file: File): Unit = {
    if (file.exists) {
      val method: Method = classOf[URLClassLoader].getDeclaredMethod("addURL", classOf[URL])

      method.setAccessible(true)
      method.invoke(getClass.getClassLoader, file.toURI.toURL)
    } else {
      log.warn(s"The file ${file.getName} not exists in path ${file.getAbsolutePath}")
    }
  }

  private def addFromLocal(filePath: String): Unit = {
    log.debug(s"Getting file from local: $filePath")
    val file = new File(pathFromLocal(filePath))
    addJarToClasspath(file)
  }

  private def pathFromURI(fileURI: String): String =
    s"/tmp/sparta/userjars/${fileURI.split("/").last}"

  private def addFromHdfs(fileHdfsPath: String): Unit = {
    log.debug(s"Getting file from HDFS: $fileHdfsPath")
    val fileName = fileHdfsPath.split("/").last
    log.debug(s"HDFS file name is $fileName")
    val file = new File(pathFromURI(fileHdfsPath))
    log.debug(s"Downloading HDFS file to local file system: ${file.getAbsoluteFile}")
    FileUtils.copyInputStreamToFile(hdfsService.getFile(fileHdfsPath), file)
    addJarToClasspath(file)
  }

  private def addFromHttp(fileURI: String): Unit = {
    log.debug(s"Getting file from HTTP: $fileURI")
    val file = new File(pathFromURI(fileURI))
    val url = new URL(fileURI)
    FileUtils.copyURLToFile(url, file)
    addJarToClasspath(file)
  }

  private def userJars(workflow: Workflow): Seq[String] = {
    val uploadedPlugins = if (workflow.settings.global.addAllUploadedPlugins)
      Try {
        hdfsFilesService.browsePlugins.flatMap { fileStatus =>
          if (fileStatus.isFile && fileStatus.getPath.getName.endsWith(".jar"))
            Option(fileStatus.getPath.toUri.toString)
          else None
        }
      }.getOrElse(Seq.empty[String])
    else Seq.empty[String]
    val userPlugins = workflow.settings.global.userPluginsJars
      .filter(userJar => userJar.jarPath.toString.nonEmpty && userJar.jarPath.toString.endsWith(".jar"))
      .map(_.jarPath.toString.trim)
      .distinct

    (uploadedPlugins ++ userPlugins).filter(_.nonEmpty).distinct
  }
}
