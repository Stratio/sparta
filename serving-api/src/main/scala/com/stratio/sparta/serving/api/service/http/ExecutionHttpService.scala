/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.pattern.ask
import com.stratio.sparta.serving.api.actor.ExecutionActor
import com.stratio.sparta.serving.api.constants.HttpConstant
import ExecutionActor._
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecution
import com.wordnik.swagger.annotations._
import spray.http.StatusCodes
import spray.routing._

import scala.util.Try

@Api(value = HttpConstant.ExecutionsPath, description = "Operations over workflow executions", position = 0)
trait ExecutionHttpService extends BaseHttpService {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    WorkflowServiceUnexpected,
    ErrorCodesMessages.getOrElse(WorkflowServiceUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route = findAll(user) ~
    update(user) ~ create(user) ~ deleteAll(user) ~ deleteById(user) ~ find(user)

  @ApiOperation(value = "Finds all executions",
    notes = "Returns an executions list",
    httpMethod = "GET",
    response = classOf[Seq[WorkflowExecution]],
    responseContainer = "List")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ExecutionsPath) {
      get {
        context =>
          for {
            response <- (supervisor ? FindAll(user))
              .mapTo[Either[Try[Seq[WorkflowExecution]], UnauthorizedResponse]]
          } yield getResponse(context, WorkflowExecutionFindAll, response, genericError)
      }
    }
  }

  @ApiOperation(value = "Finds an execution by its id",
    notes = "Find a execution by its id",
    httpMethod = "GET",
    response = classOf[WorkflowExecution])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the policy",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def find(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ExecutionsPath / Segment) { (id) =>
      get {
        context =>
          for {
            response <- (supervisor ? FindById(id, user))
              .mapTo[Either[Try[WorkflowExecution], UnauthorizedResponse]]
          } yield getResponse(context, WorkflowExecutionFindById, response, genericError)
      }
    }
  }

  @ApiOperation(value = "Deletes all executions",
    notes = "Deletes all executions",
    httpMethod = "DELETE")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ExecutionsPath) {
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteAll(user))
              .mapTo[Either[Try[Unit], UnauthorizedResponse]]
          } yield deletePostPutResponse(WorkflowExecutionDeleteAll, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @ApiOperation(value = "Deletes an execution by its id",
    notes = "Deletes an executions by its id",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the policy",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteById(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ExecutionsPath / Segment) { (id) =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteExecution(id, user))
              .mapTo[Either[Try[Unit], UnauthorizedResponse]]
          } yield deletePostPutResponse(WorkflowExecutionDeleteById, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @ApiOperation(value = "Updates an execution.",
    notes = "Updates an execution.",
    httpMethod = "PUT",
    response = classOf[WorkflowExecution]
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "execution",
      value = "execution json",
      dataType = "SubmitRequest",
      required = true,
      paramType = "body")))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def update(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ExecutionsPath) {
      put {
        entity(as[WorkflowExecution]) { request =>
          complete {
            for {
              response <- (supervisor ? Update(request, user))
                .mapTo[Either[Try[WorkflowExecution], UnauthorizedResponse]]
            } yield deletePostPutResponse(WorkflowExecutionUpdate, response, genericError, StatusCodes.OK)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates a execution",
    notes = "Returns the execution result",
    httpMethod = "POST",
    response = classOf[WorkflowExecution])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "execution",
      value = "execution json",
      dataType = "SubmitRequest",
      required = true,
      paramType = "body")))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def create(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ExecutionsPath) {
      post {
        entity(as[WorkflowExecution]) { request =>
          complete {
            for {
              response <- (supervisor ? CreateExecution(request, user))
                .mapTo[Either[Try[WorkflowExecution], UnauthorizedResponse]]
            } yield deletePostPutResponse(WorkflowExecutionCreate, response, genericError)
          }
        }
      }
    }
  }
}
