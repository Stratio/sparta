/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
      .validateMesosConstraints
      .validatePlugins

    validationResult.copy(messages = validationResult.messages.distinct)
  }

}