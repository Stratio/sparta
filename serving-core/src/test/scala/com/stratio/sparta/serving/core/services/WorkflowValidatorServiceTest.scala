/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.models.enumerators.NodeArityEnum
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.workflow._
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
    GlobalSettings(local, Seq.empty, Seq.empty, true ,Some(JsoneyString("constraint1:constraint2"))),
    StreamingSettings(
      JsoneyString("6s"), None, None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
    SparkSettings(
      JsoneyString("local[*]"), sparkKerberos = false, sparkDataStoreTls = false,
      sparkMesosSecurity = false, None, SubmitArguments(), SparkConf(SparkResourcesConf()))
  )
  val wrongSettingsModel = Settings(
    GlobalSettings(local, Seq.empty, Seq.empty, true ,Some(JsoneyString("constraint1constraint2"))),
    StreamingSettings(
      JsoneyString("6s"), None, None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
    SparkSettings(
      JsoneyString("local[*]"), sparkKerberos = false, sparkDataStoreTls = false,
      sparkMesosSecurity = false, None, SubmitArguments(), SparkConf(SparkResourcesConf()))
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
      val basicResult = workflowValidatorService.validateBasicSettings(workflow)
      val graphResult = workflowValidatorService.validateGraph(workflow)
      val advancedResult = workflowValidatorService.validateAdvancedSettings(workflow)
      val pluginsResult = workflowValidatorService.validatePlugins(workflow)
      val allResult = workflowValidatorService.validateAll(workflow)

      basicResult.valid shouldBe true
      graphResult.valid shouldBe true
      advancedResult.valid shouldBe true
      pluginsResult.valid shouldBe true
      allResult.valid shouldBe true

    }

    "validate a wrong workflow" in {
      val workflow = emptyWorkflow.copy(pipelineGraph = emptyPipeGraph)
      val graphResult = workflowValidatorService.validateGraph(workflow)
      val allResult = workflowValidatorService.validateAll(workflow)
      val advancedResult = workflowValidatorService.validateAdvancedSettings(
        workflow.copy(settings = wrongSettingsModel))

      graphResult.valid shouldBe false
      advancedResult.valid shouldBe false
      allResult.valid shouldBe false
    }

    "validate a wrong workflow with empty name" in {
      val workflow = emptyWorkflow.copy(name = "", pipelineGraph = validPipeGraph)
      val result = workflowValidatorService.validateBasicSettings(workflow)

      result.valid shouldBe false
    }

  }
}

