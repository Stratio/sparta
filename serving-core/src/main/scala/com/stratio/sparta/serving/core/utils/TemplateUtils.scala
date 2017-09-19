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

package com.stratio.sparta.serving.core.utils

import java.util.UUID

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.workflow.TemplateElement
import com.stratio.sparta.serving.core.models.{ErrorModel, SpartaSerializer}
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions

trait TemplateUtils extends SLF4JLogging with SpartaSerializer {

  val curatorFramework: CuratorFramework

  def findAllTemplates: List[TemplateElement] = {
    if (CuratorFactoryHolder.existsPath(TemplatesZkPath)) {
      val children = curatorFramework.getChildren.forPath(TemplatesZkPath)
      JavaConversions.asScalaBuffer(children).toList.flatMap(templateType => findTemplatesByType(templateType))
    } else List.empty[TemplateElement]
  }

  def findTemplatesByType(templateType: String): List[TemplateElement] = {
    val templateLocation = templatePathType(templateType)
    if (CuratorFactoryHolder.existsPath(templateLocation)) {
      val children = curatorFramework.getChildren.forPath(templateLocation)
      JavaConversions.asScalaBuffer(children).toList.map(id => findTemplateByTypeAndId(templateType, id))
    } else List.empty[TemplateElement]
  }

  def findTemplateByTypeAndId(templateType: String, id: String): TemplateElement = {
    val templateLocation = s"${templatePathType(templateType)}/$id"
    if (CuratorFactoryHolder.existsPath(templateLocation)) {
      read[TemplateElement](new String(curatorFramework.getData.forPath(templateLocation)))
    } else throw new ServerException(s"Template type: $templateType and id: $id does not exist")
  }

  def findTemplateByTypeAndName(templateType: String, name: String): Option[TemplateElement] =
    findTemplatesByType(templateType).find(template => template.name == name)

  def createTemplate(template: TemplateElement): TemplateElement =
    findTemplateByTypeAndName(template.templateType, template.name.toLowerCase)
      .getOrElse(createNewTemplate(template))

  def updateTemplate(template: TemplateElement): TemplateElement = {
    val newTemplate = template.copy(name = template.name.toLowerCase)
    curatorFramework.setData().forPath(
      s"${templatePathType(newTemplate.templateType)}/${template.id.get}", write(newTemplate).getBytes)
    newTemplate
  }

  def deleteAllTemplates(): List[TemplateElement] = {
    val templatesFound = findAllTemplates
    templatesFound.foreach(template => {
      val id = template.id.getOrElse {
        throw new ServerException(s"Template without id: ${template.name}.")
      }
      deleteTemplateByTypeAndId(template.templateType, id)
    })
    templatesFound
  }

  def deleteTemplateByType(templateType: String): Unit = {
    val children = curatorFramework.getChildren.forPath(templatePathType(templateType))
    val templatesFound = JavaConversions.asScalaBuffer(children).toList.map(element =>
      read[TemplateElement](new String(curatorFramework.getData.forPath(
        s"${templatePathType(templateType)}/$element"))))
    templatesFound.foreach(template => {
      val id = template.id.getOrElse {
        throw new ServerException(s"Template without id: ${template.name}.")
      }
      deleteTemplateByTypeAndId(templateType, id)
    })
  }

  def deleteTemplateByTypeAndId(templateType: String, id: String): Unit = {
    val templateLocation = s"${templatePathType(templateType)}/$id"
    if (CuratorFactoryHolder.existsPath(templateLocation)) curatorFramework.delete().forPath(templateLocation)
  }

  def deleteTemplateByTypeAndName(templateType: String, name: String): Unit = {
    val templateFound = findTemplateByTypeAndName(templateType, name)
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

  /* PRIVATE METHODS */

  private def createNewTemplate(template: TemplateElement): TemplateElement = {
    val newTemplate = template.copy(
      id = Option(UUID.randomUUID.toString),
      name = template.name.toLowerCase
    )
    curatorFramework.create().creatingParentsIfNeeded().forPath(
      s"${templatePathType(newTemplate.templateType)}/${newTemplate.id.get}", write(newTemplate).getBytes())

    newTemplate
  }

  private def templatePathType(templateType: String): String = {
    templateType match {
      case "input" => s"$TemplatesZkPath/input"
      case "output" => s"$TemplatesZkPath/output"
      case "transformation" => s"$TemplatesZkPath/transformation"
      case _ => throw new IllegalArgumentException("The template type must be input|output|transformation")
    }
  }
}