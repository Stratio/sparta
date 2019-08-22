/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.factory.SparkContextFactory
import com.stratio.sparta.serving.core.helpers.GraphHelper
import com.stratio.sparta.serving.core.models.enumerators.{NodeArityEnum, WorkflowExecutionMode}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SaveMode}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

class WorkflowValidationIT extends WordSpec with Matchers with MockitoSugar {

  val emptyPipeGraph = PipelineGraph(Seq.empty[NodeGraph], Seq.empty[EdgeGraph])
  val settingsModel = Settings(
    GlobalSettings(executionMode = WorkflowExecutionMode.local),
    StreamingSettings(
      JsoneyString("6s"), None, None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
    SparkSettings(
      JsoneyString("local[*]"), sparkKerberos = false, sparkDataStoreTls = false, sparkMesosSecurity = false,
      None, SubmitArguments(), SparkConf(SparkResourcesConf())
    )
  )

  val emptyWorkflow = Workflow(
    id = None,
    settings = settingsModel,
    name = "testworkflow",
    description = "whatever",
    pipelineGraph = emptyPipeGraph
  )

  "not validate a graph containing duplicate names" in {
    val nodes = Seq(
      NodeGraph("a", "Input", "", "", Seq(NodeArityEnum.NullaryToNary), Option(WriterGraph())),
      NodeGraph("stepNameTest", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), Option(WriterGraph())),
      NodeGraph("c", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), Option(WriterGraph())),
      NodeGraph("d", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), Option(WriterGraph())),
      NodeGraph("e", "Transformation", "", "", Seq(NodeArityEnum.NaryToNary), Option(WriterGraph())),
      NodeGraph("d", "Output", "", "", Seq(NodeArityEnum.NaryToNullary), Option(WriterGraph()))
    )
    val edges = Seq(
      EdgeGraph("a", "stepNameTest"),
      EdgeGraph("stepNameTest", "c"),
      EdgeGraph("c", "d"),
      EdgeGraph("d", "e"),
      EdgeGraph("e", "d")
    )

    val schema = StructType(Seq(
      StructField("name", StringType),
      StructField("surname", StringType)
    ))

    val xds = SparkContextFactory.getOrCreateStandAloneXDSession(None)

    val data = Row("name1", "surname1")
    val rdd = xds.sparkContext.parallelize(Seq(data))
    val df = xds.createDataFrame(rdd, schema)

    df.write.options(Map.empty[String, String]).mode(SaveMode.Overwrite).parquet("/tmp/test.parquet")
    xds.catalog.createTable("stepNameTest", "/tmp/test.parquet")

    implicit val workflow = emptyWorkflow.copy(pipelineGraph = PipelineGraph(nodes, edges))
    implicit val graph = GraphHelper.createGraph(workflow)

    val result = new WorkflowValidation().validateStepName

    result.valid shouldBe false
  }
}
