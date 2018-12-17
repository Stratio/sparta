/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import scala.util.{Failure, Success, Try}
import akka.actor.{Actor, PoisonPill}
import org.joda.time.DateTime
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.serving.core.actor.LauncherActor.Start
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.marathon.MarathonService
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.utils._

class MarathonLauncherActor extends Actor
  with SchedulerUtils {

  private val executionService = PostgresDaoFactory.executionPgService

  override def receive: PartialFunction[Any, Unit] = {
    case Start(execution) => doStartExecution(execution)
    case _ => log.info("Unrecognized message in Marathon Launcher Actor")
  }

  def doStartExecution(workflowExecution: WorkflowExecution): Unit = {
    Try {
      new MarathonService(context)
    } match {
      case Failure(exception) =>
        val information = s"Error initializing Workflow App"
        log.error(information, exception)
        val error = WorkflowError(
          information,
          PhaseEnum.Launch,
          exception.toString,
          ExceptionHelper.toPrintableException(exception)
        )
        executionService.updateStatus(ExecutionStatusUpdate(
          workflowExecution.getExecutionId,
          ExecutionStatus(
            state = Failed,
            statusInfo = Option(information)
          )), error)
        self ! PoisonPill
      case Success(marathonApp) =>
        val information = "Workflow App configuration initialized successfully"
        log.info(information)
        executionService.updateStatus(ExecutionStatusUpdate(
          workflowExecution.getExecutionId,
          ExecutionStatus(
            state = NotStarted
          )))
        Try {
          marathonApp.launch(workflowExecution)
          workflowExecution.getWorkflowToExecute
        } match {
          case Success(_) =>
            log.info(s"Workflow App correctly launched to Marathon API with execution id: ${workflowExecution.getExecutionId}")
            val updateStateResult = executionService.updateStatus(ExecutionStatusUpdate(
              workflowExecution.getExecutionId,
              ExecutionStatus(
                state = Uploaded,
                statusInfo = Option(information)
              )))
            executionService.setLaunchDate(updateStateResult, new DateTime())
          case Failure(exception) =>
            val information = s"An error was encountered while launching the Workflow App in the Marathon API"
            log.error(information, exception)
            val error = WorkflowError(
              information,
              PhaseEnum.Launch,
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
}