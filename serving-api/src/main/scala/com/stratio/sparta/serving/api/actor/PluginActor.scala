/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import java.io.File

import akka.actor.Actor
import com.stratio.sparta.security._
import com.stratio.sparta.serving.api.actor.PluginActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.constants.HttpConstant._
import com.stratio.sparta.serving.api.utils.FileActorUtils
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.authorization.LoggedUser
import com.stratio.sparta.serving.core.models.files.SpartaFile
import com.stratio.sparta.serving.core.services.HdfsFilesService
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import spray.http.BodyPart
import spray.httpx.Json4sJacksonSupport

import scala.util.{Failure, Success, Try}

class PluginActor() extends Actor
  with Json4sJacksonSupport with FileActorUtils with SpartaSerializer with ActionUserAuthorize {

  lazy val hdfsFilesService = HdfsFilesService()

  //The dir where the jars will be saved
  val targetDir = "plugins"
  val temporalDir = "/tmp/sparta/plugins"
  val apiPath = s"${HttpConstant.PluginsPath}/download"

  val ResourceType = "Files"

  def receiveApiActions(action : Any): Any = action match {
    case UploadPlugins(files, user) =>
      if (validateFiles(files)) uploadPlugins(files, user)
    case ListPlugins(user) => browsePlugins(user)
    case DeletePlugins(user) => deletePlugins(user)
    case DeletePlugin(fileName, user) => deletePlugin(fileName, user)
    case DownloadPlugin(fileName, user) => downloadPlugin(fileName, user)
    case _ => log.info("Unrecognized message in Plugin Actor")
  }


  def deletePlugins(user: Option[LoggedUser]): Unit =
    authorizeActions[Response](user, Map(ResourceType -> Delete)) {
      Try(hdfsFilesService.deletePlugins()).orElse(deleteFiles())
    }

  def deletePlugin(fileName: String, user: Option[LoggedUser]): Unit =
    authorizeActions[Response](user, Map(ResourceType -> Delete)) {
      Try(hdfsFilesService.deletePlugin(fileName)).orElse(deleteFile(fileName))
    }

  def browsePlugins(user: Option[LoggedUser]): Unit =
    authorizeActions[SpartaFilesResponse](user, Map(ResourceType -> View)) {
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
          log.warn(s"Error getting files with Hdfs api, getting it from local directory. ${e.getLocalizedMessage}")
          browseDirectory()
      }
    }

  def downloadPlugin(fileName: String, user: Option[LoggedUser]): Unit =
    authorizeActions[SpartaFileResponse](user, Map(ResourceType -> Download)) {
      Try {
        hdfsFilesService.downloadPluginFile(fileName, temporalDir)
      }.flatMap(localFilePath => browseFile(localFilePath)).orElse(browseFile(fileName))
    }

  def uploadPlugins(files: Seq[BodyPart], user: Option[LoggedUser]): Unit =
    authorizeActions[Response](user, Map(ResourceType -> Upload)) {
      val spartaFilesTry = uploadFiles(files, useTemporalDirectory = true)

      val spartaResponse: Try[Unit] =
        spartaFilesTry.flatMap{ spartaFiles =>
        Try {
          spartaFiles.foreach { file =>
            hdfsFilesService.uploadPluginFile(file.path)
          }
        }.orElse(uploadFiles(files).map(_ => Unit))
      }

      spartaFilesTry.foreach{ spartaFiles =>
        refreshClassLoader(spartaFiles) // TODO refresh only new Files
      }

      spartaResponse
    }

  private def refreshClassLoader(spartaFiles: Seq[SpartaFile]): Unit =
    if (sys.env.getOrElse("SPARTA_REFRESH_LATEST_PLUGIN_ENABLED", "true").toBoolean) {
      val urls = spartaFiles.map{ spartaFile => new File(spartaFile.path).toURI.toURL } // path => localPath
      JarsHelper.addPlugins(urls)
    }

  def validateFiles(files: Seq[BodyPart]): Boolean = {

    def validateFile(file: BodyPart): Boolean = {
      val filename = file.filename.orElse(file.name)
      if (filename.isEmpty) {
        sender ! Left(Failure(new Exception(s"It is necessary to specify a name in order to upload the file")))
      }

      val isValidFile =
        filename.exists{ name =>
          val hasWhitespaces =  name.contains(" ")
          if (hasWhitespaces) {
            log.warn(s"Plugin containing whitespaces found: $name")
            sender ! Left(Failure(new Exception(s"Plugins name cannot contain whitespaces")))
          }
          !hasWhitespaces
        }
      isValidFile
    }

    if (files.isEmpty) {
      sender ! Left(Failure(new Exception(s"At least one file is expected")))
    }
    files.forall(validateFile)
  }

}

object PluginActor {

  type SpartaFilesResponse = Try[Seq[SpartaFile]]

  type SpartaFileResponse = Try[SpartaFile]

  case class UploadPlugins(files: Seq[BodyPart], user: Option[LoggedUser])

  case class ListPlugins(user: Option[LoggedUser])

  case class DeletePlugins(user: Option[LoggedUser])

  case class DeletePlugin(fileName: String, user: Option[LoggedUser])

  case class DownloadPlugin(fileName: String, user: Option[LoggedUser])

}
