/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.dg.agent.commons

import com.github.nscala_time.time.Imports.DateTimeFormat
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.dg.agent.commons.LineageUtils._
import com.stratio.sparta.serving.core.models.enumerators.{NodeArityEnum, WorkflowExecutionEngine}
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionMode._
import com.stratio.sparta.serving.core.models.workflow._
import org.joda.time.DateTime
import org.junit.runner.RunWith
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
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
      val newStatusStoppedByUser = statusEvent.copy(newExecution = statusEvent.newExecution.copy(
        statuses = Seq(ExecutionStatus(
          state = WorkflowStatusEnum.StoppedByUser, lastUpdateDate = Some(new DateTime(timestampEpochTest))
      ))))
      LineageUtils.checkIfProcessableWorkflow(newStatusStoppedByUser) should be(true)
    }
    "return false" in {
      val newStatusStoppingByUser = statusEvent.copy(newExecution = statusEvent.newExecution.copy(
        statuses = Seq(ExecutionStatus(
          state = WorkflowStatusEnum.StoppingByUser, lastUpdateDate = Some(new DateTime(timestampEpochTest))
        ))))
      LineageUtils.checkIfProcessableWorkflow(newStatusStoppingByUser) should be(false)
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

  "setExecutionProperties" should {
    "return the expected map of property" in {
      val format = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

      val expectedID = "test010101010"
      val epochString = "1970-01-01 00:00:00"
      val expectedEpoch = format.parseDateTime(epochString)

      val workflow = Workflow(
        id = Option(expectedID),
        settings = settingsModel,
        name = "testworkflow",
        description = "whatever",
        pipelineGraph = PipelineGraph(Seq.empty[NodeGraph], Seq.empty[EdgeGraph])
      )

      val actualExecution = WorkflowExecution(
        id = Option(expectedID),
        statuses = Seq(ExecutionStatus(
          state = WorkflowStatusEnum.StoppedByUser, lastUpdateDate = Some(new DateTime(timestampEpochTest))
        )),
        genericDataExecution = GenericDataExecution(
          workflow = workflow,
          executionMode = marathon,
          executionContext = ExecutionContext(),
          startDate = Option(expectedEpoch)
        )
      )

      val expectedMap =  Map(
        StartKey -> s"$expectedEpoch",
        FinishedKey -> "None",
        TypeFinishedKey -> "StoppedByUser",
        ErrorKey -> "None",
        UrlKey -> s"https://sparta/sparta/#/executions/$expectedID")

      LineageUtils.setExecutionProperties(actualExecution) should equal(expectedMap)
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
      val status: WorkflowStatusEnum.Value = statusEvent.newExecution.lastStatus.state

      LineageUtils.mapSparta2GovernanceStatuses(status) should be("FINISHED")
      LineageUtils.mapSparta2GovernanceStatuses(StoppedByUser) should be("FINISHED")
      LineageUtils.mapSparta2GovernanceStatuses(Started) should be("RUNNING")
      LineageUtils.mapSparta2GovernanceStatuses(Failed) should be("ERROR")
    }
  }

  "mapSparta2GovernanceDataStoreType" should {
    "return a valid string" in {
      val dsType = statusEvent.newExecution.getWorkflowToExecute.pipelineGraph.nodes.head.classPrettyName

      LineageUtils.mapSparta2GovernanceDataStoreType(dsType) should be("HDFS")
      LineageUtils.mapSparta2GovernanceDataStoreType("Parquet") should be("HDFS")
      LineageUtils.mapSparta2GovernanceDataStoreType("Jdbc") should be("SQL")
      LineageUtils.mapSparta2GovernanceDataStoreType("Postgres") should be("SQL")
    }
  }
}