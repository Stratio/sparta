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
import akka.event.slf4j.SLF4JLogging
import com.google.common.io.BaseEncoding
import com.stratio.sparta.driver.exception.DriverException
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.core.actor._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.WorkflowStatusService
import com.typesafe.config.ConfigFactory
import org.json4s.jackson.Serialization.read

import scala.util.{Failure, Properties, Success, Try}

object SparkDriver extends SLF4JLogging with SpartaSerializer {

  val NumberOfArguments = 5
  val DetailConfigIndex = 0
  val HdfsConfigIndex = 1
  val PluginsFilesIndex = 2
  val WorkflowIdIndex = 3
  val ZookeeperConfigIndex = 4
  val JaasConfEnv = "SPARTA_JAAS_FILE"

  //scalastyle:off
  def main(args: Array[String]): Unit = {
    Try {
      assert(args.length == NumberOfArguments,
        s"Invalid number of arguments: ${args.length}, args: $args, expected: $NumberOfArguments")
      Properties.envOrNone(JaasConfEnv).foreach(jaasConf => {
        log.debug(s"Adding java security configuration file: $jaasConf")
        System.setProperty("java.security.auth.login.config", jaasConf)
      })
      log.debug(s"Arguments: ${args.mkString(", ")}")

      val workflow = read[Workflow](new String(BaseEncoding.base64().decode(args(WorkflowIdIndex))))
      log.debug(s"Obtained workflow: ${workflow.toString}")
      val detailConf = new String(BaseEncoding.base64().decode(args(DetailConfigIndex)))
      val zookeeperConf = new String(BaseEncoding.base64().decode(args(ZookeeperConfigIndex)))
      val pluginsFiles = new String(BaseEncoding.base64().decode(args(PluginsFilesIndex)))
        .split(",").filter(s => s != " " && s.nonEmpty)
      val hdfsConf = new String(BaseEncoding.base64().decode(args(HdfsConfigIndex)))

      initSpartaConfig(detailConf, zookeeperConf, hdfsConf)

      val system = ActorSystem("SparkDriver")
      val curatorInstance = CuratorFactoryHolder.getInstance()
      val statusService = new WorkflowStatusService(curatorInstance)
      Try {
        val statusListenerActor = system.actorOf(Props(new WorkflowStatusListenerActor))
        system.actorOf(Props(new ExecutionPublisherActor(curatorInstance)))
        system.actorOf(Props(new WorkflowPublisherActor(curatorInstance)))
        system.actorOf(Props(new StatusPublisherActor(curatorInstance)))
        JarsHelper.addJarsToClassPath(pluginsFiles)
        val localPlugins = JarsHelper.getLocalPathFromJars(pluginsFiles)
        val startingInfo = s"Launching workflow in Spark driver..."
        log.info(startingInfo)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = Starting,
          statusInfo = Some(startingInfo)
        ))
        val streamingContextService = StreamingContextService(curatorInstance, statusListenerActor)
        val spartaWorkflow = if(workflow.executionEngine == WorkflowExecutionEngine.Batch) {
          streamingContextService.clusterContext(workflow, localPlugins)
        } else streamingContextService.clusterStreamingContext(workflow, localPlugins)

        spartaWorkflow.cleanUp()
      } match {
        case Success(_) =>
          val information = s"Workflow in Spark driver was properly stopped"
          log.info(information)
          statusService.update(WorkflowStatus(
            id = workflow.id.get,
            status = Stopped,
            statusInfo = Some(information)
          ))
        case Failure(exception) =>
          val information = s"Error initiating workflow in Spark driver"
          statusService.update(WorkflowStatus(
            id = workflow.id.get,
            status = Failed,
            statusInfo = Option(information),
            lastError = Option(WorkflowError(information, PhaseEnum.Launch, exception.toString))
          ))
          throw DriverException(information, exception)
      }
    } match {
      case Success(_) =>
        log.info("Workflow in Spark driver successfully finished")
      case Failure(driverException: DriverException) =>
        log.error(driverException.msg, driverException.getCause)
        throw driverException
      case Failure(exception) =>
        log.error(s"Error initiating Sparta environment in Spark driver", exception)
        throw exception
    }
  }

  //scalastyle:on

  def initSpartaConfig(detailConfig: String, zKConfig: String, locationConfig: String): Unit = {
    val configStr =
      s"${detailConfig.stripPrefix("{").stripSuffix("}")}" +
        s"\n${zKConfig.stripPrefix("{").stripSuffix("}")}" +
        s"\n${locationConfig.stripPrefix("{").stripSuffix("}")}"
    log.debug(s"Parsed config: sparta { $configStr }")
    SpartaConfig.initMainConfig(Option(ConfigFactory.parseString(s"sparta{$configStr}")))
  }

}
