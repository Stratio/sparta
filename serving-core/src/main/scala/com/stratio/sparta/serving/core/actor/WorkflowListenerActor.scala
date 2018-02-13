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
import com.stratio.sparta.serving.core.actor.WorkflowPublisherActor.WorkflowChange
import com.stratio.sparta.serving.core.models.workflow.Workflow

import scala.concurrent.{Future, blocking}

class WorkflowListenerActor extends Actor with SLF4JLogging {

  import WorkflowListenerActor._

  private val workflowActions = scala.collection.mutable.Map[String, List[WorkflowChangeAction]]()
  private val genericActions = scala.collection.mutable.Map[String, List[WorkflowChangeAction]]()

  override def preStart(): Unit =
    context.system.eventStream.subscribe(self, classOf[WorkflowChange])

  override def postStop(): Unit =
    context.system.eventStream.unsubscribe(self, classOf[WorkflowChange])

  override def receive: Receive = {
    case request@OnWorkflowChangeDo(id) =>
      workflowActions += ((id, request.action :: workflowActions.getOrElse(id, Nil)))
    case request@OnWorkflowsChangesDo(key) =>
      genericActions += ((key, request.action :: workflowActions.getOrElse(key, Nil)))
    case ForgetWorkflowActions(id) =>
      workflowActions -= id
      genericActions -= id
    case WorkflowChange(_, workflow) =>
      workflowActions.getOrElse(workflow.id.getOrElse(""), Nil) foreach { callback =>
        Future {
          blocking(callback(workflow))
        }(context.dispatcher)
      }
      genericActions.foreach { case (_, actions) =>
        actions.foreach { callback =>
          Future {
            blocking(callback(workflow))
          }(context.dispatcher)
        }
      }

    case _ => log.debug("Unrecognized message in Workflow Listener Actor")
  }

}

object WorkflowListenerActor {

  type WorkflowChangeAction = Workflow => Unit

  case class OnWorkflowChangeDo(workflowId: String)(val action: WorkflowChangeAction)

  case class OnWorkflowsChangesDo(key: String)(val action: WorkflowChangeAction)

  case class ForgetWorkflowActions(id: String)

}