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
package com.stratio.sparta.serving.api.service.http

import akka.actor.ActorRef
import akka.event.slf4j.SLF4JLogging
import akka.util.Timeout
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import spray.httpx.Json4sJacksonSupport
import spray.routing._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

/**
 * It gives common operations such as error handling, i18n, etc. All HttpServices should extend of it.
 */
trait BaseHttpService extends HttpService with Json4sJacksonSupport with SLF4JLogging with SpartaSerializer {

  implicit val timeout: Timeout = Timeout(AkkaConstant.DefaultTimeout.seconds)

  implicit def executionContext: ExecutionContextExecutor = actorRefFactory.dispatcher

  implicit val actors: Map[String, ActorRef]

  val supervisor: ActorRef

  def routes(user: Option[LoggedUser] = None): Route

}
