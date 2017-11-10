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

import com.stratio.sparta.serving.core.services.WorkflowValidatorService
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class WorkflowValidationTest extends WordSpec with Matchers with MockitoSugar {

  val nodes = Seq(
    NodeGraph("a", "", "", "", WriterGraph()),
    NodeGraph("b", "", "", "", WriterGraph())
  )
  val edges = Seq(
    EdgeGraph("a", "b")
  )
  val validPipeGraph = PipelineGraph(nodes , edges)
  val emptyPipeGraph = PipelineGraph(Seq.empty[NodeGraph], Seq.empty[EdgeGraph])
  val settingsModel = Settings(
    GlobalSettings(),
    StreamingSettings("6s", None, None, None, CheckpointSettings("test/test")),
    SparkSettings("local[*]", sparkKerberos = false, sparkDataStoreTls = false, None, SubmitArguments(),
      SparkConf(SparkResourcesConf(), SparkDockerConf(), SparkMesosConf()))
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
      implicit implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyNodes

      result.valid shouldBe false
    }

    "validate one node" in {
      val pipeline = PipelineGraph(Seq(nodes.head), edges)
      implicit implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyNodes

      result.valid shouldBe false
    }

    "validate correct nodes" in {
      val pipeline = PipelineGraph(nodes , edges)
      implicit implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyNodes

      result.valid shouldBe true
    }

    "validate non empty edges" in {
      val pipeline = PipelineGraph(nodes, Seq.empty[EdgeGraph])
      implicit implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyEdges

      result.valid shouldBe false
    }

    "validate one edge" in {
      val pipeline = PipelineGraph(nodes, Seq(edges.head))
      implicit implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
      val result = new WorkflowValidation().validateNonEmptyEdges

      result.valid shouldBe true
    }

    "validate correct edges" in {
      val pipeline = PipelineGraph(nodes , edges)
      implicit implicit val workflow = emptyWorkflow.copy(pipelineGraph = pipeline)
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
  }
}

