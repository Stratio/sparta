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

package com.stratio.sparta.serving.core.services

import com.stratio.sparta.serving.core.models.enumerators.NodeArityEnum
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.services.WorkflowValidatorService
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class WorkflowValidatorServiceTest extends WordSpec with Matchers with MockitoSugar {

  val nodes = Seq(
    NodeGraph("a", "Input", "", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
    NodeGraph("b", "Output", "", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
  )
  val edges = Seq(
    EdgeGraph("a", "b")
  )
  val validPipeGraph = PipelineGraph(nodes , edges)
  val emptyPipeGraph = PipelineGraph(Seq.empty[NodeGraph], Seq.empty[EdgeGraph])
  val settingsModel = Settings(
    GlobalSettings(),
    StreamingSettings("6s", None, None, None, CheckpointSettings("test/test")),
    SparkSettings("local[*]", sparkKerberos = false, sparkDataStoreTls = false, sparkMesosSecurity = false,
      None, SubmitArguments(), SparkConf(SparkResourcesConf(), SparkDockerConf(), SparkMesosConf()))
  )
  val workflowValidatorService = new WorkflowValidatorService
  val emptyWorkflow = Workflow(
    id = None,
    settings = settingsModel,
    name = "testworkflow",
    description = "whatever",
    pipelineGraph = emptyPipeGraph
  )


  "workflowValidatorService" must {

    "validate a correct workflow" in {
      val workflow = emptyWorkflow.copy(pipelineGraph = validPipeGraph)
      val result = workflowValidatorService.validate(workflow)

      result.valid shouldBe true
    }

    "validate a wrong workflow" in {
      val workflow = emptyWorkflow.copy(pipelineGraph = emptyPipeGraph)
      val result = workflowValidatorService.validate(workflow)

      result.valid shouldBe false
    }

  }
}

