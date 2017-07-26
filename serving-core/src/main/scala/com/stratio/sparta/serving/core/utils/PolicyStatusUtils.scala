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

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.workflow.{WorkflowModel, WorkflowStatusModel}
import com.stratio.sparta.serving.core.models.{ErrorModel, SpartaSerializer}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.{NodeCache, NodeCacheListener}
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

trait PolicyStatusUtils extends SpartaSerializer with SLF4JLogging {

  val curatorFramework: CuratorFramework

  /** Functions used inside the StatusActor **/

  def clearLastError(id: String): Try[Option[WorkflowStatusModel]] = {
    Try {
      val statusPath = s"${AppConstant.ContextPath}/$id"
      if (Option(curatorFramework.checkExists.forPath(statusPath)).isDefined) {
        val actualStatus = read[WorkflowStatusModel](new String(curatorFramework.getData.forPath(statusPath)))
        val newStatus = actualStatus.copy(lastError = None)
        log.info(s"Clearing last error for context: ${actualStatus.id}")
        curatorFramework.setData().forPath(statusPath, write(newStatus).getBytes)
        Some(newStatus)
      } else None
    }
  }

  //scalastyle:off
  def updateStatus(policyStatus: WorkflowStatusModel): Try[WorkflowStatusModel] = {
    Try {
      val statusPath = s"${AppConstant.ContextPath}/${policyStatus.id}"
      if (Option(curatorFramework.checkExists.forPath(statusPath)).isDefined) {
        val actualStatus = read[WorkflowStatusModel](new String(curatorFramework.getData.forPath(statusPath)))
        val newStatus = policyStatus.copy(
          status = if (policyStatus.status == PolicyStatusEnum.NotDefined) actualStatus.status
          else policyStatus.status,
          name = if (policyStatus.name.isEmpty) actualStatus.name
          else policyStatus.name,
          description = if (policyStatus.description.isEmpty) actualStatus.description
          else policyStatus.description,
          lastError = if (policyStatus.lastError.isDefined) policyStatus.lastError
          else if (policyStatus.status == PolicyStatusEnum.NotStarted) None else actualStatus.lastError,
          submissionId = if (policyStatus.submissionId.isDefined) policyStatus.submissionId
          else if (policyStatus.status == PolicyStatusEnum.NotStarted) None else actualStatus.submissionId,
          marathonId = if (policyStatus.marathonId.isDefined) policyStatus.marathonId
          else if (policyStatus.status == PolicyStatusEnum.NotStarted) None else actualStatus.marathonId,
          submissionStatus = if (policyStatus.submissionStatus.isEmpty) actualStatus.submissionStatus
          else policyStatus.submissionStatus,
          statusInfo = if (policyStatus.statusInfo.isEmpty) actualStatus.statusInfo
          else policyStatus.statusInfo,
          lastExecutionMode = if (policyStatus.lastExecutionMode.isEmpty) actualStatus.lastExecutionMode
          else policyStatus.lastExecutionMode,
          resourceManagerUrl = if (policyStatus.status == PolicyStatusEnum.Started) policyStatus.resourceManagerUrl
          else if (policyStatus.status == PolicyStatusEnum.NotDefined) actualStatus.resourceManagerUrl else None
        )
        log.info(s"Updating context ${newStatus.id} with name ${newStatus.name.getOrElse("undefined")}:" +
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
      } else createStatus(policyStatus)
        .getOrElse(throw new ServingCoreException(
          ErrorModel.toString(new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId,
            s"Unable to create policy context with id ${policyStatus.id}."))))
    }
  }

  //scalastyle:on

  def createStatus(policyStatus: WorkflowStatusModel): Try[WorkflowStatusModel] = {
    val statusPath = s"${AppConstant.ContextPath}/${policyStatus.id}"
    if (CuratorFactoryHolder.existsPath(statusPath)) {
      updateStatus(policyStatus)
    } else {
      Try {
        log.info(s"Creating policy context ${policyStatus.id} to <${policyStatus.status}>")
        curatorFramework.create.creatingParentsIfNeeded.forPath(statusPath, write(policyStatus).getBytes)
        policyStatus
      }
    }
  }

  def findAllStatuses(): Try[Seq[WorkflowStatusModel]] =
    Try {
      val contextPath = s"${AppConstant.ContextPath}"
      if (CuratorFactoryHolder.existsPath(contextPath)) {
        val children = curatorFramework.getChildren.forPath(contextPath)
        val policiesStatus = JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[WorkflowStatusModel](new String(
            curatorFramework.getData.forPath(s"${AppConstant.ContextPath}/$element")
          ))
        )
        policiesStatus
      } else Seq.empty[WorkflowStatusModel]
    }

  def findStatusById(id: String): Try[WorkflowStatusModel] =
    Try {
      val statusPath = s"${AppConstant.ContextPath}/$id"
      if (Option(curatorFramework.checkExists.forPath(statusPath)).isDefined)
        read[WorkflowStatusModel](new String(curatorFramework.getData.forPath(statusPath)))
      else throw new ServingCoreException(
        ErrorModel.toString(new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy context with id $id.")))
    }

  def deleteAllStatuses(): Try[_] =
    Try {
      val contextPath = s"${AppConstant.ContextPath}"

      if (CuratorFactoryHolder.existsPath(contextPath)) {
        val children = curatorFramework.getChildren.forPath(contextPath)
        val policiesStatus = JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[WorkflowStatusModel](new String(curatorFramework.getData.forPath(s"${AppConstant.ContextPath}/$element")))
        )

        policiesStatus.foreach(policyStatus => {
          val statusPath = s"${AppConstant.ContextPath}/${policyStatus.id}"
          if (Option(curatorFramework.checkExists.forPath(statusPath)).isDefined) {
            log.info(s"Deleting context ${policyStatus.id} >")
            curatorFramework.delete().forPath(statusPath)
          } else throw new ServingCoreException(ErrorModel.toString(
            new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy context found with id: ${policyStatus.id}.")))
        })
      }
    }

  def deleteStatus(id: String): Try[_] =
    Try {
      val statusPath = s"${AppConstant.ContextPath}/$id"
      if (Option(curatorFramework.checkExists.forPath(statusPath)).isDefined) {
        log.info(s">> Deleting context $id >")
        curatorFramework.delete().forPath(statusPath)
      } else throw new ServingCoreException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy context found with id: $id.")))
    }

  /**
   * Adds a listener to one policy and executes the callback when it changed.
   *
   * @param id       of the policy.
   * @param callback with a function that will be executed.
   */
  def addListener(id: String, callback: (WorkflowStatusModel, NodeCache) => Unit): Unit = {
    val contextPath = s"${AppConstant.ContextPath}/$id"
    val nodeCache: NodeCache = new NodeCache(curatorFramework, contextPath)
    nodeCache.getListenable.addListener(new NodeCacheListener {
      override def nodeChanged(): Unit = {
        Try(new String(nodeCache.getCurrentData.getData)) match {
          case Success(value) =>
            callback(read[WorkflowStatusModel](value), nodeCache)
          case Failure(e) =>
            log.error(s"NodeCache value: ${nodeCache.getCurrentData}", e)
        }
      }
    })
    nodeCache.start()
  }

  /** Functions used out of StatusActor **/

  def isAnyPolicyStarted: Boolean =
    findAllStatuses() match {
      case Success(statuses) =>
        statuses.exists(_.status == PolicyStatusEnum.Started) ||
          statuses.exists(_.status == PolicyStatusEnum.Starting) ||
          statuses.exists(_.status == PolicyStatusEnum.Launched)
      case Failure(e) =>
        log.error("An error was encountered while finding all the workflow statuses", e)
        false
    }

  def isAvailableToRun(workflowModel: WorkflowModel): Boolean =
    (workflowModel.settings.global.executionMode == AppConstant.ConfigLocal, isAnyPolicyStarted) match {
      case (false, _) =>
        true
      case (true, false) =>
        true
      case (true, true) =>
        log.warn(s"A workflow is already launched")
        false
    }
}
