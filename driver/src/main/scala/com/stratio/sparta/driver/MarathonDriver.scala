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
import com.stratio.sparta.driver.actor.MarathonAppActor
import com.stratio.sparta.driver.actor.MarathonAppActor.StartApp
import com.stratio.sparta.serving.core.actor._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.exception.DriverException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow.{PhaseEnum, Workflow, WorkflowError, WorkflowStatus}
import com.stratio.sparta.serving.core.services.WorkflowStatusService
import com.typesafe.config.ConfigFactory
import org.json4s.jackson.Serialization.read

import scala.util.{Failure, Success, Try}

object MarathonDriver extends SLF4JLogging with SpartaSerializer {

  val NumberOfArguments = 3
  val WorkflowIdIndex = 0
  val ZookeeperConfigurationIndex = 1
  val DetailConfigurationIndex = 2

  def main(args: Array[String]): Unit = {
    Try {
      assert(args.length == NumberOfArguments,
        s"Invalid number of arguments: ${args.length}, args: $args, expected: $NumberOfArguments")
      log.debug(s"Arguments: ${args.mkString(", ")}")
      val workflow = read[Workflow](new String(BaseEncoding.base64().decode(args(WorkflowIdIndex))))
      log.debug(s"Obtained workflow: ${workflow.toString}")
      val zookeeperConf = new String(BaseEncoding.base64().decode(args(ZookeeperConfigurationIndex)))
      val detailConf = new String(BaseEncoding.base64().decode(args(DetailConfigurationIndex)))
      initSpartaConfig(zookeeperConf, detailConf)
      val curatorInstance = CuratorFactoryHolder.getInstance()
      val system = ActorSystem("MarathonApp")
      val statusService = new WorkflowStatusService(curatorInstance)

      Try {
        val statusListenerActor = system.actorOf(Props(new StatusListenerActor))
        system.actorOf(Props(new ExecutionPublisherActor(curatorInstance)))
        system.actorOf(Props(new WorkflowPublisherActor(curatorInstance)))
        system.actorOf(Props(new StatusPublisherActor(curatorInstance)))
        val marathonAppActor = system.actorOf(Props(
          new MarathonAppActor(curatorInstance, statusListenerActor)), MarathonAppActorName)

        marathonAppActor ! StartApp(workflow)
      } match {
        case Success(_) =>
        case Failure(exception) =>
          val information = s"Error initiating workflow app environment in Marathon driver"
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
        log.info("Workflow App environment started")
      case Failure(driverException: DriverException) =>
        log.error(driverException.msg, driverException.getCause)
        throw driverException
      case Failure(exception) =>
        log.error(s"An error was encountered while starting the Workflow App environment", exception)
        throw exception
    }
  }

  def initSpartaConfig(zKConfig: String, detailConf: String): Unit = {
    val configStr = s"${detailConf.stripPrefix("{").stripSuffix("}")}" +
      s"\n${zKConfig.stripPrefix("{").stripSuffix("}")}"
    log.debug(s"Parsed config: sparta { $configStr }")
    val composedStr = s" sparta { $configStr } "
    SpartaConfig.initMainWithFallbackConfig(ConfigFactory.parseString(composedStr))
  }
}
