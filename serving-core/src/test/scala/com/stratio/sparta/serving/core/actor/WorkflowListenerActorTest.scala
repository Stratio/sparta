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
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.actor.WorkflowListenerActor.{ForgetWorkflowActions, OnWorkflowChangeDo}
import com.stratio.sparta.serving.core.actor.WorkflowPublisherActor.{Notification, WorkflowChange}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.enumerators.NodeArityEnum
import com.stratio.sparta.serving.core.models.workflow._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}

class WorkflowListenerActorTest extends TestKit(ActorSystem("ListenerActorSpec", SpartaConfig.daemonicAkkaConfig))
  with WordSpecLike
  with Matchers
  with ImplicitSender
  with MockitoSugar {

  val nodes = Seq(
    NodeGraph("a", "", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
    NodeGraph("b", "", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
  )
  val edges = Seq(
    EdgeGraph("a", "b")
  )
  val validPipeGraph = PipelineGraph(nodes, edges)
  val settingsModel = Settings(
    GlobalSettings(executionMode = "local"),
    StreamingSettings(
      JsoneyString("6s"), None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
    SparkSettings(
      JsoneyString("local[*]"), sparkKerberos = false, sparkDataStoreTls = false, sparkMesosSecurity = false,
      None, SubmitArguments(), SparkConf(SparkResourcesConf())
    )
  )

  def publishEvent(event: Notification): Unit =
    system.eventStream.publish(event)

  "A Status Listener Actor" should {

    val workflowListenerActor = system.actorOf(Props(new WorkflowListenerActor))

    val testWorkflow01 = Workflow(
      id = Option("workflow-01"),
      settings = settingsModel,
      name = "workflow-01",
      description = "whatever",
      pipelineGraph = validPipeGraph
    )
    val testWorkflow02 = Workflow(
      id = Option("workflow-02"),
      settings = settingsModel,
      name = "workflow-02",
      description = "whatever",
      pipelineGraph = validPipeGraph
    )

    "Accept subscription requests from clients" in {
      workflowListenerActor ! OnWorkflowChangeDo(testWorkflow01.id.get)(self ! _)
      expectNoMsg()
    }

    "Execute registered callbacks when the right event has been published" in {
      publishEvent(WorkflowChange("whatever", testWorkflow01))
      expectMsg(testWorkflow01)
      publishEvent(WorkflowChange("whatever", testWorkflow02))
      expectNoMsg()
    }

    "Accept additional subscriptions and execute all registered callbacks" in {
      workflowListenerActor ! OnWorkflowChangeDo(testWorkflow02.id.get)(self ! _)
      expectNoMsg()

      publishEvent(WorkflowChange("whatever", testWorkflow01))
      expectMsg(testWorkflow01)

      publishEvent(WorkflowChange("whatever", testWorkflow02))
      expectMsg(testWorkflow02)
    }

    "Remove all callbacks associated with a workflow id" in {
      workflowListenerActor ! ForgetWorkflowActions(testWorkflow01.id.get)

      publishEvent(WorkflowChange("whatever", testWorkflow01))
      expectNoMsg()

      publishEvent(WorkflowChange("whatever", testWorkflow02))
      expectMsg(testWorkflow02)
    }


  }


}
