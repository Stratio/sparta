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

import akka.actor.{Actor, _}
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.api.actor.ConfigActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.frontend.FrontendConfiguration
import spray.httpx.Json4sJacksonSupport

import scala.util.Try

class ConfigActor extends Actor with SLF4JLogging with Json4sJacksonSupport with SpartaSerializer {

  val apiPath = HttpConstant.ConfigPath

  override def receive: Receive = {
    case FindAll => findFrontendConfig()
    case _ => log.info("Unrecognized message in ConfigActor")
  }

  def findFrontendConfig(): Unit = {
    sender ! ConfigResponse(retrieveStringConfig())
  }

  def retrieveStringConfig(): FrontendConfiguration = {
      FrontendConfiguration(
        Try(SpartaConfig.getFrontendConfig.get
          .getInt("timeout")).getOrElse(AppConstant.DefaultFrontEndTimeout))
  }

}

object ConfigActor {
  object FindAll
  case class ConfigResponse(frontendConfiguration:FrontendConfiguration)
}
