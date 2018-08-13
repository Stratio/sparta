/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.{Actor, ActorRef}
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.actor.ParameterListActor._
import com.stratio.sparta.serving.core.actor.ParameterListInMemoryApi._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.parameters.{ParameterList, ParameterListFromWorkflow}
import com.stratio.sparta.serving.core.services.ParameterListService
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework

import scala.util.Try

class ParameterListActor(
                          val curatorFramework: CuratorFramework,
                          inMemoryParameterListApi: ActorRef
                        )(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  private val parameterListService = new ParameterListService(curatorFramework)
  private val ResourceType = "ParameterList"

  //scalastyle:off
  def receiveApiActions(action : Any): Unit = action match {
    case FindAllParameterList(user) => findAll(user)
    case FindByIdParameterList(id, user) => findById(id, user)
    case FindByNameParameterList(name, user) => findByName(name, user)
    case FindByParentParameterList(parent, user) => findByParent(parent, user)
    case DeleteAllParameterList(user) => deleteAll(user)
    case DeleteByNameParameterList(name, user) => deleteByName(name, user)
    case CreateParameterList(parameterList, user) => create(parameterList, user)
    case CreateParameterListFromWorkflow(parameterListFromWorkflow, user) => createFromWorkflow(parameterListFromWorkflow, user)
    case UpdateParameterList(parameterList, user) => update(parameterList, user)
    case _ => log.info("Unrecognized message in Parameter list Actor")
  }

  //scalastyle:on

  def findAll(user: Option[LoggedUser]): Unit =
    filterResultsWithAuthorization(
      user,
      Map(ResourceType -> View),
      Option(inMemoryParameterListApi)
    ) {
      FindAllMemoryParameterList
    }

  def findById(id: String, user: Option[LoggedUser]): Unit =
    authorizeResultByResourceId(user, Map(ResourceType -> View), Option(inMemoryParameterListApi)) {
      FindByIdMemoryParameterList(id)
    }

  def findByName(name: String, user: Option[LoggedUser]): Unit =
    authorizeResultByResourceId(user, Map(ResourceType -> View), Option(inMemoryParameterListApi)) {
      FindByNameMemoryParameterList(name)
    }

  def findByParent(parent: String, user: Option[LoggedUser]): Unit = {
    filterResultsWithAuthorization(user, Map(ResourceType -> View), Option(inMemoryParameterListApi)) {
      FindByParentMemoryParameterList(parent)
    }
  }

  def create(parameterList: ParameterList, user: Option[LoggedUser]): Unit = {
    val authorizationId = parameterList.authorizationId
    authorizeActionsByResourceId(user, Map(ResourceType -> Create), authorizationId) {
      parameterListService.create(parameterList)
    }
  }

  def createFromWorkflow(parameterListFromWorkflow: ParameterListFromWorkflow, user: Option[LoggedUser]): Unit = {
    val authorizationId = parameterListFromWorkflow.authorizationId
    authorizeActionsByResourceId(user, Map(ResourceType -> Create), authorizationId) {
      parameterListService.createFromWorkflow(parameterListFromWorkflow)
    }
  }

  def update(parameterList: ParameterList, user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceType -> View, ResourceType -> Edit)
    val authorizationId = parameterList.authorizationId
    authorizeActionsByResourceId(user, actions, authorizationId) {
      parameterListService.update(parameterList)
    }
  }

  def deleteAll(user: Option[LoggedUser]): Unit = {
    val resourcesId = parameterListService.findAll()
      .map(parameterLists => parameterLists.map(_.authorizationId))
      .getOrElse(Seq.empty)
    val actions = Map(ResourceType -> Delete)
    authorizeActionsByResourcesIds[Response](user, actions, resourcesId) {
      parameterListService.deleteAll()
    }
  }

  def deleteByName(name: String, user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceType -> Delete)
    val resourceId = parameterListService.findByName(name).map(_.authorizationId).getOrElse("N/A")
    authorizeActionsByResourceId[Response](user, actions, resourceId) {
      parameterListService.deleteByName(name)
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

  case class FindAllParameterList(user: Option[LoggedUser])

  case class FindByIdParameterList(id: String, user: Option[LoggedUser])

  case class FindByNameParameterList(name: String, user: Option[LoggedUser])

  case class FindByParentParameterList(parent: String, user: Option[LoggedUser])

  case class DeleteAllParameterList(user: Option[LoggedUser])

  case class DeleteByNameParameterList(name: String, user: Option[LoggedUser])

  type ResponseParameterList = Try[ParameterList]

  type ResponseParameterLists = Try[Seq[ParameterList]]

  type Response = Try[Unit]

}

