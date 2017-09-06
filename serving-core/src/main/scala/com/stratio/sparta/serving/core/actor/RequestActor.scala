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

import akka.actor.Actor
import com.stratio.sparta.security.{Create, Delete, Edit, SpartaSecurityManager, View}
import com.stratio.sparta.serving.core.actor.RequestActor._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecution
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, RequestUtils}
import org.apache.curator.framework.CuratorFramework

class RequestActor(val curatorFramework: CuratorFramework,val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with RequestUtils with ActionUserAuthorize{

  val ResourceType = "context"

  override def receive: Receive = {
    case CreateExecution(request, user) => createExecution(request, user)
    case Update(request, user) => updateExecution(request, user)
    case FindAll(user) => findAllExecutions(user)
    case FindById(id, user) => findExecutionById(id, user)
    case DeleteAll(user) => deleteAllExecutions(user)
    case DeleteExecution(id, user) => deleteExecution(id, user)
    case _ => log.info("Unrecognized message in Policy Request Actor")
  }

  def createExecution(request: WorkflowExecution, user: Option[LoggedUser]): Unit = {
    def callback() = createRequest(request)

    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> Create), callback)
  }

  def updateExecution(request: WorkflowExecution, user: Option[LoggedUser]): Unit = {
    def callback() = updateRequest(request)

    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> Edit), callback)
  }

  def findAllExecutions(user: Option[LoggedUser]): Unit = {
    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> View), findAllRequests)
  }

  def findExecutionById(id: String, user: Option[LoggedUser]): Unit = {
    def callback() = findRequestById(id)

    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> View), callback)
  }

  def deleteAllExecutions(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> Delete), deleteAllRequests)


  def deleteExecution(id:String, user: Option[LoggedUser]): Unit = {
    def callback() = deleteRequest(id)
    securityActionAuthorizer(secManagerOpt, user, Map(ResourceType -> Delete), callback)
  }

}

object RequestActor {

  case class Update(request: WorkflowExecution, user: Option[LoggedUser])

  case class CreateExecution(request: WorkflowExecution, user: Option[LoggedUser])

  case class DeleteExecution(id: String, user: Option[LoggedUser])

  case class DeleteAll(user: Option[LoggedUser])

  case class FindAll(user: Option[LoggedUser])

  case class FindById(id: String, user: Option[LoggedUser])
}
