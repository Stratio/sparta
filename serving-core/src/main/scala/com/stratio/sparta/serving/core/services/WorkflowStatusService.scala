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
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import org.apache.curator.framework.CuratorFramework
import org.joda.time.DateTime
import org.json4s.jackson.Serialization._
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._

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
    val workflowStatusWithFields = addCreationDate(workflowStatus)
    val statusPath = s"${AppConstant.WorkflowStatusesZkPath}/${workflowStatusWithFields.id}"
    if (CuratorFactoryHolder.existsPath(statusPath)) {
      update(workflowStatusWithFields)
    } else {
      Try {
        log.info(s"Creating workflow ${workflowStatusWithFields.id} with status ${workflowStatusWithFields.status}")
        curatorFramework.create.creatingParentsIfNeeded.forPath(statusPath, write(workflowStatusWithFields).getBytes)
        workflowStatusWithFields
      }
    }
  }

  //scalastyle:off
  def update(workflowStatus: WorkflowStatus): Try[WorkflowStatus] = {
    Try {
      val statusPath = s"${AppConstant.WorkflowStatusesZkPath}/${workflowStatus.id}"
      if (CuratorFactoryHolder.existsPath(statusPath)) {
        val actualStatus = read[WorkflowStatus](new String(curatorFramework.getData.forPath(statusPath)))
        val workflowStatusWithFields = addUpdateDate(workflowStatus)
        val newStatus = workflowStatusWithFields.copy(
          status = if (workflowStatusWithFields.status == WorkflowStatusEnum.NotDefined) actualStatus.status
          else workflowStatusWithFields.status,
          lastError = if (workflowStatusWithFields.lastError.isDefined) workflowStatusWithFields.lastError
          else if (workflowStatusWithFields.status == WorkflowStatusEnum.NotStarted) None else actualStatus.lastError,
          statusInfo = if (workflowStatusWithFields.statusInfo.isEmpty) actualStatus.statusInfo
          else workflowStatusWithFields.statusInfo,
          lastExecutionMode = if (workflowStatusWithFields.lastExecutionMode.isEmpty) actualStatus.lastExecutionMode
          else workflowStatusWithFields.lastExecutionMode,
          sparkURI = updateSparkURI(workflowStatusWithFields, actualStatus)
        )
        curatorFramework.setData().forPath(statusPath, write(newStatus).getBytes)

        //Print status information saved
        val newStatusInformation = new StringBuilder
        if (actualStatus.status != newStatus.status)
          newStatusInformation.append(s"\tStatus -> ${actualStatus.status} to ${newStatus.status}")
        if (actualStatus.statusInfo != newStatus.statusInfo)
          newStatusInformation.append(s"\tInfo -> ${newStatus.statusInfo.getOrElse("No status information registered")}")
        if (actualStatus.lastError != newStatus.lastError)
          newStatusInformation.append(s"\tLast Error -> ${newStatus.lastError.getOrElse("No errors registered")}")
        if (newStatusInformation.nonEmpty)
          log.info(s"Updating status ${newStatus.id}: $newStatusInformation")

        newStatus
      } else create(workflowStatus)
        .getOrElse(throw new ServerException(s"Unable to create workflow status with id ${workflowStatus.id}."))
    }
  }

  //scalastyle:on

  def delete(id: String): Try[Unit] =
    Try {
      val statusPath = s"${AppConstant.WorkflowStatusesZkPath}/$id"
      if (CuratorFactoryHolder.existsPath(statusPath)) {
        log.info(s"Deleting status $id")
        curatorFramework.delete().forPath(statusPath)
      } else throw new ServerException(s"No workflow status found with id: $id.")
    }

  def deleteAll(): Try[Unit] =
    Try {
      val statusPath = s"${AppConstant.WorkflowStatusesZkPath}"
      if (CuratorFactoryHolder.existsPath(statusPath)) {
        val children = curatorFramework.getChildren.forPath(statusPath)

        JavaConversions.asScalaBuffer(children).toList.foreach { element =>
          curatorFramework.delete().forPath(s"${AppConstant.WorkflowStatusesZkPath}/$element")
        }
      }
    }

  def clearLastError(id: String): Try[Option[WorkflowStatus]] = {
    Try {
      val statusPath = s"${AppConstant.WorkflowStatusesZkPath}/$id"
      if (CuratorFactoryHolder.existsPath(statusPath)) {
        val actualStatus = read[WorkflowStatus](new String(curatorFramework.getData.forPath(statusPath)))
        val newStatus = actualStatus.copy(lastError = None)
        log.debug(s"Clearing last error for status: ${actualStatus.id}")
        curatorFramework.setData().forPath(statusPath, write(newStatus).getBytes)
        Some(newStatus)
      } else None
    }
  }

  private[sparta] def isAnyLocalWorkflowStarted: Boolean =
    findAll() match {
      case Success(statuses) =>
        statuses.exists(wStatus =>
          wStatus.status == WorkflowStatusEnum.Started && wStatus.lastExecutionMode == Option(AppConstant.ConfigLocal))
      case Failure(e) =>
        log.error("An error was encountered while finding all the workflow statuses", e)
        false
    }

  private[sparta] def addUpdateDate(workflowStatus: WorkflowStatus): WorkflowStatus =
    workflowStatus.copy(lastUpdateDate = Some(new DateTime()))

  private[sparta] def addCreationDate(workflowStatus: WorkflowStatus): WorkflowStatus =
    workflowStatus.creationDate match {
      case None => workflowStatus.copy(creationDate = Some(new DateTime()))
      case Some(_) => workflowStatus
    }

  @inline
  private[sparta] def updateSparkURI(workflowStatus: WorkflowStatus,
                                     zkWorkflowStatus: WorkflowStatus): Option[String] = {
    if (workflowStatus.sparkURI.notBlank.isDefined) workflowStatus.sparkURI
    else if (workflowStatus.status == WorkflowStatusEnum.NotStarted  ||
      workflowStatus.status == WorkflowStatusEnum.Stopped ||
      workflowStatus.status == WorkflowStatusEnum.Failed) None
    else zkWorkflowStatus.sparkURI
  }
}
