/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import akka.actor.Actor

import com.stratio.sparta.security._
import com.stratio.sparta.serving.api.actor.TemplateActor._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.TemplateElement
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, PostgresDaoFactory}
import scala.concurrent.Future
import scala.util.Try

class TemplateActor()(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  private val templatePgService = PostgresDaoFactory.templatePgService

  //TODO migration service
  //private val migrationService = new CassiopeiaMigrationService(curatorFramework)

  private val ResourceType = "Template"

  //scalastyle:off
  def receiveApiActions(action: Any): Any = action match {
    case FindAllTemplates(user) => findAll(user)
    case FindByType(templateType, user) => findByType(templateType, user)
    case FindById(templateType, user) => findById(templateType, user)
    case FindByTypeAndId(templateType, id, user) => findByTypeAndId(templateType, id, user)
    case FindByTypeAndName(templateType, name, user) => findByTypeAndName(templateType, name, user)
    case DeleteAllTemplates(user) => deleteAll(user)
    case DeleteByType(templateType, user) => deleteByType(templateType, user)
    case DeleteById(id, user) => deleteById(id, user)
    case DeleteByTypeAndId(templateType, id, user) => deleteByTypeAndId(templateType, id, user)
    case DeleteByTypeAndName(templateType, name, user) => deleteByTypeAndName(templateType, name, user)
    case CreateTemplate(fragment, user) => create(fragment, user)
    case Update(fragment, user) => update(fragment, user)
    //case Migrate(fragment, user) => migrateTemplateFromCassiopeia(fragment, user)
    case _ => log.info("Unrecognized message in Template Actor")
  }

  //scalastyle:on

  def findAll(user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceType -> View)) {
      templatePgService.findAllTemplates()
    }

  def findById(id: String, user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceType -> View)) {
      templatePgService.findById(id)
    }

  def findByType(templateType: String, user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceType -> View)) {
      templatePgService.findByType(templateType)
    }

  def findByTypeAndId(templateType: String, id: String, user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceType -> View)) {
      templatePgService.findByTypeAndId(templateType, id)
    }

  def findByTypeAndName(templateType: String, name: String, user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceType -> View)) {
      templatePgService.findByTypeAndName(templateType, name)
    }

  def create(template: TemplateElement, user: Option[LoggedUser]): Unit = {
    val authorizationId = template.authorizationId
    authorizeActionsByResourceId(user, Map(ResourceType -> Create), authorizationId) {
      templatePgService.createTemplate(template)
    }
  }

  def update(template: TemplateElement, user: Option[LoggedUser]): Unit = {
    val actions = Map(ResourceType -> View, ResourceType -> Edit)
    val authorizationId = template.authorizationId
    authorizeActionsByResourceId(user, actions, authorizationId) {
      templatePgService.updateTemplate(template)
    }
  }

  def deleteAll(user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      templates <- templatePgService.findAll()
    } yield {
      val resourcesId = templates.map(_.authorizationId)
      authorizeActionsByResourcesIds(user, Map(ResourceType -> Delete), resourcesId, senderResponseTo) {
        templatePgService.deleteAllTemplates()
      }
    }
  }

  def deleteById(id: String, user: Option[LoggedUser]): Future[Unit] = {
    val senderResponseTo = sender
    for {
      template <- templatePgService.findById(id)
    } yield {
      val resourceId = template.authorizationId
      val actions = Map(ResourceType -> Delete)
      authorizeActionsByResourceId(user, actions, resourceId, Option(senderResponseTo)) {
        templatePgService.deleteById(id)
      }
    }
  }

  def deleteByType(templateType: String, user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      templates <- templatePgService.findByType(templateType)
    } yield {
      val resourcesId = templates.map(_.authorizationId)
      authorizeActionsByResourcesIds(user, Map(ResourceType -> Delete), resourcesId, senderResponseTo) {
        templatePgService.deleteByType(templateType)
      }
    }
  }

  def deleteByTypeAndId(templateType: String, id: String, user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      templates <- templatePgService.findByTypeAndId(templateType, id)
    } yield {
      val resourceId = templates.authorizationId
      authorizeActionsByResourceId(user, Map(ResourceType -> Delete), resourceId, senderResponseTo) {
        templatePgService.deleteByTypeAndId(templateType, id)
      }
    }
  }

  def deleteByTypeAndName(templateType: String, name: String, user: Option[LoggedUser]): Future[Any] = {
    val senderResponseTo = Option(sender)
    for {
      templates <- templatePgService.findByTypeAndName(templateType, name)
    } yield {
      val resourceId = templates.authorizationId
      authorizeActionsByResourceId(user, Map(ResourceType -> Delete), resourceId, senderResponseTo) {
        templatePgService.deleteByTypeAndName(templateType, name)
      }
    }
  }

  /*def migrateTemplateFromCassiopeia(template: TemplateElement, user: Option[LoggedUser]): Unit =
    authorizeActionResultResources[Try[TemplateElement]](user, Map(ResourceType -> Edit)) {
      Try(migrationService.migrateTemplateFromCassiopeia(template))
    }*/
}

object TemplateActor {

  case class CreateTemplate(template: TemplateElement, user: Option[LoggedUser])

  case class Update(template: TemplateElement, user: Option[LoggedUser])

  case class FindAllTemplates(user: Option[LoggedUser])

  case class FindByType(templateType: String, user: Option[LoggedUser])

  case class FindById(id: String, user: Option[LoggedUser])

  case class FindByTypeAndId(templateType: String, id: String, user: Option[LoggedUser])

  case class FindByTypeAndName(templateType: String, name: String, user: Option[LoggedUser])

  case class DeleteAllTemplates(user: Option[LoggedUser])

  case class DeleteByType(templateType: String, user: Option[LoggedUser])

  case class DeleteById(id: String, user: Option[LoggedUser])

  case class DeleteByTypeAndId(templateType: String, id: String, user: Option[LoggedUser])

  case class DeleteByTypeAndName(templateType: String, name: String, user: Option[LoggedUser])

  case class Migrate(template: TemplateElement, user: Option[LoggedUser])

  type ResponseTemplate = Try[TemplateElement]

  type ResponseTemplates = Try[Seq[TemplateElement]]

}