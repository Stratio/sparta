/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.migration.zookeeper

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.migration.TemplateElementOrion
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions
import scala.util.Try

class ZkTemplateService(val curatorFramework: CuratorFramework) extends SLF4JLogging with SpartaSerializer {

  def findByType(templateType: String): Seq[TemplateElementOrion] = {
    Try {
      val templateLocation = templatePathType(templateType)

      if (CuratorFactoryHolder.existsPath(templateLocation)) {
        val children = curatorFramework.getChildren.forPath(templateLocation)
        JavaConversions.asScalaBuffer(children).toList.flatMap(id => findByTypeAndId(templateType, id))
      } else Seq.empty[TemplateElementOrion]
    }.getOrElse(Seq.empty[TemplateElementOrion])
  }

  def findByTypeAndId(templateType: String, id: String): Option[TemplateElementOrion] = {
    Try {
      val templateLocation = s"${templatePathType(templateType)}/$id"

      if (CuratorFactoryHolder.existsPath(templateLocation)) {
        read[TemplateElementOrion](new String(curatorFramework.getData.forPath(templateLocation)))
      } else throw new ServerException(s"Template type: $templateType and id: $id does not exist")
    }.toOption
  }

  def findAll: Seq[TemplateElementOrion] = {
    Try {
      if (CuratorFactoryHolder.existsPath(TemplatesZkPath)) {
        val children = curatorFramework.getChildren.forPath(TemplatesZkPath)
        JavaConversions.asScalaBuffer(children).toList.flatMap(templateType => findByType(templateType))
      } else Seq.empty[TemplateElementOrion]
    }.getOrElse(Seq.empty[TemplateElementOrion])
  }

  def create(template: TemplateElementOrion, pathPrefix: String): Unit = {
    val templateLocation = s"${templateOldPathType(pathPrefix, template.templateType)}/${template.id.get}"
    if (CuratorFactoryHolder.existsPath(templateLocation))
      curatorFramework.setData().forPath(templateLocation, write(template).getBytes())
    else curatorFramework.create().creatingParentsIfNeeded().forPath(templateLocation, write(template).getBytes())
  }

  def deletePath(): Unit = {
    if (CuratorFactoryHolder.existsPath(TemplatesZkPath))
      curatorFramework.delete().deletingChildrenIfNeeded().forPath(TemplatesZkPath)
  }

  /* PRIVATE METHODS */

  private def templatePathType(templateType: String): String = {
    templateType match {
      case "input" => s"$TemplatesZkPath/input"
      case "output" => s"$TemplatesZkPath/output"
      case "transformation" => s"$TemplatesZkPath/transformation"
      case _ => throw new IllegalArgumentException("The template type must be input|output|transformation")
    }
  }

  private def templateOldPathType(pathPrefix: String, templateType: String): String = {
    templateType match {
      case "input" => s"$TemplatesOldZkPath/input"
      case "output" => s"$TemplatesOldZkPath/output"
      case "transformation" => s"$TemplatesOldZkPath/transformation"
      case _ => throw new IllegalArgumentException("The template type must be input|output|transformation")
    }
  }

}