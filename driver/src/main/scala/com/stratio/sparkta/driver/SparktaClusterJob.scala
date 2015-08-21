/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.driver

import java.io.File
import java.net.URI

import com.stratio.sparkta.driver.SparktaJob._
import com.stratio.sparkta.driver.factory.SparkContextFactory
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.driver.util.{HdfsUtils, PolicyUtils}
import com.stratio.sparkta.serving.core.helpers.JarsHelper
import com.stratio.sparkta.serving.core.models.AggregationPoliciesModel
import com.stratio.sparkta.serving.core.{AppConstant, CuratorFactoryHolder, SparktaConfig}
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FileUtils

import scala.util.{Failure, Success, Try}

object SparktaClusterJob {

  def main(args: Array[String]): Unit = {

    if (checkArgs(args)) {
      log.info("############################## JOB SERVER ########################")

      val hadoopUserName = scala.util.Properties.envOrElse("HADOOP_USER_NAME", AppConstant.DefaultHadoopUserName)
      val hadoopConfDir = scala.util.Properties.envOrNone("HADOOP_CONF_DIR")
      val hdfsUtils = new HdfsUtils(hadoopUserName, hadoopConfDir)
      val pluginFiles = getPluginsFiles(hdfsUtils, args(1))
      val policy = getPolicyFromZookeeper(args)
      val streamingContextService = new StreamingContextService
      val ssc = streamingContextService.clusterStreamingContext(policy,
        pluginFiles,
        Map("spark.app.name" -> s"${policy.name}")
      ).get

      log.info("Streaming Context created")

      ssc.start
      ssc.awaitTermination
    } else log.warn("Invalid arguments")
  }

  def getPluginsFiles(hdfsUtils: HdfsUtils, hdfsPath: String): Array[URI] = {
    hdfsUtils.getFiles(hdfsPath).map(file => {
      val fileName = s"${hdfsPath}${file.getPath.getName}"
      val tempFile = File.createTempFile(file.getPath.getName, "")

      log.info(s"Downloading file from HDFS: ${file.getPath.getName}")
      log.info(s"Creating temp file: $tempFile")

      FileUtils.copyInputStreamToFile(hdfsUtils.getFile(fileName), tempFile)
      JarsHelper.addToClasspath(tempFile)
      file.getPath.toUri
    })
  }

  def getPolicyFromZookeeper(args: Array[String]): AggregationPoliciesModel = {
    val policyName = args(0)

    log.info("Zookeeper arguments: " + args(2))

    val config = ConfigFactory.parseString(args(2))

    log.info(s"Zookeeper Config received: ${config.toString}")

    Try({
      val curatorFramework = CuratorFactoryHolder.getInstance(config).get
      PolicyUtils.parseJson(new Predef.String(curatorFramework.getData.forPath(
        s"${AppConstant.PoliciesBasePath}/${policyName}")))
    }) match {
      case Success(policy) => policy
      case Failure(e) => log.error(s"Cannot load policy $policyName", e); throw e
    }
  }

  def checkArgs(args: Array[String]): Boolean = args.length == 3
}
