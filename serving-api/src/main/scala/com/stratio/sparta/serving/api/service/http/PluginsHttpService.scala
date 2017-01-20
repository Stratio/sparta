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
import com.stratio.sparta.serving.api.actor.PluginActor.{PluginResponse, UploadFile}
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.spray.oauth2.client.OauthClient
import com.wordnik.swagger.annotations._
import spray.http._
import spray.httpx.unmarshalling.{FormDataUnmarshallers, Unmarshaller}
import spray.routing.Route

import scala.util.{Failure, Success}

@Api(value = HttpConstant.PluginsPath, description = "Operations over plugins: now only for jars")
trait PluginsHttpService extends BaseHttpService with OauthClient {

  implicit def unmarshaller[T: Manifest]: Unmarshaller[MultipartFormData] =
    FormDataUnmarshallers.MultipartFormDataUnmarshaller

  override def routes: Route = create

  @Path("/{fileName}")
  @ApiOperation(value = "Upload a file to plugin directory.",
    notes = "Creates a file in the server filesystem with the uploaded jar.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fileName",
      value = "Name of the jar",
      dataType = "String",
      required = true,
      paramType = "path"),
    new ApiImplicitParam(name = "file",
      value = "The jar",
      dataType = "file",
      required = true,
      paramType = "formData")
  ))
  def create: Route = {
    path(HttpConstant.PluginsPath / Segment) { (fileName) =>
      put {
        entity(as[MultipartFormData]) { form =>
          complete {
            for {
              response <- supervisor ? UploadFile(fileName, form.fields)
            } yield response match {
              case PluginResponse(Success(a: String)) => FileCreated(a)
              case PluginResponse(Failure(exception)) => throw exception
            }
          }
        }
      }
    }
  }

  case class FileCreated(path: String)
}
