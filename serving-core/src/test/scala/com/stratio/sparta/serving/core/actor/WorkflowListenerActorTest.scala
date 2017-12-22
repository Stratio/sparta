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

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.stratio.sparta.serving.core.actor.WorkflowListenerActor.{ForgetWorkflowActions, OnWorkflowChangeDo}
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.{Notification, WorkflowChange}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}

class WorkflowListenerActorTest extends TestKit(ActorSystem("ListenerActorSpec", SpartaConfig.daemonicAkkaConfig))
  with WordSpecLike
  with Matchers
  with ImplicitSender
  with MockitoSugar {

  def publishEvent(event: Notification): Unit =
    system.eventStream.publish(event)

  "A ListenerActor" should {

    val statusListenerActor = system.actorOf(Props(new WorkflowListenerActor))

    val testWorkflowStatus01 = WorkflowStatus("workflow-01", WorkflowStatusEnum.Finished)
    val testWorkflowStatus02 = WorkflowStatus("workflow-02", WorkflowStatusEnum.Launched)

    "Accept subscription requests from clients" in {
      statusListenerActor ! OnWorkflowChangeDo(testWorkflowStatus01.id)(self ! _)
      expectNoMsg()
    }

    "Execute registered callbacks when the right event has been published" in {
      publishEvent(WorkflowChange("whatever", testWorkflowStatus01))
      expectMsg(testWorkflowStatus01)
      publishEvent(WorkflowChange("whatever", testWorkflowStatus02))
      expectNoMsg()
    }

    "Accept additional subscriptions and execute all registered callbacks" in {
      statusListenerActor ! OnWorkflowChangeDo(testWorkflowStatus02.id)(self ! _)
      expectNoMsg()

      publishEvent(WorkflowChange("whatever", testWorkflowStatus01))
      expectMsg(testWorkflowStatus01)

      publishEvent(WorkflowChange("whatever", testWorkflowStatus02))
      expectMsg(testWorkflowStatus02)
    }

    "Remove all callbacks associated with a workflow id" in {
      statusListenerActor ! ForgetWorkflowActions(testWorkflowStatus01.id)

      publishEvent(WorkflowChange("whatever", testWorkflowStatus01))
      expectNoMsg()

      publishEvent(WorkflowChange("whatever", testWorkflowStatus02))
      expectMsg(testWorkflowStatus02)
    }


  }


}
