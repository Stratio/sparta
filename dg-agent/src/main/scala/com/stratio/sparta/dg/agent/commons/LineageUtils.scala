/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.dg.agent.commons

import scala.util.{Properties, Try}
import scalax.collection._
import scalax.collection.edge.LDiEdge
import org.joda.time.DateTime
import com.stratio.governance.commons.agent.model.metadata.lineage.EventType.EventType
import com.stratio.governance.commons.agent.model.metadata.lineage.{TransformationMetadataProperties, _}
import com.stratio.governance.commons.agent.model.metadata.sparta.SpartaType
import com.stratio.governance.commons.agent.model.metadata.{MetadataPath, OperationCommandType, SourceType}
import com.stratio.sparta.dg.agent.commons.WorkflowStatusUtils.fromDatetimeToLongWithDefault
import com.stratio.sparta.core.workflow.step.{InputStep, OutputStep, TransformStep}
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum.{Failed, Finished, Started}
import com.stratio.sparta.serving.core.models.workflow._

/**
  * Utilitary object for dg-workflows methods
  */
//scalastyle:off
object LineageUtils {

  val tenantName = Properties.envOrElse("MARATHON_APP_LABEL_DCOS_SERVICE_NAME", "sparta")

  implicit def writerToMap(writer: WriterGraph): Map[String, String] = {
    writer.tableName match {
      case Some(t) if t.toString.nonEmpty => {
        val m = Map("tableName" -> writer.tableName.map(_.toString),
          "saveMode" -> Option(writer.saveMode.toString),
          "uniqueConstraintFields" -> writer.uniqueConstraintFields.map(_.toString),
          "uniqueConstraintName" -> writer.uniqueConstraintName.map(_.toString),
          "constraintType" -> writer.constraintType.map(_.toString),
          "partitionBy" -> writer.partitionBy.map(_.toString),
          "primaryKey" -> writer.primaryKey.map(_.toString),
          "errorTableName" -> writer.errorTableName.map(_.toString)
        )
        m.filter(p => p._2.isDefined && p._2.get.nonEmpty).map(p => (p._1, p._2.get))
      }
      case _ =>  Map.empty[String, String]
    }
  }

  def lineageProperties(metadataPath: MetadataPath, node: NodeGraph) = {
    if (node.lineageProperties.nonEmpty)
      node.configuration.filter(p => node.lineageProperties.contains(p._1)).map {
        case (k, v) => TransformationMetadataProperties(metadataPath, k, v.toString)
      }
    else
      node.configuration.map {
        case (k, v) => TransformationMetadataProperties(metadataPath, k, v.toString)
      }
  }

  def lineageWriterProperties(metadataPath: MetadataPath, node: NodeGraph) = {
    if (node.lineageProperties.nonEmpty)
      node.writer.filter(p => node.lineageProperties.contains(p._1)).map {
        case (k, v) => TransformationMetadataProperties(metadataPath, k, v.toString)
      }
    else
      node.writer.map {
        case (k, v) => TransformationMetadataProperties(metadataPath, k, v.toString)
      }
  }

  def workflowMetadataPathString(workflow: Workflow, extraPath: String*): MetadataPath = {
    val path = MetadataPath(Seq(
      LineageUtils.tenantName,
      workflow.group.name.substring(1).replaceAll("/", "_") ++ "_" ++
        workflow.name ++ "_" ++ workflow.version.toString).map(_.toString)
      ++ extraPath
    )
    path
  }

  def inputMetadataLineage(
                            workflow: Workflow,
                            graph: Graph[NodeGraph, LDiEdge],
                            operationCommandType: OperationCommandType = OperationCommandType.UNKNOWN
                          ): List[InputMetadata] = {
    workflow.pipelineGraph.nodes.filter(node => node.stepType.equalsIgnoreCase(InputStep.StepType)).map(
      n => {
        val metadataPath = workflowMetadataPathString(workflow, n.name)
        val input = InputMetadata(
          name = n.name,
          key = workflow.id.get,
          metadataPath = metadataPath,
          outcomingNodes = graph.get(n).diSuccessors.map(s =>
            workflowMetadataPathString(workflow, s.name)).toSeq,
          agentVersion = SpartaType.agentVersion,
          serverVersion = SpartaType.serverVersion,
          tags = workflow.tags.getOrElse(Seq.empty).toList,
          sourceType = SourceType.SPARTA,
          modificationTime = workflow.lastUpdateDate.map(_.getMillis),
          customType = SpartaType.INPUT,
          operationCommandType = operationCommandType
        )
        input.properties ++= lineageProperties(metadataPath, n)
        input.properties ++= lineageWriterProperties(metadataPath, n)
        input
      }
    ).toList
  }

  def outputMetadataLineage(
                             workflow: Workflow,
                             graph: Graph[NodeGraph, LDiEdge],
                             operationCommandType: OperationCommandType = OperationCommandType.UNKNOWN
                           ): List[OutputMetadata] = {
    workflow.pipelineGraph.nodes.filter(node => node.stepType.equalsIgnoreCase(OutputStep.StepType)).map(
      n => {
        val metadataPath = workflowMetadataPathString(workflow, n.name)
        val output = OutputMetadata(
          name = n.name,
          key = workflow.id.get,
          metadataPath = metadataPath,
          incomingNodes = graph.get(n).diPredecessors.map(pred =>
            workflowMetadataPathString(workflow, pred.name)).toSeq,
          agentVersion = SpartaType.agentVersion,
          serverVersion = SpartaType.serverVersion,
          tags = workflow.tags.getOrElse(Seq.empty).toList,
          sourceType = SourceType.SPARTA,
          modificationTime = workflow.lastUpdateDate.map(_.getMillis),
          customType = SpartaType.OUTPUT,
          operationCommandType = operationCommandType
        )
        output.properties ++= lineageProperties(metadataPath, n)
        output
      }
    ).toList
  }

  def transformationMetadataLineage(
                                     workflow: Workflow,
                                     graph: Graph[NodeGraph, LDiEdge],
                                     operationCommandType: OperationCommandType = OperationCommandType.UNKNOWN
                                   ): List[TransformationMetadata] = {
    workflow.pipelineGraph.nodes.filter(node => node.stepType.equalsIgnoreCase(TransformStep.StepType)).map(
      n => {
        val metadataPath = workflowMetadataPathString(workflow, n.name)
        val transformation = TransformationMetadata(
          name = n.name,
          key = workflow.id.get,
          metadataPath = metadataPath,
          outcomingNodes = graph.get(n).diSuccessors.map(s =>
            workflowMetadataPathString(workflow, s.name)).toSeq,
          incomingNodes = graph.get(n).diPredecessors.map(pred =>
            workflowMetadataPathString(workflow, pred.name)).toSeq,
          agentVersion = SpartaType.agentVersion,
          serverVersion = SpartaType.serverVersion,
          tags = workflow.tags.getOrElse(Seq.empty).toList,
          sourceType = SourceType.SPARTA,
          modificationTime = workflow.lastUpdateDate.map(_.getMillis),
          customType = SpartaType.TRANSFORMATION,
          operationCommandType = operationCommandType
        )
        transformation.properties ++= lineageProperties(metadataPath, n)
        transformation.properties ++= lineageWriterProperties(metadataPath, n)
        transformation
      }
    ).toList
  }

  def tenantMetadataLineage(): List[TenantMetadata] = {

    val metadataPath = MetadataPath(s"$tenantName")
    val tenantData = TenantMetadata(
      name = tenantName,
      key = tenantName,
      metadataPath = metadataPath,
      agentVersion = SpartaType.agentVersion,
      serverVersion = SpartaType.serverVersion,
      tags = List(),
      sourceType = SourceType.SPARTA,
      customType = SpartaType.TENANT)
    val props = Map[String, String](
      "oauthEnable" -> Try(Properties.envOrElse("SECURITY_OAUTH2_ENABLE", "false").toBoolean).getOrElse(false).toString,
      "gosecEnable" -> Try(Properties.envOrElse("ENABLE_GOSEC_AUTH", "false").toBoolean).getOrElse(false).toString,
      "xdCatalogEnable" -> Try(Properties.envOrElse("CROSSDATA_CORE_ENABLE_CATALOG", "false").toBoolean).getOrElse(false).toString,
      "mesosHostnameConstraint" -> Properties.envOrElse("MESOS_HOSTNAME_CONSTRAINT", ""),
      "mesosAttributeConstraint" -> Properties.envOrElse("MESOS_ATTRIBUTE_CONSTRAINT", "")
    )
    tenantData.properties ++= props.map {
      case (k, v) => TenantMetadataProperties(metadataPath, k, v)
    }

    val tenantList = List(tenantData)
    tenantList
  }

  def statusMetadataLineage(workflowStatusStream: WorkflowStatusStream): Option[List[WorkflowStatusMetadata]] = {
    import WorkflowStatusUtils._

    if (checkIfProcessableStatus(workflowStatusStream)) {
      val wfError = workflowStatusStream.execution.flatMap(_.genericDataExecution.flatMap(_.lastError))
      val metadataSerialized = new WorkflowStatusMetadata(
        name = workflowStatusStream.workflow.get.name,
        status = mapSparta2GovernanceStatuses(workflowStatusStream.workflowStatus.status),
        error = if (workflowStatusStream.workflowStatus.status == Failed && wfError.isDefined)
          Some(workflowStatusStream.execution.get.genericDataExecution.get.lastError.get.message) else Some(""),
        key = workflowStatusStream.workflowStatus.id,
        metadataPath = workflowMetadataPathString(workflowStatusStream.workflow.get, "status"),
        agentVersion = SpartaType.agentVersion,
        serverVersion = SpartaType.serverVersion,
        tags = workflowStatusStream.workflow.get.tags.getOrElse(Seq.empty).toList,
        sourceType = SourceType.SPARTA,
        modificationTime = fromDatetimeToLongWithDefault(workflowStatusStream.workflowStatus.lastUpdateDate),
        accessTime = fromDatetimeToLongWithDefault(workflowStatusStream.workflow.get.lastUpdateDate),
        customType = SpartaType.STATUS
      )

      Some(List(metadataSerialized))
    }
    else None
  }

  def workflowMetadataLineage(
                               workflow: Workflow,
                               operationCommandType: OperationCommandType = OperationCommandType.UNKNOWN
                             ): List[WorkflowMetadata] = {

    val workflowMetadata = WorkflowMetadata(
      name = workflow.name,
      key = workflow.id.get,
      description = workflow.description,
      metadataPath = workflowMetadataPathString(workflow),
      agentVersion = SpartaType.agentVersion,
      serverVersion = SpartaType.serverVersion,
      tags = workflow.tags.getOrElse(Seq.empty).toList,
      sourceType = SourceType.SPARTA,
      modificationTime = fromDatetimeToLongWithDefault(workflow.lastUpdateDate),
      customType = SpartaType.WORKFLOW,
      operationCommandType = operationCommandType
    )
    val props = Map[String, String](
      "executionMode" -> workflow.executionEngine.toString,
      "mesosConstraints" -> workflow.settings.global.mesosConstraint.getOrElse("").toString,
      "kerberosEnabled" -> workflow.settings.sparkSettings.sparkDataStoreTls.toString,
      "tlsEnabled" -> workflow.settings.sparkSettings.sparkKerberos.toString,
      "mesosSecurityEnabled" -> workflow.settings.sparkSettings.sparkMesosSecurity.toString)

    workflowMetadata.properties ++= props.map(kv => WorkflowMetadataProperties(workflowMetadataPathString(workflow), kv._1, kv._2))
    val workflowList = List(workflowMetadata)

    workflowList
  }
}

object WorkflowStatusUtils {

  def fromDatetimeToLongWithDefault(dateTime: Option[DateTime]): Option[Long] =
    dateTime.fold(Some(System.currentTimeMillis())) { dt => Some(dt.getMillis) }

  def checkIfProcessableStatus(workflowStatusStream: WorkflowStatusStream): Boolean = {
    val eventStatus = workflowStatusStream.workflowStatus.status
    (eventStatus == Started || eventStatus == Finished || eventStatus == Failed) &&
      workflowStatusStream.workflow.isDefined
  }

  def mapSparta2GovernanceStatuses(spartaStatus: WorkflowStatusEnum.Value): EventType =
    spartaStatus match {
      case Started => EventType.Running
      case Finished => EventType.Success
      case Failed => EventType.Failed
    }
}

object LineageItem extends Enumeration {

  type lineageItem = Value

  val Workflow = Value("workflow")
  val Input = Value("input")
  val Output = Value("output")
  val Transformation = Value("transformation")
  val Status = Value("status")

  implicit def value2String(value: Value): String = value.toString
}