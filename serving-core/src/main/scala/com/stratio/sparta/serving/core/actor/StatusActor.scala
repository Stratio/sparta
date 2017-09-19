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
import com.stratio.sparta.serving.core.actor.StatusActor._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import com.stratio.sparta.serving.core.services.{ListenerService, WorkflowStatusService}
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.NodeCache

import scala.util.Try

class StatusActor(val curatorFramework: CuratorFramework,val secManagerOpt: Option[SpartaSecurityManager]) extends Actor
  with ActionUserAuthorize{

  private val ResourceType = "context"
  private val statusService = new WorkflowStatusService(curatorFramework)
  private val listenerService = new ListenerService(curatorFramework)

  //scalastyle:off cyclomatic.complexity
  override def receive: Receive = {
    case CreateStatus(policyStatus, user) => createStatus(policyStatus, user)
    case Update(policyStatus, user) => update(policyStatus, user)
    case ClearLastError(id) => sender ! statusService.clearLastError(id)
    case FindAll(user) => findAll(user)
    case FindById(id, user) => findById(id, user)
    case DeleteAll(user) => deleteAll(user)
    case AddListener(name, callback) => listenerService.addWorkflowStatusListener(name, callback)
    case AddClusterListeners => listenerService.addClusterListeners(statusService.findAll(), context)
    case DeleteStatus(id, user) => deleteStatus(id, user)

    case _ => log.info("Unrecognized message in Status Actor")
  }

  def createStatus(policyStatus: WorkflowStatus, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseStatus(statusService.create(policyStatus))

    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> Create), callback)
  }

  def update(policyStatus: WorkflowStatus, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseStatus(statusService.update(policyStatus))

    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> Edit), callback)
  }

  def findAll(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> View), statusService.findAll)


  def findById(id: String, user: Option[LoggedUser]): Unit = {
    def callback() = ResponseStatus(statusService.findById(id))
    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> View), callback)
  }

  def deleteAll(user: Option[LoggedUser]): Unit = {
    def callback() = ResponseDelete(statusService.deleteAll())
    securityActionAuthorizer[ResponseDelete](secManagerOpt, user, Map(ResourceType -> Delete), callback)
  }

  def deleteStatus(id: String, user: Option[LoggedUser]): Unit = {
   def callback () = ResponseDelete(statusService.delete(id))

    securityActionAuthorizer[ResponseDelete](secManagerOpt, user, Map(ResourceType -> Delete), callback)
  }

  //scalastyle:on cyclomatic.complexity
}

object StatusActor {

  case class Update(policyStatus: WorkflowStatus, user: Option[LoggedUser])

  case class CreateStatus(policyStatus: WorkflowStatus, user: Option[LoggedUser])

  case class AddListener(name: String, callback: (WorkflowStatus, NodeCache) => Unit)

  case class DeleteStatus(id: String, user: Option[LoggedUser])

  case class DeleteAll(user: Option[LoggedUser])

  case class FindAll(user: Option[LoggedUser])

  case class FindById(id: String, user: Option[LoggedUser])

  case class ResponseStatus(policyStatus: Try[WorkflowStatus])

  case class ResponseDelete(value: Try[_])

  case class ClearLastError(id: String)

  case object AddClusterListeners

}