/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.serving.api.actor

import java.io.{File, InputStreamReader}

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import com.stratio.sparkta.serving.api.actor.TemplateActor._
import com.stratio.sparkta.serving.api.exception.ServingApiException
import com.stratio.sparkta.serving.core.models.{SparktaSerializer, ErrorModel, StreamingContextStatusEnum, TemplateModel}
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import org.json4s.native.Serialization._
import spray.httpx.Json4sJacksonSupport

import scala.util.Try

/**
 * Implementation of supported CRUD operations over templates used to composite a policy.
 */
class TemplateActor extends Actor with Json4sJacksonSupport with SLF4JLogging with SparktaSerializer{

  override def receive: Receive = {

    case FindByType(t: String) => doFindByType(t)
    case FindByTypeAndName(t, name) => doFindByTypeAndName(t, name)
  }

  def doFindByType(t: String): Unit =
    sender ! ResponseTemplates(Try({
      new File(this.getClass.getClassLoader.getResource(s"templates/${t}").toURI)
        .listFiles
        .filter(file => file.getName.endsWith(".json"))
        .map(file => {
        log.info(s"> Retrieving template: ${file.getName}")
        read[TemplateModel](new InputStreamReader(
          this.getClass.getClassLoader.getResourceAsStream(s"templates/${t}/${file.getName}")))
      }).toSeq
    }).recover {
      case e: NullPointerException => Seq()
    })


  def doFindByTypeAndName(t: String, name: String): Unit =
    sender ! ResponseTemplate(Try({
      read[TemplateModel](new InputStreamReader(
        this.getClass.getClassLoader.getResourceAsStream(s"templates/${t}/${name.toLowerCase}.json")))
    }).recover {
      case e: NullPointerException => throw new ServingApiException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsTemplatetWithName, s"No template of type $t  with name ${name}.json")
      ))
    })
}

object TemplateActor {

  case class FindByType(t: String)

  case class FindByTypeAndName(t: String, name: String)

  case class ResponseTemplates(templates: Try[Seq[TemplateModel]])

  case class ResponseTemplate(template: Try[TemplateModel])
}