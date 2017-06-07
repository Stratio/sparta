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
import com.stratio.sparta.serving.core.actor.RequestActor._
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.submit.SubmitRequest
import com.wordnik.swagger.annotations._
import spray.http.{HttpResponse, StatusCodes}
import spray.routing._

import scala.util.{Failure, Success, Try}

@Api(value = HttpConstant.ExecutionsPath, description = "Operations about executions.", position = 0)
trait ExecutionHttpService extends BaseHttpService {

  override def routes(user: Option[LoggedUser] = None): Route = findAll(user) ~
    update(user) ~ create(user) ~ deleteAll(user) ~ deleteById(user) ~ find(user)

  @ApiOperation(value = "Finds all executions",
    notes = "Returns an executions list",
    httpMethod = "GET",
    response = classOf[Seq[SubmitRequest]],
    responseContainer = "List")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ExecutionsPath) {
      get {
        complete {
          for {
            response <- (supervisor ? FindAll(user))
              .mapTo[Either[Try[Seq[SubmitRequest]], UnauthorizedResponse]]
          } yield response match {
            case Left(Failure(exception)) => throw exception
            case Left(Success(executions)) => executions
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in executions")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Finds an execution by its id",
    notes = "Find a execution by its id",
    httpMethod = "GET",
    response = classOf[SubmitRequest])
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
        complete {
          for {
            response <- (supervisor ? FindById(id, user))
              .mapTo[Either[Try[SubmitRequest], UnauthorizedResponse]]
          } yield response match {
            case Left(Failure(exception)) => throw exception
            case Left(Success(request)) => request
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in executions")
          }
        }
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
              .mapTo[Either[Try[_],UnauthorizedResponse]]
          } yield response match {
            case Left(Failure(exception)) => throw exception
            case Left(Success(_)) => StatusCodes.OK
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in executions")
          }
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
              .mapTo[Either[Try[_],UnauthorizedResponse]]
          } yield response match {
            case Left(Failure(exception))=> throw exception
            case Left(Success(_)) => StatusCodes.OK
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in executions")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Updates an execution.",
    notes = "Updates an execution.",
    httpMethod = "PUT",
    response = classOf[SubmitRequest]
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
        entity(as[SubmitRequest]) { request =>
          complete {
            for {
              response <- (supervisor ? Update(request, user))
                .mapTo[Either[Try[SubmitRequest],UnauthorizedResponse]]
            } yield response match {
              case Left(Success(status)) => HttpResponse(StatusCodes.Created)
              case Left(Failure(ex)) =>
                val message = "Unable to update execution"
                log.error(message, ex)
                throw new ServingCoreException(ErrorModel.toString(
                  ErrorModel(ErrorModel.CodeErrorUpdatingExecution, message)
                ))
              case Right(UnauthorizedResponse(exception)) => throw exception
              case _ => throw new RuntimeException("Unexpected behaviour in executions")
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates a execution",
    notes = "Returns the execution result",
    httpMethod = "POST",
    response = classOf[SubmitRequest])
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
        entity(as[SubmitRequest]) { request =>
          complete {
            for {
              response <- (supervisor ? CreateExecution(request, user))
                .mapTo[Either[Try[SubmitRequest],UnauthorizedResponse]]
            } yield {
              response match {
                case Left(Success(requestCreated)) => requestCreated
                case Left(Failure(ex: Throwable)) =>
                  val message = "Unable to create execution"
                  log.error(message, ex)
                  throw new ServingCoreException(ErrorModel.toString(
                    ErrorModel(ErrorModel.CodeErrorCreatingPolicy, message)
                  ))
                case Right(UnauthorizedResponse(exception)) => throw exception
                case _ => throw new RuntimeException("Unexpected behaviour in executions")
              }
            }
          }
        }
      }
    }
  }
}
