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

package com.stratio.sparta.serving.api.actor

import akka.actor.Actor
import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.Group
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.services.GroupService
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework
import GroupActor._

import scala.util.Try

class GroupActor(val curatorFramework: CuratorFramework)
                (implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize with SpartaSerializer {

  private val groupService = new GroupService(curatorFramework)
  private val ResourceGroupType = "group"
  private val ResourceWorkflowType = "workflow"

  //scalastyle:off
  override def receive: Receive = {
    case CreateGroup(request, user) => createGroup(request, user)
    case UpdateGroup(request, user) => updateGroup(request, user)
    case FindGroup(name, user) => findGroup(name, user)
    case FindAllGroups(user) => findAllGroups(user)
    case DeleteGroup(name, user) => deleteGroup(name, user)
    case DeleteAllGroups(user) => deleteAllGroups(user)
    case Initialize => groupService.initialize()
    case _ => log.info("Unrecognized message in Group Actor")
  }

  //scalastyle:on

  def createGroup(request: Group, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceGroupType -> Create)) {
      groupService.create(request)
    }


  def updateGroup(request: Group, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceGroupType -> Edit)) {
      groupService.update(request)
    }

  def findGroup(name: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceGroupType -> View)) {
      groupService.find(name)
    }

  def findAllGroups(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceGroupType -> View)) {
      Try(groupService.findAll)
    }

  def deleteAllGroups(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceGroupType -> Delete, ResourceWorkflowType -> Delete)) {
      groupService.deleteAll()
    }

  def deleteGroup(name: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceGroupType -> Delete, ResourceWorkflowType -> Delete)) {
      groupService.delete(name)
    }
}

object GroupActor {

  case class UpdateGroup(request: Group, user: Option[LoggedUser])

  case class CreateGroup(request: Group, user: Option[LoggedUser])

  case class DeleteGroup(name: String, user: Option[LoggedUser])

  case class DeleteAllGroups(user: Option[LoggedUser])

  case class FindGroup(name: String, user: Option[LoggedUser])

  case class FindAllGroups(user: Option[LoggedUser])

  case object Initialize

  type Response = Try[Unit]

  type ResponseGroup = Try[Group]

}
