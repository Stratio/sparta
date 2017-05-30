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
import com.stratio.sparta.serving.api.actor.PluginActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.utils.FileActorUtils
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.files.SpartaFilesResponse
import spray.http.BodyPart
import spray.httpx.Json4sJacksonSupport

import scala.util.{Failure, Try}

class PluginActor extends Actor with Json4sJacksonSupport with FileActorUtils with SpartaSerializer {

  //The dir where the jars will be saved
  val targetDir = Try(SpartaConfig.getDetailConfig.get.getString(AppConstant.PluginsPackageLocation))
    .getOrElse(AppConstant.DefaultPluginsPackageLocation)
  val apiPath = HttpConstant.PluginsPath
  override val patternFileName = Option(Pattern.compile(""".*\.jar""").asPredicate())

  override def receive: Receive = {
    case UploadPlugins(files) => if (files.isEmpty) errorResponse() else uploadPlugins(files)
    case ListPlugins => browsePlugins()
    case DeletePlugins => deletePlugins()
    case DeletePlugin(fileName) => deletePlugin(fileName)
    case _ => log.info("Unrecognized message in Plugin Actor")
  }

  def errorResponse(): Unit =
    sender ! SpartaFilesResponse(Failure(new IllegalArgumentException(s"At least one file is expected")))

  def deletePlugins(): Unit = sender ! PluginResponse(deleteFiles())

  def deletePlugin(fileName: String): Unit = sender ! PluginResponse(deleteFile(fileName))

  def browsePlugins(): Unit = sender ! SpartaFilesResponse(browseDirectory())

  def uploadPlugins(files: Seq[BodyPart]): Unit = sender ! SpartaFilesResponse(uploadFiles(files))
}

object PluginActor {

  case class UploadPlugins(files: Seq[BodyPart])

  case class PluginResponse(status: Try[_])

  case object ListPlugins

  case object DeletePlugins

  case class DeletePlugin(fileName: String)

}
