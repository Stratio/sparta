/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import scala.util.{Failure, Success, Try}
import akka.actor.{Actor, PoisonPill}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.driver.services.ContextsService
import com.stratio.sparta.serving.core.actor.LauncherActor.Start
import com.stratio.sparta.serving.core.exception.ErrorManagerException
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.helpers.{JarsHelper, WorkflowHelper}
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._

class LocalLauncherActor() extends Actor with SLF4JLogging {

  lazy private val executionService = PostgresDaoFactory.executionPgService

  override def receive: PartialFunction[Any, Unit] = {
    case Start(execution) => doStartExecution(execution)
    case _ => log.info("Unrecognized message in Local Launcher Actor")
  }

  private def doStartExecution(workflowExecution: WorkflowExecution): Unit = {
    Try {
      val workflow = workflowExecution.getWorkflowToExecute
      executionService.updateStatus(ExecutionStatusUpdate(
        workflowExecution.getExecutionId,
        ExecutionStatus(
          state = NotStarted
        )))
      val jars = WorkflowHelper.localWorkflowPlugins(workflow)
      val startedInformation = s"Starting workflow in local mode"
      log.info(startedInformation)
      executionService.updateStatus(ExecutionStatusUpdate(
        workflowExecution.getExecutionId,
        ExecutionStatus(
          state = Starting,
          statusInfo = Option(startedInformation)
        )))
      if (workflow.executionEngine == Streaming)
        ContextsService.localStreamingContext(workflowExecution, jars)
      if (workflow.executionEngine == Batch) {
        ContextsService.localContext(workflowExecution, jars)
        executionService.updateStatus(ExecutionStatusUpdate(
          workflowExecution.getExecutionId,
          ExecutionStatus(
            state = Stopping,
            statusInfo = Option("Workflow executed successfully, stopping it")
          )))
      }
    } match {
      case Success(_) =>
        log.info("Workflow executed successfully")
      case Failure(exception: ErrorManagerException) =>
        executionService.updateStatus(ExecutionStatusUpdate(
          workflowExecution.getExecutionId,
          ExecutionStatus(
            state = Failed,
            statusInfo = Option(exception.getPrintableMsg)
          )))
      case Failure(exception) =>
        val information = s"Error initiating the workflow"
        log.error(information, exception)
        val error = WorkflowError(
          information,
          PhaseEnum.Execution,
          exception.toString,
          ExceptionHelper.toPrintableException(exception)
        )
        executionService.updateStatus(ExecutionStatusUpdate(
          workflowExecution.getExecutionId,
          ExecutionStatus(
            state = Failed,
            statusInfo = Option(information)
          )), error)
    }
  }
}
