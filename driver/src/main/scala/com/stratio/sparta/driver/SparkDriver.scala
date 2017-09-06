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

import com.google.common.io.BaseEncoding
import com.stratio.sparta.driver.exception.DriverException
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.helpers.ResourceManagerLinkHelper
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow.{PhaseEnum, WorkflowError, WorkflowStatus}
import com.stratio.sparta.serving.core.utils.{TemplateUtils, PluginsFilesUtils, WorkflowStatusUtils, WorkflowUtils}
import com.typesafe.config.ConfigFactory
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Properties, Success, Try}

object SparkDriver extends PluginsFilesUtils {

  val NumberOfArguments = 5
  val DetailConfigIndex = 0
  val HdfsConfigIndex = 1
  val PluginsFilesIndex = 2
  val WorkflowIdIndex = 3
  val ZookeeperConfigIndex = 4
  val JaasConfEnv = "SPARTA_JAAS_FILE"

  //scalastyle:off
  def main(args: Array[String]): Unit = {
    assert(args.length == NumberOfArguments,
      s"Invalid number of arguments: ${args.length}, args: $args, expected: $NumberOfArguments")
    Try {
      Properties.envOrNone(JaasConfEnv).foreach(jaasConf => {
        log.info(s"Adding java security configuration file: $jaasConf")
        System.setProperty("java.security.auth.login.config", jaasConf)
      })
      log.info(s"Arguments: ${args.mkString(", ")}")
      val workflowId = args(WorkflowIdIndex)
      val detailConf = new String(BaseEncoding.base64().decode(args(DetailConfigIndex)))
      val zookeeperConf = new String(BaseEncoding.base64().decode(args(ZookeeperConfigIndex)))
      val pluginsFiles = new String(BaseEncoding.base64().decode(args(PluginsFilesIndex)))
        .split(",").filter(s => s != " " && s.nonEmpty)
      val hdfsConf = new String(BaseEncoding.base64().decode(args(HdfsConfigIndex)))

      initSpartaConfig(detailConf, zookeeperConf, hdfsConf)

      val curatorInstance = CuratorFactoryHolder.getInstance()
      val policyStatusUtils = new WorkflowStatusUtils {
        override val curatorFramework: CuratorFramework = curatorInstance
      }
      Try {
        addPluginsToClassPath(pluginsFiles)
        val policyUtils = new WorkflowUtils {
          override val curatorFramework: CuratorFramework = curatorInstance
        }
        val workflow = policyUtils.getWorkflowById(workflowId)
        val startingInfo = s"Launching workflow in cluster..."
        log.info(startingInfo)
        policyStatusUtils.updateStatus(WorkflowStatus(id = workflowId, status = Starting, statusInfo = Some(startingInfo)))
        val streamingContextService = StreamingContextService(curatorInstance)
        val (spartaWorkflow, ssc) = streamingContextService.clusterStreamingContext(workflow, pluginsFiles)
        policyStatusUtils.updateStatus(WorkflowStatus(
          id = workflowId,
          status = NotDefined,
          submissionId = Option(extractSparkApplicationId(ssc.sparkContext.applicationId))))
        spartaWorkflow.setup()
        ssc.start
        val startedInfo = s"Application with id: ${ssc.sparkContext.applicationId} was properly launched"
        log.info(startedInfo)
        policyStatusUtils.updateStatus(WorkflowStatus(
          id = workflowId,
          status = Started,
          submissionId = Option(extractSparkApplicationId(ssc.sparkContext.applicationId)),
          statusInfo = Some(startedInfo),
          resourceManagerUrl = ResourceManagerLinkHelper.getLink(
            workflow.settings.global.executionMode, workflow.settings.sparkSettings.master)
        ))
        ssc.awaitTermination()
        spartaWorkflow.cleanUp()
      } match {
        case Success(_) =>
          val information = s"Sparta job in cluster was properly stopped"
          log.info(information)
          policyStatusUtils.updateStatus(WorkflowStatus(id = workflowId, status = Stopped, statusInfo = Some(information)))
        case Failure(exception) =>
          val information = s"Error initiating Sparta job in cluster"
          log.error(information)
          policyStatusUtils.updateStatus(WorkflowStatus(
            id = workflowId,
            status = Failed,
            statusInfo = Option(information),
            lastError = Option(WorkflowError(information, PhaseEnum.Execution, exception.toString))
          ))
          throw DriverException(information, exception)
      }
    } match {
      case Success(_) =>
        log.info("Sparta job in cluster successfully finished")
      case Failure(driverException: DriverException) =>
        log.error(driverException.msg, driverException.getCause)
        throw driverException
      case Failure(exception) =>
        log.error(s"Error initiating Sparta environment: ${exception.getLocalizedMessage}", exception)
        throw exception
    }
  }

  //scalastyle:on

  def initSpartaConfig(detailConfig: String, zKConfig: String, locationConfig: String): Unit = {
    val configStr =
      s"${detailConfig.stripPrefix("{").stripSuffix("}")}" +
        s"\n${zKConfig.stripPrefix("{").stripSuffix("}")}" +
        s"\n${locationConfig.stripPrefix("{").stripSuffix("}")}"
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
