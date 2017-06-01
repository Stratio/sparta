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

import java.util.regex.Pattern

import akka.actor.Actor
import com.stratio.sparta.security._
import com.stratio.sparta.serving.api.actor.PluginActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.utils.FileActorUtils
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.files.SpartaFilesResponse
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import spray.http.BodyPart
import spray.httpx.Json4sJacksonSupport

import scala.util.{Failure, Try}

class PluginActor(val secManagerOpt: Option[SpartaSecurityManager]) extends Actor
  with Json4sJacksonSupport with FileActorUtils with SpartaSerializer with ActionUserAuthorize{

  //The dir where the jars will be saved
  val targetDir = Try(SpartaConfig.getDetailConfig.get.getString(AppConstant.PluginsPackageLocation))
    .getOrElse(AppConstant.DefaultPluginsPackageLocation)
  val apiPath = HttpConstant.PluginsPath
  override val patternFileName = Option(Pattern.compile(""".*\.jar""").asPredicate())

  val ResourceType = "plugin"

  override def receive: Receive = {
    case UploadPlugins(files, user) => if (files.isEmpty) errorResponse() else uploadPlugins(files, user)
    case ListPlugins(user) => browsePlugins(user)
    case DeletePlugins(user) => deletePlugins(user)
    case DeletePlugin(fileName, user) => deletePlugin(fileName, user)
    case _ => log.info("Unrecognized message in Plugin Actor")
  }

  def errorResponse(): Unit =
    sender ! Left(SpartaFilesResponse(Failure(new IllegalArgumentException(s"At least one file is expected"))))

  def deletePlugins(user: Option[LoggedUser]): Unit = {
    def callback() = PluginResponse(deleteFiles())

    securityActionAuthorizer[PluginResponse](secManagerOpt, user, Map(ResourceType -> Delete), callback)
  }

  def deletePlugin(fileName: String, user: Option[LoggedUser]): Unit = {
    def callback() = PluginResponse(deleteFile(fileName))

    securityActionAuthorizer[PluginResponse](secManagerOpt, user, Map(ResourceType -> Delete), callback)
  }

  def browsePlugins(user: Option[LoggedUser]): Unit = {
    def callback() = SpartaFilesResponse(browseDirectory())

    securityActionAuthorizer[SpartaFilesResponse](secManagerOpt, user, Map(ResourceType -> View), callback)
  }

  def uploadPlugins(files: Seq[BodyPart], user: Option[LoggedUser]): Unit = {
    def callback() = SpartaFilesResponse(uploadFiles(files))

    securityActionAuthorizer[SpartaFilesResponse](secManagerOpt, user, Map(ResourceType -> Upload), callback)
  }

}

object PluginActor {

  case class UploadPlugins(files: Seq[BodyPart], user: Option[LoggedUser])

  case class PluginResponse(status: Try[_])

  case class ListPlugins(user: Option[LoggedUser])

  case class DeletePlugins(user: Option[LoggedUser])

  case class DeletePlugin(fileName: String, user: Option[LoggedUser])

}
