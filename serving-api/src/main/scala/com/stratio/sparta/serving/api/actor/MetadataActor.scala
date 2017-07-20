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
import com.stratio.sparta.serving.api.actor.MetadataActor.ExecuteBackup
import com.stratio.sparta.serving.api.actor.MetadataActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.utils.{BackupRestoreUtils, FileActorUtils}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.helpers.InfoHelper
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.files.{BackupRequest, SpartaFilesResponse}
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import spray.http.BodyPart
import spray.httpx.Json4sJacksonSupport

import scala.util.{Failure, Success, Try}

class MetadataActor(val secManagerOpt: Option[SpartaSecurityManager]) extends Actor
  with Json4sJacksonSupport
  with BackupRestoreUtils
  with SpartaSerializer
  with FileActorUtils
  with ActionUserAuthorize{

  //The dir where the backups will be saved
  val targetDir = Try(SpartaConfig.getDetailConfig.get.getString(BackupsLocation)).getOrElse(DefaultBackupsLocation)
  override val apiPath = HttpConstant.MetadataPath
  override val patternFileName = Option(Pattern.compile(""".*\.json""").asPredicate())

  //The dir where the jars will be saved
  val zkConfig = Try(SpartaConfig.getZookeeperConfig.get)
    .getOrElse(throw new ServingCoreException("Zookeeper configuration is mandatory"))
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
    sender ! Left(SpartaFilesResponse(Failure(new IllegalArgumentException(s"At least one file is expected"))))

  def uploadBackups(files: Seq[BodyPart], user: Option[LoggedUser]): Unit = {
    def callback() = SpartaFilesResponse(uploadFiles(files))

    securityActionAuthorizer[SpartaFilesResponse](secManagerOpt, user, Map(ResourceType -> Upload), callback)
  }

  def browseBackups(user: Option[LoggedUser]): Unit = {
    def callback() = SpartaFilesResponse(browseDirectory())

    securityActionAuthorizer[SpartaFilesResponse](secManagerOpt, user, Map(ResourceType -> View), callback)
  }

  def buildBackup(user: Option[LoggedUser]): Unit = {
    val format = DateTimeFormat.forPattern("yyyy-MM-dd-hh:mm:ss")
    val appInfo = InfoHelper.getAppInfo
    def callback () = Try {
      dump(BaseZKPath, s"$targetDir/backup-${format.print(DateTime.now)}-${appInfo.pomVersion}.json")
    } match {
      case Success(_) =>
       SpartaFilesResponse(browseDirectory())
      case Failure(e) =>
       SpartaFilesResponse(Try(throw e))
    }

    securityActionAuthorizer[SpartaFilesResponse](secManagerOpt, user, Map(ResourceType -> Create), callback)
  }

  def deleteBackups(user: Option[LoggedUser]): Unit = {
    def callback() = BackupResponse(deleteFiles())

    securityActionAuthorizer[BackupResponse](secManagerOpt, user, Map(ResourceType -> Delete), callback)
  }

  def cleanMetadata(user: Option[LoggedUser]): Unit = {
    def callback() = BackupResponse(Try(cleanZk(BaseZKPath)))

    securityActionAuthorizer[BackupResponse](secManagerOpt, user, Map(ResourceType -> Delete), callback)
  }

  def deleteBackup(fileName: String, user: Option[LoggedUser]): Unit = {
    def callback() =  BackupResponse(deleteFile(fileName))

    securityActionAuthorizer[BackupResponse](secManagerOpt, user, Map(ResourceType -> Delete), callback)
  }

  def executeBackup(backupRequest: BackupRequest, user: Option[LoggedUser]): Unit = {
    def callback() = BackupResponse(Try{
      importer("/", s"$targetDir/${backupRequest.fileName}", backupRequest.deleteAllBefore)
    })

    securityActionAuthorizer[BackupResponse](secManagerOpt, user, Map(ResourceType -> Execute), callback)
  }
}

object MetadataActor {

  case class UploadBackups(files: Seq[BodyPart], user: Option[LoggedUser])

  case class BackupResponse(status: Try[_])

  case class ExecuteBackup(backupRequest: BackupRequest, user: Option[LoggedUser])

  case class ListBackups(user: Option[LoggedUser])

  case class BuildBackup(user: Option[LoggedUser])

  case class DeleteBackups(user: Option[LoggedUser])

  case class CleanMetadata(user: Option[LoggedUser])

  case class DeleteBackup(fileName: String, user: Option[LoggedUser])

}

