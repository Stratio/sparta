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
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowStatus}
import org.apache.curator.framework.CuratorFramework
import org.joda.time.DateTime
import org.json4s.jackson.Serialization._
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._

import scala.collection.JavaConversions
import scala.util._

class WorkflowService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  private val statusService = new WorkflowStatusService(curatorFramework)
  private val validatorService = new WorkflowValidatorService

  /** METHODS TO MANAGE WORKFLOWS IN ZOOKEEPER **/

  def findById(id: String): Workflow =
    existsById(id).getOrElse(throw new ServerException(s"No workflow with id $id"))

  def findByName(name: String): Workflow =
    existsByName(name, None).getOrElse(throw new ServerException(s"No workflow with name $name"))

  def findByTemplateType(templateType: String): List[Workflow] =
    findAll.filter(apm => apm.pipelineGraph.nodes.exists(f => f.className == templateType))

  def findByTemplateName(templateType: String, name: String): List[Workflow] =
    findAll.filter(apm => apm.pipelineGraph.nodes.exists(f => f.name == name && f.className == templateType))

  def findByIdList(workflowIds: Seq[String]): List[Workflow] = {
    val children = curatorFramework.getChildren.forPath(AppConstant.WorkflowsZkPath)

    JavaConversions.asScalaBuffer(children).toList.flatMap { id =>
      if (workflowIds.contains(id))
        Option(findById(id))
      else None
    }
  }

  def findAll: List[Workflow] = {
    val children = curatorFramework.getChildren.forPath(AppConstant.WorkflowsZkPath)

    JavaConversions.asScalaBuffer(children).toList.map(id => findById(id))
  }

  def create(workflow: Workflow): Workflow = {

    validateWorkflow(workflow)

    existsByName(workflow.name, workflow.id).foreach(searchWorkflow => throw new ServerException(
      s"Workflow with name ${workflow.name} exists. The actual workflow is: ${searchWorkflow.id}"))

    val workflowWithFields = addCreationDate(addId(workflow))
    curatorFramework.create.creatingParentsIfNeeded.forPath(
      s"${AppConstant.WorkflowsZkPath}/${workflowWithFields.id.get}", write(workflowWithFields).getBytes)
    statusService.update(WorkflowStatus(
      id = workflowWithFields.id.get,
      status = WorkflowStatusEnum.NotStarted
    ))
    workflowWithFields
  }

  def createList(workflows: Seq[Workflow]): Seq[Workflow] =
    workflows.map(create)

  def update(workflow: Workflow): Workflow = {

    validateWorkflow(workflow)

    val searchWorkflow = existsByName(workflow.name, workflow.id)

    if (searchWorkflow.isEmpty) {
      throw new ServerException(s"Workflow with name ${workflow.name} does not exist")
    } else {
      val workflowId = addUpdateDate(workflow.copy(id = searchWorkflow.get.id))
      curatorFramework.setData().forPath(
        s"${AppConstant.WorkflowsZkPath}/${workflowId.id.get}", write(workflowId).getBytes)
      statusService.update(WorkflowStatus(
        id = workflowId.id.get,
        status = WorkflowStatusEnum.NotDefined
      ))
      workflowId
    }
  }

  def updateList(workflows: Seq[Workflow]): Seq[Workflow] =
    workflows.map(update)

  def delete(id: String): Try[Unit] =
    Try {
      val workflowPath = s"${AppConstant.WorkflowsZkPath}/$id"

      if (CuratorFactoryHolder.existsPath(workflowPath)) {
        log.info(s"Deleting workflow with id: $id")
        curatorFramework.delete().forPath(s"${AppConstant.WorkflowsZkPath}/$id")
        statusService.delete(id)
      } else throw new ServerException(s"No workflow with id $id")
    }

  def deleteList(workflowIds: Seq[String]): Try[Unit] =
    Try {
      val workflowPath = s"${AppConstant.WorkflowsZkPath}"

      if (CuratorFactoryHolder.existsPath(workflowPath)) {
        log.info(s"Deleting existing workflows from id list: $workflowIds")
        val workflows = findByIdList(workflowIds)

        try {
          workflows.foreach(workflow => delete(workflow.id.get))
          log.info(s"Workflows from ids ${workflowIds.mkString(",")} deleted")
        } catch {
          case e: Exception =>
            log.error("Error deleting workflows. The workflows deleted will be rolled back", e)
            Try(workflows.foreach(create))
            throw new RuntimeException("Error deleting workflows", e)
        }
      }
    }

  def deleteAll(): Try[Unit] =
    Try {
      val workflowPath = s"${AppConstant.WorkflowsZkPath}"

      if (CuratorFactoryHolder.existsPath(workflowPath)) {
        log.info(s"Deleting all existing workflows")
        val children = curatorFramework.getChildren.forPath(workflowPath)
        val workflows = JavaConversions.asScalaBuffer(children).toList.map(workflow =>
          read[Workflow](new String(curatorFramework.getData.forPath(
            s"${AppConstant.WorkflowsZkPath}/$workflow")))
        )

        try {
          workflows.foreach(workflow => delete(workflow.id.get))
          log.info(s"All workflows deleted")
        } catch {
          case e: Exception =>
            log.error("Error deleting workflows. The workflows deleted will be rolled back", e)
            Try(workflows.foreach(create))
            throw new RuntimeException("Error deleting workflows", e)
        }
      }
    }

  /** PRIVATE METHODS **/

  private[sparta] def validateWorkflow(workflow: Workflow): Unit = {
    val validationResult = validatorService.validate(workflow)
    if (!validationResult.valid)
      throw new ServerException(s"Workflow not valid. Cause: ${validationResult.messages.mkString("-")}")
  }

  private[sparta] def existsByName(name: String, id: Option[String] = None): Option[Workflow] =
    Try {
      if (CuratorFactoryHolder.existsPath(AppConstant.WorkflowsZkPath)) {
        findAll.find { workflow =>
          if (id.isDefined && workflow.id.isDefined)
            workflow.id.get == id.get
          else workflow.name == name
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

  private[sparta] def addId(workflow: Workflow): Workflow =
    workflow.id.notBlank match {
      case None => workflow.copy(id = Some(UUID.randomUUID.toString))
      case Some(_) => workflow
    }

  private[sparta] def addCreationDate(workflow: Workflow): Workflow =
    workflow.creationDate match {
      case None => workflow.copy(creationDate = Some(new DateTime()))
      case Some(_) => workflow
    }

  private[sparta] def addUpdateDate(workflow: Workflow): Workflow =
    workflow.copy(lastUpdateDate = Some(new DateTime()))
}
