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

import akka.actor.{ActorRef, ActorSystem}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.workflow._
import org.apache.curator.framework.CuratorFramework
import org.joda.time.DateTime
import org.json4s.jackson.Serialization._
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._

import scala.collection.JavaConversions
import scala.util._

class WorkflowService(
                       curatorFramework: CuratorFramework,
                       override val serializerSystem: Option[ActorSystem] = None,
                       override val environmentStateActor: Option[ActorRef] = None
                     ) extends SpartaSerializer with SLF4JLogging {

  private val statusService = new WorkflowStatusService(curatorFramework)
  private val validatorService = new WorkflowValidatorService(Option(curatorFramework))

  /** METHODS TO MANAGE WORKFLOWS IN ZOOKEEPER **/

  def findById(id: String): Workflow = {
    existsById(id).getOrElse(throw new ServerException(s"No workflow with id $id"))
  }

  def find(query: WorkflowQuery): Workflow =
    exists(query.name, query.version.getOrElse(0L), query.group.getOrElse(DefaultGroup.id.get))
      .getOrElse(throw new ServerException(s"No workflow with name ${query.name}"))

  def findList(query: WorkflowsQuery): Seq[Workflow] =
    existsList(query.group.getOrElse(DefaultGroup.id.get), query.tags)

  def findByGroupID(groupId: String): Seq[Workflow] =
    existsList(groupId)

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

    val workflowWithFields = addCreationDate(addId(workflow))

    existsById(workflowWithFields.id.get).foreach(searchWorkflow => throw new ServerException(
      s"Workflow with name ${workflowWithFields.name} and version ${workflowWithFields.version} exists." +
        s"The actual workflow is: ${searchWorkflow.id}"))

    curatorFramework.create.creatingParentsIfNeeded.forPath(
      s"${AppConstant.WorkflowsZkPath}/${workflowWithFields.id.get}", write(workflowWithFields).getBytes)
    statusService.update(WorkflowStatus(
      id = workflowWithFields.id.get,
      status = WorkflowStatusEnum.NotStarted
    ))

    workflowWithFields
  }

  def createVersion(workflowVersion: WorkflowVersion): Workflow = {
    existsById(workflowVersion.id) match {
      case Some(workflow) =>
        val workflowWithVersionFields = workflow.copy(
          tag = workflowVersion.tag,
          group = workflowVersion.group.getOrElse(workflow.group)
        )
        val workflowWithFields = addCreationDate(incVersion(addId(workflowWithVersionFields, force = true)))

        validateWorkflow(workflowWithFields)

        curatorFramework.create.creatingParentsIfNeeded.forPath(
          s"${AppConstant.WorkflowsZkPath}/${workflowWithFields.id.get}", write(workflowWithFields).getBytes)
        statusService.update(WorkflowStatus(
          id = workflowWithFields.id.get,
          status = WorkflowStatusEnum.NotStarted
        ))

        workflowWithFields
      case None => throw new ServerException(s"Workflow with id ${workflowVersion.id} not exists.")
    }
  }


  def createList(workflows: Seq[Workflow]): Seq[Workflow] =
    workflows.map(create)

  def update(workflow: Workflow): Workflow = {

    validateWorkflow(workflow)

    val searchWorkflow = existsById(workflow.id.get)

    if (searchWorkflow.isEmpty) {
      throw new ServerException(s"Workflow with name ${workflow.name} does not exist")
    } else {
      val workflowId = addUpdateDate(workflow.copy(id = searchWorkflow.get.id))
      curatorFramework.setData().forPath(
        s"${AppConstant.WorkflowsZkPath}/${workflowId.id.get}", write(workflowId).getBytes)
      statusService.update(WorkflowStatus(id = workflowId.id.get, status = WorkflowStatusEnum.NotDefined))
      workflowId
    }
  }

  def updateList(workflows: Seq[Workflow]): Seq[Workflow] =
    workflows.map(update)

  def delete(id: String): Try[Unit] =
    Try {
      val workflowPath = s"${AppConstant.WorkflowsZkPath}/$id"

      if (CuratorFactoryHolder.existsPath(workflowPath)) {
        log.debug(s"Deleting workflow with id: $id")
        curatorFramework.delete().forPath(s"${AppConstant.WorkflowsZkPath}/$id")
        statusService.delete(id)
      } else throw new ServerException(s"No workflow with id $id")
    }

  def deleteList(workflowIds: Seq[String]): Try[Unit] =
    Try {
      val workflowPath = s"${AppConstant.WorkflowsZkPath}"

      if (CuratorFactoryHolder.existsPath(workflowPath)) {
        log.debug(s"Deleting existing workflows from id list: $workflowIds")
        val workflows = findByIdList(workflowIds)

        try {
          workflows.foreach(workflow => delete(workflow.id.get))
          log.debug(s"Workflows from ids ${workflowIds.mkString(",")} deleted")
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
        log.debug(s"Deleting all existing workflows")
        val children = curatorFramework.getChildren.forPath(workflowPath)
        val workflows = JavaConversions.asScalaBuffer(children).toList.map(workflow =>
          read[Workflow](new String(curatorFramework.getData.forPath(
            s"${AppConstant.WorkflowsZkPath}/$workflow")))
        )

        try {
          workflows.foreach(workflow => delete(workflow.id.get))
          log.debug(s"All workflows deleted")
        } catch {
          case e: Exception =>
            log.error("Error deleting workflows. The workflows deleted will be rolled back", e)
            Try(workflows.foreach(create))
            throw new RuntimeException("Error deleting workflows", e)
        }
      }
    }

  def resetAllStatuses(): Try[Unit] =
    Try {
      val workflowPath = s"${AppConstant.WorkflowsZkPath}"

      if (CuratorFactoryHolder.existsPath(workflowPath)) {
        log.debug(s"Resetting the execution status for every workflow")
        val children = curatorFramework.getChildren.forPath(workflowPath)

        JavaConversions.asScalaBuffer(children).toList.foreach(workflow =>
          statusService.update(WorkflowStatus(workflow, WorkflowStatusEnum.NotStarted))
        )
      }
    }

  def stop(id: String): Try[Any] =
    statusService.update(WorkflowStatus(id, WorkflowStatusEnum.Stopping))

  def reset(id: String): Try[Any] =
    statusService.update(WorkflowStatus(id, WorkflowStatusEnum.NotStarted))

  /** PRIVATE METHODS **/

  private[sparta] def validateWorkflow(workflow: Workflow): Unit = {
    val validationResult = validatorService.validate(workflow)
    if (!validationResult.valid)
      throw new ServerException(s"Workflow not valid. Cause: ${validationResult.messages.mkString("-")}")
  }

  private[sparta] def exists(name: String, version: Long, group: String): Option[Workflow] =
    Try {
      if (CuratorFactoryHolder.existsPath(AppConstant.WorkflowsZkPath)) {
        findAll.find { workflow =>
          workflow.name == name && workflow.version == version && workflow.group.id.get == group
        }
      } else None
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        None
    }

  private[sparta] def existsList(groupId: String, tags: Seq[String] = Seq.empty[String]): Seq[Workflow] =
    Try {
      if (CuratorFactoryHolder.existsPath(AppConstant.WorkflowsZkPath)) {
        findAll.filter { workflow =>
          workflow.group.id.get == groupId &&
            (workflow.tag.isEmpty || (workflow.tag.isDefined && tags.contains(workflow.tag.get)))
        }
      } else Seq.empty[Workflow]
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        Seq.empty[Workflow]
    }

  private[sparta] def existsById(id: String): Option[Workflow] =
    Try {
      if (CuratorFactoryHolder.existsPath(s"${AppConstant.WorkflowsZkPath}/$id")) {
        val workFlow = read[Workflow](
          new Predef.String(curatorFramework.getData.forPath(s"${AppConstant.WorkflowsZkPath}/$id")))
        Option(workFlow.copy(status =
          statusService.findById(id) recoverWith {
            case e =>
              log.error(s"Error finding workflowStatus with id ${id}", e)
              Failure(e)
          } toOption
        ))
      } else None
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        None
    }

  private[sparta] def addId(workflow: Workflow, force: Boolean = false): Workflow =
    if (workflow.id.notBlank.isEmpty || (workflow.id.notBlank.isDefined && force))
      workflow.copy(id = Some(UUID.randomUUID.toString))
    else workflow

  private[sparta] def incVersion(workflow: Workflow, userVersion: Option[Long] = None): Workflow =
    userVersion match {
      case None =>
        val maxVersion = Try(workflowVersions(workflow.name, workflow.group).map(_.version).max + 1).getOrElse(0L)
        workflow.copy(version = maxVersion)
      case Some(usrVersion) => workflow.copy(version = usrVersion)
    }

  private[sparta] def addCreationDate(workflow: Workflow): Workflow =
    workflow.creationDate match {
      case None => workflow.copy(creationDate = Some(new DateTime()))
      case Some(_) => workflow
    }

  private[sparta] def addUpdateDate(workflow: Workflow): Workflow =
    workflow.copy(lastUpdateDate = Some(new DateTime()))

  private[sparta] def workflowVersions(name: String, group: Group): Seq[Workflow] =
    Try {
      if (CuratorFactoryHolder.existsPath(AppConstant.WorkflowsZkPath)) {
        findAll.filter { workflow =>
          workflow.name == name && workflow.group == group
        }
      } else Seq.empty
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        Seq.empty
    }

}
