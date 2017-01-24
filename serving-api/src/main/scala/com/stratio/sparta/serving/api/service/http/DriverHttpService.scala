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
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.spray.oauth2.client.OauthClient
import com.wordnik.swagger.annotations._
import spray.routing.Route

import scala.util.Try

@Api(value = HttpConstant.DriverPath, description = "Operations over driver")
trait DriverHttpService extends BaseHttpService with OauthClient {

  override def routes: Route = download

  @Path("/{fileName}")
  @ApiOperation(value = "Download the driver.",
    notes = "....",
    httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fileName",
      value = "Name of the jar",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  def download: Route =
    get {
      pathPrefix(HttpConstant.DriverPath) {
        getFromDirectory(
          Try(SpartaConfig.getDetailConfig.get.getString(AppConstant.DriverPackageLocation))
            .getOrElse(AppConstant.DefaultDriverPackageLocation))
      }
    }

}
