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
import com.stratio.sparta.serving.api.actor.MetadataActor.ExecuteBackup
import com.stratio.sparta.serving.api.actor.MetadataActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.utils.{BackupRestoreUtils, FileActorUtils}
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.helpers.InfoHelper
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.files.{BackupRequest, SpartaFilesResponse}
import spray.http.BodyPart
import spray.httpx.Json4sJacksonSupport

import scala.util.{Failure, Success, Try}

class MetadataActor extends Actor with Json4sJacksonSupport with BackupRestoreUtils with SpartaSerializer
  with FileActorUtils {

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

  override def receive: Receive = {
    case UploadBackups(files) => if (files.isEmpty) errorResponse() else uploadBackups(files)
    case ListBackups => browseBackups()
    case BuildBackup => buildBackup()
    case DeleteBackups => deleteBackups()
    case CleanMetadata => cleanMetadata()
    case DeleteBackup(fileName) => deleteBackup(fileName)
    case ExecuteBackup(backupRequest) => executeBackup(backupRequest)
    case _ => log.info("Unrecognized message in Backup/Restore Actor")
  }

  def executeBackup(backupRequest: BackupRequest): Unit =
    sender ! BackupResponse(Try{
      importer("/", s"$targetDir/${backupRequest.fileName}", backupRequest.deleteAllBefore)
    })

  def errorResponse(): Unit =
    sender ! SpartaFilesResponse(Failure(new IllegalArgumentException(s"Almost one file is expected")))

  def deleteBackups(): Unit = sender ! BackupResponse(deleteFiles())

  def cleanMetadata(): Unit = sender ! BackupResponse(Try(cleanZk(BaseZKPath)))

  def buildBackup(): Unit = {
    val format = DateTimeFormat.forPattern("yyyy-MM-dd-hh:mm:ss")
    val versionInfo = InfoHelper.getVersionInfo
    Try {
      dump(BaseZKPath, s"$targetDir/backup-${format.print(DateTime.now)}-${versionInfo.pomVersion}.json")
    } match {
      case Success(_) =>
        sender ! SpartaFilesResponse(browseDirectory())
      case Failure(e) =>
        sender ! BackupResponse(Try(throw e))
    }
  }

  def deleteBackup(fileName: String): Unit = sender ! BackupResponse(deleteFile(fileName))

  def browseBackups(): Unit = sender ! SpartaFilesResponse(browseDirectory())

  def uploadBackups(files: Seq[BodyPart]): Unit = sender ! SpartaFilesResponse(uploadFiles(files))
}

object MetadataActor {

  case class UploadBackups(files: Seq[BodyPart])

  case class BackupResponse(status: Try[_])

  case class ExecuteBackup(backupRequest: BackupRequest)

  case object ListBackups

  case object BuildBackup

  case object DeleteBackups

  case object CleanMetadata

  case class DeleteBackup(fileName: String)

}

