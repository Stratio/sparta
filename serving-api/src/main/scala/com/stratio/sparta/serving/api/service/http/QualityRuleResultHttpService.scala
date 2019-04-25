/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparta.serving.api.actor.QualityRuleResultActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.constants.HttpConstant.ResponseBoolean
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.authorization.LoggedUser
import com.stratio.sparta.serving.core.models.governance.QualityRuleResult
import com.wordnik.swagger.annotations._
import spray.http.StatusCodes
import spray.routing.Route

@Api(value = HttpConstant.QualityRuleResultsPath, description = "Operations over Quality Rule results")
trait QualityRuleResultHttpService extends BaseHttpService{

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    ConfigurationUnexpected,
    ErrorCodesMessages.getOrElse(ConfigurationUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route = findAll(user) ~ findAllUnsent(user) ~
  findByExecutionId(user) ~ find(user) ~ create(user) ~ deleteAll(user) ~ deleteById(user) ~
  deleteByExecutionId(user)

  @Path("")
  @ApiOperation(value = "Finds all available quality rule results",
    notes = "Returns a list of quality rule results",
    httpMethod = "GET",
    response = classOf[List[QualityRuleResult]],
    responseContainer = "List")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.QualityRuleResultsPath) {
      get {
        context =>
          for {
            response <- (supervisor ? FindAll(user))
              .mapTo[Either[QualityRuleResultsResponse, UnauthorizedResponse]]
          } yield getResponse(context, QualityRuleResultServiceFindAll, response, genericError)
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "Finds a quality rule result by its id",
    notes = "Finds a quality rule result by its id",
    httpMethod = "GET",
    response = classOf[QualityRuleResult])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "Quality rule result id",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def find(user: Option[LoggedUser]): Route = {
    path(HttpConstant.QualityRuleResultsPath / Segment) { (id) =>
      get {
        context =>
          for {
            response <- (supervisor ? FindById(id, user))
              .mapTo[Either[QualityRuleResultResponse, UnauthorizedResponse]]
          } yield getResponse(context, QualityRuleResultServiceFindById, response, genericError)
      }
    }
  }

  @Path("/executionId/{executionId}")
  @ApiOperation(value = "Finds a quality rule result by its id",
    notes = "Finds a quality rule result by its id",
    httpMethod = "GET",
    response = classOf[List[QualityRuleResult]],
    responseContainer = "List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "executionId",
      value = "Execution id",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByExecutionId(user: Option[LoggedUser]): Route = {
    path(HttpConstant.QualityRuleResultsPath / "executionId" / Segment) { (executionId) =>
      get {
        context =>
          for {
            response <- (supervisor ? FindByExecutionId(executionId, user))
              .mapTo[Either[QualityRuleResultsResponse, UnauthorizedResponse]]
          } yield getResponse(context, QualityRuleResultServiceFindByExecutionId, response, genericError)
      }
    }
  }

  @Path("/allUnsent")
  @ApiOperation(value = "Returns all data quality results marked as non sent",
    notes = "Returns all data quality results marked as non sent",
    httpMethod = "GET",
    response = classOf[List[QualityRuleResult]],
    responseContainer = "List")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findAllUnsent(user: Option[LoggedUser]): Route = {
    path(HttpConstant.QualityRuleResultsPath / "allUnsent") {
      get {
        context =>
          for {
            response <- (supervisor ? FindAllUnsent(user))
              .mapTo[Either[QualityRuleResultsResponse, UnauthorizedResponse]]
          } yield getResponse(context, QualityRuleResultServiceFindAllUnsent, response, genericError)
      }
    }
  }

  @Path("")
  @ApiOperation(value = "Creates a quality rule result",
    notes = "Creates and returns the quality rule result",
    httpMethod = "POST",
    response = classOf[QualityRuleResult])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "qualityRuleResult",
      value = "Quality rule result json",
      dataType = "QualityRuleResult",
      required = true,
      paramType = "body")))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def create(user: Option[LoggedUser]): Route = {
    path(HttpConstant.QualityRuleResultsPath) {
      post {
        entity(as[QualityRuleResult]) { request =>
          complete {
            for {
              response <- (supervisor ? CreateQualityRuleResult(request, user))
                .mapTo[Either[QualityRuleResultResponse, UnauthorizedResponse]]
            } yield deletePostPutResponse(QualityRuleResultServiceCreate, response, genericError)
          }
        }
      }
    }
  }

  @Path("")
  @ApiOperation(value = "Deletes all quality rule results",
    notes = "Deletes all quality rule results",
    httpMethod = "DELETE")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.QualityRuleResultsPath) {
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteAll(user))
              .mapTo[Either[ResponseBoolean, UnauthorizedResponse]]
          } yield deletePostPutResponse(QualityRuleResultServiceDeleteAll, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "Deletes a quality rule result by its id",
    notes = "Deletes a quality rule result by its id",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "Quality rule result id",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteById(user: Option[LoggedUser]): Route = {
    path(HttpConstant.QualityRuleResultsPath / Segment) { (id) =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteById(id, user))
              .mapTo[Either[ResponseBoolean, UnauthorizedResponse]]
          } yield deletePostPutResponse(QualityRuleResultServiceDeleteById, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("/executionId/{executionId}")
  @ApiOperation(value = "Deletes all quality rules matching a given execution id",
    notes = "Deletes all quality rules matching a given execution id",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "Execution id",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteByExecutionId(user: Option[LoggedUser]): Route = {
    path(HttpConstant.QualityRuleResultsPath /  "executionId" / Segment) { (id) =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteByExecutionId(id, user))
              .mapTo[Either[ResponseBoolean, UnauthorizedResponse]]
          } yield deletePostPutResponse(QualityRuleResultServiceDeleteByExecutionId, response, genericError, StatusCodes.OK)
        }
      }
    }
  }
}
