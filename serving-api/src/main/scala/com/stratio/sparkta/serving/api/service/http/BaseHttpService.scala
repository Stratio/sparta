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

package com.stratio.sparkta.serving.api.service.http

import akka.actor.{ActorSelection, ActorRef}
import akka.event.slf4j.SLF4JLogging
import akka.util.Timeout
import com.stratio.sparkta.serving.core.models.StreamingContextStatusEnum
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import org.json4s.{Formats, DefaultFormats}
import org.json4s.ext.EnumNameSerializer
import spray.httpx.Json4sJacksonSupport
import spray.routing._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

/**
 * It gives common operations such as error handling, i18n, etc. All HttpServices should extend of it.
 * @author anistal
 */
trait BaseHttpService extends HttpService with Json4sJacksonSupport with SLF4JLogging {

  implicit val json4sJacksonFormats: Formats = DefaultFormats + new EnumNameSerializer(StreamingContextStatusEnum) +
    new JsoneyStringSerializer()

  implicit val timeout: Timeout = Timeout(15.seconds)

  implicit def executionContext: ExecutionContextExecutor = actorRefFactory.dispatcher

  implicit val actors: Map[String, ActorRef]

  val supervisor: ActorRef

  def routes: Route

}
