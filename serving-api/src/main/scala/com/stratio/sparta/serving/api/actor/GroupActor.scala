/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, ActorRef}
import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.Group
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.services.GroupService
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework
import GroupActor._
import com.stratio.sparta.serving.core.actor.GroupInMemoryApi._

import scala.util.Try

class GroupActor(val curatorFramework: CuratorFramework, inMemoryApiGroup: ActorRef)
                (implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize with SpartaSerializer {

  private val groupService = new GroupService(curatorFramework)
  private val ResourceGroupType = "group"
  private val ResourceWorkflowType = "workflow"

  override def receive: Receive = {
    case CreateGroup(request, user) => createGroup(request, user)
    case UpdateGroup(request, user) => updateGroup(request, user)
    case FindAllGroups(user) => findAllGroups(user)
    case FindGroupByID(id, user) => findGroupByID(id, user)
    case FindGroupByName(name, user) => findGroupByName(name, user)
    case DeleteAllGroups(user) => deleteAllGroups(user)
    case DeleteGroupByID(id, user) => deleteGroupByID(id, user)
    case DeleteGroupByName(name, user) => deleteGroupByName(name, user)
    case _ => log.info("Unrecognized message in Group Actor")
  }

  def createGroup(request: Group, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceGroupType -> Create)) {
      groupService.create(request)
    }


  def updateGroup(request: Group, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceGroupType -> Edit, ResourceWorkflowType -> Edit)) {
      groupService.update(request)
    }

  def findGroupByName(name: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(
      user,
      Map(ResourceGroupType -> View),
      Option(inMemoryApiGroup)
    ) {
      FindMemoryGroupByName(name)
    }

  def findGroupByID(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(
      user,
      Map(ResourceGroupType -> View),
      Option(inMemoryApiGroup)
    ) {
      FindMemoryGroup(id)
    }

  def findAllGroups(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(
      user,
      Map(ResourceGroupType -> View),
      Option(inMemoryApiGroup)
    ) {
      FindAllMemoryGroup
    }

  def deleteAllGroups(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceGroupType -> Delete, ResourceWorkflowType -> Delete)) {
      groupService.deleteAll()
    }

  def deleteGroupByName(name: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceGroupType -> Delete, ResourceWorkflowType -> Delete)) {
      groupService.deleteByName(name)
    }

  def deleteGroupByID(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceGroupType -> Delete, ResourceWorkflowType -> Delete)) {
      groupService.deleteById(id)
    }
}

object GroupActor {

  case class UpdateGroup(request: Group, user: Option[LoggedUser])

  case class CreateGroup(request: Group, user: Option[LoggedUser])

  case class DeleteAllGroups(user: Option[LoggedUser])

  case class DeleteGroupByID(id: String, user: Option[LoggedUser])

  case class DeleteGroupByName(name: String, user: Option[LoggedUser])

  case class FindAllGroups(user: Option[LoggedUser])

  case class FindGroupByID(id: String, user: Option[LoggedUser])

  case class FindGroupByName(name: String, user: Option[LoggedUser])

  type Response = Try[Unit]

  type ResponseGroup = Try[Group]

}
