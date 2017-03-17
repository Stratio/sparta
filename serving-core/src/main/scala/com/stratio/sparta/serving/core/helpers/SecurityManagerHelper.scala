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

import java.lang.reflect.Constructor

import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.config.SpartaConfig._
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.ErrorModel
import com.typesafe.config.Config

import scala.util.{Failure, Success, Try}

object SecurityManagerHelper {

  lazy val securityManager: Option[SpartaSecurityManager] =
    if (!isSecurityManagerEnabled) {
      log.warn("Authorization is not enabled, configure a security manager if needed")
      None
    } else {
      Try(getSecurityConfig.get.getString("manager.class")) match {
        case Success(value) =>
          val securityManagerClass = Class.forName(value)
          val constr: Constructor[_] = securityManagerClass.getConstructor()
          val secManager = constr.newInstance().asInstanceOf[SpartaSecurityManager]
          secManager.start()
          Some(secManager)
        case Failure(e) =>
          val msg = "It's mandatory to specify one manager class path when the security is enabled"
          log.error(msg)
          throw new IllegalStateException(msg)
          None
      }
    }


  def isSecurityManagerEnabled: Boolean = Try(getSecurityConfig.get.getBoolean("manager.enabled")) match {
    case Success(value) =>
      value
    case Failure(e) =>
      log.error("Incorrect value in security manager option, setting enabled value by default", e)
      true
  }

  def getSecurityConfig: Option[Config] = mainConfig.flatMap(config => getOptionConfig(ConfigSecurity, config))

  def errorResponseAuthorization(userId: String, action: Action): UnauthorizedResponse = {
    val msg = s"Authorization rejected for user: $userId performing action: $action"
    log.warn(msg)
    UnauthorizedResponse(ServingCoreException(ErrorModel.toString(ErrorModel(ErrorModel.UnauthorizedAction, msg))))
  }

  def errorNoUserFound(actions: Seq[Action]): UnauthorizedResponse = {
    val msg = s"Authorization rejected for actions: $actions. No user was found."
    log.warn(msg)
    UnauthorizedResponse(ServingCoreException(ErrorModel.toString(ErrorModel(ErrorModel.UserNotFound, msg))))
  }

  case class UnauthorizedResponse(exception : ServingCoreException)

  implicit def resourceParser (resource : String) : Resource = {
    resource match {
      case "input" => Resource(InputResource, resource)
      case "output" => Resource(OutputResource,resource)
      case "policy" => Resource(PolicyResource,resource)
      case "plugin" => Resource(PluginResource, resource)
      case "context" => Resource(ContextResource, resource)
      case "driver" => Resource(DriverResource, resource)
    }
  }
}
