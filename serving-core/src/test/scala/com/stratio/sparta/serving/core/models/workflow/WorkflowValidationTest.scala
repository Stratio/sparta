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

package com.stratio.sparta.serving.core.models.workflow

import java.util.UUID

import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.helpers.GraphHelper
import com.stratio.sparta.serving.core.models.enumerators.NodeArityEnum
import com.stratio.sparta.serving.core.services.WorkflowValidatorService
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.api.{ExistsBuilder, GetDataBuilder}
import org.apache.zookeeper.data.Stat
import org.junit.runner.RunWith
import org.mockito.Mockito.when
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class WorkflowValidationTest extends WordSpec with Matchers with MockitoSugar {

  val nodes = Seq(
    NodeGraph("a", "", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
    NodeGraph("b", "", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
  )
  val edges = Seq(
    EdgeGraph("a", "b")
  )
  val validPipeGraph = PipelineGraph(nodes , edges)
  val emptyPipeGraph = PipelineGraph(Seq.empty[NodeGraph], Seq.empty[EdgeGraph])
  val settingsModel = Settings(
    GlobalSettings(),
    StreamingSettings(JsoneyString("6s"), None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
    SparkSettings(JsoneyString("local[*]"), sparkKerberos = false, sparkDataStoreTls = false, sparkMesosSecurity = false,
      None, SubmitArguments(), SparkConf(SparkResourcesConf())
    )
  )
  implicit val workflowValidatorService = new WorkflowValidatorService
  val emptyWorkflow = Workflow(
    id = None,
    settings = settingsModel,
    name = "testworkflow",
    description = "whatever",
    pipelineGraph = emptyPipeGraph
  )


  "workflowValidation" must {

    "validate non empty nodes" in {
      val pipeline = PipelineGraph(Seq.empty[NodeGraph], edges)
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyNodes

      result.valid shouldBe false
    }

    "validate one node" in {
      val pipeline = PipelineGraph(Seq(nodes.head), edges)
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyNodes

      result.valid shouldBe false
    }

    "validate correct nodes" in {
      val pipeline = PipelineGraph(nodes , edges)
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyNodes

      result.valid shouldBe true
    }

    "validate non empty edges" in {
      val pipeline = PipelineGraph(nodes, Seq.empty[EdgeGraph])
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyEdges

      result.valid shouldBe false
    }

    "validate one edge" in {
      val pipeline = PipelineGraph(nodes, Seq(edges.head))
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyEdges

      result.valid shouldBe true
    }

    "validate correct edges" in {
      val pipeline = PipelineGraph(nodes , edges)
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyEdges

      result.valid shouldBe true
    }

    "validate all edges exists in nodes: invalid" in {
      val pipeline = PipelineGraph(Seq(nodes.head), edges)
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateEdgesNodesExists

      result.valid shouldBe false
    }

    "validate all edges exists in nodes, valid" in {
      val pipeline = PipelineGraph(nodes, edges)
      implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateEdgesNodesExists

      result.valid shouldBe true
    }

    "validate an acyclic graph" in{
      val nodes = Seq(
        NodeGraph("a", "", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("c", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("d", "", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c"),
        EdgeGraph("c", "d")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes , edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateGraphIsAcyclic

      result.valid shouldBe true

    }

    "not validate a graph with a cycle" in {
      val nodes = Seq(
        NodeGraph("a", "", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("c", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("d", "", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c"),
        EdgeGraph("c", "d"),
        EdgeGraph("d", "b")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes , edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateGraphIsAcyclic

      result.valid shouldBe false
      assert(result.messages.exists(msg => msg.contains("cycle")))
    }

    "validate a graph with correct arity" in {
      val nodes = Seq(
        NodeGraph("a", "", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "", "", "", Seq(NodeArityEnum.UnaryToNary), WriterGraph()),
        NodeGraph("c", "", "", "", Seq(NodeArityEnum.UnaryToUnary), WriterGraph()),
        NodeGraph("d", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("e", "", "", "", Seq(NodeArityEnum.BinaryToNary), WriterGraph()),
        NodeGraph("f", "", "", "", Seq(NodeArityEnum.NullaryToNary, NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("j", "", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c"),
        EdgeGraph("c", "d"),
        EdgeGraph("d", "e"),
        EdgeGraph("f", "e"),
        EdgeGraph("e", "j")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes , edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateArityOfNodes

      result.valid shouldBe true
    }

    "not validate a graph with invalid arity in input relation" in {
      val nodes = Seq(
        NodeGraph("a", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("b", "", "", "", Seq(NodeArityEnum.UnaryToNary), WriterGraph()),
        NodeGraph("c", "", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes , edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateArityOfNodes

      result.valid shouldBe false
    }

    "not validate a graph with invalid arity in output relation" in {
      val nodes = Seq(
        NodeGraph("a", "", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "", "", "", Seq(NodeArityEnum.UnaryToNary), WriterGraph()),
        NodeGraph("c", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes , edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateArityOfNodes

      result.valid shouldBe false
    }

    "not validate a graph with invalid arity in transform relation" in {
      val nodes = Seq(
        NodeGraph("a", "", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "", "", "", Seq(NodeArityEnum.BinaryToNary), WriterGraph()),
        NodeGraph("c", "", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes , edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateArityOfNodes

      result.valid shouldBe false
    }

    "not validate a graph with invalid arity two relations" in {
      val nodes = Seq(
        NodeGraph("a", "", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("b", "", "", "", Seq(NodeArityEnum.UnaryToUnary), WriterGraph()),
        NodeGraph("c", "", "", "", Seq(NodeArityEnum.UnaryToNary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes , edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateArityOfNodes

      result.valid shouldBe false
    }

    "validate a graph containing at least one Input-to-Output path" in{
      val nodes = Seq(
        NodeGraph("a", "Input", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("c", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("d", "Output", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph()),
        NodeGraph("e", "Output", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c"),
        EdgeGraph("b", "e"),
        EdgeGraph("c", "d")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes , edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateExistenceCorrectPath

      result.valid shouldBe true

    }

    "not validate a graph not containing any Input-to-Output path" in {
      val nodes = Seq(
        NodeGraph("a", "Input", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
        NodeGraph("b", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("c", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), WriterGraph()),
        NodeGraph("d", "Transformation", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
      )
      val edges = Seq(
        EdgeGraph("a", "b"),
        EdgeGraph("b", "c"),
        EdgeGraph("c", "d"),
        EdgeGraph("d", "b")
      )

      implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes , edges))
      implicit val graph = GraphHelper.createGraph(workflow)

      val result = new WorkflowValidation().validateExistenceCorrectPath

      result.valid shouldBe false
    }

    "validate a graph if the group is correct" in {
      val invalidGroupJSON =
        """
          | {
          |  "id" : "aaaaa",
          |  "name": "/home/test1"
          |  }
        """.stripMargin
      val invalidGroup= Group(Some("aaaaa"),"/home/test1")
      implicit val workflow = emptyWorkflow.copy(group= invalidGroup)
      implicit val graph = GraphHelper.createGraph(workflow)

      val curatorFramework = mock[CuratorFramework]
      val existsBuilder = mock[ExistsBuilder]
      val getDataBuilder = mock[GetDataBuilder]

      CuratorFactoryHolder.setInstance(curatorFramework)

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"/stratio/sparta/sparta/group/${invalidGroup.id.get}"))
        .thenReturn(new Stat())
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath(s"/stratio/sparta/sparta/group/${invalidGroup.id.get}"))
        .thenReturn(invalidGroupJSON.getBytes)

      val result = new WorkflowValidation().validateGroupName(workflow, Some(curatorFramework))

      result.valid shouldBe true
    }

    "not validate a graph if the group is not correct" in {
      val invalidGroupJSON =
        """
          | {
          |  "id" : "aaaaa",
          |  "name": "/home//"
          |  }
        """.stripMargin
      val invalidGroup= Group(Some("aaaaa"),"/home//")
      implicit val workflow = emptyWorkflow.copy(group= invalidGroup)
      implicit val graph = GraphHelper.createGraph(workflow)

      val curatorFramework = mock[CuratorFramework]
      val existsBuilder = mock[ExistsBuilder]
      val getDataBuilder = mock[GetDataBuilder]

      CuratorFactoryHolder.setInstance(curatorFramework)

      when(curatorFramework.checkExists())
        .thenReturn(existsBuilder)
      when(curatorFramework.checkExists()
        .forPath(s"/stratio/sparta/sparta/group/${invalidGroup.id.get}"))
        .thenReturn(new Stat())
      when(curatorFramework.getData)
        .thenReturn(getDataBuilder)
      when(curatorFramework.getData
        .forPath(s"/stratio/sparta/sparta/group/${invalidGroup.id.get}"))
        .thenReturn(invalidGroupJSON.getBytes)

      val result = new WorkflowValidation().validateGroupName(workflow, Some(curatorFramework))

      result.valid shouldBe false
    }

    "validate a correct workflow name" in {
      val pipeline = PipelineGraph(nodes , edges)
      implicit val workflow = emptyWorkflow.copy(name="workflow-correct", pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateName
      result.valid shouldBe true
    }

    "not validate an incorrect workflow name" in {
      val pipeline = PipelineGraph(nodes , edges)
      implicit val workflow = emptyWorkflow.copy(name="workflow-Incorrect!", pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateName
      result.valid shouldBe false
    }
  }
}

