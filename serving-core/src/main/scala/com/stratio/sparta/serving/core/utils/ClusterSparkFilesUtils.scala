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

import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.models.policy.PolicyModel

import scala.util.Try

case class ClusterSparkFilesUtils(policy: PolicyModel, hdfs: HdfsUtils) extends CheckpointUtils {

  private val hdfsConfig = SpartaConfig.getHdfsConfig.get
  private val host = hdfsConfig.getString(AppConstant.HdfsMaster)
  private val port = hdfsConfig.getInt(AppConstant.HdfsPort)

  def uploadDriverFile(driverJarPath: String): String = {
    val driverJar =
      JarsHelper.findDriverByPath(new File(
        Try(SpartaConfig.getDetailConfig.get.getString(AppConstant.DriverPackageLocation))
        .getOrElse(AppConstant.DefaultDriverPackageLocation))).head

    log.info(s"Uploading Sparta Driver jar ($driverJar) to HDFS cluster .... ")

    val driverJarPathParsed = driverJarPath.replace("hdfs://", "") + {
      if(driverJarPath.endsWith("/")) "" else "/"
    }
    hdfs.write(driverJar.getAbsolutePath, driverJarPathParsed, overwrite = true)

    val uploadedFilePath = if(isHadoopEnvironmentDefined) s"hdfs://$driverJarPathParsed${driverJar.getName}"
    else s"hdfs://$host:$port$driverJarPathParsed${driverJar.getName}"

    log.info(s"Uploaded Sparta Driver jar to HDFS path: $uploadedFilePath")

    uploadedFilePath
  }
}
