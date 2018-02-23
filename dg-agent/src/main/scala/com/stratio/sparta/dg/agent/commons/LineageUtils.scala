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

package com.stratio.sparta.dg.agent.commons

import com.stratio.governance.commons.agent.model.metadata.MetadataPath
import com.stratio.governance.commons.agent.model.metadata.lineage.EventType
import com.stratio.governance.commons.agent.model.metadata.lineage.EventType.EventType
import com.stratio.sparta.dg.agent.commons.WorkflowStatusUtils.fromDatetimeToLongWithDefault
import com.stratio.sparta.dg.agent.model._
import com.stratio.sparta.sdk.workflow.step.{InputStep, OutputStep, TransformStep}
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum.{Failed, Finished, Started}
import com.stratio.sparta.serving.core.models.workflow.{NodeGraph, Workflow, WorkflowStatus, WorkflowStatusStream}
import org.joda.time.DateTime

import scala.util.{Properties, Try}
import scalax.collection.GraphEdge.DiEdge
import scalax.collection._

/**
  * Utilitary object for dg-workflows methods
  */
object LineageUtils {

  import LineageItem._

  val tenantName = Properties.envOrElse("MARATHON_APP_LABEL_DCOS_SERVICE_NAME", "sparta")


  def workflowMetadataPathString(workflow: Workflow,
                                 workflowStatus: Option[WorkflowStatus],
                                 extraPath: String*) : MetadataPath = {
    val path = MetadataPath(Seq(
      LineageUtils.tenantName,
      workflow.group.name.substring(1).replaceAll("/", "_"),
      workflow.name,
      workflow.version,
      workflowStatus.fold(workflow.lastUpdateDate.getOrElse(DateTime.now()).getMillis) { wfStatus =>
        wfStatus.lastUpdateDateWorkflow.getOrElse(DateTime.now()).getMillis
      }).map(_.toString)
      ++ extraPath
    )
    path
  }

  def inputMetadataLineage(workflow: Workflow, graph: Graph[NodeGraph, DiEdge]): List[SpartaInputMetadata] = {
    workflow.pipelineGraph.nodes.filter(node => node.stepType.equalsIgnoreCase(InputStep.StepType)).map(
      n => SpartaInputMetadata(
        name = n.name,
        key = workflow.id.get,
        metadataPath = workflowMetadataPathString(workflow, None, LineageItem.Input, n.name),
        outcomingNodes = graph.get(n).diSuccessors.map(s =>
          workflowMetadataPathString(workflow, None, LineageItem.Input, s.name)).toSeq,
        tags = workflow.tags.getOrElse(Seq.empty).toList,
        modificationTime = workflow.lastUpdateDate.map(_.getMillis))
    ).toList
  }

  def outputMetadataLineage(workflow: Workflow, graph: Graph[NodeGraph, DiEdge]): List[SpartaOutputMetadata] = {
    workflow.pipelineGraph.nodes.filter(node => node.stepType.equalsIgnoreCase(OutputStep.StepType)).map(
      n => SpartaOutputMetadata(
        name = n.name,
        key = workflow.id.get,
        metadataPath = workflowMetadataPathString(workflow, None, LineageItem.Output, n.name),
        incomingNodes = graph.get(n).diPredecessors.map(pred =>
          workflowMetadataPathString(workflow, None, LineageItem.Output, pred.name)).toSeq,
        tags = workflow.tags.getOrElse(Seq.empty).toList,
        modificationTime = workflow.lastUpdateDate.map(_.getMillis))
    ).toList
  }

  def transformationMetadataLineage(workflow: Workflow, graph: Graph[NodeGraph, DiEdge])
  : List[SpartaTransformationMetadata] = {
    workflow.pipelineGraph.nodes.filter(node => node.stepType.equalsIgnoreCase(TransformStep.StepType)).map(
      n => SpartaTransformationMetadata(
        name = n.name,
        key = workflow.id.get,
        metadataPath = workflowMetadataPathString(workflow, None, LineageItem.Transformation, n.name),
        outcomingNodes = graph.get(n).diSuccessors.map(s =>
          workflowMetadataPathString(workflow, None, LineageItem.Transformation , s.name)).toSeq,
        incomingNodes = graph.get(n).diPredecessors.map(pred =>
          workflowMetadataPathString(workflow, None, LineageItem.Transformation, pred.name)).toSeq,
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
      val metadataSerialized = new SpartaWorkflowStatusMetadata(
        name = workflowStatusStream.workflow.get.name,
        status = mapSparta2GovernanceStatuses(workflowStatusStream.workflowStatus.status),
        error = if (workflowStatusStream.workflowStatus.status == Failed
          && workflowStatusStream.workflowStatus.lastError.isDefined)
          Some(workflowStatusStream.workflowStatus.lastError.get.message) else None,
        key = workflowStatusStream.workflowStatus.id,
        metadataPath = workflowMetadataPathString(workflowStatusStream.workflow.get,
          Some(workflowStatusStream.workflowStatus),
          LineageItem.Status,
          fromDatetimeToLongWithDefault(workflowStatusStream.workflowStatus.lastUpdateDate).get.toString),
        tags = workflowStatusStream.workflow.get.tags.getOrElse(Seq.empty).toList,
        modificationTime = fromDatetimeToLongWithDefault(workflowStatusStream.workflow.get.lastUpdateDate),
        accessTime = fromDatetimeToLongWithDefault(workflowStatusStream.workflowStatus.lastUpdateDate)
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
      modificationTime = fromDatetimeToLongWithDefault(workflow.lastUpdateDate)
    ))

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