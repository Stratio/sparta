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

import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.helpers.InfoHelper
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.info.VersionInfo
import com.wordnik.swagger.annotations._
import spray.routing._

import scala.util.Try

@Api(value = HttpConstant.ServerInfoPath, description = "Operations about information of service")
trait InfoServiceHttpService extends BaseHttpService {

  override def routes(user: Option[LoggedUser] = None): Route = getInfo

  @Path("")
  @ApiOperation(value = "Return server version info",
    notes = "Return server version info",
    httpMethod = "GET")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Return version info", response = classOf[VersionInfo])))
  def getInfo: Route = {
    path(HttpConstant.ServerInfoPath) {
      get {
        complete {
          Try(InfoHelper.getVersionInfo).getOrElse(
            throw new ServingCoreException(ErrorModel.toString(
              new ErrorModel(ErrorModel.CodeUnknown, s"Imposible to extract service information")
            ))
          )
        }
      }
    }
  }
}
