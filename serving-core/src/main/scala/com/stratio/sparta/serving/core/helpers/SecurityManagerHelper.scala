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

import com.stratio.gosec.dyplon.plugins.sparta.GoSecSpartaSecurityManager
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.config.SpartaConfig._
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.typesafe.config.Config
import spray.http.StatusCodes

import scala.util.{Failure, Success, Try}

object SecurityManagerHelper {

  lazy val securityManager: Option[SpartaSecurityManager] =
    if (!isSecurityManagerEnabled) {
      log.info("Authorization is not enabled, configure a security manager if needed")
      None
    } else {
      val secManager = new GoSecSpartaSecurityManager().asInstanceOf[SpartaSecurityManager]
      secManager.start
      Some(secManager)
    }

  def isSecurityManagerEnabled: Boolean = Try(getSecurityConfig.get.getBoolean("manager.enabled")) match {
    case Success(value) =>
      value
    case Failure(e) =>
      log.error("Incorrect value in security manager option, setting enabled value by default", e)
      true
  }

  def getSecurityConfig: Option[Config] = mainConfig.flatMap(config => getOptionConfig(ConfigSecurity, config))

  def errorResponseAuthorization(userId: String, resource: String): UnauthorizedResponse = {
    val msg = s"Unauthorized action on resource: $resource. User $userId doesn't have enough permissions."
    log.warn(msg)
    UnauthorizedResponse(ServerException(ErrorModel.toString(ErrorModel(
      StatusCodes.Unauthorized.intValue,
      StatusCodes.Unauthorized.intValue.toString,
      ErrorCodesMessages.getOrElse(StatusCodes.Unauthorized.intValue.toString, UnknownError),
      Option(msg)
    ))))
  }

  def errorNoUserFound(actions: Seq[Action]): UnauthorizedResponse = {
    val msg = s"Authorization rejected for actions: $actions. No user was found."
    log.warn(msg)
    UnauthorizedResponse(ServerException(ErrorModel.toString(ErrorModel(
      StatusCodes.InternalServerError.intValue,
      UserNotFound,
      ErrorCodesMessages.getOrElse(UserNotFound, UnknownError),
      Option(msg)
    ))))
  }

  case class UnauthorizedResponse(exception: ServerException)

  implicit def resourceParser(resource: String): Resource = {
    resource match {
      case "input" => Resource(InputResource, resource)
      case "output" => Resource(OutputResource, resource)
      case "policy" => Resource(PolicyResource, resource)
      case "plugin" => Resource(PluginResource, resource)
      case "context" => Resource(ContextResource, resource)
      case "driver" => Resource(DriverResource, resource)
      case "checkpoint" => Resource(CheckpointResource, resource)
      case "backup" => Resource(BackupResource, resource)
      case "catalog" => Resource(CatalogResource, resource)
    }
  }
}
