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

package com.stratio.sparta.serving.core.helpers

import java.io.Serializable

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.MarathonConstant.DcosServiceName
import com.stratio.sparta.serving.core.models.workflow.{NodeGraph, Workflow}
import com.stratio.sparta.serving.core.utils.ClasspathUtils

import scala.collection.JavaConversions._
import scala.util.Properties

object WorkflowHelper extends SLF4JLogging {

  lazy val classpathUtils = new ClasspathUtils

  def getSparkConfsReflec(elements: Seq[NodeGraph], methodName: String): Map[String, String] = {
    log.info("Initializing reflection")
    elements.flatMap(o => {
      val classType = o.configuration.getOrElse(AppConstant.CustomTypeKey, o.className).toString
      val clazzToInstance = classpathUtils.getClasspathMap.getOrElse(classType, o.className)
      val clazz = Class.forName(clazzToInstance)
      clazz.getMethods.find(p => p.getName == methodName) match {
        case Some(method) =>
          method.setAccessible(true)
          method.invoke(clazz, o.configuration.asInstanceOf[Map[String, Serializable]])
            .asInstanceOf[Seq[(String, String)]]
        case None =>
          Seq.empty[(String, String)]
      }
    }).toMap
  }

  def getMarathonId(workflowModel: Workflow) : String = {
    val inputServiceName = Properties.envOrElse(DcosServiceName, "undefined")

    s"sparta/$inputServiceName/workflows/${workflowModel.name}"
  }
}
