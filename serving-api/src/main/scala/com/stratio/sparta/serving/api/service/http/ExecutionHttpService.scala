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

import akka.pattern.ask
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.ExecutionActor._
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
