/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.helpers

import java.io.File
import java.lang.reflect.Method
import java.net.{URL, URLClassLoader}
import java.util.{Calendar, UUID}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.services.HdfsService
import org.apache.commons.io.FileUtils

object JarsHelper extends JarsHelper

trait JarsHelper extends SLF4JLogging {

  protected lazy val hdfsService = HdfsService()

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

  def getJdbcDriverPaths: Seq[String] = {
    val jdbcDrivers = new File("/jdbc-drivers")
    if (jdbcDrivers.exists && jdbcDrivers.isDirectory) {
      jdbcDrivers.listFiles()
        .filter(file => file.isFile && file.getName.endsWith("jar"))
        .map(file => file.getAbsolutePath)
    } else Seq.empty[String]
  }

  /**
    * Adds a file to the classpath of the application.
    *
    * @param file to add in the classpath.
    */
  def addJarToClasspath(file: File): Unit = {
    if (file.exists) {
      val method: Method = classOf[URLClassLoader].getDeclaredMethod("addURL", classOf[URL])

      method.setAccessible(true)
      method.invoke(getClass.getClassLoader, file.toURI.toURL)
    } else {
      log.warn(s"The file ${file.getName} not exists.")
    }
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

  private def pathFromLocal(filePath: String) : String = filePath.replace("file://", "")

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

}
