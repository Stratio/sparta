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
import com.stratio.sparta.serving.api.actor.ConfigActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.wordnik.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import spray.routing.Route


@Api(value = HttpConstant.ConfigPath, description = "Operations on Sparta Configuration")
trait ConfigHttpService extends BaseHttpService with SpartaSerializer {

  override def routes(user: Option[LoggedUser] = None): Route = getAll(user)

  @Path("")
  @ApiOperation(value = "Retrieve all frontend configuration settings",
    notes = "Returns configuration value for frontend",
    httpMethod = "GET")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def getAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ConfigPath) {
      get {
        complete {
          for {
            response <- (supervisor ? FindAll).mapTo[ConfigResponse]
          } yield response match {
            case ConfigResponse(config) => config
          }
        }
      }
    }
  }

}
