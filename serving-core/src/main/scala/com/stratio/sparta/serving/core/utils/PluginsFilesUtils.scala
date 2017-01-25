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

package com.stratio.sparta.serving.core.utils

import java.io.File
import java.net.URL
import java.util.{Calendar, UUID}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.helpers.JarsHelper
import org.apache.commons.io.FileUtils

trait PluginsFilesUtils extends SLF4JLogging {

  def addPluginsToClassPath(pluginsFiles: Array[String]): Unit = {
    log.info(pluginsFiles.mkString(","))
    pluginsFiles.foreach(filePath => {
      if (filePath.startsWith("/")) addFromLocal(filePath)
      if (filePath.startsWith("hdfs")) addFromHdfs(filePath)
      if (filePath.startsWith("http")) addFromHttp(filePath)
    })
  }

  private def addFromLocal(filePath: String): Unit = {
    val file = new File(filePath)
    JarsHelper.addToClasspath(file)
  }

  private def addFromHdfs(fileHdfsPath: String): Unit = {
    log.info(s"Getting file from HDFS: $fileHdfsPath")
    val inputStream = HdfsUtils().getFile(fileHdfsPath)
    val fileName = fileHdfsPath.split("/").last
    log.info(s"HDFS file name is $fileName")
    val file = new File(s"/tmp/sparta/userjars/${UUID.randomUUID().toString}/$fileName")
    log.info(s"Downloading HDFS file to local file system: ${file.getAbsoluteFile}")
    FileUtils.copyInputStreamToFile(inputStream, file)
    JarsHelper.addToClasspath(file)
  }

  private def addFromHttp(fileURI: String): Unit = {
    val tempFile = File.createTempFile(s"sparta-plugin-${Calendar.getInstance().getTimeInMillis}", ".jar")
    val url = new URL(fileURI)
    FileUtils.copyURLToFile(url, tempFile)
    JarsHelper.addToClasspath(tempFile)
  }
}
