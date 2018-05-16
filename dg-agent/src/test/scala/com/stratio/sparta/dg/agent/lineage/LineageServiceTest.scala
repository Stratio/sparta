/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.dg.agent.lineage

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{ActorKilledException, ActorSystem, Kill, Props}
import akka.testkit.{EventFilter, ImplicitSender, TestActorRef, TestKit}
import com.stratio.sparta.dg.agent.commons.LineageUtils
import com.stratio.sparta.dg.agent.model.SpartaWorkflowStatusMetadata
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.actor.{StatusListenerActor, WorkflowListenerActor}
import com.stratio.sparta.serving.core.helpers.GraphHelper
import com.stratio.sparta.serving.core.models.enumerators.{NodeArityEnum, WorkflowExecutionMode, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.workflow._
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scalax.collection.Graph
import scalax.collection.edge.LDiEdge

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
      GlobalSettings(executionMode = WorkflowExecutionMode.local),
      StreamingSettings(
        JsoneyString("6s"), None, None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
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
    val graph: Graph[NodeGraph, LDiEdge] = GraphHelper.createGraph(testWorkflow01)

    val indexTypeEvent = 3
  }

  trait CommonActors {

    val stListenerActor = TestActorRef[StatusListenerActor](Props[StatusListenerActor])
    val workflowListenerActor = TestActorRef[WorkflowListenerActor](Props[WorkflowListenerActor])
    val lineageService = TestActorRef[LineageService](LineageService.props(stListenerActor, workflowListenerActor))
  }

  "LineageService" should {

    "LineageUtils workflowToMetadatapath" in new CommonMetadata {
      LineageUtils.workflowMetadataPathString(testWorkflow01, None, "input").toString()
        .split("/")(1) should equal(testWorkflow01.group.name.substring(1).replace("_", "/") ++ "_" ++
      testWorkflow01.name ++ "_" ++ testWorkflow01.version.toString)
    }

    "LineageUtils InputMetadata return metadataList with outcoming nodes" in new CommonMetadata {
      val result = LineageUtils.inputMetadataLineage(testWorkflow01, graph)
      result.head.outcomingNodes.length shouldBe 3
      result.head.name should equal(nodes.head.name)
      assert(result.forall(node => node.metadataPath.toString().split("/")(indexTypeEvent)
        .equals("a")))
    }

    "LineageUtils TransformationMetadata return metadataList with incoming and outcoming nodes" in new CommonMetadata {
      val result = LineageUtils.transformationMetadataLineage(testWorkflow01, graph)
      result.head.incomingNodes.length shouldBe 1
      result.head.outcomingNodes.length shouldBe 1
      assert(result.forall(node => node.metadataPath.toString().split("/")(indexTypeEvent)
        .equals("t")))
    }

    "LineageUtils OutputMetadata return metadataList with incoming nodes" in new CommonMetadata {
      val result = LineageUtils.outputMetadataLineage(testWorkflow01, graph)
      result.head.incomingNodes.length shouldBe 1
      nodes.filter(_.stepType == "Output").map(_.name) should contain(result.head.name)
      assert(result.head.metadataPath.toString().split("/")(indexTypeEvent)
        .equals("b"))
      assert(result.last.metadataPath.toString().split("/")(indexTypeEvent)
        .equals("c"))
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
        Option(WorkflowExecution("qwerty12345", None, None, None, None, Option(GenericDataExecution(
          testWorkflow01, WorkflowExecutionMode.dispatcher, "1234"))))))
      result.head.size shouldBe 1
      result.get.head.genericType.value should equal("status")
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

      result.head.metadataPath.toString().split("/")(1) should equal ("home_workflow-01_0")
    }

  }
}