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

import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.wordnik.swagger.annotations._
import org.apache.curator.framework.CuratorFramework
import spray.http.StatusCodes
import spray.routing._

@Api(value = HttpConstant.AppStatus, description = "Sparta service status")
trait AppStatusHttpService extends BaseHttpService {

  override def routes(user: Option[LoggedUser] = None): Route = checkStatus

  val curatorInstance : CuratorFramework

  @ApiOperation(value = "Checks Sparta status based on the Zookeeper connection",
    notes = "Returns Sparta status",
    httpMethod = "GET",
    response = classOf[String],
    responseContainer = "List")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def checkStatus: Route = {
    path(HttpConstant.AppStatus) {
      get {
        complete {
          if (!curatorInstance.getZookeeperClient.getZooKeeper.getState.isConnected)
            throw new ServingCoreException(ErrorModel.toString(
              new ErrorModel(ErrorModel.CodeUnknown, s"Zk isn't connected at" +
                s" ${curatorInstance.getZookeeperClient.getCurrentConnectionString}.")
            ))
          else StatusCodes.OK
        }
      }
    }
  }
}
