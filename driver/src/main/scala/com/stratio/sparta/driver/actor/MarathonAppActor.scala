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

package com.stratio.sparta.driver.actor

import akka.actor.{Actor, ActorRef, Props}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.driver.actor.MarathonAppActor.{StartApp, StopApp}
import com.stratio.sparta.serving.core.actor.ClusterLauncherActor
import com.stratio.sparta.serving.core.actor.LauncherActor.StartWithRequest
import com.stratio.sparta.serving.core.actor.WorkflowStatusListenerActor.{ForgetWorkflowStatusActions, OnWorkflowStatusChangeDo}
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
    case StartApp(workflowId) => doStartApp(workflowId)
    case StopApp => preStopActions()
    case _ => log.info("Unrecognized message in Workflow App Actor")
  }

  def preStopActions(): Unit = {
    log.info("Shutting down Sparta Marathon Actor system")
    //Await.ready(context.system.terminate(), 1 minute)
    context.system.shutdown()
  }

  //scalastyle:off
  def doStartApp(workflow: Workflow): Unit = {
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
            executionService.findById(workflow.id.get) match {
              case Success(executionSubmit) =>
                log.debug(s"Starting execution: ${executionSubmit.toString}")
                val clusterLauncherActor =
                  context.actorOf(Props(new ClusterLauncherActor(curatorFramework, listenerActor)), ClusterLauncherActorName)
                clusterLauncherActor ! StartWithRequest(workflow, executionSubmit)
              case Failure(exception) => throw exception
            }
          } else {
            val information = "Workflow App launched by Marathon with incorrect state, the job was not executed"
            log.info(information)
            preStopActions()
            statusService.update(WorkflowStatus(
              id = workflow.id.get,
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
        statusService.update(WorkflowStatus(
          id = workflow.id.get,
          status = Failed,
          statusInfo = Option(information),
          lastError = Option(WorkflowError(information, PhaseEnum.Launch, exception.toString))
        ))
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

  case class StartApp(workflow: Workflow)

  case object StopApp

}