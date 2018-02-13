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
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.WorkflowStatusChange
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus

import scala.concurrent.Future
import scala.concurrent.blocking

class WorkflowStatusListenerActor extends Actor with SLF4JLogging {

  import WorkflowStatusListenerActor._

  private case class ActorState(
                                 genericActions: Map[String, List[WorkflowStatusChangeAction]],
                                 workflowActions: Map[String, List[WorkflowStatusChangeAction]]
                               )

  override def preStart(): Unit =
    context.system.eventStream.subscribe(self, classOf[WorkflowStatusChange])

  override def postStop(): Unit =
    context.system.eventStream.unsubscribe(self, classOf[WorkflowStatusChange])

  def receive: Receive = receive(ActorState(Map.empty, Map.empty)) // Initial state

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
      actions.workflowActions.getOrElse(workflowStatus.id, Nil) foreach { callback =>
        Future {
          blocking(callback(workflowStatus))
        }(context.dispatcher)
      }
      actions.genericActions.foreach { case (_, genericActions) =>
        genericActions.foreach { callback =>
          Future {
            blocking(callback(workflowStatus))
          }(context.dispatcher)
        }
      }
    case _ =>
      log.debug("Unrecognized message in Workflow Status Listener Actor")
  }

  def receive(actions: ActorState): Receive =
    managementReceive(actions).orElse(eventsReceive(actions))

}

object WorkflowStatusListenerActor {

  type WorkflowStatusChangeAction = WorkflowStatus => Unit

  case class OnWorkflowStatusChangeDo(workflowId: String)(val action: WorkflowStatusChangeAction)

  case class OnWorkflowStatusesChangeDo(key: String)(val action: WorkflowStatusChangeAction)

  case class ForgetWorkflowStatusActions(id: String)

}