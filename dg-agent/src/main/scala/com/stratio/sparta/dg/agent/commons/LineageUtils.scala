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

import scalax.collection.GraphEdge.DiEdge
import scalax.collection._
import org.joda.time.DateTime
import com.stratio.governance.commons.agent.model.metadata.MetadataPath
import com.stratio.governance.commons.agent.model.metadata.lineage.EventType
import com.stratio.governance.commons.agent.model.metadata.lineage.EventType.EventType
import com.stratio.sparta.dg.agent.model._
import com.stratio.sparta.sdk.workflow.step.{InputStep, OutputStep, TransformStep}
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum.{Failed, Finished, Started}
import com.stratio.sparta.serving.core.models.workflow.{NodeGraph, Workflow, WorkflowStatusStream}

import scala.util.{Properties, Try}

import scala.util.Properties
import com.stratio.sparta.dg.agent.model.{SpartaInputMetadata, SpartaOutputMetadata, SpartaTenantMetadata, SpartaTransformationMetadata}

import scala.util.{Properties, Try}

/**
  * Utilitary object for dg-workflows methods
  */
object LineageUtils {

  val tenantName = Properties.envOrElse("MARATHON_APP_LABEL_DCOS_SERVICE_NAME", "sparta")


  def workflowMetadataPathString(workflow: Workflow): String =
    s"${workflow.group.name.replaceAll("/","_")}/${workflow.name}" +
    s"/${workflow.version}/${workflow.lastUpdateDate.getOrElse(DateTime.now()).getMillis}"

  def inputMetadataLineage(workflow: Workflow, graph: Graph[NodeGraph, DiEdge]): List[SpartaInputMetadata] = {
    val metadataPath = workflowMetadataPathString(workflow)
    workflow.pipelineGraph.nodes.filter(node => node.stepType.equalsIgnoreCase(InputStep.StepType)).map(
      n => SpartaInputMetadata(
        name = n.name,
        key = n.classPrettyName,
        metadataPath = MetadataPath(metadataPath),
        outcomingNodes = graph.get(n).diSuccessors.map(s => MetadataPath(s"$metadataPath/${s.name}")).toSeq,
        tags = workflow.tags.getOrElse(Seq.empty).toList,
        modificationTime = workflow.lastUpdateDate.map(_.getMillis))
    ).toList
  }

  def outputMetadataLineage(workflow: Workflow, graph: Graph[NodeGraph, DiEdge]): List[SpartaOutputMetadata] = {
    val metadataPath = workflowMetadataPathString(workflow)
    workflow.pipelineGraph.nodes.filter(node => node.stepType.equalsIgnoreCase(OutputStep.StepType)).map(
      n => SpartaOutputMetadata(
        name = n.name,
        key = n.classPrettyName,
        metadataPath = MetadataPath(metadataPath),
        incomingNodes = graph.get(n).diPredecessors.map(pred => MetadataPath(s"$metadataPath/${pred.name}")).toSeq,
        tags = workflow.tags.getOrElse(Seq.empty).toList,
        modificationTime = workflow.lastUpdateDate.map(_.getMillis))
    ).toList
  }

  def transformationMetadataLineage(workflow: Workflow, graph: Graph[NodeGraph, DiEdge])
  : List[SpartaTransformationMetadata] = {
    val metadataPath = workflowMetadataPathString(workflow)
    workflow.pipelineGraph.nodes.filter(node => node.stepType.equalsIgnoreCase(TransformStep.StepType)).map(
      n => SpartaTransformationMetadata(
        name = n.name,
        key = n.classPrettyName,
        metadataPath = MetadataPath(metadataPath),
        outcomingNodes = graph.get(n).diSuccessors.map(s => MetadataPath(s"$metadataPath/${s.name}")).toSeq,
        incomingNodes = graph.get(n).diPredecessors.map(pred => MetadataPath(s"$metadataPath/${pred.name}")).toSeq,
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
        metadataPath = extractMetadataPath(workflowStatusStream),
        tags = workflowStatusStream.workflow.get.tag.fold(List.empty[String]) { tag => tag.split(",").toList },
        modificationTime = fromDatetimeToLongWithDefault(workflowStatusStream.workflow.get.lastUpdateDate),
        accessTime = fromDatetimeToLongWithDefault(workflowStatusStream.workflowStatus.lastUpdateDate)
      )
      Some(List(metadataSerialized))
    }
    else None
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

  def extractMetadataPath(workflowStatusStream: WorkflowStatusStream) : MetadataPath =
    MetadataPath(Seq(
      LineageUtils.tenantName,
      workflowStatusStream.workflow.get.group.name.replaceAll("/", "_"),
      workflowStatusStream.workflow.get.name,
      workflowStatusStream.workflow.get.version,
      workflowStatusStream.workflow.get.lastUpdateDate.getOrElse(DateTime.now()).getMillis,
      "status"
    ).map(_.toString))

  def mapSparta2GovernanceStatuses(spartaStatus: WorkflowStatusEnum.Value) : EventType =
    spartaStatus match {
      case Started => EventType.Running
      case Finished => EventType.Success
      case Failed => EventType.Failed
    }
}

