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
import java.lang.reflect.Method
import java.net.{URLClassLoader, URL}
import java.util.UUID

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.google.common.io.BaseEncoding
import com.stratio.sparta.driver.SpartaJob._
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.driver.util.{HdfsUtils, PolicyUtils}
import com.stratio.sparta.serving.core.actor.FragmentActor
import com.stratio.sparta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.dao.ErrorDAO
import com.stratio.sparta.serving.core.helpers.{JarsHelper, PolicyHelper}
import com.stratio.sparta.serving.core.models.{AggregationPoliciesModel, PolicyStatusModel, SpartaSerializer}
import com.stratio.sparta.serving.core.policy.status.PolicyStatusActor.Update
import com.stratio.sparta.serving.core.policy.status.{PolicyStatusActor, PolicyStatusEnum}
import com.stratio.sparta.serving.core.{CuratorFactoryHolder, SpartaConfig}
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FileUtils
import org.apache.curator.framework.CuratorFramework

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object SpartaClusterJob extends SpartaSerializer {

  implicit val timeout: Timeout = Timeout(3.seconds)
  final val PolicyIdIndex = 0
  final val ZookeperConfigurationIndex = 1
  final val DetailConfigurationIndex = 2
  final val PluginsFilesIndex = 3
  final val HdfsConfigIndex = 4

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
      val policyZk = getPolicyFromZookeeper(policyId, curatorFramework)
      implicit val system = ActorSystem(policyId)
      val fragmentActor = system.actorOf(Props(new FragmentActor(curatorFramework)), AkkaConstant.FragmentActor)
      val policy = PolicyHelper.getPolicyWithFragments(policyZk, fragmentActor)
      val policyStatusActor = system.actorOf(Props(new PolicyStatusActor(curatorFramework)),
        AkkaConstant.PolicyStatusActor)

      Try {
        policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Starting))
        Try(ErrorDAO().dao.delete(policy.id.get))

        val streamingContextService = new StreamingContextService(Some(policyStatusActor))
        val ssc = streamingContextService.clusterStreamingContext(
          policy,
          pluginsFiles,
          Map("spark.app.name" -> s"${policy.name}")
        ).get

        ssc.start
        policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Started))
        log.info(s"Starting Streaming Context for policy $policyId")
        ssc.awaitTermination()
      } match {
        case Success(_) =>
          policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Stopped))
          log.info(s"Stopped Streaming Context for policy $policyId")
        case Failure(exception) =>
          log.error(exception.getLocalizedMessage, exception)
          policyStatusActor ? Update(PolicyStatusModel(policyId, PolicyStatusEnum.Failed))
      }
    } match {
      case Success(_) => log.info("Streaming context is running")
      case Failure(exception) => log.error(exception.getLocalizedMessage, exception)
    }
  }

  private def addPluginsToClassPath(pluginsFiles: Array[String]) = {
    log.error(pluginsFiles.mkString(","))
    pluginsFiles.foreach {
      fileHdfsPath => {
        log.info(s"Getting file from HDFS: ${fileHdfsPath}")
        val inputStream = HdfsUtils(SpartaConfig.getHdfsConfig).getFile(fileHdfsPath)
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

  def getPolicyFromZookeeper(policyId: String, curatorFramework: CuratorFramework): AggregationPoliciesModel = {
    Try {
      PolicyUtils.parseJson(new Predef.String(curatorFramework.getData.forPath(
        s"${AppConstant.PoliciesBasePath}/$policyId")))
    } match {
      case Success(policy) => policy
      case Failure(e) => log.error(s"Cannot load policy $policyId", e); throw e
    }
  }
}
