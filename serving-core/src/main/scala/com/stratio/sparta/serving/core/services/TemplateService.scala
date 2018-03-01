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

package com.stratio.sparta.serving.core.services

import java.util.UUID

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.{TemplateElement, Workflow}
import org.apache.curator.framework.CuratorFramework
import org.joda.time.DateTime
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

class TemplateService(val curatorFramework: CuratorFramework) extends SLF4JLogging with SpartaSerializer {

  private val workflowService = new WorkflowService(curatorFramework)

  def findByType(templateType: String): List[TemplateElement] = {
    val templateLocation = templatePathType(templateType)

    if (CuratorFactoryHolder.existsPath(templateLocation)) {
      val children = curatorFramework.getChildren.forPath(templateLocation)
      JavaConversions.asScalaBuffer(children).toList.map(id => findByTypeAndId(templateType, id))
    } else List.empty[TemplateElement]
  }

  def findByTypeAndId(templateType: String, id: String): TemplateElement = {
    val templateLocation = s"${templatePathType(templateType)}/$id"

    if (CuratorFactoryHolder.existsPath(templateLocation)) {
      read[TemplateElement](new String(curatorFramework.getData.forPath(templateLocation)))
    } else throw new ServerException(s"Template type: $templateType and id: $id does not exist")
  }

  def findByTypeAndName(templateType: String, name: String): TemplateElement =
    findByType(templateType).find(template => template.name == name)
      .getOrElse(throw new ServerException(s"Template type: $templateType and name: $name does not exist"))

  def findAll: List[TemplateElement] = {
    if (CuratorFactoryHolder.existsPath(TemplatesZkPath)) {
      val children = curatorFramework.getChildren.forPath(TemplatesZkPath)
      JavaConversions.asScalaBuffer(children).toList.flatMap(templateType => findByType(templateType))
    } else List.empty[TemplateElement]
  }

  def create(template: TemplateElement): TemplateElement =
    Try(findByTypeAndName(template.templateType, template.name)).toOption
      .getOrElse {
        val newTemplate = addCreationDate(addId(template))
        curatorFramework.create().creatingParentsIfNeeded().forPath(
          s"${templatePathType(newTemplate.templateType)}/${newTemplate.id.get}", write(newTemplate).getBytes())
        newTemplate
      }

  def createList(templates: Seq[TemplateElement]): Seq[TemplateElement] =
    templates.map(create)

  def update(template: TemplateElement): TemplateElement = {
    val newTemplate = addUpdateDate(addId(template))
    val workflowsToUpdate = workflowService.findByTemplateId(template.id.get)

    Try {
      workflowsToUpdate.foreach(workflow => workflowService.update(updateWorkflowWithTemplate(template, workflow)))
    } match {
      case Success(_) =>
        curatorFramework.setData().forPath(
          s"${templatePathType(newTemplate.templateType)}/${template.id.get}", write(newTemplate).getBytes)
        newTemplate
      case Failure(e) =>
        log.error("Error updating template in workflows. All workflows will be rolled back", e)
        val detailMsg = Try {
          workflowsToUpdate.foreach(workflow => workflowService.update(workflow))
        } match {
          case Success(_) =>
            log.warn("Restoring data process after update template completed successfully")
            None
          case Failure(exception) =>
            log.error("Restoring data process after update template has failed." +
              " The data may be corrupted. Contact the technical support", exception)
            Option(exception.getLocalizedMessage)
        }

        throw new Exception(s"Error updating template." +
          s"${if(detailMsg.isDefined) s" Restoring error: ${detailMsg.get}" else ""}", e)
    }
  }

  def updateList(templates: Seq[TemplateElement]): Seq[TemplateElement] =
    templates.map(update)

  def deleteByType(templateType: String): Unit = {
    val children = curatorFramework.getChildren.forPath(templatePathType(templateType))
    val templatesFound = JavaConversions.asScalaBuffer(children).toList.map(element =>
      read[TemplateElement](new String(curatorFramework.getData.forPath(
        s"${templatePathType(templateType)}/$element"))))

    templatesFound.foreach(template => {
      val id = template.id.getOrElse {
        throw new ServerException(s"Template without id: ${template.name}.")
      }
      deleteByTypeAndId(templateType, id)
    })
  }

  def deleteByTypeAndId(templateType: String, id: String): Unit = {
    val templateLocation = s"${templatePathType(templateType)}/$id"
    if (CuratorFactoryHolder.existsPath(templateLocation)) curatorFramework.delete().forPath(templateLocation)
  }

  def deleteByTypeAndName(templateType: String, name: String): Unit = {
    val templateFound = Try(findByTypeAndName(templateType, name)).toOption

    if (templateFound.isDefined && templateFound.get.id.isDefined) {
      val id = templateFound.get.id.get
      val templateLocation = s"${templatePathType(templateType)}/$id"
      if (CuratorFactoryHolder.existsPath(templateLocation))
        curatorFramework.delete().forPath(templateLocation)
      else throw new ServerException(s"Template type: $templateType and id: $id does not exist")
    } else {
      throw new ServerException(s"Template without id: $name.")
    }
  }

  def deleteAll(): List[TemplateElement] = {
    val templatesFound = findAll
    templatesFound.foreach(template => {
      val id = template.id.getOrElse {
        throw new ServerException(s"Template without id: ${template.name}.")
      }
      deleteByTypeAndId(template.templateType, id)
    })
    templatesFound
  }

  /* PRIVATE METHODS */

  private def updateWorkflowWithTemplate(template: TemplateElement, workflow: Workflow) : Workflow = {
    val newNodes = workflow.pipelineGraph.nodes.map { node =>
      if(node.nodeTemplate.isDefined && node.nodeTemplate.get.id == template.id.get)
        node.copy(configuration = template.configuration)
      else node
    }

    workflow.copy(pipelineGraph = workflow.pipelineGraph.copy(nodes = newNodes))
  }

  private def templatePathType(templateType: String): String = {
    templateType match {
      case "input" => s"$TemplatesZkPath/input"
      case "output" => s"$TemplatesZkPath/output"
      case "transformation" => s"$TemplatesZkPath/transformation"
      case _ => throw new IllegalArgumentException("The template type must be input|output|transformation")
    }
  }

  private[sparta] def addId(template: TemplateElement): TemplateElement =
    template.id match {
      case None => template.copy(id = Some(UUID.randomUUID.toString))
      case Some(_) => template
    }

  private[sparta] def addCreationDate(template: TemplateElement): TemplateElement =
    template.creationDate match {
      case None => template.copy(creationDate = Some(new DateTime()))
      case Some(_) => template
    }

  private[sparta] def addUpdateDate(template: TemplateElement): TemplateElement =
    template.copy(lastUpdateDate = Some(new DateTime()))
}