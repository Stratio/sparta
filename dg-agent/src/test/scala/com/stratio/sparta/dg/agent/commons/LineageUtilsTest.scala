/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.dg.agent.commons

import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.models.enumerators.{NodeArityEnum, WorkflowExecutionEngine, WorkflowStatusEnum}
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.workflow._
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}


@RunWith(classOf[JUnitRunner])
class LineageUtilsTest extends WordSpec with Matchers {

  val writerA = WriterGraph(
    SaveModeEnum.Append,
    Option(JsoneyString("tableName"))
  )

  val nodes = Seq(
    NodeGraph("a", "input", "ParquetInputStep", "Parquet", Seq(NodeArityEnum.NullaryToNary), writerA),
    NodeGraph("b", "output", "PostgresOutputStep", "Postgres", Seq(NodeArityEnum.NaryToNullary), WriterGraph())
  )
  val edges = Seq(
    EdgeGraph("a", "b")
  )

  val settingsModel = Settings(
    GlobalSettings(local, Seq.empty, Seq.empty, Seq.empty, true, Some(JsoneyString("constraint1:constraint2"))),
    StreamingSettings(
      JsoneyString("6s"), None, None, None, None, None, None, CheckpointSettings(JsoneyString("test/test"))),
    SparkSettings(
      JsoneyString("local[*]"), sparkKerberos = false, sparkDataStoreTls = false,
      sparkMesosSecurity = false, None, SubmitArguments(), SparkConf(SparkResourcesConf()))
  )

  val timestampEpochTest = 1519051473L
  val pipeline = PipelineGraph(nodes, edges)
  val testWorkflow = Workflow(Option("qwerty12345"), "parquet-postgres",
    settings = settingsModel,
    tags = Option(List.empty[String]),
    pipelineGraph = pipeline,
    executionEngine = WorkflowExecutionEngine.Batch,
    group = Group(Option("987654"), "/home/test/subgroup"),
    lastUpdateDate = Option(new DateTime(timestampEpochTest))
  )

  val statusEvent: WorkflowExecutionStatusChange = WorkflowExecutionStatusChange(
    WorkflowExecution(
      id = Option("qwerty12345"),
      statuses = Seq(ExecutionStatus(
        state = WorkflowStatusEnum.Created,
        lastUpdateDate = Some(new DateTime(timestampEpochTest))
      )),
      genericDataExecution = GenericDataExecution(testWorkflow, local, ExecutionContext()),
      executionEngine = Option(WorkflowExecutionEngine.Batch)
    ),
    WorkflowExecution(
      id = Option("qwerty12345"),
      statuses = Seq(ExecutionStatus(
        state = WorkflowStatusEnum.Finished,
        lastUpdateDate = Some(new DateTime(timestampEpochTest))
      )),
      genericDataExecution = GenericDataExecution(testWorkflow, local, ExecutionContext()),
      executionEngine = Option(WorkflowExecutionEngine.Batch)
    )
  )

  "checkIfProcessableWorkflow" should {
    "return true" in {

      LineageUtils.checkIfProcessableWorkflow(statusEvent) should be(true)
    }
  }

  "getOutputNodesWithWriter" should {
    "return a list of output nodes and its writers" in {
      val workflow = statusEvent.newExecution.getWorkflowToExecute

      LineageUtils.getOutputNodesWithWriter(workflow).head should be(("b", "tableName"))
    }
  }

  "mapSparta2GovernanceJobType" should {
    "return a parsed execution engine" in {
      val exEngine = statusEvent.newExecution.getWorkflowToExecute.executionEngine

      LineageUtils.mapSparta2GovernanceJobType(exEngine) should be("BATCH")
    }
  }

  "mapSparta2GovernanceStepType" should {
    "return a valid string" in {
      val stepType = statusEvent.newExecution.getWorkflowToExecute.pipelineGraph.nodes.head.stepType

      LineageUtils.mapSparta2GovernanceStepType(stepType) should be("IN")
    }
  }

  "mapSparta2GovernanceStatuses" should {
    "return a valid string" in {
      val status = statusEvent.newExecution.lastStatus.state

      LineageUtils.mapSparta2GovernanceStatuses(status) should be("FINISHED")
    }
  }

  "mapSparta2GovernanceDataStoreType" should {
    "return a valid string" in {
      val dsType = statusEvent.newExecution.getWorkflowToExecute.pipelineGraph.nodes.head.classPrettyName

      LineageUtils.mapSparta2GovernanceDataStoreType(dsType) should be("HDFS")
    }
  }
}