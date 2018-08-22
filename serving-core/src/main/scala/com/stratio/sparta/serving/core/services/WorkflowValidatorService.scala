/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.models.WorkflowValidationMessage
import com.stratio.sparta.serving.core.helpers.GraphHelper
import com.stratio.sparta.serving.core.models.workflow.{NodeGraph, Workflow, WorkflowValidation}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import org.apache.commons.lang.exception.ExceptionUtils
import org.apache.curator.framework.CuratorFramework
import scalax.collection.Graph
import scalax.collection.edge.LDiEdge

import scala.util.{Failure, Success, Try}

class WorkflowValidatorService(curatorFramework: Option[CuratorFramework] = None) {

  implicit val curator: Option[CuratorFramework] = curatorFramework
  val maxNumAllowedChars = 100

  def validateAll(workflow: Workflow): WorkflowValidation = {

    implicit val workflowToValidate: Workflow = workflow
    implicit val graph: Graph[NodeGraph, LDiEdge] = GraphHelper.createGraph(workflow)

    val validationResult = handleValidateError{
      new WorkflowValidation()
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
    }

    validationResult.copy(messages = validationResult.messages.distinct)
  }

  def validateGraph(workflow: Workflow): WorkflowValidation = {

    implicit val workflowToValidate: Workflow = workflow
    implicit val graph: Graph[NodeGraph, LDiEdge] = GraphHelper.createGraph(workflow)

    val validationResult = handleValidateError {
      new WorkflowValidation()
        .validateNonEmptyNodes
        .validateNonEmptyEdges
        .validateEdgesNodesExists
        .validateGraphIsAcyclic
        .validateDuplicateNames
    }

    validationResult.copy(messages = validationResult.messages.distinct)
  }

  def validateBasicSettings(workflow: Workflow): WorkflowValidation = {

    implicit val workflowToValidate: Workflow = workflow

    val validationResult = handleValidateError{
      new WorkflowValidation()
        .validateGroupName
        .validateName
    }

    validationResult.copy(messages = validationResult.messages.distinct)
  }

  def validateAdvancedSettings(workflow: Workflow): WorkflowValidation = {

    //Is recommended send workflow with environment substitution applied
    implicit val workflowToValidate: Workflow = workflow
    implicit val graph: Graph[NodeGraph, LDiEdge] = GraphHelper.createGraph(workflow)

    val validationResult = handleValidateError{
      new WorkflowValidation()
        .validateExistenceCorrectPath
        .validateErrorOutputs
        .validateDeployMode
        .validateSparkCores
        .validateMesosConstraints
    }

    validationResult.copy(messages = validationResult.messages.distinct)
  }

  def validatePlugins(workflow: Workflow): WorkflowValidation = {

    //Is recommended send workflow with environment substitution applied
    implicit val workflowToValidate: Workflow = workflow
    implicit val graph: Graph[NodeGraph, LDiEdge] = GraphHelper.createGraph(workflow)

    val validationResult = handleValidateError{
      new WorkflowValidation()
        .validateArityOfNodes
        .validateCheckpointCubes
        .validatePlugins
    }

    validationResult.copy(messages = validationResult.messages.distinct)
  }

  private def handleValidateError(f: => WorkflowValidation): WorkflowValidation = {
    Try(f) match {
      case Success(valid) => valid
      case Failure(e: Exception) => WorkflowValidation(valid = false,
        messages = Seq(WorkflowValidationMessage(s"Validation error " +
          s"${getValidationStepFromStackTrace(e).fold(""){ stepError => s" during $stepError "}}" +
          s": ${Try(ExceptionUtils.getRootCauseMessage(e)).filter(s => s.length < maxNumAllowedChars)
            .getOrElse {
              val exception = ExceptionHelper.toPrintableException(e)
              if (exception.length > maxNumAllowedChars) exception.slice(0, maxNumAllowedChars) + " ..."
              else exception
            }}"
        )))
    }
  }

  private def getValidationStepFromStackTrace(exception: Exception): Option[String] ={
    Try(ExceptionUtils.getStackTrace(exception).replaceAll("\\(.*\\)", "").split("\\s+"))
      .toOption.fold(Some("")){ exceptionsArray =>
      exceptionsArray.find(error => error.startsWith("com.stratio.sparta.serving.core.models.workflow.WorkflowValidation"))
        .fold(Some("")){ error => Some(error.split('.').last)}
    }.notBlank
  }

}