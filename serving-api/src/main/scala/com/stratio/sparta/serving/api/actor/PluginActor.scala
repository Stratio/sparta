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

import akka.actor.Actor
import com.stratio.sparta.security._
import com.stratio.sparta.serving.api.actor.PluginActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.utils.FileActorUtils
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.files.SpartaFile
import com.stratio.sparta.serving.core.services.HdfsFilesService
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import spray.http.BodyPart
import spray.httpx.Json4sJacksonSupport

import scala.util.{Failure, Success, Try}

class PluginActor(implicit val secManagerOpt: Option[SpartaSecurityManager]) extends Actor
  with Json4sJacksonSupport with FileActorUtils with SpartaSerializer with ActionUserAuthorize {

  lazy val hdfsFilesService = HdfsFilesService()

  //The dir where the jars will be saved
  val targetDir = "plugins"
  val temporalDir = "/tmp/sparta/plugins"
  val apiPath = s"${HttpConstant.PluginsPath}/download"

  val ResourceType = "plugin"

  override def receive: Receive = {
    case UploadPlugins(files, user) => if (files.isEmpty) errorResponse() else uploadPlugins(files, user)
    case ListPlugins(user) => browsePlugins(user)
    case DeletePlugins(user) => deletePlugins(user)
    case DeletePlugin(fileName, user) => deletePlugin(fileName, user)
    case DownloadPlugin(fileName, user) => downloadPlugin(fileName, user)
    case _ => log.info("Unrecognized message in Plugin Actor")
  }

  def errorResponse(): Unit =
    sender ! Left(Failure(new Exception(s"At least one file is expected")))

  def deletePlugins(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[PluginResponse](user, Map(ResourceType -> Delete)) {
      Try(hdfsFilesService.deletePlugins()).orElse(deleteFiles())
    }

  def deletePlugin(fileName: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[PluginResponse](user, Map(ResourceType -> Delete)) {
      Try(hdfsFilesService.deletePlugin(fileName)).orElse(deleteFile(fileName))
    }

  def browsePlugins(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[SpartaFilesResponse](user, Map(ResourceType -> View)) {
      Try {
        hdfsFilesService.browsePlugins.flatMap { fileStatus =>
          if (fileStatus.isFile)
            Option(SpartaFile(
              fileStatus.getPath.getName,
              s"$url/${fileStatus.getPath.getName}",
              fileStatus.getPath.toUri.toString))
          else None
        }
      } match {
        case Success(files) =>
          Try(files)
        case Failure(e: java.io.FileNotFoundException) =>
          Try(Seq.empty[SpartaFile])
        case Failure(e: Exception) =>
          log.warn("Error getting files with Hdfs api, getting it from local directory", e)
          browseDirectory()
      }
    }

  def downloadPlugin(fileName: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[SpartaFileResponse](user, Map(ResourceType -> Download)) {
      Try {
        hdfsFilesService.downloadPluginFile(fileName, temporalDir)
      }.flatMap(localFilePath => browseFile(localFilePath)).orElse(browseFile(fileName))
    }

  def uploadPlugins(files: Seq[BodyPart], user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[PluginResponse](user, Map(ResourceType -> Upload)) {
      uploadFiles(files, useTemporalDirectory = true).flatMap { spartaFile =>
        Try {
          spartaFile.foreach { file =>
            hdfsFilesService.uploadPluginFile(file.path)
          }
        }.orElse(uploadFiles(files).map(_ => Unit))
      }
    }
}

object PluginActor {

  type SpartaFilesResponse = Try[Seq[SpartaFile]]

  type SpartaFileResponse = Try[SpartaFile]

  type PluginResponse = Try[Unit]

  case class UploadPlugins(files: Seq[BodyPart], user: Option[LoggedUser])

  case class ListPlugins(user: Option[LoggedUser])

  case class DeletePlugins(user: Option[LoggedUser])

  case class DeletePlugin(fileName: String, user: Option[LoggedUser])

  case class DownloadPlugin(fileName: String, user: Option[LoggedUser])

}
