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
import com.stratio.sparta.dg.agent.model.{SpartaInputMetadata, SpartaOutputMetadata, SpartaTransformationMetadata}
import com.stratio.sparta.sdk.workflow.step.{InputStep, OutputStep, TransformStep}
import com.stratio.sparta.serving.core.models.workflow.{NodeGraph, Workflow}

/**
  * Utilitary object for dg-workflows methods
  */
object LineageUtils {

  def workflowMetadataPathString(workflow: Workflow): String = s"${workflow.group.name.replaceAll("/","_")}/${workflow.name}" +
    s"/${workflow.version}/${workflow.lastUpdateDate.getOrElse(DateTime.now()).getMillis}"

  def inputMetadataLineage(workflow: Workflow, graph: Graph[NodeGraph, DiEdge]): List[SpartaInputMetadata] = {
    val metadataPath = workflowMetadataPathString(workflow)
    workflow.pipelineGraph.nodes.filter(node => node.stepType.equalsIgnoreCase(InputStep.StepType)).map(
      n => SpartaInputMetadata(
        name = n.name,
        key = n.classPrettyName,
        metadataPath = MetadataPath(metadataPath),
        outcomingNodes = graph.get(n).diSuccessors.map(s => MetadataPath(s"$metadataPath/${s.name}")).toSeq,
        tags = workflow.tag.toList,
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
        tags = workflow.tag.toList,
        modificationTime = workflow.lastUpdateDate.map(_.getMillis))
    ).toList
  }

  def transformationMetadataLineage(workflow: Workflow, graph: Graph[NodeGraph, DiEdge]): List[SpartaTransformationMetadata] = {
    val metadataPath = workflowMetadataPathString(workflow)
    workflow.pipelineGraph.nodes.filter(node => node.stepType.equalsIgnoreCase(TransformStep.StepType)).map(
      n => SpartaTransformationMetadata(
        name = n.name,
        key = n.classPrettyName,
        metadataPath = MetadataPath(metadataPath),
        outcomingNodes = graph.get(n).diSuccessors.map(s => MetadataPath(s"$metadataPath/${s.name}")).toSeq,
        incomingNodes = graph.get(n).diPredecessors.map(pred => MetadataPath(s"$metadataPath/${pred.name}")).toSeq,
        tags = workflow.tag.toList,
        modificationTime = workflow.lastUpdateDate.map(_.getMillis))
    ).toList
  }
}
