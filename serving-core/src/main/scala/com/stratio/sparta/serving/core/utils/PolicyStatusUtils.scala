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

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.helpers.ResourceManagerLinkHelper
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.policy.{PoliciesStatusModel, PolicyModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.models.{ErrorModel, SpartaSerializer}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.{NodeCache, NodeCacheListener}
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

trait PolicyStatusUtils extends SpartaSerializer with PolicyConfigUtils {

  val curatorFramework: CuratorFramework

  /** Functions used inside the StatusActor **/

  def clearLastError(id: String): Try[Option[PolicyStatusModel]] = {
    Try {
      val statusPath = s"${AppConstant.ContextPath}/$id"
      if (Option(curatorFramework.checkExists.forPath(statusPath)).isDefined) {
        val actualStatus = read[PolicyStatusModel](new String(curatorFramework.getData.forPath(statusPath)))
        val newStatus = actualStatus.copy(lastError = None)
        log.info(s"Clearing last error for context: ${actualStatus.id}")
        curatorFramework.setData().forPath(statusPath, write(newStatus).getBytes)
        Some(newStatus)
      } else None
    }
  }

  def updateStatus(policyStatus: PolicyStatusModel): Try[PolicyStatusModel] = {
    Try {
      val statusPath = s"${AppConstant.ContextPath}/${policyStatus.id}"
      if (Option(curatorFramework.checkExists.forPath(statusPath)).isDefined) {
        val actualStatus = read[PolicyStatusModel](new String(curatorFramework.getData.forPath(statusPath)))
        val newStatus = policyStatus.copy(
          status = if (policyStatus.status == PolicyStatusEnum.NotDefined) actualStatus.status
          else policyStatus.status,
          name = if (policyStatus.name.isEmpty) actualStatus.name
          else policyStatus.name,
          description = if (policyStatus.description.isEmpty) actualStatus.description
          else policyStatus.description,
          lastError = if (policyStatus.lastError.isEmpty) actualStatus.lastError
          else policyStatus.lastError,
          submissionId = if (policyStatus.submissionId.isEmpty) actualStatus.submissionId
          else policyStatus.submissionId,
          submissionStatus = if (policyStatus.submissionStatus.isEmpty) actualStatus.submissionStatus
          else policyStatus.submissionStatus,
          statusInfo = if (policyStatus.statusInfo.isEmpty) actualStatus.statusInfo
          else policyStatus.statusInfo
        )
        log.info(s"Updating context ${newStatus.id} with name ${newStatus.name}: " +
          s"\n\t Status: ${actualStatus.status} to ${newStatus.status}" +
          s"\n\t Status Information: ${actualStatus.statusInfo.getOrElse("undefined")}" +
          s" to ${newStatus.statusInfo.getOrElse("undefined")} " +
          s"\n\t Submission Id: ${actualStatus.submissionId.getOrElse("undefined")}" +
          s" to ${newStatus.submissionId.getOrElse("undefined")}" +
          s"\n\t Submission Status: ${actualStatus.submissionStatus.getOrElse("undefined")}" +
          s" to ${newStatus.submissionStatus.getOrElse("undefined")}" +
          s"\n\t Last Error: ${actualStatus.lastError.getOrElse("undefined")}" +
          s" to ${newStatus.lastError.getOrElse("undefined")}")
        curatorFramework.setData().forPath(statusPath, write(newStatus).getBytes)
        newStatus
      } else createStatus(policyStatus)
        .getOrElse(throw new ServingCoreException(
          ErrorModel.toString(new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId,
            s"Is not possible to create policy context with id ${policyStatus.id}."))))
    }
  }

  def createStatus(policyStatus: PolicyStatusModel): Try[PolicyStatusModel] = {
    val statusPath = s"${AppConstant.ContextPath}/${policyStatus.id}"
    if (CuratorFactoryHolder.existsPath(statusPath)) {
      updateStatus(policyStatus)
    } else {
      Try {
        log.info(s"Creating policy context |${policyStatus.id}| to <${policyStatus.status}>")
        curatorFramework.create.creatingParentsIfNeeded.forPath(statusPath, write(policyStatus).getBytes)
        policyStatus
      }
    }
  }

  def findAllStatuses(): Try[PoliciesStatusModel] =
    Try {
      val contextPath = s"${AppConstant.ContextPath}"
      if (CuratorFactoryHolder.existsPath(contextPath)) {
        val children = curatorFramework.getChildren.forPath(contextPath)
        val policiesStatus = JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[PolicyStatusModel](new String(
            curatorFramework.getData.forPath(s"${AppConstant.ContextPath}/$element")
          ))
        )
        PoliciesStatusModel(policiesStatus, ResourceManagerLinkHelper.getLink)
      } else PoliciesStatusModel(Seq(), ResourceManagerLinkHelper.getLink)
    }

  def findStatusById(id: String): Try[PolicyStatusModel] =
    Try {
      val statusPath = s"${AppConstant.ContextPath}/$id"
      if (Option(curatorFramework.checkExists.forPath(statusPath)).isDefined)
        read[PolicyStatusModel](new String(curatorFramework.getData.forPath(statusPath)))
      else throw new ServingCoreException(
        ErrorModel.toString(new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy context with id $id.")))
    }

  def deleteAll(): Try[_] =
    Try {
      val contextPath = s"${AppConstant.ContextPath}"

      if (CuratorFactoryHolder.existsPath(contextPath)) {
        val children = curatorFramework.getChildren.forPath(contextPath)
        val policiesStatus = JavaConversions.asScalaBuffer(children).toList.map(element =>
          read[PolicyStatusModel](new String(curatorFramework.getData.forPath(s"${AppConstant.ContextPath}/$element")))
        )

        policiesStatus.foreach(policyStatus => {
          val statusPath = s"${AppConstant.ContextPath}/${policyStatus.id}"
          if (Option(curatorFramework.checkExists.forPath(statusPath)).isDefined) {
            log.info(s"Deleting context ${policyStatus.id} >")
            curatorFramework.delete().forPath(statusPath)
          } else throw new ServingCoreException(ErrorModel.toString(
            new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy context with id ${policyStatus.id}.")))
        })
      }
    }

  def delete(id: String): Try[_] =
    Try {
      val statusPath = s"${AppConstant.ContextPath}/$id"
      if (Option(curatorFramework.checkExists.forPath(statusPath)).isDefined) {
        log.info(s">> Deleting context $id >")
        curatorFramework.delete().forPath(statusPath)
      } else throw new ServingCoreException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy context with id $id.")))
    }

  /**
   * Adds a listener to one policy and executes the callback when it changed.
   *
   * @param id       of the policy.
   * @param callback with a function that will be executed.
   */
  def addListener(id: String, callback: (PolicyStatusModel, NodeCache) => Unit): Unit = {
    val contextPath = s"${AppConstant.ContextPath}/$id"
    val nodeCache: NodeCache = new NodeCache(curatorFramework, contextPath)
    nodeCache.getListenable.addListener(new NodeCacheListener {
      override def nodeChanged(): Unit = {
        Try(new String(nodeCache.getCurrentData.getData)) match {
          case Success(value) =>
            callback(read[PolicyStatusModel](value), nodeCache)
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
        statuses.policiesStatus.exists(_.status == PolicyStatusEnum.Started) ||
          statuses.policiesStatus.exists(_.status == PolicyStatusEnum.Starting) ||
          statuses.policiesStatus.exists(_.status == PolicyStatusEnum.Launched)
      case Failure(e) =>
        log.error("Error when find all policy statuses.", e)
        false
    }

  def isAvailableToRun(policyModel: PolicyModel): Boolean =
    (isLocal(policyModel), isAnyPolicyStarted) match {
      case (false, _) =>
        true
      case (true, false) =>
        true
      case (true, true) =>
        log.warn(s"One policy is already launched")
        false
    }
}
