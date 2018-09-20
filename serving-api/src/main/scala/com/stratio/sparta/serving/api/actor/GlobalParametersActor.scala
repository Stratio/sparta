/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import scala.util.Try

import akka.actor.Actor

import com.stratio.sparta.security._
import com.stratio.sparta.serving.api.actor.GlobalParametersActor._
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.parameters.{GlobalParameters, ParameterVariable}
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, PostgresDaoFactory}

class GlobalParametersActor()(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize with SpartaSerializer {

  implicit val system = Option(context.system)
  private val globalParametersService = PostgresDaoFactory.globalParametersService
  private val ResourceType = "GlobalParameters"

  //scalastyle:off
  def receiveApiActions(action: Any): Any = action match {
    case CreateGlobalParameters(request, user) => createGlobalParameters(request, user)
    case CreateGlobalParametersVariable(request, user) => createGlobalParametersVariable(request, user)
    case UpdateGlobalParameters(request, user) => updateGlobalParameters(request, user)
    case UpdateGlobalParametersVariable(request, user) => updateGlobalParametersVariable(request, user)
    case FindGlobalParameters(user) => findGlobalParameters(user)
    case FindGlobalParametersVariable(name, user) => findGlobalParametersVariable(name, user)
    case DeleteGlobalParametersVariable(name, user) => deleteGlobalParametersVariable(name, user)
    case DeleteGlobalParameters(user) => deleteGlobalParameters(user)
    case _ => log.info("Unrecognized message in Global Parameters Actor")
  }

  //scalastyle:on

  //scalastyle:on

  def createGlobalParameters(request: GlobalParameters, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Create)) {
      globalParametersService.createGlobalParameters(request)
    }

  def createGlobalParametersVariable(request: ParameterVariable, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Create)) {
      globalParametersService.upsertParameterVariable(request)
    }

  def updateGlobalParameters(request: GlobalParameters, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Edit)) {
      globalParametersService.updateGlobalParameters(request)
    }

  def updateGlobalParametersVariable(request: ParameterVariable, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> Edit)) {
      globalParametersService.upsertParameterVariable(request)
    }

  def findGlobalParameters(user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> View)) {
      globalParametersService.find()
    }

  def findGlobalParametersVariable(name: String, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> View)) {
      globalParametersService.findGlobalParameterVariable(name)
    }

  def deleteGlobalParameters(user: Option[LoggedUser]): Unit = {
    val senderResponseTo = Option(sender)
    authorizeActions(user, Map(ResourceType -> Delete), senderResponseTo) {
      globalParametersService.deleteGlobalParameters()
    }
  }

  def deleteGlobalParametersVariable(name: String, user: Option[LoggedUser]): Unit = {
    val senderResponseTo = Option(sender)
    authorizeActions(user, Map(ResourceType -> Delete), senderResponseTo) {
      globalParametersService.deleteGlobalParameterVariable(name)
    }
  }
}

object GlobalParametersActor {

  case class UpdateGlobalParameters(request: GlobalParameters, user: Option[LoggedUser])

  case class UpdateGlobalParametersVariable(request: ParameterVariable, user: Option[LoggedUser])

  case class CreateGlobalParametersVariable(request: ParameterVariable, user: Option[LoggedUser])

  case class CreateGlobalParameters(request: GlobalParameters, user: Option[LoggedUser])

  case class DeleteGlobalParameters(user: Option[LoggedUser])

  case class FindGlobalParameters(user: Option[LoggedUser])

  case class FindGlobalParametersVariable(name: String, user: Option[LoggedUser])

  case class DeleteGlobalParametersVariable(name: String, user: Option[LoggedUser])

  type ResponseGlobalParameters = Try[GlobalParameters]

  type ResponseGlobalParametersVariable = Try[ParameterVariable]
}
