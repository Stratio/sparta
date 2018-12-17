/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.Actor
import com.stratio.sparta.security._
import com.stratio.sparta.serving.api.actor.ParameterListActor._
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.parameters.{ParameterList, ParameterListAndContexts, ParameterListFromWorkflow}
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize

import scala.concurrent.Future
import scala.util.Try

class ParameterListActor()(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  private val parameterListPostgresDao = PostgresDaoFactory.parameterListPostgresDao
  private val ResourceType = "ParameterList"

  //scalastyle:off
  def receiveApiActions(action: Any): Any = action match {
    case FindAllParameterList(user) => findAll(user)
    case FindByIdParameterList(id, user) => findById(id, user)
    case FindByNameParameterList(name, user) => findByName(name, user)
    case FindByParentParameterList(parent, user) => findByParent(parent, user)
    case FindByParentWithContexts(parent, user) => findByParentWithContexts(parent, user)
    case DeleteAllParameterList(user) => deleteAll(user)
    case DeleteByNameParameterList(name, user) => deleteByName(name, user)
    case DeleteByIdParameterList(name, user) => deleteById(name, user)
    case CreateParameterList(parameterList, user) => create(parameterList, user)
    case CreateParameterListFromWorkflow(parameterListFromWorkflow, user) => createFromWorkflow(parameterListFromWorkflow, user)
    case UpdateParameterList(parameterList, user) => update(parameterList, user)
    case UpdateList(parameterLists, user) => updateList(parameterLists, user)
    case _ => log.info("Unrecognized message in Parameter list Actor")
  }

  //scalastyle:on

  def findAll(user: Option[LoggedUser]): Unit = {
    authorizeActionResultResources(user, Map(ResourceType -> View)) {
      parameterListPostgresDao.findAllParametersList()
    }
  }

  def findById(id: String, user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceType -> View)) {
      parameterListPostgresDao.findById(id)
    }

  def findByName(name: String, user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceType -> View)) {
      parameterListPostgresDao.findByName(name)
    }

  def findByParent(parent: String, user: Option[LoggedUser]): Unit = {
    authorizeActionResultResources(user, Map(ResourceType -> View)) {
      parameterListPostgresDao.findByParent(parent)
    }
  }

  def findByParentWithContexts(parent: String, user: Option[LoggedUser]): Unit = {
    authorizeActionResultResources(user, Map(ResourceType -> View)) {
      parameterListPostgresDao.findByParentWithContexts(parent)
    }
  }

  def create(parameterList: ParameterList, user: Option[LoggedUser]): Unit = {
    val authorizationId = parameterList.authorizationId
    authorizeActionsByResourceId(user, Map(ResourceType -> Create), authorizationId) {
      parameterListPostgresDao.createFromParameterList(parameterList)
    }
  }

  def createFromWorkflow(parameterListFromWorkflow: ParameterListFromWorkflow, user: Option[LoggedUser]): Unit = {
    val authorizationId = parameterListFromWorkflow.authorizationId
    authorizeActionsByResourceId(user, Map(ResourceType -> Create), authorizationId) {
      parameterListPostgresDao.createFromWorkflow(parameterListFromWorkflow)
    }
  }

  def update(parameterList: ParameterList, user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceType -> View, ResourceType -> Edit)
    val authorizationId = parameterList.authorizationId
    authorizeActionsByResourceId(user, actions, authorizationId) {
      parameterListPostgresDao.update(parameterList)
    }
  }

  def updateList(parameterLists: Seq[ParameterList], user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceType -> View, ResourceType -> Edit)
    val authorizationIds =  parameterLists.map(_.authorizationId)
    authorizeActionsByResourcesIds(user, actions, authorizationIds) {
      parameterListPostgresDao.updateList(parameterLists)
    }
  }

  def deleteAll(user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      parameterLists <- parameterListPostgresDao.findAllParametersList()
    } yield {
      val resourcesId = parameterLists.map(parameterLists => parameterLists.authorizationId)
      val actions = Map(ResourceType -> Delete)
      authorizeActionsByResourcesIds(user, actions, resourcesId, senderResponseTo) {
        parameterListPostgresDao.deleteAllParameterList()
      }
    }
  }

  def deleteByName(name: String, user: Option[LoggedUser]): Future[Unit] = {
    val senderResponseTo = Option(sender)
    for {
      parameterList <- parameterListPostgresDao.findByName(name)
    } yield {
      val resourceId = parameterList.authorizationId
      val actions = Map(ResourceType -> Delete)
      authorizeActionsByResourceId(user, actions, resourceId, senderResponseTo) {
        parameterListPostgresDao.deleteByName(name)
      }
    }
  }

  def deleteById(id: String, user: Option[LoggedUser]): Future[Unit] = {
    val senderResponseTo = sender
    for {
      parameterList <- parameterListPostgresDao.findById(id)
    } yield {
      val resourceId = parameterList.authorizationId
      val actions = Map(ResourceType -> Delete)
      authorizeActionsByResourceId(user, actions, resourceId, Option(senderResponseTo)) {
        parameterListPostgresDao.deleteById(id)
      }
    }
  }

}

object ParameterListActor {

  case class CreateParameterList(parameterList: ParameterList, user: Option[LoggedUser])

  case class CreateParameterListFromWorkflow(
                                              parameterListFromWorkflow: ParameterListFromWorkflow,
                                              user: Option[LoggedUser]
                                            )

  case class UpdateParameterList(parameterList: ParameterList, user: Option[LoggedUser])
  case class UpdateList(parameterList: Seq[ParameterList], user: Option[LoggedUser])

  case class FindAllParameterList(user: Option[LoggedUser])

  case class FindByIdParameterList(id: String, user: Option[LoggedUser])

  case class FindByNameParameterList(name: String, user: Option[LoggedUser])

  case class FindByParentParameterList(parent: String, user: Option[LoggedUser])

  case class FindByParentWithContexts(parent: String, user: Option[LoggedUser])

  case class DeleteAllParameterList(user: Option[LoggedUser])

  case class DeleteByNameParameterList(name: String, user: Option[LoggedUser])

  case class DeleteByIdParameterList(name: String, user: Option[LoggedUser])

  type ResponseParameterList = Try[ParameterList]

  type ResponseParameterListAndContexts = Try[ParameterListAndContexts]

  type ResponseParameterLists = Try[Seq[ParameterList]]

}

