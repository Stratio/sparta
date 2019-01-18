/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.dg.agent.commons

import com.stratio.sparta.core.ContextBuilder.ContextBuilderImplicits
import com.stratio.sparta.core.workflow.step.{InputStep, OutputStep}
import com.stratio.sparta.dg.agent.models.LineageWorkflow
import com.stratio.sparta.serving.core.error.PostgresNotificationManagerImpl
import com.stratio.sparta.serving.core.helpers.GraphHelper.createGraph
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine._
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.workflow.SpartaWorkflow
import org.apache.spark.sql.Dataset
import org.apache.spark.streaming.dstream.DStream

import scala.util.Properties
import scalax.collection._
import scalax.collection.edge.LDiEdge

//scalastyle:off
object LineageUtils extends ContextBuilderImplicits{

  val StartKey = "StartedAt"
  val FinishedKey = "FinishedAt"
  val ErrorKey = "Error"
  val UrlKey = "Link"

  lazy val spartaVHost = Properties.envOrNone("HAPROXY_HOST").getOrElse("sparta")
  lazy val spartaInstanceName = Properties.envOrNone("MARATHON_APP_LABEL_DCOS_SERVICE_NAME").getOrElse("sparta")

  def checkIfProcessableWorkflow(executionStatusChange: WorkflowExecutionStatusChange): Boolean = {
    val eventStatus = executionStatusChange.newExecution.lastStatus.state
    val exEngine = executionStatusChange.newExecution.executionEngine.get

    (exEngine == Batch && eventStatus == WorkflowStatusEnum.Finished || eventStatus == WorkflowStatusEnum.Failed) ||
      (exEngine == Streaming && eventStatus == WorkflowStatusEnum.Started
        || eventStatus == WorkflowStatusEnum.Finished || eventStatus == WorkflowStatusEnum.Failed)
  }

  def getOutputNodesWithWriter(workflow: Workflow): Seq[(String, String)] = {
    import com.stratio.sparta.serving.core.helpers.GraphHelperImplicits._

    val graph: Graph[NodeGraph, LDiEdge] = createGraph(workflow)

    workflow.pipelineGraph.nodes.filter(_.stepType.toLowerCase == OutputStep.StepType)
      .sorted
      .flatMap { outputNode =>
        val outNodeGraph = graph.get(outputNode)
        val predecessors = outNodeGraph.diPredecessors.toList
        predecessors.map { node =>
          val writerName = node.writer.tableName.map(_.toString).getOrElse("")
          val tableName = if (writerName.nonEmpty) writerName else node.name

          outNodeGraph.name -> tableName
        }
      }.sorted
  }

  def getAllStepsProperties(workflow: Workflow) : Map[String, Map[String, String]] = {

    val errorManager = PostgresNotificationManagerImpl(workflow)
    val inOutNodes = workflow.pipelineGraph.nodes.filter(node =>
      node.stepType.toLowerCase == OutputStep.StepType || node.stepType.toLowerCase == InputStep.StepType).map(_.name)

    if (workflow.executionEngine == Streaming) {
      val spartaWorkflow = SpartaWorkflow[DStream](workflow, errorManager)
      spartaWorkflow.stages(execute = false)
      spartaWorkflow.lineageProperties(inOutNodes)
    } else if (workflow.executionEngine == Batch) {
      val spartaWorkflow = SpartaWorkflow[Dataset](workflow, errorManager)
      spartaWorkflow.stages(execute = false)
      spartaWorkflow.lineageProperties(inOutNodes)
    } else Map.empty
  }

  def setExecutionUrl(executionId: String): String = {
    "https://" + spartaVHost  + "/" + spartaInstanceName + "/#/executions/" + executionId
  }

  def setExecutionProperties(newExecution: WorkflowExecution): Map[String,String] = {
    Map(
      StartKey -> newExecution.genericDataExecution.startDate.getOrElse(None).toString,
      FinishedKey -> newExecution.resumedDate.getOrElse(None).toString,
      ErrorKey -> newExecution.genericDataExecution.lastError.toString,
      UrlKey -> setExecutionUrl(newExecution.getExecutionId))
  }

  def updateLineageWorkflow(responseWorkflow: LineageWorkflow, newWorkflow: LineageWorkflow) : LineageWorkflow = {
    LineageWorkflow (
      id = responseWorkflow.id,
      name = responseWorkflow.name,
      description = responseWorkflow.description,
      tenant = responseWorkflow.tenant,
      properties = newWorkflow.properties,
      transactionId = responseWorkflow.transactionId,
      actorType = responseWorkflow.actorType,
      jobType = responseWorkflow.jobType,
      statusCode = newWorkflow.statusCode,
      version = responseWorkflow.version,
      listActorMetaData = responseWorkflow.listActorMetaData
    )
  }

  def mapSparta2GovernanceJobType(executionEngine: ExecutionEngine): String =
    executionEngine match {
      case Streaming => "STREAM"
      case Batch => "BATCH"
    }

  def mapSparta2GovernanceStepType(stepType: String): String =
    stepType match {
      case InputStep.StepType => "IN"
      case OutputStep.StepType => "OUT"
    }

  def mapSparta2GovernanceStatuses(spartaStatus: WorkflowStatusEnum.Value): String =
    spartaStatus match {
      case Started => "RUNNING"
      case Finished => "FINISHED"
      case Failed => "ERROR"
    }

  def mapSparta2GovernanceDataStoreType(dataStoreType: String): String =
    dataStoreType match {
      case "Avro" | "Csv" | "FileSystem" | "Parquet" | "Xml" | "Json" => "HDFS"
      case "Jdbc" | "Postgres" => "SQL"
    }
}