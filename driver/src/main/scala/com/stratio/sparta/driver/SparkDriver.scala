/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.driver

import akka.event.slf4j.SLF4JLogging
import com.google.common.io.BaseEncoding
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.driver.services.ContextsService
import com.stratio.sparta.serving.core.exception.{DriverException, ErrorManagerException}
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.dao.WorkflowExecutionPostgresDao
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Properties, Success, Try}

object SparkDriver extends SLF4JLogging with SpartaSerializer {

  import scala.concurrent.ExecutionContext.Implicits.global

  val NumberOfArguments = 2
  val ExecutionIdIndex = 0
  val PluginsFilesIndex = 1
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

      val executionId = args(ExecutionIdIndex)
      val pluginsFiles = new String(BaseEncoding.base64().decode(args(PluginsFilesIndex)))
        .split(",").filter(s => s != " " && s.nonEmpty)
      val executionService = new WorkflowExecutionPostgresDao

      val execution = Await.result(executionService.findExecutionById(executionId), Duration.Inf)

      Try {
        val startingInfo = s"Launching workflow in Spark driver..."
        log.info(startingInfo)
        executionService.updateStatus(ExecutionStatusUpdate(
          executionId,
          ExecutionStatus(state = Starting, statusInfo = Option(startingInfo))
        ))
        val contextService = ContextsService()
        val workflow = execution.getWorkflowToExecute
        if (workflow.executionEngine == WorkflowExecutionEngine.Batch)
          contextService.clusterContext(execution, pluginsFiles)
        else contextService.clusterStreamingContext(execution, pluginsFiles)
      } match {
        case Success(_) =>
          val information = s"Workflow in Spark driver was properly stopped"
          Try {
            log.info(information)
            val updateStateResult = executionService.updateStatus(ExecutionStatusUpdate(
              executionId,
              ExecutionStatus(state = Stopped, statusInfo = Option(information))
            ))
            Await.result(updateStateResult, 60 seconds)
          } match {
            case Success(_) =>
            case Failure(_) => throw DriverException("Error updating finish status in Spark driver.")
          }
        case Failure(exception: ErrorManagerException) =>
          Try {
            val updateStateResult = executionService.updateStatus(ExecutionStatusUpdate(
              executionId,
              ExecutionStatus(state = Failed, statusInfo = Option(exception.getPrintableMsg))
            ))
            Await.result(updateStateResult, 60 seconds)
          } match {
            case Success(_) =>
              throw exception
            case Failure(_) =>
              throw ErrorManagerException(exception.msg + ". Error updating finish status in Spark driver.", exception)
          }
        case Failure(exception) =>
          val information = s"Error initiating workflow in Spark driver"
          Try {
            val error = WorkflowError(
              information,
              PhaseEnum.Launch,
              exception.toString,
              ExceptionHelper.toPrintableException(exception)
            )
            val updateStateResult = for {
              _ <- executionService.setLastError(executionId, error)
              _ <- executionService.updateStatus(ExecutionStatusUpdate(
                executionId,
                ExecutionStatus(state = Failed, statusInfo = Option(information))
              ))
            } yield {
              log.debug(s"Updated correctly the execution status ${execution.getExecutionId} to $Failed in Spark Driver")
            }
            Await.result(updateStateResult, 60 seconds)
          } match {
            case Success(_) =>
              throw DriverException(information, exception)
            case Failure(_) =>
              throw DriverException(information + ". Error updating finish status in Spark driver.", exception)
          }
      }
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

  //scalastyle:on

}
