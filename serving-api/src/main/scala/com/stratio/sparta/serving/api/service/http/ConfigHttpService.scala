/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparta.serving.api.actor.ConfigActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.frontend.FrontendConfiguration
import com.stratio.sparta.serving.core.models.{ErrorModel, SpartaSerializer}
import com.wordnik.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import spray.http.StatusCodes
import spray.routing.Route

import scala.util.Try


@Api(value = HttpConstant.ConfigPath, description = "Operations over Sparta Configuration")
trait ConfigHttpService extends BaseHttpService {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    ConfigurationUnexpected,
    ErrorCodesMessages.getOrElse(ConfigurationUnexpected, UnknownError)
  )

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
        context =>
          for {
            response <- (supervisor ? FindAll(user))
              .mapTo[Either[Try[FrontendConfiguration], UnauthorizedResponse]]
          } yield getResponse(context, ConfigurationFind, response, genericError)
      }
    }
  }

}
