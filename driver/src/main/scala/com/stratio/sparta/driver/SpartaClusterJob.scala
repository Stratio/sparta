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

package com.stratio.sparta.driver

import java.io.File
import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.google.common.io.BaseEncoding
import com.stratio.sparta.driver.exception.DriverException
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.core.actor.FragmentActor
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.dao.ErrorDAO
import com.stratio.sparta.serving.core.helpers.{FragmentsHelper, JarsHelper}
import com.stratio.sparta.serving.core.models.PolicyStatusModel
import com.stratio.sparta.serving.core.policy.status.PolicyStatusActor.Update
import com.stratio.sparta.serving.core.policy.status.{PolicyStatusActor, PolicyStatusEnum}
import com.stratio.sparta.serving.core.utils.{HdfsUtils, PolicyUtils}
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FileUtils

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object SpartaClusterJob extends PolicyUtils {

  override implicit val timeout: Timeout = Timeout(AkkaConstant.DefaultTimeout.seconds)
  final val PolicyIdIndex = 0
  final val ZookeperConfigurationIndex = 1
  final val DetailConfigurationIndex = 2
  final val PluginsFilesIndex = 3
  final val HdfsConfigIndex = 4

  //scalastyle:off
  def main(args: Array[String]): Unit = {
    assert(args.length == 5, s"Invalid number of params: ${args.length}, args: $args")
    Try {
      val policyId = args(PolicyIdIndex)
      val detailConfiguration = new String(BaseEncoding.base64().decode(args(DetailConfigurationIndex)))
      val zookeperConfiguration = new String(BaseEncoding.base64().decode(args(ZookeperConfigurationIndex)))
      val pluginsFiles = new String(BaseEncoding.base64().decode(args(PluginsFilesIndex)))
        .split(",").filter(s => s != " " && s.nonEmpty && s != "")
      val hdfsConfiguration = new String(BaseEncoding.base64().decode(args(HdfsConfigIndex)))

      initSpartaConfig(detailConfiguration, zookeperConfiguration, hdfsConfiguration)

      addPluginsToClassPath(pluginsFiles)

      val curatorFramework = CuratorFactoryHolder.getInstance()
      implicit val system = ActorSystem(policyId)
      val fragmentActor = system.actorOf(Props(new FragmentActor(curatorFramework)), AkkaConstant.FragmentActor)
      val policy = FragmentsHelper.getPolicyWithFragments(byId(policyId, curatorFramework), fragmentActor)
      val policyStatusActor = system.actorOf(Props(new PolicyStatusActor(curatorFramework)),
        AkkaConstant.PolicyStatusActor)

      Try(policyStatusActor ! Update(PolicyStatusModel(policyId, PolicyStatusEnum.Starting))) match {
        case Failure(exception) =>
          log.error(exception.getLocalizedMessage, exception)
          policyStatusActor ! Update(PolicyStatusModel(policyId, PolicyStatusEnum.Failed))
          DriverException(s"Error updating context to Starting. Error: ${exception.getLocalizedMessage}", exception)
        case Success(_) =>
          log.info("The Policy is starting ...")
      }

      Try(ErrorDAO().dao.delete(policy.id.get))

      Try {
        val streamingContextService = new StreamingContextService(Some(policyStatusActor))
        val ssc = streamingContextService.clusterStreamingContext(
          policy,
          pluginsFiles,
          Map("spark.app.name" -> s"${policy.name}")
        )

        ssc.start
        policyStatusActor ! Update(PolicyStatusModel(policyId, PolicyStatusEnum.Started))
        log.info(s"Starting Streaming Context for policy $policyId")
        ssc.awaitTermination()
      } match {
        case Success(_) =>
          log.info(s"Finished Streaming Context for policy $policyId")
        case Failure(exception) =>
          val message = s"Error initiating Sparta environment: ${exception.getLocalizedMessage}"
          policyStatusActor ! Update(PolicyStatusModel(policyId, PolicyStatusEnum.Stopping))
          DriverException(message, exception)
      }
    } match {
      case Success(_) =>
        log.info("Finished correctly Sparta Cluster Job")
      case Failure(driverException: DriverException) =>
        log.error(driverException.msg, driverException.getCause)
      case Failure(exception: Exception) =>
        log.error(s"Error initiating Sparta environment: ${exception.getLocalizedMessage}", exception)
    }
  }

  //scalastyle:on
  private def addPluginsToClassPath(pluginsFiles: Array[String]) = {
    log.error(pluginsFiles.mkString(","))
    pluginsFiles.foreach {
      fileHdfsPath => {
        log.info(s"Getting file from HDFS: ${fileHdfsPath}")
        val inputStream = HdfsUtils().getFile(fileHdfsPath)
        val fileName = fileHdfsPath.split("/").last
        log.info(s"HDFS file name is ${fileName}")
        val file = new File(s"/tmp/sparta/userjars/${UUID.randomUUID().toString}/${fileName}")
        log.info(s"Downloading HDFS file to local file system: ${file.getAbsoluteFile}")
        FileUtils.copyInputStreamToFile(inputStream, file)
        JarsHelper.addToClasspath(file)
      }
    }
  }

  def initSpartaConfig(detailConfig: String, zKConfig: String, hdfsConfig: String): Unit = {
    val configStr =
      s"${detailConfig.stripPrefix("{").stripSuffix("}")}" +
        s"\n${zKConfig.stripPrefix("{").stripSuffix("}")}" +
        s"\n${hdfsConfig.stripPrefix("{").stripSuffix("}")}"
    log.info(s"Parsed config: sparta { $configStr }")
    SpartaConfig.initMainConfig(Option(ConfigFactory.parseString(s"sparta{$configStr}")))
    SpartaConfig.initDAOs
  }
}
