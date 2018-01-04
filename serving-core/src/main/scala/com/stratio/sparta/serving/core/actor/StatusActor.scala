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

import scala.util.Try

class StatusActor(curatorFramework: CuratorFramework, statusListenerActor: ActorRef
                 )(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  private val ResourceType = "status"
  private val statusService = new WorkflowStatusService(curatorFramework)
  private val listenerService = new ListenerService(curatorFramework, statusListenerActor)

  //scalastyle:off cyclomatic.complexity
  override def receive: Receive = {
    case CreateStatus(policyStatus, user) => createStatus(policyStatus, user)
    case Update(policyStatus, user) => update(policyStatus, user)
    case ClearLastError(id) => sender ! statusService.clearLastError(id)
    case FindAll(user) => findAll(user)
    case FindById(id, user) => findById(id, user)
    case DeleteAll(user) => deleteAll(user)
    case AddClusterListeners => listenerService.addClusterListeners(statusService.findAll(), context)
    case DeleteStatus(id, user) => deleteStatus(id, user)
    case _ => log.info("Unrecognized message in Status Actor")
  }

  //scalastyle:on cyclomatic.complexity

  def createStatus(policyStatus: WorkflowStatus, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceType -> Create)) {
      statusService.create(policyStatus)
    }

  def update(policyStatus: WorkflowStatus, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceType -> Edit)) {
      statusService.update(policyStatus)
    }


  def findAll(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceType -> View)) {
      statusService.findAll()
    }


  def findById(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceType -> View)) {
      statusService.findById(id)
    }

  def deleteAll(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[Response](user, Map(ResourceType -> Delete)) {
      statusService.deleteAll()
    }

  def deleteStatus(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[Response](user, Map(ResourceType -> Delete)) {
      statusService.delete(id)
    }
}

object StatusActor {

  case class Update(policyStatus: WorkflowStatus, user: Option[LoggedUser])

  case class CreateStatus(policyStatus: WorkflowStatus, user: Option[LoggedUser])

  case class DeleteStatus(id: String, user: Option[LoggedUser])

  case class DeleteAll(user: Option[LoggedUser])

  case class FindAll(user: Option[LoggedUser])

  case class FindById(id: String, user: Option[LoggedUser])

  case class ClearLastError(id: String)

  case object AddClusterListeners

  type Response = Try[Unit]

  type ResponseStatuses = Try[Seq[WorkflowStatus]]

  type ResponseStatus = Try[WorkflowStatus]

}