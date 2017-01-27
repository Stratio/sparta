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

package com.stratio.sparta.serving.core.actor

import akka.actor.{Actor, _}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.actor.StatusActor._
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.helpers.ResourceManagerLinkHelper
import com.stratio.sparta.serving.core.models.enumerators.PolicyStatusEnum
import com.stratio.sparta.serving.core.models.policy.{PoliciesStatusModel, PolicyStatusModel}
import com.stratio.sparta.serving.core.models.{SpartaSerializer, _}
import com.stratio.sparta.serving.core.utils.PolicyStatusUtils
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.{NodeCache, NodeCacheListener}
import org.json4s.jackson.Serialization.{read, write}

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

class StatusActor(curatorFramework: CuratorFramework)
  extends Actor with SLF4JLogging with SpartaSerializer with PolicyStatusUtils {

  override def receive: Receive = {
    case Create(policyStatus) => sender ! create(policyStatus)
    case Update(policyStatus) => sender ! update(policyStatus)
    case FindAll => findAll()
    case FindById(id) => sender ! findById(id)
    case DeleteAll => deleteAll()
    case AddListener(name, callback) => addListener(name, callback)
    case Delete(id) => sender ! delete(id)
    case _ => log.info("Unrecognized message in Policy Status Actor")
  }

  def update(policyStatus: PolicyStatusModel): Option[PolicyStatusModel] = {
    val statusPath = s"${AppConstant.ContextPath}/${policyStatus.id}"
    if (Option(curatorFramework.checkExists.forPath(statusPath)).isDefined) {
      val actualStatus = read[PolicyStatusModel](new String(curatorFramework.getData.forPath(statusPath)))
      val newStatus = policyStatus.copy(
        status = if (policyStatus.status == PolicyStatusEnum.NotDefined)
          actualStatus.status
        else policyStatus.status,
        submissionId = if (policyStatus.submissionId.isEmpty)
          actualStatus.submissionId
        else policyStatus.submissionId,
        submissionStatus = if (policyStatus.submissionStatus.isEmpty)
          actualStatus.submissionStatus
        else policyStatus.submissionStatus,
        information = if (policyStatus.information.isEmpty)
          actualStatus.information
        else policyStatus.information
      )
      log.info(s"Updating context ${newStatus.id} : " +
        s"\n\t Status: ${actualStatus.status} to ${newStatus.status}" +
        s"\n\t Submission Id: ${actualStatus.submissionId.getOrElse("undefined")}" +
        s" to ${newStatus.submissionId.getOrElse("undefined")}" +
        s"\n\t Submission Status: ${actualStatus.submissionStatus.getOrElse("undefined")}" +
        s" to ${newStatus.submissionStatus.getOrElse("undefined")}" +
        s"\n\t Information: ${actualStatus.information.getOrElse("undefined")}" +
        s" to ${newStatus.information.getOrElse("undefined")}")
      curatorFramework.setData().forPath(statusPath, write(newStatus).getBytes)
      Some(newStatus)
    } else None
  }

  def create(policyStatus: PolicyStatusModel): Option[PolicyStatusModel] = {
    val statusPath = s"${AppConstant.ContextPath}/${policyStatus.id}"
    if (CuratorFactoryHolder.existsPath(statusPath)) {
      update(policyStatus)
    } else {
      log.info(s"Creating policy context |${policyStatus.id}| to <${policyStatus.status}>")
      curatorFramework.create.creatingParentsIfNeeded.forPath(statusPath, write(policyStatus).getBytes)
      Some(policyStatus)
    }
  }

  def findAll(): Unit = {
    sender ! Response(
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
    )
  }

  def findById(id: String): ResponseStatus =
    ResponseStatus(
      Try {
        val statusPath = s"${AppConstant.ContextPath}/$id"
        if (Option(curatorFramework.checkExists.forPath(statusPath)).isDefined)
          read[PolicyStatusModel](new String(curatorFramework.getData.forPath(statusPath)))
        else throw new ServingCoreException(
          ErrorModel.toString(new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy context with id $id.")))
      }
    )

  def deleteAll(): Unit = {
    sender ! ResponseDelete(Try({
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
    }))
  }

  def delete(id: String): ResponseDelete =
    ResponseDelete(
      Try {
        val statusPath = s"${AppConstant.ContextPath}/$id"
        if (Option(curatorFramework.checkExists.forPath(statusPath)).isDefined) {
          log.info(s">> Deleting context $id >")
          curatorFramework.delete().forPath(statusPath)
        } else throw new ServingCoreException(ErrorModel.toString(
          new ErrorModel(ErrorModel.CodeNotExistsPolicyWithId, s"No policy context with id $id.")))
      }
    )

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
}

object StatusActor {

  case class Update(policyStatus: PolicyStatusModel)

  case class Create(policyStatus: PolicyStatusModel)

  case class AddListener(name: String, callback: (PolicyStatusModel, NodeCache) => Unit)

  case class Delete(id: String)

  case object DeleteAll

  case object FindAll

  case class FindById(id: String)

  case class Response(policyStatus: Try[PoliciesStatusModel])

  case class ResponseStatus(policyStatus: Try[PolicyStatusModel])

  case class ResponseDelete(value: Try[Unit])

}