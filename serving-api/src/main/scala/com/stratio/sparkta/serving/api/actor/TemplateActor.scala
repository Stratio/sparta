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
import com.stratio.sparkta.serving.core.models.{StreamingContextStatusEnum, TemplateModel}
import org.apache.zookeeper.KeeperException.NoNodeException
import org.json4s.DefaultFormats
import org.json4s.ext.EnumNameSerializer
import org.json4s.native.Serialization._
import spray.httpx.Json4sJacksonSupport

import scala.util.Try

/**
 * List of all possible akka messages used to manage templates.
 */
case class TemplateSupervisorActor_findByType(t: String)
case class TemplateSupervisorActor_findByTypeAndName(t: String, name: String)
case class TemplateSupervisorActor_response_templates(templates: Try[Seq[TemplateModel]])
case class TemplateSupervisorActor_response_template(template: Try[TemplateModel])

/**
 * Implementation of supported CRUD operations over templates used to composite a policy.
 * @author anistal
 */
class TemplateActor extends Actor with Json4sJacksonSupport with SLF4JLogging {

  implicit val json4sJacksonFormats = DefaultFormats +
    new EnumNameSerializer(StreamingContextStatusEnum) +
    new JsoneyStringSerializer()

  override def receive: Receive = {

    case TemplateSupervisorActor_findByType(t: String) => doFindByType(t)
    case TemplateSupervisorActor_findByTypeAndName(t, name) => doFindByTypeAndName(t, name)
  }

  def doFindByType(t: String): Unit =
    sender ! TemplateSupervisorActor_response_templates(Try({
      new File(this.getClass.getClassLoader.getResource(s"templates/${t}").toURI)
        .listFiles
        .filter(file => file.getName.endsWith(".json"))
        .map(file => {
        log.info(s"> Retrieving template: ${file.getName}")
        read[TemplateModel](new InputStreamReader(
          this.getClass.getClassLoader.getResourceAsStream(s"templates/${t}/${file.getName}")))
      }).toSeq
    }).recover {
      case e: NoNodeException => Seq()
    })


  def doFindByTypeAndName(t: String, name: String): Unit = {
    sender ! TemplateSupervisorActor_response_template(Try({
      read[TemplateModel](new InputStreamReader(
        this.getClass.getClassLoader.getResourceAsStream(s"templates/${t}/${name}.json")))
    }))
  }
}