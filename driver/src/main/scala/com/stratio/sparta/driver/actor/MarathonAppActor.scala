/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.driver.actor

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.driver.actor.MarathonAppActor.{StartApp, StopApp}
import com.stratio.sparta.serving.core.actor.ClusterLauncherActor
import com.stratio.sparta.serving.core.actor.LauncherActor.StartWithRequest
import com.stratio.sparta.serving.core.actor.StatusListenerActor.{ForgetWorkflowStatusActions, OnWorkflowStatusChangeDo}
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.{ExecutionService, WorkflowStatusService}
import org.apache.curator.framework.CuratorFramework

import scala.util.{Failure, Success, Try}

class MarathonAppActor(
                        val curatorFramework: CuratorFramework,
                        listenerActor: ActorRef
                      ) extends Actor with SLF4JLogging {

  private val statusService = new WorkflowStatusService(curatorFramework)
  private val executionService = new ExecutionService(curatorFramework)

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
    val workflow = execution.genericDataExecution.get.workflow
    Try {
      log.debug(s"Obtaining status with workflow id: ${workflow.id.get}")
      statusService.findById(workflow.id.get) match {
        case Success(status) =>
          log.debug(s"Obtained status: ${status.status}")
          if (status.status != Stopped && status.status != Stopping && status.status != Failed &&
            status.status != Finished) {
            log.debug(s"Closing checker with id: ${workflow.id.get} and name: ${workflow.name}")
            closeChecker(workflow.id.get, workflow.name)
            log.debug(s"Obtaining execution with workflow id: ${workflow.id.get}")

            log.debug(s"Starting execution: ${execution.toString}")
            val clusterLauncherActor =
              context.actorOf(Props(new ClusterLauncherActor(curatorFramework, listenerActor)), ClusterLauncherActorName)
            clusterLauncherActor ! StartWithRequest(workflow, execution)
          } else {
            val information = "Workflow App launched by Marathon with incorrect state, the job was not executed"
            log.info(information)
            preStopActions()
            statusService.update(WorkflowStatus(
              id = workflow.id.get,
              statusId = UUID.randomUUID.toString,
              status = if(status.status == Stopping) Stopped else status.status,
              statusInfo = Option(information)
            ))
          }
        case Failure(e) => throw e
      }
    } match {
      case Success(_) =>
        log.info(s"StartApp in Workflow App executed without errors")
      case Failure(exception) =>
        val information = s"Error executing Spark Submit in Workflow App"
        log.error(information, exception)
        preStopActions()
        val error = WorkflowError(information, PhaseEnum.Launch, exception.toString)
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          statusId = UUID.randomUUID.toString,
          status = Failed,
          statusInfo = Option(information)
        ))
        executionService.setLastError(workflow.id.get, error)
    }
  }

  //scalastyle:on

  def closeChecker(workflowId: String, workflowName: String): Unit = {
    log.debug(s"Close checker added to $workflowName with id: $workflowId")

    listenerActor ! OnWorkflowStatusChangeDo(workflowId) { workflowStatusStream =>
      if (workflowStatusStream.workflowStatus.status == Stopped ||
        workflowStatusStream.workflowStatus.status == Failed) {
        try {
          val information = s"Executing pre-close actions in Workflow App ..."
          log.info(information)
          preStopActions()
        } finally {
          listenerActor ! ForgetWorkflowStatusActions(workflowId)
        }
      }
    }
  }

}

object MarathonAppActor {

  case class StartApp(execution: WorkflowExecution)

  case object StopApp

}