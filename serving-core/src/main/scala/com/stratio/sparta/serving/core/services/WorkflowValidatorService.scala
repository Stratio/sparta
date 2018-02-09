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

package com.stratio.sparta.serving.core.services

import com.stratio.sparta.serving.core.helpers.GraphHelper
import com.stratio.sparta.serving.core.models.workflow.{NodeGraph, Workflow, WorkflowValidation}
import org.apache.curator.framework.CuratorFramework

import scalax.collection.Graph
import scalax.collection.GraphEdge.DiEdge

class WorkflowValidatorService(curatorFramework: Option[CuratorFramework] = None) {

  implicit val curator: Option[CuratorFramework] = curatorFramework

  def validate(workflow: Workflow): WorkflowValidation = {

    implicit val workflowToValidate: Workflow = workflow
    implicit val graph: Graph[NodeGraph, DiEdge] = GraphHelper.createGraph(workflow)

    val validationResult = new WorkflowValidation()
      .validateGroupName
      .validateName
      .validateErrorOutputs
      .validateNonEmptyNodes
      .validateNonEmptyEdges
      .validateEdgesNodesExists
      .validateGraphIsAcyclic
      .validateArityOfNodes
      .validateExistenceCorrectPath
      .validateDuplicateNames
      .validateCheckpointCubes

    validationResult.copy(messages = validationResult.messages.distinct)
  }

  def validateJsoneySettings(workflow: Workflow): WorkflowValidation = {

    implicit val workflowToValidate: Workflow = workflow
    implicit val graph: Graph[NodeGraph, DiEdge] = GraphHelper.createGraph(workflow)

    val validationResult = new WorkflowValidation()
      .validateExecutionMode
      .validateDeployMode
      .validateSparkCores

    validationResult.copy(messages = validationResult.messages.distinct)
  }

}