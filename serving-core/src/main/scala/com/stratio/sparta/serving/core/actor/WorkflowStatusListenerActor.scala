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
package com.stratio.sparta.serving.core.actor

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.actor.ExecutionPublisherActor.WorkflowExecutionChange
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.WorkflowStatusChange
import com.stratio.sparta.serving.core.actor.WorkflowPublisherActor.{WorkflowChange, WorkflowRemove}
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowExecution, WorkflowStatus, WorkflowStatusStream}

import scala.concurrent.{Future, blocking}

class WorkflowStatusListenerActor extends Actor with SLF4JLogging {

  import WorkflowStatusListenerActor._

  private case class ActorState(
                                 genericActions: Map[String, List[WorkflowStatusChangeAction]],
                                 workflowActions: Map[String, List[WorkflowStatusChangeAction]],
                                 workflows: Map[String, Workflow],
                                 statuses: Map[String, WorkflowStatus],
                                 executions: Map[String, WorkflowExecution]
                               )

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[WorkflowStatusChange])
    context.system.eventStream.subscribe(self, classOf[WorkflowExecutionChange])
    context.system.eventStream.subscribe(self, classOf[WorkflowChange])
    context.system.eventStream.subscribe(self, classOf[WorkflowRemove])
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[WorkflowStatusChange])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowExecutionChange])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowChange])
    context.system.eventStream.unsubscribe(self, classOf[WorkflowRemove])
  }

  def receive: Receive = receive(ActorState(Map.empty, Map.empty, Map.empty, Map.empty, Map.empty)) // Initial state

  def managementReceive(actions: ActorState): Receive = {
    case request@OnWorkflowStatusChangeDo(id) =>
      val newActions = actions.workflowActions + ((id, request.action :: actions.workflowActions.getOrElse(id, Nil)))
      context.become(receive(actions.copy(workflowActions = newActions)))
    case request@OnWorkflowStatusesChangeDo(key) =>
      val newActions = actions.genericActions + ((key, request.action :: actions.genericActions.getOrElse(key, Nil)))
      context.become(receive(actions.copy(genericActions = newActions)))
    case ForgetWorkflowStatusActions(id) =>
      context.become(receive(actions.copy(
        genericActions = actions.genericActions - id,
        workflowActions = actions.workflowActions - id
      )))
  }

  def eventsReceive(actions: ActorState): Receive = {
    case WorkflowStatusChange(_, workflowStatus) =>
      doWorkflowChange(actions, workflowStatus)
      context.become(receive(actions.copy(
        statuses = actions.statuses + (workflowStatus.id -> workflowStatus)
      )))
    case WorkflowChange(_, workflow) =>
      context.become(receive(actions.copy(
        workflows = actions.workflows + (workflow.id.get -> workflow)
      )))
    case WorkflowExecutionChange(_, execution) =>
      context.become(receive(actions.copy(
        executions = actions.executions + (execution.id -> execution)
      )))
    case WorkflowRemove(_, workflow) =>
      actions.statuses.get(workflow.id.get).foreach { wStatus =>
        doWorkflowChange(actions, wStatus.copy(status = WorkflowStatusEnum.Stopped))
      }
      context.become(receive(actions.copy(
        workflowActions = actions.workflowActions - workflow.id.get,
        workflows = actions.workflows.filterKeys(key => key != workflow.id.get),
        executions = actions.executions.filterKeys(key => key != workflow.id.get),
        statuses = actions.statuses.filterKeys(key => key != workflow.id.get)
      )))
    case _ =>
      log.debug("Unrecognized message in Workflow Status Listener Actor")
  }

  def doWorkflowChange(actions: ActorState, workflowStatus: WorkflowStatus): Unit = {
    actions.workflowActions.getOrElse(workflowStatus.id, Nil) foreach { callback =>
      Future {
        try {
          blocking(callback(WorkflowStatusStream(
            workflowStatus,
            actions.workflows.get(workflowStatus.id),
            actions.executions.get(workflowStatus.id)
          )))
        } catch {
          case e: Exception => log.error(s"Error executing action for workflow status ${workflowStatus.id}." +
            s" With exception: ${e.getLocalizedMessage}")
        }
      }(context.dispatcher)
    }
    actions.genericActions.foreach { case (_, genericActions) =>
      genericActions.foreach { callback =>
        Future {
          try {
            blocking(callback(WorkflowStatusStream(
              workflowStatus,
              actions.workflows.get(workflowStatus.id),
              actions.executions.get(workflowStatus.id)
            )))
          } catch {
            case e: Exception => log.error(s"Error executing action for workflow status ${workflowStatus.id}." +
              s" With exception: ${e.getLocalizedMessage}")
          }
        }(context.dispatcher)
      }
    }
  }

  def receive(actions: ActorState): Receive =
    managementReceive(actions).orElse(eventsReceive(actions))

}

object WorkflowStatusListenerActor {

  type WorkflowStatusChangeAction = WorkflowStatusStream => Unit

  case class OnWorkflowStatusChangeDo(workflowId: String)(val action: WorkflowStatusChangeAction)

  case class OnWorkflowStatusesChangeDo(key: String)(val action: WorkflowStatusChangeAction)

  case class ForgetWorkflowStatusActions(id: String)

}