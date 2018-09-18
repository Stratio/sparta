/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, ActorRef, PoisonPill}
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.serving.core.actor.LauncherActor.Start
import com.stratio.sparta.serving.core.marathon.MarathonService
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.dao.WorkflowExecutionPostgresDao
import com.stratio.sparta.serving.core.utils._
import org.apache.curator.framework.CuratorFramework
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}

class MarathonLauncherActor extends Actor
  with SchedulerUtils {

  private val executionService = new WorkflowExecutionPostgresDao

  override def receive: PartialFunction[Any, Unit] = {
    case Start(execution) => doStartExecution(execution)
    case _ => log.info("Unrecognized message in Marathon Launcher Actor")
  }

  def doStartExecution(workflowExecution: WorkflowExecution): Unit = {
    Try {
      new MarathonService(context, workflowExecution)
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
        executionService.setLastError(workflowExecution.getExecutionId, error)
        executionService.updateStatus(ExecutionStatusUpdate(
          workflowExecution.getExecutionId,
          ExecutionStatus(
            state = Failed,
            statusInfo = Option(information)
          )))
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
          marathonApp.launch()
          workflowExecution.getWorkflowToExecute
        } match {
          case Success(_) =>
            executionService.updateStatus(ExecutionStatusUpdate(
              workflowExecution.getExecutionId,
              ExecutionStatus(
                state = Uploaded,
                statusInfo = Option(information)
              )))
            executionService.setLaunchDate(workflowExecution.getExecutionId, new DateTime())
          case Failure(exception) =>
            val information = s"An error was encountered while launching the Workflow App in the Marathon API"
            val error = WorkflowError(
              information,
              PhaseEnum.Launch,
              exception.toString,
              ExceptionHelper.toPrintableException(exception)
            )
            executionService.setLastError(workflowExecution.getExecutionId, error)
            executionService.updateStatus(ExecutionStatusUpdate(
              workflowExecution.getExecutionId,
              ExecutionStatus(
                state = Failed,
                statusInfo = Option(information)
              )))
        }
    }
  }
}