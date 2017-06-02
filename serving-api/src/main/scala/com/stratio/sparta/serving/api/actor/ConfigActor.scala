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
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.frontend.FrontendConfiguration
import com.typesafe.config.Config
import spray.httpx.Json4sJacksonSupport

import scala.util.Try

class ConfigActor extends Actor with SLF4JLogging with Json4sJacksonSupport with SpartaSerializer {

  val apiPath = HttpConstant.ConfigPath

  val oauthConfig: Option[Config] = SpartaConfig.getOauth2Config
  val enabledSecurity: Boolean = Try(oauthConfig.get.getString("enable").toBoolean).getOrElse(false)
  val emptyField = ""

  override def receive: Receive = {
    case FindAll(user) => findFrontendConfig(user)
    case _ => log.info("Unrecognized message in ConfigActor")
  }

  def findFrontendConfig(user: Option[LoggedUser]): Unit = {
    sender ! ConfigResponse(retrieveStringConfig(user))
  }

  def retrieveStringConfig(user: Option[LoggedUser]): FrontendConfiguration = {
    enabledSecurity match {
      case true => FrontendConfiguration(
        Try(SpartaConfig.getFrontendConfig.get
          .getInt("timeout")).getOrElse(AppConstant.DefaultFrontEndTimeout),
        retrieveNameUser(user))
      case false => FrontendConfiguration(Try(SpartaConfig.getFrontendConfig.get
        .getInt("timeout")).getOrElse(AppConstant.DefaultFrontEndTimeout), emptyField)
    }
  }

  private def retrieveNameUser(user: Option[LoggedUser]): String = user match {
    case Some(currentUser) if currentUser.name.nonEmpty => currentUser.name
    case Some(currentUser) if currentUser.name.isEmpty => "Anonymous"
    case None => emptyField
  }
}

object ConfigActor {
  case class FindAll(user: Option[LoggedUser])
  case class ConfigResponse(frontendConfiguration:FrontendConfiguration)
}
