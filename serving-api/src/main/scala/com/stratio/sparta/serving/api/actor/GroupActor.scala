/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor


import scala.concurrent.Future
import scala.util.Try

import akka.actor.Actor

import com.stratio.sparta.security.{SpartaSecurityManager, _}
import com.stratio.sparta.serving.api.actor.GroupActor._
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.Group
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, PostgresDaoFactory}

class GroupActor()(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize with SpartaSerializer {

  private val groupPgService = PostgresDaoFactory.groupPgService
  private val ResourceGroupType = "Groups"


  def receiveApiActions(action: Any): Any = action match {
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
    authorizeActionsByResourceId(user, Map(ResourceGroupType -> Create), request.authorizationId) {
      groupPgService.createFromGroup(request)
    }

  def updateGroup(request: Group, user: Option[LoggedUser]): Unit =
    authorizeActionsByResourceId(user, Map(ResourceGroupType -> Edit), request.authorizationId) {
      groupPgService.update(request)
    }

  def findGroupByName(name: String, user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceGroupType -> View)) {
      groupPgService.findGroupByName(name)
    }

  def findGroupByID(id: String, user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceGroupType -> View)) {
      groupPgService.findGroupById(id)
    }

  def findAllGroups(user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceGroupType -> View)) {
      groupPgService.findAllGroups()
    }

  def deleteAllGroups(user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      groups <- groupPgService.findAllGroups()
    } yield {
      val resourcesId = groups.map(_.authorizationId)
      authorizeActionsByResourcesIds(user, Map(ResourceGroupType -> Delete), resourcesId, senderResponseTo) {
        groupPgService.deleteAllGroups()
      }
    }
  }

  def deleteGroupByName(name: String, user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      group <- groupPgService.findGroupByName(name)
    } yield {
      val resourcesId = Seq(group.authorizationId)
      authorizeActionsByResourcesIds(user, Map(ResourceGroupType -> Delete), resourcesId, senderResponseTo) {
        groupPgService.deleteByName(name)
      }
    }
  }

  //scala style:off
  def deleteGroupByID(id: String, user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      group <- groupPgService.findGroupById(id)
    } yield {
      val resourcesId = Seq(group.authorizationId)
      authorizeActionsByResourcesIds(user, Map(ResourceGroupType -> Delete), resourcesId, senderResponseTo) {
        groupPgService.deleteById(id)
      }
    }
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

  type ResponseGroup = Try[Group]

  type ResponseGroups = Try[Seq[Group]]
}
