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
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.WorkflowChange
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus

import scala.concurrent.Future
import scala.concurrent.blocking

object ListenerActor {
  type WorkflowChangeAction = WorkflowStatus => Unit

  case class OnWorkflowChangeDo(workflowId: String)(val action: WorkflowChangeAction)
  case class ForgetWorkflowActions(id: String)
}

class ListenerActor extends Actor {
  import ListenerActor._

  type ActorState = Map[String, List[WorkflowChangeAction]]

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[WorkflowChange])
  }

  def receive: Receive = receive(Map.empty) // Initial state

  def managementReceive(workflowId2action: ActorState): Receive = {
    case request @ OnWorkflowChangeDo(id) =>
      val newState = workflowId2action + ((id, request.action :: workflowId2action.getOrElse(id, Nil)))
      context.become(receive(newState))
    case ForgetWorkflowActions(id) =>
      context.become(receive(workflowId2action - id))
  }

  def eventsReceive(workflowId2action: ActorState): Receive = {
    case WorkflowChange(_, workflowStatus) =>
      workflowId2action.getOrElse(workflowStatus.id, Nil) foreach { callback =>
        Future {
          blocking((callback(workflowStatus)))
        } (context.dispatcher)
      }
  }

  def receive(workflowId2action: ActorState): Receive =
    managementReceive(workflowId2action) orElse eventsReceive(workflowId2action)

}