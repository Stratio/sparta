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

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowStatus}
import com.stratio.sparta.serving.core.models.{ErrorModel, SpartaSerializer}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.{NodeCache, NodeCacheListener}
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

class WorkflowStatusService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  def findById(id: String): Try[WorkflowStatus] =
    Try {
      val statusPath = s"${AppConstant.WorkflowStatusesZkPath}/$id"
      if (CuratorFactoryHolder.existsPath(statusPath))
        read[WorkflowStatus](new String(curatorFramework.getData.forPath(statusPath)))
      else throw new ServerException(s"No workflow status with id $id.")
    }

  def findAll(): Try[Seq[WorkflowStatus]] =
    Try {
      val statusPath = s"${AppConstant.WorkflowStatusesZkPath}"
      if (CuratorFactoryHolder.existsPath(statusPath)) {
        val children = curatorFramework.getChildren.forPath(statusPath)
        val policiesStatus = JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[WorkflowStatus](new String(
            curatorFramework.getData.forPath(s"${AppConstant.WorkflowStatusesZkPath}/$element")
          ))
        )
        policiesStatus
      } else Seq.empty[WorkflowStatus]
    }

  def create(workflowStatus: WorkflowStatus): Try[WorkflowStatus] = {
    val statusPath = s"${AppConstant.WorkflowStatusesZkPath}/${workflowStatus.id}"
    if (CuratorFactoryHolder.existsPath(statusPath)) {
      update(workflowStatus)
    } else {
      Try {
        log.info(s"Creating workflow status ${workflowStatus.id} to <${workflowStatus.status}>")
        curatorFramework.create.creatingParentsIfNeeded.forPath(statusPath, write(workflowStatus).getBytes)
        workflowStatus
      }
    }
  }

  //scalastyle:off
  def update(workflowStatus: WorkflowStatus): Try[WorkflowStatus] = {
    Try {
      val statusPath = s"${AppConstant.WorkflowStatusesZkPath}/${workflowStatus.id}"
      if (CuratorFactoryHolder.existsPath(statusPath)) {
        val actualStatus = read[WorkflowStatus](new String(curatorFramework.getData.forPath(statusPath)))
        val newStatus = workflowStatus.copy(
          status = if (workflowStatus.status == WorkflowStatusEnum.NotDefined) actualStatus.status
          else workflowStatus.status,
          name = if (workflowStatus.name.isEmpty) actualStatus.name
          else workflowStatus.name,
          description = if (workflowStatus.description.isEmpty) actualStatus.description
          else workflowStatus.description,
          lastError = if (workflowStatus.lastError.isDefined) workflowStatus.lastError
          else if (workflowStatus.status == WorkflowStatusEnum.NotStarted) None else actualStatus.lastError,
          submissionId = if (workflowStatus.submissionId.isDefined) workflowStatus.submissionId
          else if (workflowStatus.status == WorkflowStatusEnum.NotStarted) None else actualStatus.submissionId,
          marathonId = if (workflowStatus.marathonId.isDefined) workflowStatus.marathonId
          else if (workflowStatus.status == WorkflowStatusEnum.NotStarted) None else actualStatus.marathonId,
          submissionStatus = if (workflowStatus.submissionStatus.isEmpty) actualStatus.submissionStatus
          else workflowStatus.submissionStatus,
          statusInfo = if (workflowStatus.statusInfo.isEmpty) actualStatus.statusInfo
          else workflowStatus.statusInfo,
          lastExecutionMode = if (workflowStatus.lastExecutionMode.isEmpty) actualStatus.lastExecutionMode
          else workflowStatus.lastExecutionMode,
          resourceManagerUrl = if (workflowStatus.status == WorkflowStatusEnum.Started) workflowStatus.resourceManagerUrl
          else if (workflowStatus.status == WorkflowStatusEnum.NotDefined) actualStatus.resourceManagerUrl else None
        )
        log.info(s"Updating status ${newStatus.id} with name ${newStatus.name.getOrElse("undefined")}:" +
          s"\n\tStatus:\t${actualStatus.status}\t--->\t${newStatus.status}" +
          s"\n\tStatus Information:\t${actualStatus.statusInfo.getOrElse("undefined")}" +
          s"\t--->\t${newStatus.statusInfo.getOrElse("undefined")} " +
          s"\n\tSubmission Id:\t${actualStatus.submissionId.getOrElse("undefined")}" +
          s"\t--->\t${newStatus.submissionId.getOrElse("undefined")}" +
          s"\n\tSubmission Status:\t${actualStatus.submissionStatus.getOrElse("undefined")}" +
          s"\t--->\t${newStatus.submissionStatus.getOrElse("undefined")}" +
          s"\n\tKill Url:\t${actualStatus.killUrl.getOrElse("undefined")}" +
          s"\t--->\t${newStatus.killUrl.getOrElse("undefined")}" +
          s"\n\tMarathon Id:\t${actualStatus.marathonId.getOrElse("undefined")}" +
          s"\t--->\t${newStatus.marathonId.getOrElse("undefined")}" +
          s"\n\tLast Error:\t${actualStatus.lastError.getOrElse("undefined")}" +
          s"\t--->\t${newStatus.lastError.getOrElse("undefined")}" +
          s"\n\tLast Execution Mode:\t${actualStatus.lastExecutionMode.getOrElse("undefined")}" +
          s"\t--->\t${newStatus.lastExecutionMode.getOrElse("undefined")}" +
          s"\n\tResource Manager URL:\t${actualStatus.resourceManagerUrl.getOrElse("undefined")}" +
          s"\t--->\t${newStatus.resourceManagerUrl.getOrElse("undefined")}")
        curatorFramework.setData().forPath(statusPath, write(newStatus).getBytes)
        newStatus
      } else create(workflowStatus)
        .getOrElse(throw new ServerException(s"Unable to create workflow status with id ${workflowStatus.id}."))
    }
  }

  //scalastyle:on

  def delete(id: String): Try[_] =
    Try {
      val statusPath = s"${AppConstant.WorkflowStatusesZkPath}/$id"
      if (CuratorFactoryHolder.existsPath(statusPath)) {
        log.info(s"Deleting status $id")
        curatorFramework.delete().forPath(statusPath)
      } else throw new ServerException(s"No workflow status found with id: $id.")
    }

  def deleteAll(): Try[_] =
    Try {
      val statusPath = s"${AppConstant.WorkflowStatusesZkPath}"
      if (CuratorFactoryHolder.existsPath(statusPath)) {
        val children = curatorFramework.getChildren.forPath(statusPath)
        val policiesStatus = JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[WorkflowStatus](new String(curatorFramework.getData.forPath(s"${AppConstant.WorkflowStatusesZkPath}/$element")))
        )

        policiesStatus.foreach(workflowStatus => {
          val statusPath = s"${AppConstant.WorkflowStatusesZkPath}/${workflowStatus.id}"
          if (Option(curatorFramework.checkExists.forPath(statusPath)).isDefined) {
            log.info(s"Deleting status ${workflowStatus.id} >")
            curatorFramework.delete().forPath(statusPath)
          } else throw new ServerException(s"No workflow status found with id: ${workflowStatus.id}.")
        })
      }
    }

  def clearLastError(id: String): Try[Option[WorkflowStatus]] = {
    Try {
      val statusPath = s"${AppConstant.WorkflowStatusesZkPath}/$id"
      if (CuratorFactoryHolder.existsPath(statusPath)) {
        val actualStatus = read[WorkflowStatus](new String(curatorFramework.getData.forPath(statusPath)))
        val newStatus = actualStatus.copy(lastError = None)
        log.info(s"Clearing last error for status: ${actualStatus.id}")
        curatorFramework.setData().forPath(statusPath, write(newStatus).getBytes)
        Some(newStatus)
      } else None
    }
  }

  private[sparta] def isAnyWorkflowStarted: Boolean =
    findAll() match {
      case Success(statuses) =>
        statuses.exists(_.status == WorkflowStatusEnum.Started) ||
          statuses.exists(_.status == WorkflowStatusEnum.Starting) ||
          statuses.exists(_.status == WorkflowStatusEnum.Launched)
      case Failure(e) =>
        log.error("An error was encountered while finding all the workflow statuses", e)
        false
    }

  private[sparta] def isAvailableToRun(workflowModel: Workflow): Boolean =
    (workflowModel.settings.global.executionMode == AppConstant.ConfigLocal, isAnyWorkflowStarted) match {
      case (false, _) =>
        true
      case (true, false) =>
        true
      case (true, true) =>
        log.warn(s"The workflow ${workflowModel.name} is already launched")
        false
    }
}
