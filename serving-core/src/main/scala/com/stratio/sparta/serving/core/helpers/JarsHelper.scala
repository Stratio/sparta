/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparta.serving.core.helpers

import java.io.File
import java.lang.reflect.Method
import java.net.{URL, URLClassLoader}
import java.util.{Calendar, UUID}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.services.HdfsService
import org.apache.commons.io.FileUtils

object JarsHelper extends SLF4JLogging {

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

  private def addFromLocal(filePath: String): Unit = {
    log.debug(s"Getting file from local: $filePath")
    val file = new File(filePath.replace("file://", ""))
    addJarToClasspath(file)
  }

  private def addFromHdfs(fileHdfsPath: String): Unit = {
    log.debug(s"Getting file from HDFS: $fileHdfsPath")
    val inputStream = HdfsService().getFile(fileHdfsPath)
    val fileName = fileHdfsPath.split("/").last
    log.debug(s"HDFS file name is $fileName")
    val file = new File(s"/tmp/sparta/userjars/${UUID.randomUUID().toString}/$fileName")
    log.debug(s"Downloading HDFS file to local file system: ${file.getAbsoluteFile}")
    FileUtils.copyInputStreamToFile(inputStream, file)
    addJarToClasspath(file)
  }

  private def addFromHttp(fileURI: String): Unit = {
    log.debug(s"Getting file from HTTP: $fileURI")
    val tempFile = File.createTempFile(s"sparta-plugin-${Calendar.getInstance().getTimeInMillis}", ".jar")
    val url = new URL(fileURI)
    FileUtils.copyURLToFile(url, tempFile)
    addJarToClasspath(tempFile)
  }

}
