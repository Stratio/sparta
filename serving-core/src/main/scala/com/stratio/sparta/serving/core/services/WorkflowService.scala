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

import java.util.UUID

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowStatus}
import com.stratio.sparta.serving.core.models.{ErrorModel, SpartaSerializer}
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util._

class WorkflowService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  private val statusService = new WorkflowStatusService(curatorFramework)

  /** METHODS TO MANAGE WORKFLOWS IN ZOOKEEPER **/

  def findById(id: String): Workflow =
    existsById(id).getOrElse(throw new ServingCoreException(ErrorModel.toString(
      new ErrorModel(ErrorModel.CodeNotExistsWorkflowWithId, s"No workflow with id $id"))))

  def findByName(name: String): Workflow =
    existsByName(name, None).getOrElse(throw new ServingCoreException(ErrorModel.toString(
      new ErrorModel(ErrorModel.CodeNotExistsWorkflowWithName, s"No workflow with name $name"))))

  def findByTemplateType(templateType: String): List[Workflow] =
    findAll.filter(apm => apm.pipelineGraph.nodes.exists(f => f.className == templateType))

  def findByTemplateName(templateType: String, name: String): List[Workflow] =
    findAll.filter(apm => apm.pipelineGraph.nodes.exists(f => f.name == name && f.className == templateType))

  def findAll: List[Workflow] = {
    val children = curatorFramework.getChildren.forPath(AppConstant.WorkflowsZkPath)

    JavaConversions.asScalaBuffer(children).toList.map(id => findById(id))
  }

  def create(workflow: Workflow): Workflow = {
    val searchWorkflow = existsByName(workflow.name, workflow.id)
    if (searchWorkflow.isDefined) {
      throw new ServingCoreException(ErrorModel.toString(new ErrorModel(
        ErrorModel.CodeExistsWorkflowWithName,
        s"Workflow with name ${workflow.name} exists. The actual workflow name is: ${searchWorkflow.get.name}"
      )))
    }
    val workflowId = addId(workflow)
    curatorFramework.create().creatingParentsIfNeeded().forPath(
      s"${AppConstant.WorkflowsZkPath}/${workflowId.id.get}", write(workflowId).getBytes)
    statusService.update(WorkflowStatus(
      id = workflowId.id.get,
      status = WorkflowStatusEnum.NotStarted,
      name = Option(workflow.name),
      description = Option(workflow.description)
    ))
    workflowId
  }

  def update(workflow: Workflow): Workflow = {
    val searchWorkflow = existsByName(workflow.name, workflow.id)
    if (searchWorkflow.isEmpty) {
      throw new ServingCoreException(ErrorModel.toString(new ErrorModel(
        ErrorModel.CodeExistsWorkflowWithName,
        s"Workflow with name ${workflow.name} does not exist"
      )))
    } else {
      val workflowId = addId(workflow)
      curatorFramework.setData().forPath(
        s"${AppConstant.WorkflowsZkPath}/${workflow.id.get}", write(workflow).getBytes)
      statusService.update(WorkflowStatus(
        id = workflowId.id.get,
        status = WorkflowStatusEnum.NotDefined,
        name = Option(workflow.name),
        description = Option(workflow.description)
      ))
      workflowId
    }
  }

  def delete(id: String): Unit = {
    curatorFramework.delete().forPath(s"${AppConstant.WorkflowsZkPath}/$id")
    statusService.delete(id)
  }

  def deleteAll(): List[Workflow] =
    findAll.map { workflow =>
      delete(workflow.id.get)
      workflow
    }

  /** PRIVATE METHODS **/

  private[sparta] def existsByName(name: String, id: Option[String] = None): Option[Workflow] =
    Try {
      if (CuratorFactoryHolder.existsPath(AppConstant.WorkflowsZkPath)) {
        findAll.find { workflow =>
          if (id.isDefined && workflow.id.isDefined) workflow.id.get == id.get else workflow.name == name.toLowerCase
        }
      } else None
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        None
    }

  private[sparta] def existsById(id: String): Option[Workflow] =
    Try {
      if (CuratorFactoryHolder.existsPath(s"${AppConstant.WorkflowsZkPath}/$id")) {
        Option(read[Workflow](
          new Predef.String(curatorFramework.getData.forPath(s"${AppConstant.WorkflowsZkPath}/$id"))))
      } else None
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        None
    }

  private[sparta] def addId(workflow: Workflow): Workflow = {
    (workflow.id match {
      case None => workflow.copy(id = Some(UUID.randomUUID.toString))
      case Some(_) => workflow
    }).copy(name = workflow.name.toLowerCase)
  }
}
