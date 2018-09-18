/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.wordnik.swagger.annotations._
import spray.http.StatusCodes
import spray.routing._

@Api(value = HttpConstant.AppStatus, description = "Sparta service status")
trait AppStatusHttpService extends BaseHttpService {

  override def routes(user: Option[LoggedUser] = None): Route = checkStatus

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
          if (!CuratorFactoryHolder.getInstance().getZookeeperClient.getZooKeeper.getState.isConnected)
            throw new ServerException(ErrorModel.toString(ErrorModel(
              StatusCodes.InternalServerError.intValue,
              AppStatus,
              ErrorCodesMessages.getOrElse(AppStatus, UnknownError)
            )))
          else StatusCodes.OK
        }
      }
    }
  }
}
