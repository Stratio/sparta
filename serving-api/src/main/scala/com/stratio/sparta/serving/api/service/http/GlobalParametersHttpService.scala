/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparta.serving.api.actor.GlobalParametersActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.constants.HttpConstant._
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.authorization.LoggedUser
import com.stratio.sparta.serving.core.models.parameters.{GlobalParameters, ParameterVariable}
import com.wordnik.swagger.annotations._
import spray.http.StatusCodes
import spray.routing._

@Api(value = HttpConstant.GlobalParametersPath, description = "Operations over global parameters", position = 0)
trait GlobalParametersHttpService extends BaseHttpService {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    GlobalParametersServiceUnexpected,
    ErrorCodesMessages.getOrElse(GlobalParametersServiceUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route =
    find(user) ~ update(user) ~ create(user) ~ deleteEnv(user) ~
      createVariable(user) ~ updateVariable(user) ~ deleteVariable(user) ~ findVariable(user)

  @ApiOperation(value = "Find global parameters",
    notes = "Returns an global parameters with all variables",
    httpMethod = "GET",
    response = classOf[GlobalParameters]
  )
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def find(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GlobalParametersPath) {
      get {
        context =>
          for {
            response <- (supervisor ? FindGlobalParameters(user)).mapTo[Either[ResponseGlobalParameters, UnauthorizedResponse]]
          } yield getResponse(context, GlobalParametersServiceFindEnvironment, response, genericError)
      }
    }
  }

  @Path("/variable/{name}")
  @ApiOperation(value = "Finds an variable by its name",
    notes = "Find a variable by its name",
    httpMethod = "GET",
    response = classOf[ParameterVariable])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name",
      value = "name of the global parameters variable",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findVariable(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GlobalParametersPath / "variable" / Segment) { (name) =>
      get {
        context =>
          for {
            response <- (supervisor ? FindGlobalParametersVariable(name, user))
              .mapTo[Either[ResponseGlobalParametersVariable, UnauthorizedResponse]]
          } yield getResponse(context, GlobalParametersServiceFindEnvironmentVariable, response, genericError)
      }
    }
  }

  @Path("")
  @ApiOperation(value = "Delete global parameters",
    notes = "Deletes the global parameters variables",
    httpMethod = "DELETE")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteEnv(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GlobalParametersPath) {
      pathEndOrSingleSlash {
        delete {
          complete {
            for {
              response <- (supervisor ? DeleteGlobalParameters(user))
                .mapTo[Either[ResponseBoolean, UnauthorizedResponse]]
            } yield deletePostPutResponse(GlobalParametersServiceDeleteEnvironment, response, genericError, StatusCodes.OK)
          }
        }
      }
    }
  }

  @Path("/variable/{name}")
  @ApiOperation(value = "Deletes an global parameters variable by its name",
    notes = "Deletes an global parameters variable by its name",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name",
      value = "name of the global parameters variable",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteVariable(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GlobalParametersPath / "variable" / Segment) { (name) =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteGlobalParametersVariable(name, user))
              .mapTo[Either[ResponseGlobalParameters, UnauthorizedResponse]]
          } yield {
            deletePostPutResponse(GlobalParametersDeleteEnvironmentVariable, response, genericError, StatusCodes.OK)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Updates an global parameters.",
    notes = "Updates an global parameters.",
    httpMethod = "PUT",
    response = classOf[GlobalParameters]
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "global parameters",
      value = "global parameters json",
      dataType = "Environment",
      required = true,
      paramType = "body")))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def update(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GlobalParametersPath) {
      put {
        entity(as[GlobalParameters]) { request =>
          complete {
            for {
              response <- (supervisor ? UpdateGlobalParameters(request, user))
                .mapTo[Either[ResponseGlobalParametersVariable, UnauthorizedResponse]]
            } yield deletePostPutResponse(GlobalParametersServiceUpdateEnvironment, response, genericError, StatusCodes.OK)
          }
        }
      }
    }
  }

  @Path("/variable")
  @ApiOperation(value = "Updates an global parameters variable.",
    notes = "Updates an global parameters variable.",
    httpMethod = "PUT",
    response = classOf[GlobalParameters]
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "global parameters variable",
      value = "global parameters variable json",
      dataType = "EnvironmentVariable",
      required = true,
      paramType = "body")))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def updateVariable(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GlobalParametersPath / "variable") {
      put {
        entity(as[ParameterVariable]) { request =>
          complete {
            for {
              response <- (supervisor ? UpdateGlobalParametersVariable(request, user))
                .mapTo[Either[ResponseGlobalParameters, UnauthorizedResponse]]
            } yield deletePostPutResponse(GlobalParametersServiceUpdateEnvironment, response, genericError, StatusCodes.OK)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates a global parameters",
    notes = "Returns the global parameters",
    httpMethod = "POST",
    response = classOf[GlobalParameters])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "global parameters",
      value = "global parameters json",
      dataType = "Environment",
      required = true,
      paramType = "body")))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def create(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GlobalParametersPath) {
      post {
        entity(as[GlobalParameters]) { request =>
          complete {
            for {
              response <- (supervisor ? CreateGlobalParameters(request, user))
                .mapTo[Either[ResponseGlobalParameters, UnauthorizedResponse]]
            } yield deletePostPutResponse(GlobalParametersServiceCreateEnvironment, response, genericError)
          }
        }
      }
    }
  }

  @Path("/variable")
  @ApiOperation(value = "Creates a global parameters variable",
    notes = "Returns the global parameters variable",
    httpMethod = "POST",
    response = classOf[ParameterVariable])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "global parameters variable",
      value = "global parameters variable json",
      dataType = "EnvironmentVariable",
      required = true,
      paramType = "body")))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def createVariable(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GlobalParametersPath / "variable") {
      post {
        entity(as[ParameterVariable]) { request =>
          complete {
            for {
              response <- (supervisor ? CreateGlobalParametersVariable(request, user))
                .mapTo[Either[ResponseGlobalParametersVariable, UnauthorizedResponse]]
            } yield deletePostPutResponse(GlobalParametersServiceCreateEnvironmentVariable, response, genericError)
          }
        }
      }
    }
  }

}
