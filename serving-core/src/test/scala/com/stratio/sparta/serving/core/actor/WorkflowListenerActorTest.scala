/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.actor.WorkflowListenerActor.{ForgetWorkflowActions, OnWorkflowChangeDo}
import com.stratio.sparta.serving.core.actor.WorkflowPublisherActor.{Notification, WorkflowChange}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.enumerators.{NodeArityEnum, WorkflowExecutionMode}
import com.stratio.sparta.serving.core.models.workflow._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}

class WorkflowListenerActorTest extends
  TestKit(ActorSystem("WorkflowListenerActorSpec", SpartaConfig.daemonicAkkaConfig))
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
    GlobalSettings(executionMode = WorkflowExecutionMode.local),
    StreamingSettings(
      JsoneyString("6s"), None, None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
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
