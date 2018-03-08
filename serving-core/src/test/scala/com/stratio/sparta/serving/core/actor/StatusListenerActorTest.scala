/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.{Notification, StatusChange}
import com.stratio.sparta.serving.core.actor.StatusListenerActor.{ForgetWorkflowStatusActions, OnWorkflowStatusChangeDo}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}

class StatusListenerActorTest extends TestKit(ActorSystem("ListenerActorSpec", SpartaConfig.daemonicAkkaConfig))
  with WordSpecLike
  with Matchers
  with ImplicitSender
  with MockitoSugar {

  def publishEvent(event: Notification): Unit =
    system.eventStream.publish(event)

  "A Workflow Status Listener Actor" should {

    val statusListenerActor = system.actorOf(Props(new StatusListenerActor))

    val testWorkflowStatus01 = WorkflowStatus("workflow-01", WorkflowStatusEnum.Finished)
    val testWorkflowStatus02 = WorkflowStatus("workflow-02", WorkflowStatusEnum.Launched)

    "Accept subscription requests from clients" in {
      statusListenerActor ! OnWorkflowStatusChangeDo(testWorkflowStatus01.id)(self ! _)
      expectNoMsg()
    }

    "Execute registered callbacks when the right event has been published" in {
      publishEvent(StatusChange("whatever", testWorkflowStatus01))
      expectMsg(testWorkflowStatus01)
      publishEvent(StatusChange("whatever", testWorkflowStatus02))
      expectNoMsg()
    }

    "Accept additional subscriptions and execute all registered callbacks" in {
      statusListenerActor ! OnWorkflowStatusChangeDo(testWorkflowStatus02.id)(self ! _)
      expectNoMsg()

      publishEvent(StatusChange("whatever", testWorkflowStatus01))
      expectMsg(testWorkflowStatus01)

      publishEvent(StatusChange("whatever", testWorkflowStatus02))
      expectMsg(testWorkflowStatus02)
    }

    "Remove all callbacks associated with a workflow id" in {
      statusListenerActor ! ForgetWorkflowStatusActions(testWorkflowStatus01.id)

      publishEvent(StatusChange("whatever", testWorkflowStatus01))
      expectNoMsg()

      publishEvent(StatusChange("whatever", testWorkflowStatus02))
      expectMsg(testWorkflowStatus02)
    }


  }


}
