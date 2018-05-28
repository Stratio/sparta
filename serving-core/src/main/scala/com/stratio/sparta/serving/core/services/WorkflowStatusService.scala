/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services

import java.util.UUID

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import org.apache.curator.framework.CuratorFramework
import org.joda.time.DateTime
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

class WorkflowStatusService(curatorFramework: CuratorFramework) extends SpartaSerializer with SLF4JLogging {

  def findById(id: String): Try[WorkflowStatus] = {
    log.debug(s"Finding workflow status with id $id")
    Try {
      val statusPath = s"${AppConstant.WorkflowStatusesZkPath}/$id"
      if (CuratorFactoryHolder.existsPath(statusPath))
        read[WorkflowStatus](new String(curatorFramework.getData.forPath(statusPath)))
      else throw new ServerException(s"No workflow status with id $id.")
    }
  }

  def findAll(): Try[Seq[WorkflowStatus]] = {
    log.debug(s"Finding all workflow statuses")
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
  }

  def create(workflowStatus: WorkflowStatus): Try[WorkflowStatus] = {
    log.debug(s"Creating workflow ${workflowStatus.id} with status ${workflowStatus.status}")
    val workflowStatusWithFields = addCreationDate(workflowStatus)
    val statusPath = s"${AppConstant.WorkflowStatusesZkPath}/${workflowStatusWithFields.id}"
    val workflowStatusWithFieldsAndId = addStatusId(workflowStatusWithFields)
    if (CuratorFactoryHolder.existsPath(statusPath)) {
      log.debug(s"The workflow status ${workflowStatus.id} exists, updating it")
      update(workflowStatusWithFieldsAndId)
    } else {
      Try {
        curatorFramework.create.creatingParentsIfNeeded.forPath(statusPath,
          write(workflowStatusWithFieldsAndId).getBytes)
        workflowStatusWithFieldsAndId
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
          status = {
            if (workflowStatus.status == NotDefined) actualStatus.status
            else workflowStatus.status
          },
          statusInfo = {
            if (workflowStatus.statusInfo.isEmpty) actualStatus.statusInfo
            else workflowStatus.statusInfo
          },
          lastUpdateDate = {
            if (workflowStatus.status == NotDefined || workflowStatus.status == actualStatus.status)
              actualStatus.lastUpdateDate
            else getNewUpdateDate
          },
          lastUpdateDateWorkflow = {
            if (workflowStatus.lastUpdateDateWorkflow.isDefined)
              workflowStatus.lastUpdateDateWorkflow
            else actualStatus.lastUpdateDateWorkflow
          },
          statusId = {
            if (workflowStatus.status != NotDefined && workflowStatus.status != actualStatus.status)
              Option(UUID.randomUUID.toString)
            else
              actualStatus.statusId
          }
        )
        curatorFramework.setData().forPath(statusPath, write(newStatus).getBytes)

        //Print status information saved
        val newStatusInformation = new StringBuilder
        if (actualStatus.status != newStatus.status)
          newStatusInformation.append(s"\tStatus -> ${actualStatus.status} to ${newStatus.status}")
        if (actualStatus.statusInfo != newStatus.statusInfo)
          newStatusInformation.append(s"\tInfo -> ${newStatus.statusInfo.getOrElse("No status information registered")}")
        if (newStatusInformation.nonEmpty)
          log.info(s"Updating status ${newStatus.id}: $newStatusInformation")

        newStatus
      } else create(workflowStatus)
        .getOrElse(throw new ServerException(s"Unable to create workflow status with id ${workflowStatus.id}."))
    }
  }

  //scalastyle:on

  def delete(id: String): Try[Unit] = {
    log.debug(s"Deleting workflow status $id")
    Try {
      val statusPath = s"${AppConstant.WorkflowStatusesZkPath}/$id"
      if (CuratorFactoryHolder.existsPath(statusPath)) {
        curatorFramework.delete().forPath(statusPath)
      } else throw new ServerException(s"No workflow status found with id: $id.")
    }
  }

  def deleteAll(): Try[Unit] = {
    log.debug(s"Deleting all workflow statuses")
    Try {
      val statusPath = s"${AppConstant.WorkflowStatusesZkPath}"
      if (CuratorFactoryHolder.existsPath(statusPath)) {
        val children = curatorFramework.getChildren.forPath(statusPath)

        JavaConversions.asScalaBuffer(children).toList.foreach { element =>
          curatorFramework.delete().forPath(s"${AppConstant.WorkflowStatusesZkPath}/$element")
        }
      }
    }
  }

  private[sparta] def workflowsStarted: Seq[WorkflowStatus] =
    findAll() match {
      case Success(statuses) =>
        statuses.filter(wStatus =>
          wStatus.status == Started)
      case Failure(e) =>
        log.error("An error was encountered while finding all the workflow statuses", e)
        Seq()
    }

  private[sparta] def getNewUpdateDate: Option[DateTime] = Option(new DateTime())

  private[sparta] def addCreationDate(workflowStatus: WorkflowStatus): WorkflowStatus =
    workflowStatus.creationDate match {
      case None => workflowStatus.copy(creationDate = Some(new DateTime()))
      case Some(_) => workflowStatus
    }

  private[sparta] def addStatusId(workflowStatus: WorkflowStatus): WorkflowStatus =
    workflowStatus.statusId match{
      case None => workflowStatus.copy(statusId = Option(UUID.randomUUID.toString))
      case Some(_) => workflowStatus
    }
}
