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
import com.google.common.io.BaseEncoding
import com.stratio.sparta.driver.exception.DriverException
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.core.actor.FragmentActor
import com.stratio.sparta.serving.core.actor.StatusActor
import com.stratio.sparta.serving.core.actor.StatusActor.Update
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.helpers.ResourceManagerLinkHelper
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum._
import com.stratio.sparta.serving.core.models.policy.{PhaseEnum, PolicyErrorModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.utils.{PluginsFilesUtils, PolicyConfigUtils, PolicyStatusUtils, PolicyUtils}
import com.stratio.sparta.serving.core.utils.{FragmentUtils, PluginsFilesUtils, PolicyConfigUtils, PolicyUtils}
import com.typesafe.config.ConfigFactory
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

object SpartaClusterJob extends PluginsFilesUtils {

  val NumberOfArguments = 6
  val ClusterConfigIndex = 0
  val DetailConfigurationIndex = 1
  val PluginsFilesIndex = 2
  val PolicyIdIndex = 3
  val DriverLocationConfigIndex = 4
  val ZookeeperConfigurationIndex = 5

  //scalastyle:off
  def main(args: Array[String]): Unit = {
    assert(args.length == NumberOfArguments,
      s"Invalid number of arguments: ${args.length}, args: $args, expected: $NumberOfArguments")
    Try {
      val policyId = args(PolicyIdIndex)
      val detailConf = new String(BaseEncoding.base64().decode(args(DetailConfigurationIndex)))
      val zookeeperConf = new String(BaseEncoding.base64().decode(args(ZookeeperConfigurationIndex)))
      val pluginsFiles = new String(BaseEncoding.base64().decode(args(PluginsFilesIndex)))
        .split(",").filter(s => s != " " && s.nonEmpty)
      val driverLocationConf = new String(BaseEncoding.base64().decode(args(DriverLocationConfigIndex)))
      val clusterConf = new String(BaseEncoding.base64().decode(args(ClusterConfigIndex)))

      initSpartaConfig(detailConf, zookeeperConf, driverLocationConf, clusterConf)

      val curatorInstance = CuratorFactoryHolder.getInstance()
      val policyStatusUtils = new PolicyStatusUtils {
        override val curatorFramework: CuratorFramework = curatorInstance
      }
      Try {
        addPluginsToClassPath(pluginsFiles)
        val policyUtils = new PolicyUtils {
          override val curatorFramework: CuratorFramework = curatorInstance
        }
        val fragmentUtils = new FragmentUtils {
          override val curatorFramework: CuratorFramework = curatorInstance
        }
        val system = ActorSystem(policyId, SpartaConfig.daemonicAkkaConfig)
        val policy = fragmentUtils.getPolicyWithFragments(policyUtils.getPolicyById(policyId))
        val startingInfo = s"Starting policy in cluster"
        log.info(startingInfo)
        policyStatusUtils.updateStatus(PolicyStatusModel(id = policyId, status = Starting, statusInfo = Some(startingInfo)))
        val streamingContextService = StreamingContextService(curatorInstance)
        val ssc = streamingContextService.clusterStreamingContext(policy, pluginsFiles)
        policyStatusUtils.updateStatus(PolicyStatusModel(
          id = policyId,
          status = NotDefined,
          submissionId = Option(extractSparkApplicationId(ssc.sparkContext.applicationId))))
        ssc.start
        val policyConfigUtils = new PolicyConfigUtils {}
        val startedInfo = s"Started correctly application id: ${ssc.sparkContext.applicationId}"
        log.info(startedInfo)
        policyStatusUtils.updateStatus(PolicyStatusModel(
          id = policyId,
          status = Started,
          submissionId = Option(extractSparkApplicationId(ssc.sparkContext.applicationId)),
          statusInfo = Some(startedInfo),
          resourceManagerUrl = ResourceManagerLinkHelper.getLink(
            policyConfigUtils.executionMode(policy), policy.monitoringLink)
        ))
        ssc.awaitTermination()
      } match {
        case Success(_) =>
          val information = s"Stopped correctly Sparta cluster job"
          log.info(information)
          policyStatusUtils.updateStatus(PolicyStatusModel(id = policyId, status = Stopped, statusInfo = Some(information)))
        case Failure(exception) =>
          val information = s"Error initiating Sparta cluster job"
          log.error(information)
          policyStatusUtils.updateStatus(PolicyStatusModel(
            id = policyId,
            status = Failed,
            statusInfo = Option(information),
            lastError = Option(PolicyErrorModel(information, PhaseEnum.Execution, exception.toString))
          ))
          throw DriverException(information, exception)
      }
    } match {
      case Success(_) =>
        log.info("Finished correctly Sparta cluster job")
      case Failure(driverException: DriverException) =>
        log.error(driverException.msg, driverException.getCause)
        throw driverException
      case Failure(exception) =>
        log.error(s"Error initiating Sparta environment: ${exception.getLocalizedMessage}", exception)
        throw exception
    }
  }

  //scalastyle:on

  def initSpartaConfig(detailConfig: String, zKConfig: String, locationConfig: String, clusterConfig: String): Unit = {
    val configStr =
      s"${detailConfig.stripPrefix("{").stripSuffix("}")}" +
        s"\n${zKConfig.stripPrefix("{").stripSuffix("}")}" +
        s"\n${locationConfig.stripPrefix("{").stripSuffix("}")}" +
        s"\n${clusterConfig.stripPrefix("{").stripSuffix("}")}"
    log.info(s"Parsed config: sparta { $configStr }")
    SpartaConfig.initMainConfig(Option(ConfigFactory.parseString(s"sparta{$configStr}")))
  }

  def extractSparkApplicationId(contextId: String): String = {
    if (contextId.contains("driver")) {
      val sparkApplicationId = contextId.substring(contextId.indexOf("driver"))
      log.info(s"The extracted Framework id is: ${contextId.substring(0, contextId.indexOf("driver") - 1)}")
      log.info(s"The extracted Spark application id is: $sparkApplicationId")
      sparkApplicationId
    } else contextId
  }
}
