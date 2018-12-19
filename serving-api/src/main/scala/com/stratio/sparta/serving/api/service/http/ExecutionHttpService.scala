/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.service.http

import javax.ws.rs.Path

import akka.pattern.ask
import com.wordnik.swagger.annotations._
import spray.http.StatusCodes
import spray.routing._

import com.stratio.sparta.serving.api.actor.ExecutionActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.api.constants.HttpConstant._
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel.{WorkflowExecutionQuery, _}
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow._

@Api(value = HttpConstant.ExecutionsPath, description = "Operations over workflow executions", position = 0)
trait ExecutionHttpService extends BaseHttpService {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    WorkflowServiceUnexpected,
    ErrorCodesMessages.getOrElse(WorkflowServiceUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route = findAll(user) ~ findAllDto(user) ~ dashboard(user) ~
    update(user) ~ create(user) ~ deleteAll(user) ~ deleteById(user) ~ find(user) ~ stop(user) ~ archived(user) ~
    findByQuery(user) ~ findByQueryDto(user) ~ reRun(user)

  @Path("/findByQuery")
  @ApiOperation(value = "Find executions from query.",
    notes = "Find executions from query.",
    httpMethod = "POST",
    response = classOf[Seq[WorkflowExecution]],
    responseContainer = "List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "executionQuery",
      value = "query execution model json",
      dataType = "com.stratio.sparta.serving.core.models.workflow.WorkflowExecutionQuery",
      required = true,
      paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByQuery(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ExecutionsPath / "findByQuery") {
      pathEndOrSingleSlash {
        post {
          entity(as[WorkflowExecutionQuery]) { executionQuery =>
            complete {
              for {
                response <- (supervisor ? QueryExecution(executionQuery, user))
                  .mapTo[Either[ResponseWorkflowExecutions, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowExecutionQuery, response, genericError)
            }
          }
        }
      }
    }
  }

  @Path("/findByQueryDto")
  @ApiOperation(value = "Find executions from query.",
    notes = "Find executions from query.",
    httpMethod = "POST",
    response = classOf[Seq[WorkflowExecutionDto]],
    responseContainer = "List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "executionQuery",
      value = "query execution model json",
      dataType = "com.stratio.sparta.serving.core.models.workflow.WorkflowExecutionQuery",
      required = true,
      paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByQueryDto(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ExecutionsPath / "findByQueryDto") {
      pathEndOrSingleSlash {
        post {
          entity(as[WorkflowExecutionQuery]) { executionQuery =>
            complete {
              for {
                response <- (supervisor ? QueryExecutionDto(executionQuery, user))
                  .mapTo[Either[ResponseWorkflowExecutionsDto, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowExecutionQueryDto, response, genericError)
            }
          }
        }
      }
    }
  }

  @Path("/dashboard")
  @ApiOperation(value = "Returns the dashboard view",
    notes = "Returns the dashboard view",
    httpMethod = "GET",
    response = classOf[DashboardView])
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def dashboard(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ExecutionsPath / "dashboard") {
      get {
        context =>
          for {
            response <- (supervisor ? CreateDashboardView(user))
              .mapTo[Either[ResponseDashboardView, UnauthorizedResponse]]
          } yield getResponse(context, WorkflowExecutionDashboard, response, genericError)
      }
    }
  }

  @Path("")
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
              .mapTo[Either[ResponseWorkflowExecutions, UnauthorizedResponse]]
          } yield getResponse(context, WorkflowExecutionFindAll, response, genericError)
      }
    }
  }

  @Path("/findAllDto")
  @ApiOperation(value = "Finds all executions dto",
    notes = "Returns an executions list with less fields",
    httpMethod = "GET",
    response = classOf[Seq[WorkflowExecutionDto]],
    responseContainer = "List")
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findAllDto(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ExecutionsPath / "findAllDto") {
      get {
        context =>
          for {
            response <- (supervisor ? FindAllDto(user))
              .mapTo[Either[ResponseWorkflowExecutionsDto, UnauthorizedResponse]]
          } yield getResponse(context, WorkflowExecutionFindAllDto, response, genericError)
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "Finds an execution by its id",
    notes = "Find a execution by its id",
    httpMethod = "GET",
    response = classOf[WorkflowExecution])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the execution",
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
              .mapTo[Either[ResponseWorkflowExecution, UnauthorizedResponse]]
          } yield getResponse(context, WorkflowExecutionFindById, response, genericError)
      }
    }
  }

  @Path("")
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
              .mapTo[Either[ResponseBoolean, UnauthorizedResponse]]
          } yield deletePostPutResponse(WorkflowExecutionDeleteAll, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("/{ids}")
  @ApiOperation(value = "Deletes an execution by its id",
    notes = "Deletes an executions by its id",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the execution",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def deleteById(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ExecutionsPath / Segments) { (ids) =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteExecution(ids, user))
              .mapTo[Either[ResponseBoolean, UnauthorizedResponse]]
          } yield deletePostPutResponse(WorkflowExecutionDeleteById, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("")
  @ApiOperation(value = "Updates an execution.",
    notes = "Updates an execution.",
    httpMethod = "PUT",
    response = classOf[WorkflowExecution]
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "execution",
      value = "execution json",
      dataType = "com.stratio.sparta.serving.core.models.workflow.WorkflowExecution",
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
                .mapTo[Either[ResponseWorkflowExecution, UnauthorizedResponse]]
            } yield deletePostPutResponse(WorkflowExecutionUpdate, response, genericError, StatusCodes.OK)
          }
        }
      }
    }
  }

  @Path("")
  @ApiOperation(value = "Creates a execution",
    notes = "Returns the execution result",
    httpMethod = "POST",
    response = classOf[WorkflowExecution])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "execution",
      value = "execution json",
      dataType = "com.stratio.sparta.serving.core.models.workflow.WorkflowExecution",
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
                .mapTo[Either[ResponseWorkflowExecution, UnauthorizedResponse]]
            } yield deletePostPutResponse(WorkflowExecutionCreate, response, genericError)
          }
        }
      }
    }
  }

  @Path("/stop/{id}")
  @ApiOperation(value = "Stop a execution by its id.",
    notes = "Stop a execution by its id.",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "id",
      value = "id of the execution",
      dataType = "String",
      required = true,
      paramType = "path"
    )
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def stop(user: Option[LoggedUser] = None): Route = {
    path(HttpConstant.ExecutionsPath / "stop" / Segment) { id =>
      post {
        complete {
          for {
            response <- (supervisor ? Stop(id, user))
              .mapTo[Either[ResponseWorkflowExecution, UnauthorizedResponse]]
          } yield deletePostPutResponse(WorkflowServiceStop, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("/reRun/{id}")
  @ApiOperation(value = "Re-run an execution by its id.",
    notes = "Re-run an execution by its id.",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "id",
      value = "id of the execution",
      dataType = "String",
      required = true,
      paramType = "path"
    )
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def reRun(user: Option[LoggedUser] = None): Route = {
    path(HttpConstant.ExecutionsPath / "reRun" / Segment) { id =>
      post {
        complete {
          for {
            response <- (supervisor ? ReRunExecutionById(id, user))
              .mapTo[Either[ResponseReRun, UnauthorizedResponse]]
          } yield deletePostPutResponse(WorkflowExecutionReRun, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("/archived")
  @ApiOperation(value = "Archive a execution",
    notes = "Archive a execution",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "archived",
      value = "archive execution query json",
      dataType = "com.stratio.sparta.serving.core.models.workflow.ArchiveExecutionQuery",
      required = true,
      paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def archived(user: Option[LoggedUser] = None): Route = {
    path(HttpConstant.ExecutionsPath / "archived") {
      post {
        entity(as[ArchivedExecutionQuery]) { request =>
          complete {
            for {
              response <- (supervisor ? ArchiveExecution(request, user))
                .mapTo[Either[ResponseWorkflowExecutions, UnauthorizedResponse]]
            } yield deletePostPutResponse(WorkflowExecutionArchived, response, genericError)
          }
        }
      }
    }
  }
}
