/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.performance.dsl

import java.util.UUID

import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.models.enumerators.NodeArityEnum
import com.stratio.sparta.serving.core.models.workflow._

object WorkflowBuilder {

  def workflow: WorkflowBuilder = {

    val randomID = UUID.randomUUID().toString

    val settingsModel = Settings(
      GlobalSettings(),
      StreamingSettings(
        JsoneyString("6s"), None, None, None, None, Some(JsoneyString("100")), None, None,
        CheckpointSettings(JsoneyString("test/test"))),
      SparkSettings(JsoneyString("local[*]"), false, false, false, None, SubmitArguments(),
        SparkConf(SparkResourcesConf()))
    )

    val nodes = Seq(
      NodeGraph("a", "Input", "TestInputStep", "", Seq(NodeArityEnum.NullaryToNary), WriterGraph()),
      NodeGraph("b", "Output", "PrintOutputStep", "", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
    )
    val edges = Seq(
      EdgeGraph("a", "b")
    )

    new WorkflowBuilder(
      Workflow(
        id = Option(randomID.toString),
        settings = settingsModel,
        name = s"testworkflow$randomID",
        description = "whatever",
        pipelineGraph = PipelineGraph(nodes, edges)
      )
    )
  }

}

class WorkflowBuilder(val baseWorkflow: Workflow){

  def withId(id: String): WorkflowBuilder = {
    new WorkflowBuilder(baseWorkflow.copy(id = Option(id)))
  }

}