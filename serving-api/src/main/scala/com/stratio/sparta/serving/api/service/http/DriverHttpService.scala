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
import com.stratio.sparta.serving.api.actor.DriverActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.policy.files.JarFilesResponse
import com.stratio.spray.oauth2.client.OauthClient
import com.wordnik.swagger.annotations._
import spray.http._
import spray.httpx.unmarshalling.{FormDataUnmarshallers, Unmarshaller}
import spray.routing.Route

import scala.util.{Failure, Success, Try}

@Api(value = HttpConstant.DriverPath, description = "Operations over plugins: now only to upload/download jars")
trait DriverHttpService extends BaseHttpService with OauthClient {

  implicit def unmarshaller[T: Manifest]: Unmarshaller[MultipartFormData] =
    FormDataUnmarshallers.MultipartFormDataUnmarshaller

  override def routes(user: Option[LoggedUser] = None): Route = upload(user) ~
    download(user) ~ getAll(user) ~ deleteAllFiles(user) ~ deleteFile(user)

  @Path("")
  @ApiOperation(value = "Uploads a file to the driver directory",
    notes = "Creates a file in the server filesystem with the uploaded jar",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "file",
      value = "The jar",
      dataType = "file",
      required = true,
      paramType = "formData")
  ))
  def upload(user: Option[LoggedUser]): Route = {
    path(HttpConstant.DriverPath) {
      put {
        entity(as[MultipartFormData]) { form =>
          complete {
            for {
              response <- (supervisor ? UploadDrivers(form.fields, user))
                .mapTo[Either[JarFilesResponse,UnauthorizedResponse]]
            } yield response match {
              case Left(JarFilesResponse(Success(newFilesUris))) => newFilesUris
              case Left(JarFilesResponse(Failure(exception))) => throw exception
              case Right(UnauthorizedResponse(exception)) => throw exception
              case _ => throw new RuntimeException("Unexpected behaviour in drivers")
            }
          }
        }
      }
    }
  }

  @Path("/{fileName}")
  @ApiOperation(value = "Downloads a file from the driver directory",
    httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fileName",
      value = "Name of the jar",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  def download(user: Option[LoggedUser]): Route =
    get {
      pathPrefix(HttpConstant.DriverPath) {
        getFromDirectory(
          Try(SpartaConfig.getDetailConfig.get.getString(AppConstant.DriverPackageLocation))
            .getOrElse(AppConstant.DefaultDriverPackageLocation))
      }
    }

  @Path("")
  @ApiOperation(value = "Browse all drivers uploaded",
    notes = "Finds all drivers",
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def getAll(user: Option[LoggedUser]): Route =
    path(HttpConstant.DriverPath) {
      get {
        complete {
          for {
            response <- (supervisor ? ListDrivers(user))
              .mapTo[Either[JarFilesResponse,UnauthorizedResponse]]
          } yield response match {
            case Left(JarFilesResponse(Success(filesUris))) => filesUris
            case Left(JarFilesResponse(Failure(exception))) => throw exception
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in drivers")
          }
        }
      }
    }

  @Path("")
  @ApiOperation(value = "Deletes all drivers uploaded",
    notes = "Deletes all drivers",
    httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def deleteAllFiles(user: Option[LoggedUser]): Route =
    path(HttpConstant.DriverPath) {
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteDrivers(user))
              .mapTo[Either[DriverResponse,UnauthorizedResponse]]
          } yield response match {
            case Left(DriverResponse(Success(_))) => StatusCodes.OK
            case Left(DriverResponse(Failure(exception))) => throw exception
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in drivers")
          }
        }
      }
    }

  @Path("/{fileName}")
  @ApiOperation(value = "Deletes a driver uploaded by its name",
    notes = "Deletes one driver",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fileName",
      value = "Name of the jar",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def deleteFile(user: Option[LoggedUser]): Route = {
    path(HttpConstant.DriverPath / Segment) { file =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteDriver(file, user))
              .mapTo[Either[DriverResponse,UnauthorizedResponse]]
          } yield response match {
            case Left(DriverResponse(Success(_))) => StatusCodes.OK
            case Left(DriverResponse(Failure(exception))) => throw exception
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in drivers")
          }
        }
      }
    }
  }
}
