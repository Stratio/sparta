/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import javax.ws.rs.Path

import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.helpers.InfoHelper
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel.{ErrorCodesMessages, UnknownError}
import com.stratio.sparta.serving.core.models.authorization.LoggedUser
import com.stratio.sparta.serving.core.models.info.AppInfo
import com.wordnik.swagger.annotations._
import spray.http.StatusCodes
import spray.routing._

import scala.util.Try

@Api(value = HttpConstant.AppInfoPath, description = "Information of Sparta service")
trait AppInfoHttpService extends BaseHttpService {

  override def routes(user: Option[LoggedUser] = None): Route = getInfo

  @Path("")
  @ApiOperation(value = "Return the server info",
    notes = "Return the server info",
    httpMethod = "GET")
  @ApiResponses(Array(new ApiResponse(code = 200, message = "Return the server info", response = classOf[AppInfo])))
  def getInfo: Route = {
    path(HttpConstant.AppInfoPath) {
      get {
        complete {
          Try(InfoHelper.getAppInfo).getOrElse(
            throw new ServerException(ErrorModel.toString(ErrorModel(
              StatusCodes.InternalServerError.intValue,
              ErrorModel.AppInfo,
              ErrorCodesMessages.getOrElse(ErrorModel.AppInfo, UnknownError)
            )))
          )
        }
      }
    }
  }
}
