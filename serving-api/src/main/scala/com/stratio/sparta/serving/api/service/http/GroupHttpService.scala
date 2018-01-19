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

import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparta.serving.api.actor.GroupActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.Group
import com.wordnik.swagger.annotations._
import spray.http.StatusCodes
import spray.routing._

import scala.util.Try

@Api(value = HttpConstant.GroupsPath, description = "Operations over workflow groups", position = 0)
trait GroupHttpService extends BaseHttpService {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    WorkflowServiceUnexpected,
    ErrorCodesMessages.getOrElse(GroupServiceUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route = create(user) ~ findAll(user) ~ findByID(user) ~
    findByName(user) ~ deleteAll(user) ~ deleteById(user) ~ deleteByName(user) ~ update(user)

  @ApiOperation(value = "Finds all groups",
    notes = "Returns an groups list",
    httpMethod = "GET",
    response = classOf[Seq[Group]],
    responseContainer = "List")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GroupsPath) {
      get {
        context =>
          for {
            response <- (supervisor ? FindAllGroups(user))
              .mapTo[Either[Try[Seq[Group]], UnauthorizedResponse]]
          } yield getResponse(context, GroupServiceFindAllGroups, response, genericError)
      }
    }
  }

  @Path("/findById/{id}")
  @ApiOperation(value = "Finds an group by its name",
    notes = "Find a group by its id",
    httpMethod = "GET",
    response = classOf[Group])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "name of the group",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByID(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GroupsPath / "findById" / Segment) { (name) =>
      get {
        context =>
          for {
            response <- (supervisor ? FindGroupByID(name, user))
              .mapTo[Either[Try[Group], UnauthorizedResponse]]
          } yield getResponse(context, GroupServiceFindGroup, response, genericError)
      }
    }
  }

  @Path("/findByName/{name}")
  @ApiOperation(value = "Finds an group by its name",
    notes = "Find a group by its name",
    httpMethod = "GET",
    response = classOf[Group])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name",
      value = "name of the group",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByName(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GroupsPath / "findByName" / Rest) { (name) =>
      get {
        context =>
          for {
            response <- (supervisor ? FindGroupByName(name, user))
              .mapTo[Either[Try[Group], UnauthorizedResponse]]
          } yield getResponse(context, GroupServiceFindGroup, response, genericError)
      }
    }
  }

  @ApiOperation(value = "Deletes all groups",
    notes = "Deletes all groups",
    httpMethod = "DELETE")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GroupsPath) {
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteAllGroups(user))
              .mapTo[Either[Try[Unit], UnauthorizedResponse]]
          } yield deletePostPutResponse(GroupServiceDeleteAllGroups, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("/deleteById/{id}")
  @ApiOperation(value = "Deletes a group by its id",
    notes = "Deletes a group by its id",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the group",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteById(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GroupsPath / "deleteById" / Segment) { (name) =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteGroupByID(name, user))
              .mapTo[Either[Try[Unit], UnauthorizedResponse]]
          } yield deletePostPutResponse(GroupServiceDeleteGroup, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("/deleteByName/{name}")
  @ApiOperation(value = "Deletes a group by its name",
    notes = "Deletes a group by its name",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name",
      value = "name of the group",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteByName(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GroupsPath / "deleteByName" / Segment) { (name) =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteGroupByName(name, user))
              .mapTo[Either[Try[Unit], UnauthorizedResponse]]
          } yield deletePostPutResponse(GroupServiceDeleteGroup, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @ApiOperation(value = "Updates a group.",
    notes = "Updates a group.",
    httpMethod = "PUT",
    response = classOf[Group]
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "group",
      value = "group json",
      dataType = "Group",
      required = true,
      paramType = "body")))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def update(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GroupsPath) {
      put {
        entity(as[Group]) { request =>
          complete {
            for {
              response <- (supervisor ? UpdateGroup(request, user))
                .mapTo[Either[Try[Group], UnauthorizedResponse]]
            } yield deletePostPutResponse(GroupServiceUpdateGroup, response, genericError, StatusCodes.OK)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates a group",
    notes = "Returns the group result",
    httpMethod = "POST",
    response = classOf[Group])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "group",
      value = "group json",
      dataType = "Group",
      required = true,
      paramType = "body")))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def create(user: Option[LoggedUser]): Route = {
    path(HttpConstant.GroupsPath) {
      post {
        entity(as[Group]) { request =>
          complete {
            for {
              response <- (supervisor ? CreateGroup(request, user))
                .mapTo[Either[Try[Group], UnauthorizedResponse]]
            } yield deletePostPutResponse(GroupServiceCreateGroup, response, genericError)
          }
        }
      }
    }
  }
}
