/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.dao

import java.util.UUID

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.dao.TemplateDao
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.workflow.{NodeTemplateInfo, TemplateElement, Workflow}
import com.stratio.sparta.serving.core.utils.{JdbcSlickConnection, PostgresDaoFactory}

class TemplatePostgresDao extends TemplateDao {

  override val profile = PostgresProfile
  override val db = JdbcSlickConnection.getDatabase

  import profile.api._

  lazy val workflowService = PostgresDaoFactory.workflowPgService

  def findAllTemplates(): Future[Seq[TemplateElement]] = findAll()

  def findById(id: String): Future[TemplateElement] = findByIdHead(id)

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
  def updateTemplate(template: TemplateElement): Future[Unit] = {
    val newTemplate = addSpartaVersion(addUpdateDate(addId(template)))
    val id = newTemplate.id.getOrElse(
      throw new ServerException(s"No template by id ${newTemplate.id}"))

    findById(id).flatMap { _ =>
      val workflowTemplateActions = for {
        workflowsActions <- updateWorkflowsTemplate(newTemplate)
      } yield workflowsActions

      workflowTemplateActions.flatMap { actions =>
        val actionsToExecute = actions :+ upsertAction(newTemplate)
        db.run(txHandler(DBIO.seq(actionsToExecute: _*).transactionally)).map(_ =>
          if (cacheEnabled)
            workflowService.updateFromCacheByTemplate(template)
        )
      }
    }
  }

  def deleteAllTemplates(): Future[Boolean] =
    for {
      templates <- findAll()
      result <- deleteYield(templates)
    } yield result

  def deleteById(id: String): Future[Boolean] =
    for {
      template <- findById(id)
      result <- deleteYield(Seq(template))
    } yield result

  def deleteByType(templateType: String): Future[Boolean] =
    for {
      templates <- filterByType(templateType)
      result <- deleteYield(templates)
    } yield result

  def deleteByTypeAndId(templateType: String, id: String): Future[Boolean] =
    for {
      templates <- filterByTypeId(templateType, id)
      result <- deleteYield(templates)
    } yield result

  def deleteByTypeAndName(templateType: String, name: String): Future[Boolean] =
    for {
      templates <- filterByTypeName(templateType, name)
      result <- deleteYield(templates)
    } yield result

  /** PRIVATE METHODS */

  private[services] def deleteYield(templates: Seq[TemplateElement]): Future[Boolean] = {
    val updateDeleteActions = templates.map { template =>
      val workflowTemplateActions = for {
        workflowsActions <- removeWorkflowsTemplate(template)
      } yield workflowsActions

      workflowTemplateActions.map { actions =>
        actions :+ deleteByIDAction(template.id.get)
      }
    }

    Future.sequence(updateDeleteActions).map { actionsSequence =>
      val actions = actionsSequence.flatten
      Try(db.run(txHandler(DBIO.seq(actions: _*).transactionally))) match {
        case Success(_) =>
          log.info(s"Templates ${templates.map(_.name).mkString(",")} deleted")
          if (cacheEnabled)
            templates.foreach(template => workflowService.deleteFromCacheByTemplate(template))
          true
        case Failure(e) =>
          throw e
      }
    }
  }

  private[services] def findByIdHead(id: String): Future[TemplateElement] =
    for {
      templates <- db.run(filterById(id).result)
    } yield {
      if (templates.nonEmpty)
        templates.head
      else throw new ServerException(s"No template found by id $id")
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

  private[services] def updateWorkflowsTemplate(templateElement: TemplateElement) = {
    workflowService.findAll().map { workflows =>
      val workflowsToUpdate = replaceWorkflowsWithTemplate(templateElement, workflows)
      workflowsToUpdate.map(workflow => workflowService.upsertAction(workflow))
    }
  }

  private[services] def removeWorkflowsTemplate(templateElement: TemplateElement) = {
    workflowService.findAll().map { workflows =>
      val workflowsToUpdate = updateWorkflowsWithRemovedTemplate(templateElement, workflows)
      workflowsToUpdate.map(workflow => workflowService.upsertAction(workflow))
    }
  }

  private[services] def replaceWorkflowsWithTemplate(
                                                      templateElement: TemplateElement,
                                                      workflows: Seq[Workflow]
                                                    ): Seq[Workflow] = {
    workflows.flatMap { workflow =>
      val nodesWithTemplate = workflow.pipelineGraph.nodes.filter { node =>
        node.nodeTemplate.isDefined && node.nodeTemplate.get.id == templateElement.id.get
      }
      if (nodesWithTemplate.nonEmpty) {
        val newNodes = workflow.pipelineGraph.nodes.map { node =>
          if (node.nodeTemplate.isDefined && node.nodeTemplate.get.id == templateElement.id.get)
            node.copy(nodeTemplate = Option(NodeTemplateInfo(name = templateElement.name, id = templateElement.id.get)))
          else node
        }
        Option(WorkflowPostgresDao.addUpdateDate(workflow.copy(pipelineGraph = workflow.pipelineGraph.copy(nodes = newNodes))))
      } else None
    }
  }

  private[services] def updateWorkflowsWithRemovedTemplate(
                                                            templateElement: TemplateElement,
                                                            workflows: Seq[Workflow]
                                                          ): Seq[Workflow] = {
    workflows.flatMap { workflow =>
      val nodesWithTemplate = workflow.pipelineGraph.nodes.filter { node =>
        node.nodeTemplate.isDefined && node.nodeTemplate.get.id == templateElement.id.get
      }
      if (nodesWithTemplate.nonEmpty) {
        val newNodes = workflow.pipelineGraph.nodes.map { node =>
          if (node.nodeTemplate.isDefined && node.nodeTemplate.get.id == templateElement.id.get)
            node.copy(nodeTemplate = None)
          else node
        }
        Option(workflow.copy(pipelineGraph = workflow.pipelineGraph.copy(nodes = newNodes)))
      } else None
    }
  }
}
