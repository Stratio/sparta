/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.Actor
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.actor.TemplateActor._
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.TemplateElement
import com.stratio.sparta.serving.core.services.TemplateService
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework

import scala.util.Try

class  TemplateActor(val curatorFramework: CuratorFramework)(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  private val templateService = new TemplateService(curatorFramework)
  private val ResourceType = "Template"

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
        if (secManager.authorize(userLogged.id, ResourceType, View))
          sender ! Left(Try(templateService.findAll))
        else
          sender ! Right(errorResponseAuthorization(userLogged.id, ResourceType))
      case (Some(_), None) => sender ! Right(errorNoUserFound(Seq(View)))
      case (None, _) => sender ! Left(Try(templateService.findAll))
    }

  def findByType(fragmentType: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseTemplates](user, Map(ResourceType -> View)) {
      Try(templateService.findByType(fragmentType))
    }

  def findByTypeAndId(fragmentType: String, id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseTemplate](user, Map(ResourceType -> View)) {
      Try(templateService.findByTypeAndId(fragmentType, id))
    }

  def findByTypeAndName(fragmentType: String, name: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseTemplate](user, Map(ResourceType -> View)) {
      Try(templateService.findByTypeAndName(fragmentType, name))
    }

  def create(fragment: TemplateElement, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[ResponseTemplate](user, Map(ResourceType -> Create)) {
      Try(templateService.create(fragment))
    }

  def update(fragment: TemplateElement, user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceType -> View, ResourceType -> Edit)
    securityActionAuthorizer[Response](user, actions) {
      Try(templateService.update(fragment))
    }
  }


  def deleteAll(user: Option[LoggedUser]): Unit =
    (secManagerOpt, user) match {
      case (Some(secManager), Some(userLogged)) =>
        if (secManager.authorize(userLogged.id, ResourceType, View) &&
          secManager.authorize(userLogged.id, ResourceType, Delete)
        )
          sender ! Left(Try(templateService.deleteAll()))
        else
          sender ! Right(errorResponseAuthorization(userLogged.id, ResourceType))
      case (Some(_), None) => sender ! Right(errorNoUserFound(Seq(Delete)))
      case (None, _) => sender ! Left(Try(templateService.deleteAll()))
    }

  def deleteByType(fragmentType: String, user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceType -> Delete, ResourceType -> View)
    securityActionAuthorizer[Response](user, actions) {
      Try(templateService.deleteByType(fragmentType))
    }
  }

  def deleteByTypeAndId(fragmentType: String, id: String, user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceType -> Delete, ResourceType -> View)
    securityActionAuthorizer[Response](user, actions) {
      Try(templateService.deleteByTypeAndId(fragmentType, id))
    }
  }

  def deleteByTypeAndName(fragmentType: String, name: String, user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceType -> Delete, ResourceType -> View)
    securityActionAuthorizer[Response](user, actions) {
      Try(templateService.deleteByTypeAndName(fragmentType, name))
    }
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