/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.pattern.ask
import com.stratio.sparta.core.models.SpartaQualityRule
import com.stratio.sparta.serving.api.actor.PlannedQualityRuleEndpointActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.constants.HttpConstant.ResponseBoolean
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.authorization.LoggedUser
import com.wordnik.swagger.annotations._
import javax.ws.rs.Path
import spray.http.StatusCodes
import spray.routing.Route

@Api(value = HttpConstant.PlannedQualityRulePath, description = "Operations over Planned Quality Rule")
trait PlannedQualityRuleHttpService extends BaseHttpService{

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    ConfigurationUnexpected,
    ErrorCodesMessages.getOrElse(ConfigurationUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route =
    findAll(user) ~ findByTaskId(user) ~ find(user) ~ deleteAll(user) ~ deleteById(user)

  @Path("")
  @ApiOperation(value = "Finds all available planned quality rules",
    notes = "Returns a list of quality rules",
    httpMethod = "GET",
    response = classOf[List[SpartaQualityRule]],
    responseContainer = "List")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.PlannedQualityRulePath) {
      get {
        context =>
          for {
            response <- (supervisor ? FindAll(user))
              .mapTo[Either[PlannedQualityRuleResultsResponse, UnauthorizedResponse]]
          } yield getResponse(context, PlannedQualityRuleServiceFindAll, response, genericError)
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "Finds a planned quality rule result by its id",
    notes = "Finds a quality rule result by its id",
    httpMethod = "GET",
    response = classOf[SpartaQualityRule])
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
    path(HttpConstant.PlannedQualityRulePath / Segment) { id =>
      get {
        context =>
          for {
            response <- (supervisor ? FindById(id, user))
              .mapTo[Either[PlannedQualityRuleResultResponse, UnauthorizedResponse]]
          } yield getResponse(context, PlannedQualityRuleServiceFindById, response, genericError)
      }
    }
  }

  @Path("/taskId/{taskId}")
  @ApiOperation(value = "Finds a planned quality rule result by its taskId",
    notes = "Finds a planned quality rule by its taskId",
    httpMethod = "GET",
    response = classOf[SpartaQualityRule])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "taskId",
      value = "Task id",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByTaskId(user: Option[LoggedUser]): Route = {
    path(HttpConstant.PlannedQualityRulePath / "taskId" / Segment) { taskId =>
      get {
        context =>
          for {
            response <- (supervisor ? FindByTaskId(taskId, user))
              .mapTo[Either[PlannedQualityRuleResultResponse, UnauthorizedResponse]]
          } yield getResponse(context, PlannedQualityRuleServiceFindByTaskId, response, genericError)
      }
    }
  }

  @Path("")
  @ApiOperation(value = "Deletes all planned quality rules",
    notes = "Deletes all planned quality rules",
    httpMethod = "DELETE")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.PlannedQualityRulePath) {
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteAll(user))
              .mapTo[Either[ResponseBoolean, UnauthorizedResponse]]
          } yield deletePostPutResponse(PlannedQualityRuleServiceDeleteAll, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "Deletes a planned quality rule by its id",
    notes = "Deletes a planned quality rule by its id",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "Planned Quality rule id",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteById(user: Option[LoggedUser]): Route = {
    path(HttpConstant.PlannedQualityRulePath / Segment) { (id) =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteById(id, user))
              .mapTo[Either[ResponseBoolean, UnauthorizedResponse]]
          } yield deletePostPutResponse(PlannedQualityRuleServiceDeleteById, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

}
