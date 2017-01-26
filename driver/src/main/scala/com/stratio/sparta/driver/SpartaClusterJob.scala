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

import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import com.google.common.io.BaseEncoding
import com.stratio.sparta.driver.exception.DriverException
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.core.actor.PolicyStatusActor.Update
import com.stratio.sparta.serving.core.actor.{FragmentActor, PolicyStatusActor}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.dao.ErrorDAO
import com.stratio.sparta.serving.core.helpers.FragmentsHelper
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.policy.PolicyStatusModel
import com.stratio.sparta.serving.core.utils.{PluginsFilesUtils, PolicyUtils}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object SpartaClusterJob extends PolicyUtils with PluginsFilesUtils {

  override implicit val timeout: Timeout = Timeout(AkkaConstant.DefaultTimeout.seconds)
  final val PolicyIdIndex = 0
  final val ZookeperConfigurationIndex = 1
  final val DetailConfigurationIndex = 2
  final val PluginsFilesIndex = 3
  final val DriverLocationConfigIndex = 4

  //scalastyle:off
  def main(args: Array[String]): Unit = {
    assert(args.length == 5, s"Invalid number of params: ${args.length}, args: $args")
    Try {
      val policyId = args(PolicyIdIndex)
      val detailConf = new String(BaseEncoding.base64().decode(args(DetailConfigurationIndex)))
      val zookeperConf = new String(BaseEncoding.base64().decode(args(ZookeperConfigurationIndex)))
      val pluginsFiles = new String(BaseEncoding.base64().decode(args(PluginsFilesIndex)))
        .split(",").filter(s => s != " " && s.nonEmpty)
      val driverLocationConf = new String(BaseEncoding.base64().decode(args(DriverLocationConfigIndex)))

      initSpartaConfig(detailConf, zookeperConf, driverLocationConf)

      addPluginsToClassPath(pluginsFiles)

      val curatorFramework = CuratorFactoryHolder.getInstance()
      implicit val system = ActorSystem(policyId, SpartaConfig.daemonicAkkaConfig)
      val fragmentActor = system.actorOf(Props(new FragmentActor(curatorFramework)), AkkaConstant.FragmentActor)
      val policy = FragmentsHelper.getPolicyWithFragments(byId(policyId, curatorFramework), fragmentActor)
      val policyStatusActor = system.actorOf(Props(new PolicyStatusActor(curatorFramework)),
        AkkaConstant.PolicyStatusActor)

      Try {
        policyStatusActor ! Update(PolicyStatusModel(policyId, PolicyStatusEnum.Starting))
        val streamingContextService = new StreamingContextService(Some(policyStatusActor))
        val ssc = streamingContextService.clusterStreamingContext(
          policy,
          Seq.empty[String],
          Map("spark.app.name" -> s"${policy.name}")
        )
        ssc.start
        val information = s"Started correctly application id: ${ssc.sparkContext.applicationId}"
        log.info(information)
        policyStatusActor ! Update(PolicyStatusModel(policyId, PolicyStatusEnum.Started, Option(ssc.sparkContext.applicationId), None, Some(information)))
        ssc.awaitTermination()
      } match {
        case Success(_) =>
          val information = s"Stopped correctly Sparta Streaming Context Job for policy: ${policy.id.get}"
          log.info(information)
          policyStatusActor ! Update(PolicyStatusModel(policyId, PolicyStatusEnum.Stopped, None, None, Some(information)))
        case Failure(exception) =>
          val information = s"Error initiating Sparta Streaming Context Job: ${exception.getLocalizedMessage}"
          log.error(information)
          policyStatusActor ! Update(PolicyStatusModel(policyId, PolicyStatusEnum.Failed, None, None, Some(information)))
          throw DriverException(information, exception)
      }
    } match {
      case Success(_) =>
        log.info("Finished correctly Sparta Streaming Context Job")
      case Failure(driverException: DriverException) =>
        log.error(driverException.msg, driverException.getCause)
        throw driverException
      case Failure(exception: Exception) =>
        log.error(s"Error initiating Sparta environment: ${exception.getLocalizedMessage}", exception)
        throw exception
    }
  }

  //scalastyle:on

  def initSpartaConfig(detailConfig: String, zKConfig: String, clusterConfig: String): Unit = {
    val configStr =
      s"${detailConfig.stripPrefix("{").stripSuffix("}")}" +
        s"\n${zKConfig.stripPrefix("{").stripSuffix("}")}" +
        s"\n${clusterConfig.stripPrefix("{").stripSuffix("}")}"
    log.info(s"Parsed config: sparta { $configStr }")
    SpartaConfig.initMainConfig(Option(ConfigFactory.parseString(s"sparta{$configStr}")))
    SpartaConfig.initDAOs
  }
}
