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
import com.stratio.sparta.serving.api.actor.PluginActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.spray.oauth2.client.OauthClient
import com.wordnik.swagger.annotations._
import spray.http._
import spray.httpx.unmarshalling.{FormDataUnmarshallers, Unmarshaller}
import spray.routing.Route

import scala.util.{Failure, Success, Try}

@Api(value = HttpConstant.PluginsPath, description = "Operations over plugins: now only to upload/download jars.")
trait PluginsHttpService extends BaseHttpService with OauthClient {

  implicit def unmarshaller[T: Manifest]: Unmarshaller[MultipartFormData] =
    FormDataUnmarshallers.MultipartFormDataUnmarshaller

  override def routes: Route = upload ~ download ~ getAll ~ deleteAllFiles ~ deleteFile

  @Path("")
  @ApiOperation(value = "Upload a file to plugin directory.",
    notes = "Creates a file in the server filesystem with the uploaded jar.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "file",
      value = "The jar",
      dataType = "file",
      required = true,
      paramType = "formData")
  ))
  def upload: Route = {
    path(HttpConstant.PluginsPath) {
      put {
        entity(as[MultipartFormData]) { form =>
          complete {
            for {
              response <- (supervisor ? UploadPlugins(form.fields)).mapTo[PluginResponse]
            } yield response match {
              case PluginResponse(Success(newFilesUris: Seq[String])) => FilesUris(newFilesUris)
              case PluginResponse(Failure(exception)) => throw exception
            }
          }
        }
      }
    }
  }

  @Path("/{fileName}")
  @ApiOperation(value = "Download a file from the plugin directory.",
    httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fileName",
      value = "Name of the jar",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  def download: Route =
    path(HttpConstant.PluginsPath) {
      get {
        getFromDirectory(
          Try(SpartaConfig.getDetailConfig.get.getString(AppConstant.PluginsPackageLocation))
            .getOrElse(AppConstant.DefaultPluginsPackageLocation))
      }
    }

  @Path("")
  @ApiOperation(value = "Browse all plugins uploaded",
    notes = "Finds all plugins.",
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def getAll: Route =
    path(HttpConstant.PluginsPath) {
      get {
        complete {
          for {
            response <- (supervisor ? ListPlugins).mapTo[PluginResponse]
          } yield response match {
            case PluginResponse(Success(filesUris: Seq[String])) => FilesUris(filesUris)
            case PluginResponse(Failure(exception)) => throw exception
          }
        }
      }
    }

  @Path("")
  @ApiOperation(value = "Delete all plugins uploaded",
    notes = "Delete all plugins.",
    httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def deleteAllFiles: Route =
    path(HttpConstant.PluginsPath) {
      delete {
        complete {
          for {
            response <- (supervisor ? DeletePlugins).mapTo[PluginResponse]
          } yield response match {
            case PluginResponse(Success(_)) => StatusCodes.OK
            case PluginResponse(Failure(exception)) => throw exception
          }
        }
      }
    }

  @Path("/{fileName}")
  @ApiOperation(value = "Delete one plugin uploaded",
    notes = "Delete one plugin.",
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
  def deleteFile: Route = {
    path(HttpConstant.PluginsPath / Segment) { file =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeletePlugin(file)).mapTo[PluginResponse]
          } yield response match {
            case PluginResponse(Success(_)) => StatusCodes.OK
            case PluginResponse(Failure(exception)) => throw exception
          }
        }
      }
    }
  }

  case class FilesUris(uris: Seq[String])

}
