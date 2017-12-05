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

package com.stratio.sparta.driver.helpers

import java.io.File
import java.net.URL
import java.nio.file.Paths

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.services.HdfsService
import org.apache.commons.io.FileUtils

object PluginFilesHelper extends SLF4JLogging {

  lazy private val hdfsService = HdfsService()

  def downloadPlugins(pluginsFiles: Seq[String], addToClasspath: Boolean = true): Seq[String] = {
    log.info(pluginsFiles.mkString(","))
    pluginsFiles.flatMap { filePath =>
      if (filePath.startsWith("/") || filePath.startsWith("file://")) Option(filePath)
      else if (filePath.startsWith("hdfs")) Option(downloadFromHdfs(filePath))
      else if (filePath.startsWith("http")) Option(downloadFromHttp(filePath))
      else None
    }
  }

  def addPluginsToClasspath(localPluginsFiles: Seq[String]): Unit = {
    localPluginsFiles.foreach { filePath =>
      log.info(s"Getting file from local: $filePath")
      val file = new File(filePath.replace("file://", ""))
      log.info(s"Adding plugin file to classpath: ${file.getAbsolutePath}")
      JarsHelper.addJarToClasspath(file)
    }
  }

  private[driver] def downloadFromHdfs(fileHdfsPath: String): String = {
    log.info(s"Getting file from HDFS: $fileHdfsPath")
    val inputStream = hdfsService.getFile(fileHdfsPath)
    val fileName = fileHdfsPath.split("/").last
    log.info(s"HDFS file name is $fileName")
    val file = new File(s"/tmp/sparta/plugins/$fileName")
    log.info(s"Downloading HDFS file to local file system: ${file.getAbsoluteFile}")
    FileUtils.copyInputStreamToFile(inputStream, file)
    file.getAbsolutePath
  }

  private[driver] def downloadFromHttp(fileURI: String): String = {
    log.info(s"Getting file from HTTP: $fileURI")
    val url = new URL(fileURI)
    val fileName = Paths.get(url.getPath).getFileName
    val file = new File(s"/tmp/sparta/plugins/$fileName")
    log.info(s"Downloading HTTP file to local file system: ${file.getAbsoluteFile}")
    FileUtils.copyURLToFile(url, file)
    file.getAbsolutePath
  }
}
