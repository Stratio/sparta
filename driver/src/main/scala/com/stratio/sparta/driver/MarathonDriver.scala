/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
import com.stratio.sparta.serving.core.models.workflow.{PhaseEnum, WorkflowError, WorkflowStatus}
import com.stratio.sparta.serving.core.services.{ExecutionService, WorkflowStatusService}
import com.typesafe.config.ConfigFactory

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
      val workflowId = args(WorkflowIdIndex)
      val zookeeperConf = new String(BaseEncoding.base64().decode(args(ZookeeperConfigurationIndex)))
      val detailConf = new String(BaseEncoding.base64().decode(args(DetailConfigurationIndex)))
      initSpartaConfig(zookeeperConf, detailConf)
      val curatorInstance = CuratorFactoryHolder.getInstance()
      val system = ActorSystem("MarathonApp")
      val statusService = new WorkflowStatusService(curatorInstance)
      val executionService = new ExecutionService(curatorInstance)
      val execution = executionService.findById(workflowId)
        .getOrElse(throw new Exception(s"Impossible to find execution for workflowId: $workflowId"))

      Try {
        val statusListenerActor = system.actorOf(Props(new StatusListenerActor))
        system.actorOf(Props(new ExecutionPublisherActor(curatorInstance)))
        system.actorOf(Props(new WorkflowPublisherActor(curatorInstance)))
        system.actorOf(Props(new StatusPublisherActor(curatorInstance)))
        val marathonAppActor = system.actorOf(Props(
          new MarathonAppActor(curatorInstance, statusListenerActor)), MarathonAppActorName)

        marathonAppActor ! StartApp(execution)
      } match {
        case Success(_) =>
        case Failure(exception) =>
          val information = s"Error initiating workflow app environment in Marathon driver"
          val error = WorkflowError(information, PhaseEnum.Launch, exception.toString)
          statusService.update(WorkflowStatus(
            id = workflowId,
            status = Failed,
            statusInfo = Option(information),
            lastError = Option(error)
          ))
          executionService.setLastError(workflowId, error)
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
