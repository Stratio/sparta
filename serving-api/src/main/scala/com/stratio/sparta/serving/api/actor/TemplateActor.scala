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
package com.stratio.sparta.serving.api.actor

import java.io.{File, InputStreamReader}
import java.net.{URI, URL}

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.api.actor.TemplateActor._
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.{ErrorModel, SpartaSerializer, TemplateModel}
import org.json4s.jackson.Serialization.read
import spray.httpx.Json4sJacksonSupport

import scala.util.Try

class TemplateActor extends Actor
  with Json4sJacksonSupport
  with SLF4JLogging
  with SpartaSerializer {

  override def receive: Receive = {

    case FindByType(t: String) => doFindByType(t)
    case FindByTypeAndName(t, name) => doFindByTypeAndName(t, name)
  }

  def doFindByType(t: String): Unit =
    sender ! ResponseTemplates(Try({
      getFilesFromURI(getResource(s"templates/${t}").toURI)
        .filter(file => file.getName.endsWith(".json"))
        .map(file => {
          log.info(s"> Retrieving template: ${file.getName}")
          read[TemplateModel](getInputStreamFromResource(s"templates/${t}/${file.getName}"))
        })
    }).recover {
      case e: NullPointerException => Seq()
    })

  def doFindByTypeAndName(t: String, name: String): Unit =
    sender ! ResponseTemplate(Try({
      read[TemplateModel](getInputStreamFromResource(s"templates/${t}/${name.toLowerCase}.json"))
    }).recover {
      case e: NullPointerException => throw new ServingCoreException(ErrorModel.toString(
        new ErrorModel(ErrorModel.CodeNotExistsTemplateWithName, s"No template of type $t  with name ${name}.json")
      ))
    })

  // XXX Protected methods
  protected def getResource(resource: String): URL =
    this.getClass.getClassLoader.getResource(resource)

  protected def getInputStreamFromResource(resource: String): InputStreamReader =
    new InputStreamReader(this.getClass.getClassLoader.getResourceAsStream(resource))

  protected def getFilesFromURI(uri: URI): Seq[File] =
    new File(uri).listFiles
}

object TemplateActor {

  case class FindByType(t: String)

  case class FindByTypeAndName(t: String, name: String)

  case class ResponseTemplates(templates: Try[Seq[TemplateModel]])

  case class ResponseTemplate(template: Try[TemplateModel])

}