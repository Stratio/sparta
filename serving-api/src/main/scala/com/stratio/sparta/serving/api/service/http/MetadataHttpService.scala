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

package com.stratio.sparta.serving.api.service.http

import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparta.serving.api.actor.MetadataActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.files.{BackupRequest, SpartaFilesResponse}
import com.stratio.spray.oauth2.client.OauthClient
import com.wordnik.swagger.annotations._
import spray.http._
import spray.httpx.unmarshalling.{FormDataUnmarshallers, Unmarshaller}
import spray.routing.Route

import scala.util.{Failure, Success, Try}

@Api(value = HttpConstant.MetadataPath, description = "Operations over Sparta metadata")
trait MetadataHttpService extends BaseHttpService with OauthClient {

  implicit def unmarshaller[T: Manifest]: Unmarshaller[MultipartFormData] =
    FormDataUnmarshallers.MultipartFormDataUnmarshaller

  override def routes(user: Option[LoggedUser] = None): Route = uploadBackup(user) ~ executeBackup(user) ~
    downloadBackup(user) ~ getAllBackups(user) ~ deleteAllBackups(user) ~ deleteBackup(user) ~ buildBackup(user) ~
    cleanMetadata(user)

  @Path("/backup/build")
  @ApiOperation(value = "Build a new backup of the metadata",
    notes = "Build a new backup.",
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def buildBackup(user: Option[LoggedUser]): Route =
    path(HttpConstant.MetadataPath / "backup" / "build") {
      get {
        complete {
          for {
            response <- supervisor ? BuildBackup
          } yield response match {
            case SpartaFilesResponse(Success(filesUris)) => filesUris
            case SpartaFilesResponse(Failure(exception)) => throw exception
            case BackupResponse(Failure(exception)) => throw exception
            case BackupResponse(Success(_)) => throw new ServingCoreException("Backup build not completed")
          }
        }
      }
    }

  @Path("/backup")
  @ApiOperation(value = "Execute one backup.",
    notes = "Execute backup.",
    httpMethod = "POST",
    response = classOf[BackupRequest])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "backup",
      defaultValue = "",
      value = "backup request json",
      dataType = "BackupRequest",
      required = true,
      paramType = "body")))
  def executeBackup(user: Option[LoggedUser]): Route = {
    path(HttpConstant.MetadataPath / "backup") {
      post {
        entity(as[BackupRequest]) { backupRequest =>
          complete {
            for {
              response <- (supervisor ? ExecuteBackup(backupRequest)).mapTo[BackupResponse]
            } yield response match {
              case BackupResponse(Success(_)) => StatusCodes.OK
              case BackupResponse(Failure(exception)) => throw exception
            }
          }
        }
      }
    }
  }

  @Path("/backup")
  @ApiOperation(value = "Upload one backup file.",
    notes = "Creates a backup file in the server filesystem with the uploaded backup file.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "file",
      value = "The json backup",
      dataType = "file",
      required = true,
      paramType = "formData")
  ))
  def uploadBackup(user: Option[LoggedUser]): Route = {
    path(HttpConstant.MetadataPath / "backup") {
      put {
        entity(as[MultipartFormData]) { form =>
          complete {
            for {
              response <- (supervisor ? UploadBackups(form.fields)).mapTo[SpartaFilesResponse]
            } yield response match {
              case SpartaFilesResponse(Success(newFilesUris)) => newFilesUris
              case SpartaFilesResponse(Failure(exception)) => throw exception
            }
          }
        }
      }
    }
  }

  @Path("/backup/{fileName}")
  @ApiOperation(value = "Download a backup file from directory backups.",
    httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fileName",
      value = "Name of the backup",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  def downloadBackup(user: Option[LoggedUser]): Route =
    get {
      pathPrefix(HttpConstant.MetadataPath / "backup") {
        getFromDirectory(
          Try(SpartaConfig.getDetailConfig.get.getString(AppConstant.BackupsLocation))
            .getOrElse(AppConstant.DefaultBackupsLocation))
      }
    }

  @Path("/backup")
  @ApiOperation(value = "Browse all backups uploaded",
    notes = "Finds all backups.",
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def getAllBackups(user: Option[LoggedUser]): Route =
    path(HttpConstant.MetadataPath / "backup") {
      get {
        complete {
          for {
            response <- (supervisor ? ListBackups).mapTo[SpartaFilesResponse]
          } yield response match {
            case SpartaFilesResponse(Success(filesUris)) => filesUris
            case SpartaFilesResponse(Failure(exception)) => throw exception
          }
        }
      }
    }

  @Path("/backup")
  @ApiOperation(value = "Delete all backups uploaded",
    notes = "Delete all backups.",
    httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def deleteAllBackups(user: Option[LoggedUser]): Route =
    path(HttpConstant.MetadataPath / "backup") {
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteBackups).mapTo[BackupResponse]
          } yield response match {
            case BackupResponse(Success(_)) => StatusCodes.OK
            case BackupResponse(Failure(exception)) => throw exception
          }
        }
      }
    }

  @Path("/backup/{fileName}")
  @ApiOperation(value = "Delete one backup uploaded or created",
    notes = "Delete one backup.",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fileName",
      value = "Name of the backup",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def deleteBackup(user: Option[LoggedUser]): Route = {
    path(HttpConstant.MetadataPath / "backup" / Segment) { file =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteBackup(file)).mapTo[BackupResponse]
          } yield response match {
            case BackupResponse(Success(_)) => StatusCodes.OK
            case BackupResponse(Failure(exception)) => throw exception
          }
        }
      }
    }
  }

  @Path("")
  @ApiOperation(value = "Clean metadata",
    notes = "Clean metadata.",
    httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def cleanMetadata(user: Option[LoggedUser]): Route =
    path(HttpConstant.MetadataPath) {
      delete {
        complete {
          for {
            response <- (supervisor ? CleanMetadata).mapTo[BackupResponse]
          } yield response match {
            case BackupResponse(Success(_)) => StatusCodes.OK
            case BackupResponse(Failure(exception)) => throw exception
          }
        }
      }
    }
}
