/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.dao

import java.util.UUID

import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.dao.TemplateDao
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.workflow.TemplateElement
import com.stratio.sparta.serving.core.utils.JdbcSlickConnection
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class TemplatePostgresDao extends TemplateDao {

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.db

  import profile.api._

  def findAllTemplates(): Future[Seq[TemplateElement]] = findAll()

  def findByType(templateType: String): Future[Seq[TemplateElement]] =
    for {
      templates <- filterByType(templateType)
    } yield templates


  def findByTypeAndId(templateType: String, id: String): Future[TemplateElement] =
    for {
      template <- findByTypeId(templateType, id)
    } yield template.getOrElse(
      throw new ServerException(s"Template type: $templateType and id: $id does not exist")
    )


  def findByTypeAndName(templateType: String, name: String): Future[TemplateElement] =
    for {
      template <- findByTypeName(templateType, name)
    } yield template.getOrElse(
      throw new ServerException(s"Template type: $templateType and name: $name does not exist")
    )

  def createTemplate(template: TemplateElement): Future[TemplateElement] =
    findByTypeName(template.templateType, template.name).flatMap { templateInPg =>
      if (templateInPg.nonEmpty)
        throw new ServerException(s"Unable to create template ${templateInPg.get.name} because it already exists")
      else createAndReturn(addSpartaVersion(addCreationDate(addId(template))))
    }

  //scalastyle:off
  def updateTemplate(template: TemplateElement): Future[TemplateElement] = {
    for {
      exists <- findByOtherTemplate(template)
    } yield {
      if (exists.isDefined)
        throw new ServerException(s"Cannot update template with id ${template.id.get}" +
          s" because a template of the same type and name ${template.name} already exists")
      else {
        //TODO traer los workflows que tengan al template y actualizarlos
        val templateToUpdate = addSpartaVersion(addUpdateDate(addId(template)))
        upsert(templateToUpdate)
        templateToUpdate
      }
    }
  }

  //TODO que hacer con los templates que esten dentro de workflows

  def deleteAllTemplates(): Future[Boolean] =
    for {
      templates <- findAll()
    } yield {
      templates.foreach { template =>
        log.debug(s"Deleting template ${template.name} with id ${template.id.get}")
        Try(deleteByID(template.id.get)) match {
          case Success(_) =>
            log.info(s"Template ${template.name} with id ${template.id.get} deleted")
          case Failure(e) =>
            throw e
        }
      }
      true
    }

  def deleteByType(templateType: String): Future[Boolean] =
    for {
      templates <- filterByType(templateType)
    } yield deleteYield(templates)

  def deleteByTypeAndId(templateType: String, id: String): Future[Boolean] =
    for {
      templates <- filterByTypeId(templateType, id)
    } yield deleteYield(templates)

  def deleteByTypeAndName(templateType: String, name: String): Future[Boolean] =
    for {
      templates <- filterByTypeName(templateType, name)
    } yield deleteYield(templates)


  /** PRIVATE METHODS */

  private[services] def deleteYield(templates: Seq[TemplateElement]) : Boolean = {
    templates.foreach { template =>
      log.debug(s"Deleting template ${template.name} with id ${template.id.get}")
      Try(deleteByID(template.id.get)) match {
        case Success(_) =>
          log.info(s"Template ${template.name} with id ${template.id.get} deleted")
        case Failure(e) =>
          throw e
      }
    }
    true
  }

  private[services] def filterByType(templateType: String): Future[Seq[TemplateElement]] =
    db.run(table.filter(t => t.templateType === templateType).result)

  private[services] def filterByTypeId(templateType: String, id: String): Future[Seq[TemplateElement]] =
    db.run(table.filter(t => t.templateType === templateType && t.id === id).result)

  private[services] def filterByTypeName(templateType: String, name: String): Future[Seq[TemplateElement]] =
    db.run(table.filter(t => t.templateType === templateType && t.name === name).result)

  private[services] def findByTypeId(templateType: String, id: String): Future[Option[TemplateElement]] =
    db.run(table.filter(t => t.templateType === templateType && t.id === id).result.headOption)

  private[services] def findByTypeName(templateType: String, name: String): Future[Option[TemplateElement]] =
    db.run(table.filter(t => t.templateType === templateType && t.name === name).result.headOption)

  private[services] def findByOtherTemplate(template: TemplateElement): Future[Option[TemplateElement]] =
    db.run(table.filter(t => t.templateType === template.templateType && t.name === template.name && t.id =!= template.id.get).result.headOption)

  private[services] def addId(template: TemplateElement): TemplateElement =
    template.id match {
      case None => template.copy(id = Some(UUID.randomUUID.toString))
      case Some(_) => template
    }

  private[services] def addCreationDate(template: TemplateElement): TemplateElement =
    template.creationDate match {
      case None => template.copy(creationDate = Some(new DateTime()))
      case Some(_) => template
    }

  private[services] def addUpdateDate(template: TemplateElement): TemplateElement =
    template.copy(lastUpdateDate = Some(new DateTime()))

  private[services] def addSpartaVersion(template: TemplateElement): TemplateElement =
    template.versionSparta match {
      case None => template.copy(versionSparta = Some(AppConstant.version))
      case Some(_) => template
    }
}
