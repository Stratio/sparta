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
package com.stratio.sparta.driver.util

import java.io.File

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.models.AggregationPoliciesModel

case class ClusterSparkFiles(policy: AggregationPoliciesModel, hdfs: HdfsUtils) extends SLF4JLogging {

  private val hdfsConfig = SpartaConfig.getHdfsConfig.get

  def getPluginsFiles(pluginsJarsPath: String): Seq[String] = {
    PolicyUtils.jarsFromPolicy(policy)
      .filter(file => !file.getName.contains("driver")).map(file => {
      hdfs.write(file.getAbsolutePath, pluginsJarsPath, true)
      file.getName -> s"hdfs://${hdfsConfig.getString(AppConstant.HdfsMaster)}$pluginsJarsPath${file.getName}"
    }).toMap.values.toSeq
  }

  def getDriverFile(driverJarPath: String): String = {
    val driverJar =
      JarsHelper.findDriverByPath(new File(SpartaConfig.spartaHome, AppConstant.ClusterExecutionJarFolder)).head
    hdfs.write(driverJar.getAbsolutePath, driverJarPath, true)
    s"hdfs://${hdfsConfig.getString(AppConstant.HdfsMaster)}$driverJarPath${driverJar.getName}"
  }
}
