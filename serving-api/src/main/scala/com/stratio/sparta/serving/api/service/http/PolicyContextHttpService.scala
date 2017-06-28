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

import akka.actor.ActorRef
import akka.pattern.ask
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.FragmentActor
import com.stratio.sparta.serving.core.actor.LauncherActor.Launch
import com.stratio.sparta.serving.core.actor.StatusActor.{DeleteStatus, FindAll, _}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.helpers.FragmentsHelper
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.models.workflow.fragment.{FragmentElementModel, FragmentType}
import com.wordnik.swagger.annotations._
import spray.http.{HttpResponse, StatusCodes}
import spray.routing._

import scala.util.{Failure, Success, Try}

@Api(value = HttpConstant.PolicyContextPath, description = "Operations about policy contexts.", position = 0)
trait PolicyContextHttpService extends BaseHttpService {

  override def routes(user: Option[LoggedUser] = None): Route = findAll(user) ~
    update(user) ~ deleteAll(user) ~ deleteById(user) ~ find(user)

  @ApiOperation(value = "Finds all policy contexts",
    notes = "Returns a policies list",
    httpMethod = "GET",
    response = classOf[Try[Seq[WorkflowStatusModel]]],
    responseContainer = "List")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.PolicyContextPath) {
      get {
        complete {
          val statusActor = actors(AkkaConstant.StatusActorName)
          for {
            policiesStatuses <- (statusActor ? FindAll(user))
              .mapTo[Either[Try[Seq[WorkflowStatusModel]], UnauthorizedResponse]]
          } yield policiesStatuses match {
            case Left(Failure(exception)) => throw exception
            case Left(Success(statuses)) => statuses
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in context")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Finds a policy context from its id.",
    notes = "Find a policy context from its id.",
    httpMethod = "GET",
    response = classOf[WorkflowStatusModel])
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
    path(HttpConstant.PolicyContextPath / Segment) { (id) =>
      get {
        complete {
          val statusActor = actors(AkkaConstant.StatusActorName)
          for {
            policyStatus <- (statusActor ? FindById(id, user))
              .mapTo[Either[ResponseStatus, UnauthorizedResponse]]
          } yield policyStatus match {
            case Left(ResponseStatus(Failure(exception))) => throw exception
            case Left(ResponseStatus(Success(policy))) => policy
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in context")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Deletes all policy contexts",
    notes = "Delete all policy contexts",
    httpMethod = "DELETE")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.PolicyContextPath) {
      delete {
        complete {
          val statusActor = actors(AkkaConstant.StatusActorName)
          for {
            responseCode <- (statusActor ? DeleteAll)
              .mapTo[Either[ResponseDelete, UnauthorizedResponse]]
          } yield responseCode match {
            case Left(ResponseDelete(Failure(exception))) => throw exception
            case Left(ResponseDelete(Success(_))) => StatusCodes.OK
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in context")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Deletes a policy contexts by its id",
    notes = "Delete a policy contexts by its id",
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
    path(HttpConstant.PolicyContextPath / Segment) { (id) =>
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
            case _ => throw new RuntimeException("Unexpected behaviour in context")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Updates a policy status.",
    notes = "Updates a policy status.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "policy status",
      value = "policy json",
      dataType = "PolicyStatusModel",
      required = true,
      paramType = "body")))
  def update(user: Option[LoggedUser]): Route = {
    path(HttpConstant.PolicyContextPath) {
      put {
        entity(as[WorkflowStatusModel]) { policyStatus =>
          complete {
            val statusActor = actors(AkkaConstant.StatusActorName)
            for {
              response <- (statusActor ? Update(policyStatus, user))
                .mapTo[Either[ResponseStatus, UnauthorizedResponse]]
            } yield response match {
              case Left(ResponseStatus(Success(status))) => HttpResponse(StatusCodes.Created)
              case Left(ResponseStatus(Failure(ex))) =>
                log.error("Can't update policy", ex)
                throw new ServingCoreException(ErrorModel.toString(
                  ErrorModel(ErrorModel.CodeErrorUpdatingPolicy, "Can't update policy")
                ))
              case Right(UnauthorizedResponse(exception)) => throw exception
              case _ => throw new RuntimeException("Unexpected behaviour in context")
            }
          }
        }
      }
    }
  }
}
