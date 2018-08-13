/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.pattern.ask
import com.stratio.sparta.serving.api.actor.StatusActor
import com.stratio.sparta.serving.api.constants.HttpConstant
import StatusActor.{DeleteStatus, FindAll, _}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow._
import com.wordnik.swagger.annotations._
import spray.http.StatusCodes
import spray.routing._

import scala.util.Try

@Api(value = HttpConstant.WorkflowStatusesPath, description = "Operations over workflow statuses", position = 0)
trait WorkflowStatusHttpService extends BaseHttpService {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    WorkflowStatusUnexpected,
    ErrorCodesMessages.getOrElse(WorkflowStatusUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route = findAll(user) ~
    update(user) ~ deleteAll(user) ~ deleteById(user) ~ find(user)

  @ApiOperation(value = "Finds all workflow statuses",
    notes = "Returns a workflows list",
    httpMethod = "GET",
    response = classOf[Seq[WorkflowStatus]],
    responseContainer = "List")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowStatusesPath) {
      get {
        context =>
          val statusActor = actors(AkkaConstant.StatusActorName)
          for {
            response <- (statusActor ? FindAll(user))
              .mapTo[Either[Try[Seq[WorkflowStatus]], UnauthorizedResponse]]
          } yield getResponse(context, WorkflowStatusFindAll, response, genericError)
      }
    }
  }

  @ApiOperation(value = "Finds a workflow status from its id.",
    notes = "Find a workflow status from its id.",
    httpMethod = "GET",
    response = classOf[WorkflowStatus])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the workflow",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def find(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowStatusesPath / Segment) { (id) =>
      get {
        context =>
          val statusActor = actors(AkkaConstant.StatusActorName)
          for {
            response <- (statusActor ? FindById(id, user))
              .mapTo[Either[ResponseStatus, UnauthorizedResponse]]
          } yield getResponse(context, WorkflowStatusFindById, response, genericError)
      }
    }
  }

  @ApiOperation(value = "Deletes all workflow statuses",
    notes = "Delete all workflow statuses",
    httpMethod = "DELETE")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowStatusesPath) {
      delete {
        complete {
          val statusActor = actors(AkkaConstant.StatusActorName)
          for {
            response <- (statusActor ? DeleteAll(user))
              .mapTo[Either[Response, UnauthorizedResponse]]
          } yield deletePostPutResponse(WorkflowStatusDeleteAll, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @ApiOperation(value = "Deletes a workflow statuses by its id",
    notes = "Delete a workflow statuses by its id",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the workflow",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteById(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowStatusesPath / Segment) { (id) =>
      delete {
        complete {
          val statusActor = actors(AkkaConstant.StatusActorName)
          for {
            response <- (statusActor ? DeleteStatus(id, user))
              .mapTo[Either[Response, UnauthorizedResponse]]
          } yield deletePostPutResponse(WorkflowStatusDeleteById, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @ApiOperation(value = "Updates a workflow status.",
    notes = "Updates a workflow status.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "workflow status",
      value = "workflow json",
      dataType = "WorkflowStatusModel",
      required = true,
      paramType = "body")))
  def update(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowStatusesPath) {
      put {
        entity(as[WorkflowStatus]) { workflowStatus =>
          complete {
            val statusActor = actors(AkkaConstant.StatusActorName)
            for {
              response <- (statusActor ? Update(workflowStatus, user))
                .mapTo[Either[ResponseStatus, UnauthorizedResponse]]
            } yield deletePostPutResponse(WorkflowStatusDeleteById, response, genericError, StatusCodes.OK)
          }
        }
      }
    }
  }
}
