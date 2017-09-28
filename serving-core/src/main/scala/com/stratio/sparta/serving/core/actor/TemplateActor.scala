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
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.actor.TemplateActor._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper._
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel.{ErrorCodesMessages, UnknownError}
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.{TemplateElement, TemplateType}
import com.stratio.sparta.serving.core.services.TemplateService
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework
import spray.http.StatusCodes

import scala.util.Try

class TemplateActor(val curatorFramework: CuratorFramework)(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  //TODO change dyplon to new names: policy -> workflow
  private val templateService = new TemplateService(curatorFramework)
  private val PolicyResource = "policy"

  //scalastyle:off
  override def receive: Receive = {
    case FindAllTemplates(user) => findAll(user)
    case FindByType(fragmentType, user) => findByType(fragmentType, user)
    case FindByTypeAndId(fragmentType, id, user) => findByTypeAndId(fragmentType, id, user)
    case FindByTypeAndName(fragmentType, name, user) => findByTypeAndName(fragmentType, name.toLowerCase(), user)
    case DeleteAllTemplates(user) => deleteAll(user)
    case DeleteByType(fragmentType, user) => deleteByType(fragmentType, user)
    case DeleteByTypeAndId(fragmentType, id, user) => deleteByTypeAndId(fragmentType, id, user)
    case DeleteByTypeAndName(fragmentType, name, user) => deleteByTypeAndName(fragmentType, name, user)
    case CreateTemplate(fragment, user) => create(fragment, user)
    case Update(fragment, user) => update(fragment, user)
    case _ => log.info("Unrecognized message in Template Actor")
  }

  //scalastyle:on

  def findAll(user: Option[LoggedUser]): Unit =
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        if (secManager.authorize(userLogged.id, TemplateType.InputValue, View) &&
          secManager.authorize(userLogged.id, TemplateType.OutputValue, View))
          sender ! Left(Try(templateService.findAll)) // [T]
        else
          sender ! Right(errorResponseAuthorization(userLogged.id, PolicyResource))
      case (Some(_), None) => sender ! Right(errorNoUserFound(Seq(View)))
      case (None, _) => sender ! Left(Try(templateService.findAll))
    }

  def findByType(fragmentType: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseTemplates](user, Map(fragmentType -> View)) {
    Try(templateService.findByType(fragmentType))
  }

  def findByTypeAndId(fragmentType: String, id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseTemplate](user, Map(fragmentType -> View)) {
    Try(templateService.findByTypeAndId(fragmentType, id))
  }

  def findByTypeAndName(fragmentType: String, name: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseTemplate]( user, Map(fragmentType -> View)) {
      Try(templateService.findByTypeAndName(fragmentType, name)
      .getOrElse(errorTemplateNotFound(fragmentType, name)))
  }

  def create(fragment: TemplateElement, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseTemplate](user, Map(fragment.templateType -> Create)) {
    Try(templateService.create(fragment))
  }

  def update(fragment: TemplateElement, user: Option[LoggedUser]): Unit = {
    val actions = Map(fragment.templateType -> Edit, PolicyResource -> View, PolicyResource -> Edit)
    securityActionAuthorizer[Response](user, actions) {
      Try(templateService.update(fragment))
    }
  }



  def deleteAll(user: Option[LoggedUser]): Unit =
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        if (secManager.authorize(userLogged.id, TemplateType.InputValue, Delete) &&
          secManager.authorize(userLogged.id, TemplateType.OutputValue, Delete) &&
          secManager.authorize(userLogged.id, PolicyResource, View) &&
          secManager.authorize(userLogged.id, PolicyResource, Delete)
        )
          sender ! Left(Try(templateService.deleteAll()))
        else
          sender ! Right(errorResponseAuthorization(userLogged.id, PolicyResource))
      case (Some(_), None) => sender ! Right(errorNoUserFound(Seq(Delete)))
      case (None, _) => sender ! Left(Try(templateService.deleteAll()))
    }

  def deleteByType(fragmentType: String, user: Option[LoggedUser]): Unit = {
    val actions = Map(fragmentType -> Delete, PolicyResource -> Delete, PolicyResource -> View)
    securityActionAuthorizer[Response](user, actions) {
      Try(templateService.deleteByType(fragmentType))
    }
  }

  def deleteByTypeAndId(fragmentType: String, id: String, user: Option[LoggedUser]): Unit = {
    val actions = Map(fragmentType -> Delete, PolicyResource -> Delete, PolicyResource -> View)
    securityActionAuthorizer[Response](user, actions) {
      Try(templateService.deleteByTypeAndId(fragmentType, id))
    }
  }

  def deleteByTypeAndName(fragmentType: String, name: String, user: Option[LoggedUser]): Unit = {
    val actions = Map(fragmentType -> Delete, PolicyResource -> Delete, PolicyResource -> View)
    securityActionAuthorizer[Response](user, actions) {
      Try(templateService.deleteByTypeAndName(fragmentType, name))
    }
  }

  //PRIVATE METHODS

  private def errorTemplateNotFound(fragmentType: String, name: String): Nothing = {
    throw new ServerException(ErrorModel.toString(new ErrorModel(
      StatusCodes.OK.intValue,
      ErrorModel.TemplateServiceNotFound,
      ErrorCodesMessages.getOrElse(ErrorModel.TemplateServiceNotFound, UnknownError),
      Option(s"No fragment of type $fragmentType with name $name"),
      None
    )))
  }
}

object TemplateActor {

  case class CreateTemplate(fragment: TemplateElement, user: Option[LoggedUser])

  case class Update(fragment: TemplateElement, user: Option[LoggedUser])

  case class FindAllTemplates(user: Option[LoggedUser])

  case class FindByType(templateType: String, user: Option[LoggedUser])

  case class FindByTypeAndId(templateType: String, id: String, user: Option[LoggedUser])

  case class FindByTypeAndName(templateType: String, name: String, user: Option[LoggedUser])

  case class DeleteAllTemplates(user: Option[LoggedUser])

  case class DeleteByType(templateType: String, user: Option[LoggedUser])

  case class DeleteByTypeAndId(templateType: String, id: String, user: Option[LoggedUser])

  case class DeleteByTypeAndName(templateType: String, name: String, user: Option[LoggedUser])

  type ResponseTemplate = Try[TemplateElement]

  type ResponseTemplates = Try[Seq[TemplateElement]]

  type Response = Try[Unit]

}