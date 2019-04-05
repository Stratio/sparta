/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.pattern.ask
import com.stratio.sparta.serving.api.actor.ScheduledWorkflowTaskActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.constants.HttpConstant._
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.authorization.LoggedUser
import com.stratio.sparta.serving.core.models.orchestrator._
import com.wordnik.swagger.annotations._
import javax.ws.rs.Path
import spray.http.StatusCodes
import spray.routing._

@Api(value = HttpConstant.ScheduledWorkflowTasksPath, description = "Operations over scheduled workflow tasks", position = 20)
trait ScheduledWorkflowTaskHttpService extends BaseHttpService {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    ScheduledWorkflowTaskServiceUnexpected,
    ErrorCodesMessages.getOrElse(ScheduledWorkflowTaskServiceUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route =
    create(user) ~ update(user) ~
      findAll(user) ~ findAllDto(user) ~ findByID(user) ~ findByActive(user) ~ findByActiveAndState(user) ~
      deleteAll(user) ~ deleteById(user)

  @ApiOperation(value = "Finds all scheduled workflow tasks",
    notes = "Returns an scheduled workflow tasks list",
    httpMethod = "GET",
    response = classOf[ScheduledWorkflowTask])
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ScheduledWorkflowTasksPath) {
      get {
        context =>
          for {
            response <- (supervisor ? FindAllScheduledWorkflowTasks(user))
              .mapTo[Either[ResponseScheduledWorkflowTasks, UnauthorizedResponse]]
          } yield getResponse(context, ScheduledWorkflowTaskServiceFindAll, response, genericError)
      }
    }
  }

  @Path("/findAllDto")
  @ApiOperation(value = "Finds all scheduled workflow tasks dto",
    notes = "Returns an scheduled workflow tasks dto list",
    httpMethod = "GET",
    response = classOf[ScheduledWorkflowTaskDto])
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findAllDto(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ScheduledWorkflowTasksPath / "findAllDto") {
      get {
        context =>
          for {
            response <- (supervisor ? FindAllScheduledWorkflowTasksDto(user))
              .mapTo[Either[ResponseScheduledWorkflowTasksDto, UnauthorizedResponse]]
          } yield getResponse(context, ScheduledWorkflowTaskServiceFindAll, response, genericError)
      }
    }
  }

  @Path("/findById/{id}")
  @ApiOperation(value = "Finds an scheduled workflow task by its name",
    notes = "Find a scheduled workflow task by its id",
    httpMethod = "GET",
    response = classOf[ScheduledWorkflowTask])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "name of the scheduled workflow task",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByID(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ScheduledWorkflowTasksPath / "findById" / Segment) { id =>
      get {
        context =>
          for {
            response <- (supervisor ? FindScheduledWorkflowTaskByID(id, user))
              .mapTo[Either[ResponseScheduledWorkflowTask, UnauthorizedResponse]]
          } yield getResponse(context, ScheduledWorkflowTaskServiceFindById, response, genericError)
      }
    }
  }

  @Path("/findByActive/{active}")
  @ApiOperation(value = "Finds an scheduled workflow tasks by active",
    notes = "Find a scheduled workflow task by active",
    httpMethod = "GET",
    response = classOf[ScheduledWorkflowTask])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "active",
      value = "active",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByActive(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ScheduledWorkflowTasksPath / "findByActive" / Rest) { active =>
      get {
        context =>
          for {
            response <- (supervisor ? FindScheduledWorkflowTaskByActive(active.toBoolean, user))
              .mapTo[Either[ResponseScheduledWorkflowTasks, UnauthorizedResponse]]
          } yield getResponse(context, ScheduledWorkflowTaskServiceFindByActive, response, genericError)
      }
    }
  }

  @Path("/findByActiveAndState/{active}/{state}")
  @ApiOperation(value = "Finds an scheduled workflow tasks by active and state",
    notes = "Find a scheduled workflow task by active and state",
    httpMethod = "GET",
    response = classOf[ScheduledWorkflowTask])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "active",
      value = "active",
      dataType = "string",
      required = true,
      paramType = "path"),
    new ApiImplicitParam(name = "state",
      value = "state",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByActiveAndState(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ScheduledWorkflowTasksPath / "findByActiveAndState" / Segment / Rest) { (active, state) =>
      get {
        context =>
          for {
            response <- (supervisor ? FindScheduledWorkflowTaskByActiveAndState(active.toBoolean, state, user))
              .mapTo[Either[ResponseScheduledWorkflowTasks, UnauthorizedResponse]]
          } yield getResponse(context, ScheduledWorkflowTaskServiceFindByActiveAndState, response, genericError)
      }
    }
  }

  @ApiOperation(value = "Deletes all scheduled workflow tasks",
    notes = "Deletes all scheduled workflow tasks",
    httpMethod = "DELETE")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ScheduledWorkflowTasksPath) {
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteAllScheduledWorkflowTasks(user))
              .mapTo[Either[ResponseBoolean, UnauthorizedResponse]]
          } yield deletePostPutResponse(ScheduledWorkflowTaskServiceDeleteAll, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("/deleteById/{id}")
  @ApiOperation(value = "Deletes a scheduled workflow task by its id",
    notes = "Deletes a scheduled workflow task by its id",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the scheduled workflow task",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteById(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ScheduledWorkflowTasksPath / "deleteById" / Segment) { id =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteScheduledWorkflowTaskByID(id, user))
              .mapTo[Either[ResponseBoolean, UnauthorizedResponse]]
          } yield deletePostPutResponse(ScheduledWorkflowTaskServiceDeleteById, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @ApiOperation(value = "Updates a scheduled workflow task.",
    notes = "Updates a scheduled workflow task.",
    httpMethod = "PUT",
    response = classOf[ScheduledWorkflowTask]
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "scheduled workflow task",
      value = "scheduled workflow task json",
      dataType = "ScheduledWorkflowTask",
      required = true,
      paramType = "body")))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def update(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ScheduledWorkflowTasksPath) {
      put {
        entity(as[ScheduledWorkflowTask]) { request =>
          complete {
            for {
              response <- (supervisor ? UpdateScheduledWorkflowTask(request, user))
                .mapTo[Either[ResponseScheduledWorkflowTask, UnauthorizedResponse]]
            } yield deletePostPutResponse(ScheduledWorkflowTaskServiceUpdate, response, genericError)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates a scheduled workflow task",
    notes = "Returns the scheduled workflow task result",
    httpMethod = "POST",
    response = classOf[ScheduledWorkflowTask])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "scheduled workflow task",
      value = "scheduled workflow task json",
      dataType = "com.stratio.sparta.serving.core.models.orchestrator.ScheduledWorkflowTaskInsert",
      required = true,
      paramType = "body")))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def create(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ScheduledWorkflowTasksPath) {
      post {
        entity(as[ScheduledWorkflowTaskInsert]) { request =>
          complete {
            for {
              response <- (supervisor ? CreateScheduledWorkflowTask(request, user))
                .mapTo[Either[ResponseScheduledWorkflowTask, UnauthorizedResponse]]
            } yield deletePostPutResponse(ScheduledWorkflowTaskServiceCreate, response, genericError)
          }
        }
      }
    }
  }
}
