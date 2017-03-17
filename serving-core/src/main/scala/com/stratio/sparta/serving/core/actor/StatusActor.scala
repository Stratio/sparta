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
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper._
import com.stratio.sparta.serving.core.actor.StatusActor._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.policy.PolicyStatusModel
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, ClusterListenerUtils, PolicyStatusUtils}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.NodeCache

import scala.util.Try

class StatusActor(val curatorFramework: CuratorFramework,val secManagerOpt: Option[SpartaSecurityManager]) extends Actor
  with PolicyStatusUtils with ClusterListenerUtils with ActionUserAuthorize{

  val ResourceType = "context"

  //scalastyle:off cyclomatic.complexity
  override def receive: Receive = {
    case CreateStatus(policyStatus, user) => createStatus(policyStatus, user)
    case Update(policyStatus, user) => update(policyStatus, user)
    case ClearLastError(id) => sender ! clearLastError(id)
    case FindAll(user) => findAll(user)
    case FindById(id, user) => findById(id, user)
    case DeleteAll(user) => deleteAll(user)
    case AddListener(name, callback) => addListener(name, callback)
    case AddClusterListeners => addClusterListeners(findAllStatuses(), context)
    case DeleteStatus(id, user) => sender ! ResponseDelete(deleteStatus(id))
    case _ => log.info("Unrecognized message in Policy Status Actor")
  }

  def createStatus(policyStatus: PolicyStatusModel, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseStatus(createStatus(policyStatus))

    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> Create), callback)
  }

  def update(policyStatus: PolicyStatusModel, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseStatus(updateStatus(policyStatus))

    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> Edit), callback)
  }

  def findAll(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> View), findAllStatuses)


  def findById(id: String, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseStatus(findStatusById(id))
    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> View), callback)
  }

  def deleteAll(user: Option[LoggedUser]): Unit = {
    def callback() = ResponseDelete(deleteAllStatuses())
    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> Delete), callback)
  }

  def deleteStatus(id: String, user: Option[LoggedUser]): Unit = {
   def callback () = ResponseDelete(deleteStatus(id))

    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> Delete), callback)
  }

  //scalastyle:on cyclomatic.complexity
}

object StatusActor {

  case class Update(policyStatus: PolicyStatusModel, user: Option[LoggedUser])

  case class CreateStatus(policyStatus: PolicyStatusModel, user: Option[LoggedUser])

  case class AddListener(name: String, callback: (PolicyStatusModel, NodeCache) => Unit)

  case class DeleteStatus(id: String, user: Option[LoggedUser])

  case class DeleteAll(user: Option[LoggedUser])

  case class FindAll(user: Option[LoggedUser])

  case class FindById(id: String, user: Option[LoggedUser])

  case class ResponseStatus(policyStatus: Try[PolicyStatusModel])

  case class ResponseDelete(value: Try[_])

  case class ClearLastError(id: String)

  case object AddClusterListeners

}