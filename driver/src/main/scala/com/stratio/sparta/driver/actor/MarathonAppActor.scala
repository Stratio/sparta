/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.driver.actor

import akka.actor.{Actor, ActorRef, Props}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.enumerators.PhaseEnum
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.models.WorkflowError
import com.stratio.sparta.driver.actor.MarathonAppActor.{StartApp, StopApp}
import com.stratio.sparta.serving.core.actor.ClusterLauncherActor
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangeListenerActor.{ForgetExecutionStatusActions, OnExecutionStatusChangeDo}
import com.stratio.sparta.serving.core.actor.LauncherActor.Run
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.dao.WorkflowExecutionPostgresDao
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success, Try}

class MarathonAppActor(executionStatusListenerActor: ActorRef) extends Actor with SLF4JLogging {

  lazy val executionService = new WorkflowExecutionPostgresDao

  def receive: PartialFunction[Any, Unit] = {
    case StartApp(execution) => doStartApp(execution)
    case StopApp => preStopActions()
    case _ => log.info("Unrecognized message in Workflow App Actor")
  }

  def preStopActions(): Unit = {
    log.info("Shutting down Sparta Marathon Actor system")
    //Await.ready(context.system.terminate(), 1 minute)
    context.system.shutdown()
  }

  //scalastyle:off
  def doStartApp(execution: WorkflowExecution): Unit = {
    Try {
      log.debug(s"Obtained status: ${execution.lastStatus.state}")
      if (execution.lastStatus.state != Stopped && execution.lastStatus.state != Stopping &&
        execution.lastStatus.state != Failed && execution.lastStatus.state != Finished) {
        log.debug(s"Closing checker with execution id: ${execution.getExecutionId}")
        closeChecker(execution.getExecutionId)
        log.debug(s"Obtaining execution with workflow id: ${execution.getExecutionId}")

        log.debug(s"Starting execution: ${execution.toString}")
        val clusterLauncherActor =
          context.actorOf(Props(new ClusterLauncherActor(executionStatusListenerActor)), ClusterLauncherActorName)
        clusterLauncherActor ! Run(execution)
      } else {
        val information = "Workflow App launched by Marathon with incorrect state, the job was not executed"
        log.info(information)
        executionService.updateStatus(ExecutionStatusUpdate(
          execution.getExecutionId,
          ExecutionStatus(
          state = if (execution.lastStatus.state == Stopping) Stopped else execution.lastStatus.state,
          statusInfo = Option(information)
          )))
      }
    } match {
      case Success(_) =>
        log.info(s"StartApp in Workflow App executed without errors")
      case Failure(exception) =>
        val information = s"Error executing Spark Submit in Workflow App"
        log.error(information, exception)
        val error = WorkflowError(
          information,
          PhaseEnum.Launch,
          exception.toString,
          ExceptionHelper.toPrintableException(exception)
        )
        for {
          _ <- executionService.setLastError(execution.getExecutionId, error)
          _ <- executionService.updateStatus(ExecutionStatusUpdate(
            execution.getExecutionId,
            ExecutionStatus(state = Failed, statusInfo = Option(information))
          ))
        } yield {
          log.debug(s"Updated correctly the execution status ${execution.getExecutionId} to $Failed in MarathonAppActor")
        }
    }
  }

  //scalastyle:on

  def closeChecker(executionId: String): Unit = {
    log.debug(s"Close checker added to execution id: $executionId")

    executionStatusListenerActor ! OnExecutionStatusChangeDo(executionId) { executionStatusChange =>
      if (executionStatusChange.newExecution.lastStatus.state == Stopped ||
        executionStatusChange.newExecution.lastStatus.state == Failed) {
        try {
          val information = s"Executing pre-close actions in Workflow App ..."
          log.info(information)
          preStopActions()
        } finally {
          executionStatusListenerActor ! ForgetExecutionStatusActions(executionId)
        }
      }
    }
  }

}

object MarathonAppActor {

  case class StartApp(execution: WorkflowExecution)

  case object StopApp

}