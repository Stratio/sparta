/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparta.serving.api.actor.DriverActor.SpartaFilesResponse
import com.stratio.sparta.serving.api.actor.MetadataActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.files.BackupRequest
import com.stratio.spray.oauth2.client.OauthClient
import com.wordnik.swagger.annotations._
import spray.http._
import spray.httpx.unmarshalling.{FormDataUnmarshallers, Unmarshaller}
import spray.routing.Route

import scala.util.Try

@Api(value = HttpConstant.MetadataPath, description = "Operations over Sparta metadata")
trait MetadataHttpService extends BaseHttpService with OauthClient {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    MetadataServiceUnexpected,
    ErrorCodesMessages.getOrElse(MetadataServiceUnexpected, UnknownError)
  )

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
        context =>
          for {
            response <- (supervisor ? BuildBackup(user))
              .mapTo[Either[SpartaFilesResponse, UnauthorizedResponse]]
          } yield getResponse(context, MetadataServiceBuildBackup, response, genericError)
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
              response <- (supervisor ? ExecuteBackup(backupRequest, user))
                .mapTo[Either[BackupResponse, UnauthorizedResponse]]
            } yield deletePostPutResponse(MetadataServiceExecuteBackup, response, genericError, StatusCodes.OK)
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
              response <- (supervisor ? UploadBackups(form.fields, user))
                .mapTo[Either[SpartaFilesResponse, UnauthorizedResponse]]
            } yield deletePostPutResponse(MetadataServiceUploadBackup, response, genericError)
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
        context =>
          for {
            response <- (supervisor ? ListBackups(user))
              .mapTo[Either[SpartaFilesResponse, UnauthorizedResponse]]
          } yield getResponse(context, MetadataServiceFindAllBackups, response, genericError)
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
            response <- (supervisor ? DeleteBackups(user))
              .mapTo[Either[BackupResponse, UnauthorizedResponse]]
          } yield deletePostPutResponse(MetadataServiceDeleteAllBackups, response, genericError, StatusCodes.OK)
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
            response <- (supervisor ? DeleteBackup(file, user))
              .mapTo[Either[BackupResponse, UnauthorizedResponse]]
          } yield deletePostPutResponse(MetadataServiceDeleteBackup, response, genericError, StatusCodes.OK)
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
            response <- (supervisor ? CleanMetadata(user))
              .mapTo[Either[BackupResponse, UnauthorizedResponse]]
          } yield deletePostPutResponse(MetadataServiceCleanAll, response, genericError, StatusCodes.OK)
        }
      }
    }
}
