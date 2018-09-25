/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparta.serving.api.actor.MlModelActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.intelligence.IntelligenceModel
import com.wordnik.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import spray.http.StatusCodes
import spray.routing.Route


@Api(value = HttpConstant.MlModelsPath, description = "Operations over Machine Learning models repository")
trait MlModelsHttpService extends BaseHttpService {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    ConfigurationUnexpected,
    ErrorCodesMessages.getOrElse(ConfigurationUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route = getAll(user)

  @Path("")
  @ApiOperation(value = "Retrieve all machine learning models from intelligence repository",
    notes = "Retrieve all machine learning models from intelligence repository",
    httpMethod = "GET",
    response = classOf[Seq[IntelligenceModel]],
    responseContainer = "List"
  )
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def getAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.MlModelsPath) {
      get {
        context =>
          for {
            response <- (supervisor ? FindAllModels(user))
              .mapTo[Either[ResponseListIntelligenceModels, UnauthorizedResponse]]
          } yield getResponse(context, MlModelsServiceFindAll, response, genericError)
      }
    }
  }

}
