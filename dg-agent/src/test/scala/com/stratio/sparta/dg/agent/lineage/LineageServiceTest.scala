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

package com.stratio.sparta.dg.agent.lineage

import scalax.collection.Graph
import scalax.collection.GraphEdge.DiEdge

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{ActorKilledException, ActorSystem, Kill, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import com.stratio.sparta.dg.agent.commons.{LineageItem, LineageUtils}
import com.stratio.sparta.dg.agent.model.SpartaWorkflowStatusMetadata
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.actor.{WorkflowListenerActor, WorkflowStatusListenerActor}
import com.stratio.sparta.serving.core.helpers.GraphHelper
import com.stratio.sparta.serving.core.models.enumerators.{NodeArityEnum, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.workflow._

@RunWith(classOf[JUnitRunner])
class LineageServiceTest extends TestKit(ActorSystem("LineageActorSpec", ConfigFactory.parseString(
  """
  akka.loggers = ["akka.testkit.TestEventListener"]
  """)))
  with WordSpecLike
  with Matchers
  with ImplicitSender
  with MockitoSugar
  with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  trait CommonMetadata {

    val nodes = Seq(
      NodeGraph("a", "Input", "Input_A_Step", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
      NodeGraph("t", "Transformation", "Transformation_A_Abis", "", Seq(NodeArityEnum.UnaryToNary), WriterGraph()),
      NodeGraph("b", "Output", "Output_B_Step", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph()),
      NodeGraph("c", "Output", "Output_C_Step", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
    )
    val edges = Seq(
      EdgeGraph("a", "b"),
      EdgeGraph("a", "c"),
      EdgeGraph("a", "t"),
      EdgeGraph("t", "c")
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
    val testWorkflow01 = Workflow(
      id = Option("workflow-01"),
      settings = settingsModel,
      name = "workflow-01",
      description = "whatever",
      pipelineGraph = validPipeGraph
    )
    val graph: Graph[NodeGraph, DiEdge] = GraphHelper.createGraph(testWorkflow01)

    val indexTypeEvent = 5
  }

  trait CommonActors {

    val stListenerActor = TestActorRef[WorkflowStatusListenerActor](Props[WorkflowStatusListenerActor])
    val workflowListenerActor = TestActorRef[WorkflowListenerActor](Props[WorkflowListenerActor])
    val lineageService = TestActorRef[LineageService](LineageService.props(stListenerActor, workflowListenerActor))
  }

  "LineageService" should {

    "LineageUtils workflowToMetadatapath" in new CommonMetadata {
      LineageUtils.workflowMetadataPathString(testWorkflow01, None, "input").toString()
        .split("/")(1) should equal(testWorkflow01.group.name.substring(1).replace("_", "/"))
    }

    "LineageUtils InputMetadata return metadataList with outcoming nodes" in new CommonMetadata {
      val result = LineageUtils.inputMetadataLineage(testWorkflow01, graph)
      result.head.outcomingNodes.length shouldBe 3
      result.head.name should equal(nodes.head.name)
      assert(result.forall(node => node.metadataPath.toString().split("/")(indexTypeEvent)
        .equals(LineageItem.Input.toString)))
    }

    "LineageUtils TransformationMetadata return metadataList with incoming and outcoming nodes" in new CommonMetadata {
      val result = LineageUtils.transformationMetadataLineage(testWorkflow01, graph)
      result.head.incomingNodes.length shouldBe 1
      result.head.outcomingNodes.length shouldBe 1
      assert(result.forall(node => node.metadataPath.toString().split("/")(indexTypeEvent)
        .equals(LineageItem.Transformation.toString)))
    }

    "LineageUtils OutputMetadata return metadataList with incoming nodes" in new CommonMetadata {
      val result = LineageUtils.outputMetadataLineage(testWorkflow01, graph)
      result.head.incomingNodes.length shouldBe 1
      nodes.filter(_.stepType == "Output").map(_.name) should contain(result.head.name)
      assert(result.forall(node => node.metadataPath.toString().split("/")(indexTypeEvent)
        .equals(LineageItem.Output.toString)))
    }

    "LineageUtils TenantMetadata return default values for attributes" in {
      val result = LineageUtils.tenantMetadataLineage()

      result.head.oauthEnable shouldBe false
      result.head.gosecEnable shouldBe false
      result.head.xdCatalogEnable shouldBe false
      result.head.mesosAttributeConstraint shouldBe empty
      result.head.mesosHostnameConstraint shouldBe empty
    }

    "LineageUtils StatusMetadata return metadataList with status" in new CommonMetadata {
      val result: Option[List[SpartaWorkflowStatusMetadata]] = LineageUtils.statusMetadataLineage(WorkflowStatusStream(
        WorkflowStatus("qwerty12345", WorkflowStatusEnum.Failed),
        Option(testWorkflow01),
        None))
      result.head.size shouldBe 1
      result.get.head.metadataPath.toString().split("/")(indexTypeEvent) should equal(LineageItem.Status.toString)
    }

    "send start message on actor creation" in new CommonActors {
      expectNoMsg()
    }

    "when senderKafka is dead supervisor restart the actor" in new CommonActors {
      EventFilter[ActorKilledException](occurrences = 1) intercept {
        lineageService.underlyingActor.senderKafka ! Kill
      }
    }

    "supervisor strategy is always restart" in new CommonActors {
      val strategy = lineageService.underlyingActor.supervisorStrategy.decider
      strategy(new RuntimeException("boom")) should be(Restart)
    }

    "LineageUtils WorkflowMetadata correct values for attributes" in new CommonMetadata {
      val result = LineageUtils.workflowMetadataLineage(testWorkflow01)

      result.head.name shouldBe "workflow-01"
      result.head.key shouldBe "workflow-01"
      result.head.description shouldBe "whatever"
      result.head.executionMode shouldBe "Streaming"
      result.head.mesosConstraints shouldBe empty
      result.head.kerberosEnabled shouldBe false
      result.head.tlsEnabled shouldBe false
      result.head.mesosSecurityEnabled shouldBe false
      result.head.tags shouldBe List.empty
      result.head.modificationTime.isDefined shouldBe true

      result.head.metadataPath.toString().split("/")(2) should equal ("workflow-01")
    }

  }
}