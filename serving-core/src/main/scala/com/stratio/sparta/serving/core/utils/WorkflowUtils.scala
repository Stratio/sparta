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

package com.stratio.sparta.serving.core.utils

import java.util.UUID

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowStatus}
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util._

trait WorkflowUtils extends WorkflowStatusUtils with CheckpointUtils with TemplateUtils {

  /** METHODS TO MANAGE WORKFLOWS IN ZOOKEEPER **/

  def getWorkflowById(id: String): Workflow = {
    read[Workflow](
      new Predef.String(curatorFramework.getData.forPath(s"${AppConstant.WorkflowsZkPath}/$id")))
  }

  def findAllWorkflows: List[Workflow] = {
    val children = curatorFramework.getChildren.forPath(AppConstant.WorkflowsZkPath)

    JavaConversions.asScalaBuffer(children).toList.map(id => getWorkflowById(id))
  }

  def deleteAllWorkflows(): List[Workflow] = {
    val workflows = findAllWorkflows
    workflows.foreach { workflow =>
      if (workflow.settings.checkpointSettings.autoDeleteCheckpoint) deleteCheckpointPath(workflow)
      doDeleteWorkflow(workflow)
    }
    workflows
  }

  def findWorkflowsByTemplateType(templateType: String): List[Workflow] =
    findAllWorkflows.filter(apm => apm.pipelineGraph.nodes.exists(f => f.`type` == templateType))

  def findWorkflowsByTemplateName(templateType: String, name: String): List[Workflow] =
    findAllWorkflows.filter(apm => apm.pipelineGraph.nodes.exists(f => f.name == name && f.`type` == templateType))

  def findWorkflowByName(name: String): Workflow =
    existsWorkflowByNameId(name, None).getOrElse(throw new ServingCoreException(ErrorModel.toString(
      new ErrorModel(ErrorModel.CodeNotExistsWorkflowWithName, s"No workflow with name $name"))))

  def createWorkflow(workflow: Workflow): Workflow = {
    val searchWorkflow = existsWorkflowByNameId(workflow.name, workflow.id)
    if (searchWorkflow.isDefined) {
      throw new ServingCoreException(ErrorModel.toString(new ErrorModel(
        ErrorModel.CodeExistsWorkflowWithName,
        s"Workflow with name ${workflow.name} exists. The actual workflow name is: ${searchWorkflow.get.name}"
      )))
    }
    val workflowSaved = writeWorkflow(workflowWithId(workflow))
    updateStatus(WorkflowStatus(
      id = workflowSaved.id.get,
      status = WorkflowStatusEnum.NotStarted,
      name = Option(workflow.name),
      description = Option(workflow.description)
    ))
    workflowSaved
  }

  def updateWorkflow(workflow: Workflow): Workflow = {
    val searchWorkflow = existsWorkflowByNameId(workflow.name, workflow.id)
    if (searchWorkflow.isEmpty) {
      throw new ServingCoreException(ErrorModel.toString(new ErrorModel(
        ErrorModel.CodeExistsWorkflowWithName,
        s"Workflow with name ${workflow.name} does not exist"
      )))
    } else {
      val workflowSaved = doUpdateWorkflow(workflowWithId(workflow))
      updateStatus(WorkflowStatus(
        id = workflowSaved.id.get,
        status = WorkflowStatusEnum.NotDefined,
        name = Option(workflow.name),
        description = Option(workflow.description)
      ))
      workflowSaved
    }
  }

  def deleteWorkflow(id: String): Unit = {
    val workflow = getWorkflowById(id)
    if (workflow.settings.checkpointSettings.autoDeleteCheckpoint) deleteCheckpointPath(workflow)
    doDeleteWorkflow(workflow)
  }

  /** PRIVATE METHODS **/

  private[sparta] def existsPath: Boolean = CuratorFactoryHolder.existsPath(AppConstant.WorkflowsZkPath)

  private[sparta] def existsWorkflowByNameId(name: String, id: Option[String] = None): Option[Workflow] =
    Try {
      if (existsPath) {
        findAllWorkflows.find { workflow =>
          if (id.isDefined && workflow.id.isDefined) workflow.id.get == id.get else workflow.name == name.toLowerCase
        }
      } else None
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        None
    }

  private[sparta] def doDeleteWorkflow(workflow: Workflow): Unit = {
    curatorFramework.delete().forPath(s"${AppConstant.WorkflowsZkPath}/${workflow.id.get}")
    deleteStatus(workflow.id.get)
  }

  private[sparta] def writeWorkflow(workflow: Workflow): Workflow = {
    curatorFramework.create().creatingParentsIfNeeded().forPath(
      s"${AppConstant.WorkflowsZkPath}/${workflow.id.get}", write(workflow).getBytes)
    workflow
  }

  private[sparta] def doUpdateWorkflow(workflow: Workflow): Workflow = {
    curatorFramework.setData().forPath(
      s"${AppConstant.WorkflowsZkPath}/${workflow.id.get}", write(workflow).getBytes)
    workflow
  }

  private[sparta] def workflowWithId(workflow: Workflow): Workflow = {
    (workflow.id match {
      case None => populateWorkflowWithRandomUUID(workflow)
      case Some(_) => workflow
    }).copy(name = workflow.name.toLowerCase)
  }

  private[sparta] def populateWorkflowWithRandomUUID(workflow: Workflow): Workflow =
    workflow.copy(id = Some(UUID.randomUUID.toString))
}
