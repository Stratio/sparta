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
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.submit.SubmitRequest
import io.swagger.annotations._
import spray.http.{HttpResponse, StatusCodes}
import spray.routing._

import scala.util.{Failure, Success, Try}

@Api(value = HttpConstant.ExecutionsPath, description = "Operations about executions.", position = 0)
trait ExecutionHttpService extends BaseHttpService {

  override def routes(user: Option[LoggedUser] = None): Route = findAll(user) ~
    update(user) ~ create(user) ~ deleteAll(user) ~ deleteById(user) ~ find(user)

  @ApiOperation(value = "Finds all executions",
    notes = "Returns executions list",
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
            response <- (supervisor ? FindAll).mapTo[Try[Seq[SubmitRequest]]]
          } yield response match {
            case Failure(exception) => throw exception
            case Success(executions) => executions
          }
        }
      }
    }
  }

  @ApiOperation(value = "Find a execution from its id.",
    notes = "Find a execution from its id.",
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
            response <- (supervisor ? new FindById(id)).mapTo[Try[SubmitRequest]]
          } yield response match {
            case Failure(exception) => throw exception
            case Success(request) => request
          }
        }
      }
    }
  }

  @ApiOperation(value = "Delete all executions",
    notes = "Delete all executions",
    httpMethod = "DELETE")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ExecutionsPath) {
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteAll).mapTo[Try[_]]
          } yield response match {
            case Failure(exception) => throw exception
            case Success(_) => StatusCodes.OK
          }
        }
      }
    }
  }

  @ApiOperation(value = "Delete a executions by its id",
    notes = "Delete a executions by its id",
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
            response <- (supervisor ? Delete(id)).mapTo[Try[_]]
          } yield response match {
            case Failure(exception) => throw exception
            case Success(_) => StatusCodes.OK
          }
        }
      }
    }
  }

  @ApiOperation(value = "Updates a execution.",
    notes = "Updates a execution.",
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
              response <- (supervisor ? Update(request)).mapTo[Try[SubmitRequest]]
            } yield response match {
              case Success(status) => HttpResponse(StatusCodes.Created)
              case Failure(ex) =>
                val message = "Can't update execution"
                log.error(message, ex)
                throw new ServingCoreException(ErrorModel.toString(
                  ErrorModel(ErrorModel.CodeErrorUpdatingExecution, message)
                ))
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
              response <- (supervisor ? Create(request)).mapTo[Try[SubmitRequest]]
            } yield {
              response match {
                case Success(requestCreated) => requestCreated
                case Failure(ex: Throwable) =>
                  val message = "Can't create execution"
                  log.error(message, ex)
                  throw new ServingCoreException(ErrorModel.toString(
                    ErrorModel(ErrorModel.CodeErrorCreatingPolicy, message)
                  ))
              }
            }
          }
        }
      }
    }
  }
}
