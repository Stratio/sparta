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
import com.stratio.sparta.serving.core.actor.StatusActor.{DeleteStatus, FindAll, _}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow._
import com.wordnik.swagger.annotations._
import spray.http.{HttpResponse, StatusCodes}
import spray.routing._

import scala.util.{Failure, Success, Try}

@Api(value = HttpConstant.WorkflowStatusesPath, description = "Operations over workflow statuses", position = 0)
trait WorkflowStatusHttpService extends BaseHttpService {

  override def routes(user: Option[LoggedUser] = None): Route = findAll(user) ~
    update(user) ~ deleteAll(user) ~ deleteById(user) ~ find(user)

  @ApiOperation(value = "Finds all workflow statuses",
    notes = "Returns a workflows list",
    httpMethod = "GET",
    response = classOf[Try[Seq[WorkflowStatus]]],
    responseContainer = "List")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowStatusesPath) {
      get {
        complete {
          val statusActor = actors(AkkaConstant.StatusActorName)
          for {
            workflowsStatuses <- (statusActor ? FindAll(user))
              .mapTo[Either[Try[Seq[WorkflowStatus]], UnauthorizedResponse]]
          } yield workflowsStatuses match {
            case Left(Failure(exception)) => throw exception
            case Left(Success(statuses)) => statuses
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in status")
          }
        }
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
        complete {
          val statusActor = actors(AkkaConstant.StatusActorName)
          for {
            workflowStatus <- (statusActor ? FindById(id, user))
              .mapTo[Either[ResponseStatus, UnauthorizedResponse]]
          } yield workflowStatus match {
            case Left(ResponseStatus(Failure(exception))) => throw exception
            case Left(ResponseStatus(Success(workflow))) => workflow
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in status")
          }
        }
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
            responseCode <- (statusActor ? DeleteAll(user))
              .mapTo[Either[ResponseDelete, UnauthorizedResponse]]
          } yield responseCode match {
            case Left(ResponseDelete(Failure(exception))) => throw exception
            case Left(ResponseDelete(Success(_))) => StatusCodes.OK
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in status")
          }
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
            responseDelete <- (statusActor ? DeleteStatus(id, user))
              .mapTo[Either[ResponseDelete, UnauthorizedResponse]]
          } yield responseDelete match {
            case Left(ResponseDelete(Failure(exception))) => throw exception
            case Left(ResponseDelete(Success(_))) => StatusCodes.OK
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in status")
          }
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
            } yield response match {
              case Left(ResponseStatus(Success(status))) => HttpResponse(StatusCodes.Created)
              case Left(ResponseStatus(Failure(ex))) =>
                log.error("Can't update workflow", ex)
                //TODO refactor to ServerException
                throw new Exception("Can't update workflow")
              case Right(UnauthorizedResponse(exception)) => throw exception
              case _ => throw new RuntimeException("Unexpected behaviour in status")
            }
          }
        }
      }
    }
  }
}
