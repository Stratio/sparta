/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.driver

import akka.actor.{ActorSystem, Props}
import akka.event.slf4j.SLF4JLogging
import com.google.common.io.BaseEncoding
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.driver.services.ContextsService
import com.stratio.sparta.serving.core.actor._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.exception.{DriverException, ErrorManagerException}
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.{ExecutionService, WorkflowStatusService}
import com.typesafe.config.ConfigFactory

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

      val workflowId = args(WorkflowIdIndex)
      val detailConf = new String(BaseEncoding.base64().decode(args(DetailConfigIndex)))
      val zookeeperConf = new String(BaseEncoding.base64().decode(args(ZookeeperConfigIndex)))
      val pluginsFiles = new String(BaseEncoding.base64().decode(args(PluginsFilesIndex)))
        .split(",").filter(s => s != " " && s.nonEmpty)
      val hdfsConf = new String(BaseEncoding.base64().decode(args(HdfsConfigIndex)))

      initSpartaConfig(detailConf, zookeeperConf, hdfsConf)

      val system = ActorSystem("SparkDriver")
      val curatorInstance = CuratorFactoryHolder.getInstance()
      val statusService = new WorkflowStatusService(curatorInstance)
      val executionService = new ExecutionService(curatorInstance)
      val execution = executionService.findById(workflowId)
        .getOrElse(throw new Exception(s"Impossible to find execution for workflowId: $workflowId"))
      val workflow = execution.genericDataExecution.get.workflow

      Try {
        system.actorOf(Props(new ExecutionPublisherActor(curatorInstance)))
        system.actorOf(Props(new WorkflowPublisherActor(curatorInstance)))
        system.actorOf(Props(new StatusPublisherActor(curatorInstance)))

        val startingInfo = s"Launching workflow in Spark driver..."
        log.info(startingInfo)
        statusService.update(WorkflowStatus(
          id = workflowId,
          status = Starting,
          statusInfo = Some(startingInfo)
        ))
        val contextService = ContextsService(curatorInstance)

        if(workflow.executionEngine == WorkflowExecutionEngine.Batch)
          contextService.clusterContext(workflow, pluginsFiles)
        else contextService.clusterStreamingContext(workflow, pluginsFiles)
      } match {
        case Success(_) =>
          val information = s"Workflow in Spark driver was properly stopped"
          log.info(information)
          statusService.update(WorkflowStatus(
            id = workflow.id.get,
            status = Stopped,
            statusInfo = Some(information)
          ))
        case Failure(exception: ErrorManagerException) =>
          statusService.update(WorkflowStatus(
            id = workflow.id.get,
            status = Failed,
            statusInfo = Option(exception.msg)
          ))
          throw exception
        case Failure(exception) =>
          val information = s"Error initiating workflow in Spark driver"
          val error = WorkflowError(
            information,
            PhaseEnum.Launch,
            exception.toString,
            ExceptionHelper.toPrintableException(exception)
          )
          executionService.setLastError(workflowId, error)
          statusService.update(WorkflowStatus(
            id = workflow.id.get,
            status = Failed,
            statusInfo = Option(information)
          ))
          throw DriverException(information, exception)
      }
    } match {
      case Success(_) =>
        log.info("Workflow in Spark driver successfully finished")
      case Failure(exception: ErrorManagerException) =>
        log.error(exception.msg, exception.getCause)
        throw exception
      case Failure(exception: DriverException) =>
        log.error(exception.msg, exception.getCause)
        throw exception
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
