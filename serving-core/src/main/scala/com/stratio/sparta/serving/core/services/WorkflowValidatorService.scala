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
import scalax.collection.edge.LDiEdge

class WorkflowValidatorService(curatorFramework: Option[CuratorFramework] = None) {

  implicit val curator: Option[CuratorFramework] = curatorFramework

  def validateAll(workflow: Workflow): WorkflowValidation = {

    implicit val workflowToValidate: Workflow = workflow
    implicit val graph: Graph[NodeGraph, LDiEdge] = GraphHelper.createGraph(workflow)

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
      .validateDeployMode
      .validateSparkCores
      .validateMesosConstraints
      .validatePlugins

    validationResult.copy(messages = validationResult.messages.distinct)
  }

  def validateGraph(workflow: Workflow): WorkflowValidation = {

    implicit val workflowToValidate: Workflow = workflow
    implicit val graph: Graph[NodeGraph, LDiEdge] = GraphHelper.createGraph(workflow)

    val validationResult = new WorkflowValidation()
      .validateNonEmptyNodes
      .validateNonEmptyEdges
      .validateEdgesNodesExists
      .validateGraphIsAcyclic
      .validateDuplicateNames

    validationResult.copy(messages = validationResult.messages.distinct)
  }

  def validateBasicSettings(workflow: Workflow): WorkflowValidation = {

    implicit val workflowToValidate: Workflow = workflow

    val validationResult = new WorkflowValidation()
      .validateGroupName
      .validateName

    validationResult.copy(messages = validationResult.messages.distinct)
  }

  def validateAdvancedSettings(workflow: Workflow): WorkflowValidation = {

    //Is recommended send workflow with environment substitution applied
    implicit val workflowToValidate: Workflow = workflow
    implicit val graph: Graph[NodeGraph, LDiEdge] = GraphHelper.createGraph(workflow)

    val validationResult = new WorkflowValidation()
      .validateExistenceCorrectPath
      .validateErrorOutputs
      .validateDeployMode
      .validateSparkCores
      .validateMesosConstraints

    validationResult.copy(messages = validationResult.messages.distinct)
  }

  def validatePlugins(workflow: Workflow): WorkflowValidation = {

    //Is recommended send workflow with environment substitution applied
    implicit val workflowToValidate: Workflow = workflow
    implicit val graph: Graph[NodeGraph, LDiEdge] = GraphHelper.createGraph(workflow)

    val validationResult = new WorkflowValidation()
      .validateArityOfNodes
      .validateCheckpointCubes
      .validatePlugins

    validationResult.copy(messages = validationResult.messages.distinct)
  }

}