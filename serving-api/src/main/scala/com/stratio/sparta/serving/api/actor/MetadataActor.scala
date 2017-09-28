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
import com.github.nscala_time.time.Imports.{DateTime, DateTimeFormat}
import com.stratio.sparta.security._
import com.stratio.sparta.serving.api.actor.DriverActor.SpartaFilesResponse
import com.stratio.sparta.serving.api.actor.MetadataActor.ExecuteBackup
import com.stratio.sparta.serving.api.actor.MetadataActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.utils.{BackupRestoreUtils, FileActorUtils}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.helpers.InfoHelper
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.files.BackupRequest
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import spray.http.BodyPart
import spray.httpx.Json4sJacksonSupport

import scala.util.{Failure, Success, Try}

class MetadataActor(implicit val secManagerOpt: Option[SpartaSecurityManager]) extends Actor
  with Json4sJacksonSupport
  with BackupRestoreUtils
  with SpartaSerializer
  with FileActorUtils
  with ActionUserAuthorize {

  //The dir where the backups will be saved
  val targetDir = Try(SpartaConfig.getDetailConfig.get.getString(BackupsLocation)).getOrElse(DefaultBackupsLocation)
  override val apiPath = HttpConstant.MetadataPath
  override val patternFileName = Option(Pattern.compile(""".*\.json""").asPredicate())

  //The dir where the jars will be saved
  val zkConfig = Try(SpartaConfig.getZookeeperConfig.get)
    .getOrElse(throw new ServerException("Zookeeper configuration is mandatory"))
  override val uri = Try(zkConfig.getString("connectionString")).getOrElse(DefaultZKConnection)
  override val connectionTimeout = Try(zkConfig.getInt("connectionTimeout")).getOrElse(DefaultZKConnectionTimeout)
  override val sessionTimeout = Try(zkConfig.getInt("sessionTimeout")).getOrElse(DefaultZKSessionTimeout)

  val ResourceType = "backup"

  override def receive: Receive = {
    case UploadBackups(files, user) => if (files.isEmpty) errorResponse() else uploadBackups(files, user)
    case ListBackups(user) => browseBackups(user)
    case BuildBackup(user) => buildBackup(user)
    case DeleteBackups(user) => deleteBackups(user)
    case CleanMetadata(user) => cleanMetadata(user)
    case DeleteBackup(fileName, user) => deleteBackup(fileName, user)
    case ExecuteBackup(backupRequest, user) => executeBackup(backupRequest, user)
    case _ => log.info("Unrecognized message in Backup/Restore Actor")
  }

  def errorResponse(): Unit =
    sender ! Left(Failure(new IllegalArgumentException(s"At least one file is expected")))

  def uploadBackups(files: Seq[BodyPart], user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[SpartaFilesResponse](user, Map(ResourceType -> Upload)) {
      uploadFiles(files)
    }

  def browseBackups(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[SpartaFilesResponse](user, Map(ResourceType -> View)) {
      browseDirectory()
    }

  def buildBackup(user: Option[LoggedUser]): Unit = {
    val format = DateTimeFormat.forPattern("yyyy-MM-dd-hh:mm:ss")
    val appInfo = InfoHelper.getAppInfo
    securityActionAuthorizer[SpartaFilesResponse](user, Map(ResourceType -> Create)) {
      for {
        _ <- Try(dump(BaseZkPath, s"$targetDir/backup-${format.print(DateTime.now)}-${appInfo.pomVersion}.json"))
        browseResult <- browseDirectory()
      } yield browseResult
    }
  }


  def deleteBackups(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[BackupResponse](user, Map(ResourceType -> Delete)) {
      deleteFiles()
    }

  def cleanMetadata(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[BackupResponse](user, Map(ResourceType -> Delete)) {
      Try(cleanZk(BaseZkPath))
    }


  def deleteBackup(fileName: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[BackupResponse](user, Map(ResourceType -> Delete)) {
      deleteFile(fileName)
    }

  def executeBackup(backupRequest: BackupRequest, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[BackupResponse](user, Map(ResourceType -> Execute)) {
      Try(importer("/", s"$targetDir/${backupRequest.fileName}", backupRequest.deleteAllBefore))
    }


}

object MetadataActor {

  case class UploadBackups(files: Seq[BodyPart], user: Option[LoggedUser])

  type BackupResponse = Try[Unit]

  case class ExecuteBackup(backupRequest: BackupRequest, user: Option[LoggedUser])

  case class ListBackups(user: Option[LoggedUser])

  case class BuildBackup(user: Option[LoggedUser])

  case class DeleteBackups(user: Option[LoggedUser])

  case class CleanMetadata(user: Option[LoggedUser])

  case class DeleteBackup(fileName: String, user: Option[LoggedUser])

}

