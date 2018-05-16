/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.dg.agent.commons

import com.stratio.governance.commons.agent.model.metadata.MetadataPath
import com.stratio.governance.commons.agent.model.metadata.lineage.EventType
import com.stratio.governance.commons.agent.model.metadata.lineage.EventType.EventType
import com.stratio.sparta.dg.agent.commons.WorkflowStatusUtils.fromDatetimeToLongWithDefault
import com.stratio.sparta.dg.agent.model._
import com.stratio.sparta.sdk.workflow.step.{InputStep, OutputStep, TransformStep}
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum.{Failed, Finished, Started}
import com.stratio.sparta.serving.core.models.workflow._
import org.joda.time.DateTime

import scala.util.{Properties, Try}
import scalax.collection._
import scalax.collection.edge.LDiEdge

/**
  * Utilitary object for dg-workflows methods
  */
object LineageUtils {

  val tenantName = Properties.envOrElse("MARATHON_APP_LABEL_DCOS_SERVICE_NAME", "sparta")


  def workflowMetadataPathString(workflow: Workflow,
                                 workflowStatus: Option[WorkflowStatus],
                                 extraPath: String*) : MetadataPath = {
    val path = MetadataPath(Seq(
      LineageUtils.tenantName,
      workflow.group.name.substring(1).replaceAll("/", "_") ++ "_" ++
      workflow.name ++ "_" ++ workflow.version.toString,
      workflowStatus.fold(workflow.lastUpdateDate.getOrElse(DateTime.now()).getMillis) { wfStatus =>
        wfStatus.lastUpdateDateWorkflow.getOrElse(DateTime.now()).getMillis
      }).map(_.toString)
      ++ extraPath
    )
    path
  }

  def inputMetadataLineage(workflow: Workflow, graph: Graph[NodeGraph, LDiEdge]): List[SpartaInputMetadata] = {
    workflow.pipelineGraph.nodes.filter(node => node.stepType.equalsIgnoreCase(InputStep.StepType)).map(
      n => SpartaInputMetadata(
        name = n.name,
        key = workflow.id.get,
        metadataPath = workflowMetadataPathString(workflow, None, n.name),
        outcomingNodes = graph.get(n).diSuccessors.map(s =>
          workflowMetadataPathString(workflow, None, s.name)).toSeq,
        tags = workflow.tags.getOrElse(Seq.empty).toList,
        modificationTime = workflow.lastUpdateDate.map(_.getMillis))
    ).toList
  }

  def outputMetadataLineage(workflow: Workflow, graph: Graph[NodeGraph, LDiEdge]): List[SpartaOutputMetadata] = {
    workflow.pipelineGraph.nodes.filter(node => node.stepType.equalsIgnoreCase(OutputStep.StepType)).map(
      n => SpartaOutputMetadata(
        name = n.name,
        key = workflow.id.get,
        metadataPath = workflowMetadataPathString(workflow, None, n.name),
        incomingNodes = graph.get(n).diPredecessors.map(pred =>
          workflowMetadataPathString(workflow, None, pred.name)).toSeq,
        tags = workflow.tags.getOrElse(Seq.empty).toList,
        modificationTime = workflow.lastUpdateDate.map(_.getMillis))
    ).toList
  }

  def transformationMetadataLineage(workflow: Workflow, graph: Graph[NodeGraph, LDiEdge])
  : List[SpartaTransformationMetadata] = {
    workflow.pipelineGraph.nodes.filter(node => node.stepType.equalsIgnoreCase(TransformStep.StepType)).map(
      n => SpartaTransformationMetadata(
        name = n.name,
        key = workflow.id.get,
        metadataPath = workflowMetadataPathString(workflow, None, n.name),
        outcomingNodes = graph.get(n).diSuccessors.map(s =>
          workflowMetadataPathString(workflow, None, s.name)).toSeq,
        incomingNodes = graph.get(n).diPredecessors.map(pred =>
          workflowMetadataPathString(workflow, None , pred.name)).toSeq,
        tags = workflow.tags.getOrElse(Seq.empty).toList,
        modificationTime = workflow.lastUpdateDate.map(_.getMillis))
    ).toList
  }

  def tenantMetadataLineage() : List[SpartaTenantMetadata] = {

    val tenantList = List(SpartaTenantMetadata(
      name = tenantName,
      key = tenantName,
      metadataPath = MetadataPath(s"$tenantName"),
      tags = List(),
      oauthEnable = Try(Properties.envOrElse("SECURITY_OAUTH2_ENABLE", "false").toBoolean).getOrElse(false),
      gosecEnable = Try(Properties.envOrElse("ENABLE_GOSEC_AUTH", "false").toBoolean).getOrElse(false),
      xdCatalogEnable = Try(Properties.envOrElse("CROSSDATA_CORE_ENABLE_CATALOG", "false").toBoolean).getOrElse(false),
      mesosHostnameConstraint = Properties.envOrElse("MESOS_HOSTNAME_CONSTRAINT", ""),
      mesosAttributeConstraint = Properties.envOrElse("MESOS_ATTRIBUTE_CONSTRAINT", "")
    ))

    tenantList
  }

  def statusMetadataLineage(workflowStatusStream: WorkflowStatusStream): Option[List[SpartaWorkflowStatusMetadata]] = {
    import WorkflowStatusUtils._

    if (checkIfProcessableStatus(workflowStatusStream)) {
      val wfError = workflowStatusStream.execution.flatMap(_.genericDataExecution.flatMap(_.lastError))
      val metadataSerialized = new SpartaWorkflowStatusMetadata(
        name = workflowStatusStream.workflow.get.name,
        status = mapSparta2GovernanceStatuses(workflowStatusStream.workflowStatus.status),
        error = if (workflowStatusStream.workflowStatus.status == Failed && wfError.isDefined)
          Some(workflowStatusStream.execution.get.genericDataExecution.get.lastError.get.message) else Some(""),
        key = workflowStatusStream.workflowStatus.id,
        metadataPath = workflowMetadataPathString(workflowStatusStream.workflow.get,
          Some(workflowStatusStream.workflowStatus),
          fromDatetimeToLongWithDefault(workflowStatusStream.workflowStatus.lastUpdateDate).get.toString),
        tags = workflowStatusStream.workflow.get.tags.getOrElse(Seq.empty).toList,
        modificationTime = fromDatetimeToLongWithDefault(workflowStatusStream.workflowStatus.lastUpdateDate),
        accessTime = fromDatetimeToLongWithDefault(workflowStatusStream.workflow.get.lastUpdateDate)
      )

      Some(List(metadataSerialized))
    }
    else None
  }

  def workflowMetadataLineage(workflow: Workflow): List[SpartaWorkflowMetadata] = {

    val workflowList = List(SpartaWorkflowMetadata(
      name = workflow.name,
      key = workflow.id.get,
      description = workflow.description,
      executionMode = workflow.executionEngine.toString,
      mesosConstraints = workflow.settings.global.mesosConstraint.getOrElse("").toString,
      kerberosEnabled = workflow.settings.sparkSettings.sparkDataStoreTls,
      tlsEnabled = workflow.settings.sparkSettings.sparkKerberos,
      mesosSecurityEnabled = workflow.settings.sparkSettings.sparkMesosSecurity,
      metadataPath = workflowMetadataPathString(workflow, None),
      tags = workflow.tags.getOrElse(Seq.empty).toList,
      modificationTime = fromDatetimeToLongWithDefault(workflow.lastUpdateDate)))

    workflowList
  }
}

object WorkflowStatusUtils {

  def fromDatetimeToLongWithDefault(dateTime: Option[DateTime]) : Option[Long] =
    dateTime.fold(Some(System.currentTimeMillis())){dt => Some(dt.getMillis)}

  def checkIfProcessableStatus(workflowStatusStream: WorkflowStatusStream): Boolean = {
    val eventStatus = workflowStatusStream.workflowStatus.status
    (eventStatus == Started || eventStatus == Finished || eventStatus == Failed) &&
      workflowStatusStream.workflow.isDefined
  }

  def mapSparta2GovernanceStatuses(spartaStatus: WorkflowStatusEnum.Value) : EventType =
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

  implicit def value2String(value: Value) : String = value.toString
}