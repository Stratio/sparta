/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.migration

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.TemplateElement
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization._

import scala.collection.JavaConversions

class TemplateService(val curatorFramework: CuratorFramework) extends SLF4JLogging with SpartaSerializer {

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

  def findAll: List[TemplateElement] = {
    if (CuratorFactoryHolder.existsPath(TemplatesZkPath)) {
      val children = curatorFramework.getChildren.forPath(TemplatesZkPath)
      JavaConversions.asScalaBuffer(children).toList.flatMap(templateType => findByType(templateType))
    } else List.empty[TemplateElement]
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

}