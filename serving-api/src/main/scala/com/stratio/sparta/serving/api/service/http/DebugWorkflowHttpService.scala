/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import akka.pattern.ask
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.DebugWorkflow
import com.stratio.sparta.serving.api.actor.DebugWorkflowActor._
import com.wordnik.swagger.annotations._
import spray.routing.Route
import javax.ws.rs.Path
import spray.http.StatusCodes

@Api(value = HttpConstant.DebugWorkflowsPath, description = "Workflow debug utility")
trait DebugWorkflowHttpService extends BaseHttpService {


  override def routes(user: Option[LoggedUser] = None): Route = findAll(user) ~ create(user) ~ run(user) ~
    findById(user) ~ resultsById(user)

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    DebugWorkflowServiceUnexpected,
    ErrorCodesMessages.getOrElse(DebugWorkflowServiceUnexpected, UnknownError)
  )

  @Path("")
  @ApiOperation(value = "Finds all debug workflows.",
    notes = "Finds all debug workflows.",
    httpMethod = "GET",
    response = classOf[Array[DebugWorkflow]])
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.DebugWorkflowsPath) {
      pathEndOrSingleSlash {
        get {
          context =>
            for {
              response <- (supervisor ? FindAll(user))
                .mapTo[Either[ResponseDebugWorkflows, UnauthorizedResponse]]
            } yield getResponse(context, DebugWorkflowServiceFindAll, response, genericError)
        }
      }
    }
  }

  @Path("")
  @ApiOperation(value = "Creates a debug workflow.",
    notes = "Creates a workflow.",
    httpMethod = "POST",
    response = classOf[DebugWorkflow])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "workflow",
      defaultValue = "",
      value = "workflow json",
      dataType = "Workflow",
      required = true,
      paramType = "body")))
  def create(user: Option[LoggedUser]): Route = {
    path(HttpConstant.DebugWorkflowsPath) {
      pathEndOrSingleSlash {
        post {
          entity(as[DebugWorkflow]) { workflow =>
            complete {
              for {
                response <- (supervisor ? CreateDebugWorkflow(workflow, user))
                  .mapTo[Either[ResponseDebugWorkflow, UnauthorizedResponse]]
              } yield deletePostPutResponse(DebugWorkflowServiceCreate, response, genericError)
            }
          }
        }
      }
    }
  }

  @Path("/run/{id}")
  @ApiOperation(value = "Runs a debug workflow from by id.",
    notes = "Execute the debug workflow associated to the workflow for this id.",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the workflow",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def run(user: Option[LoggedUser]): Route = {
    path(HttpConstant.DebugWorkflowsPath / "run" / JavaUUID) { id =>
      post {
        complete {
          for {
            response <- (supervisor ? Run(id.toString, user))
              .mapTo[Either[ResponseRun, UnauthorizedResponse]]
          } yield deletePostPutResponse(DebugWorkflowServiceRun, response, genericError)
        }
      }
    }
  }

  @Path("/findById/{id}")
  @ApiOperation(value = "Finds a debug workflow from its id.",
    notes = "Finds a debug workflow from its id.",
    httpMethod = "GET",
    response = classOf[DebugWorkflow])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the workflow",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findById(user: Option[LoggedUser]): Route = {
    path(HttpConstant.DebugWorkflowsPath / "findById" / JavaUUID) { id =>
      get {
        context =>
          for {
            response <- (supervisor ? Find(id.toString, user))
              .mapTo[Either[ResponseDebugWorkflow, UnauthorizedResponse]]
          } yield getResponse(context, DebugWorkflowServiceFindById, response, genericError)
      }
    }
  }

  @Path("/resultsById/{id}")
  @ApiOperation(value = "Retrieve the debug results for that workflow id.",
    notes = "Retrieve the debug results for that workflow id.",
    httpMethod = "GET",
    response = classOf[ResponseResult])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the workflow",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def resultsById(user: Option[LoggedUser]): Route = {
    path(HttpConstant.DebugWorkflowsPath / "resultsById" / JavaUUID) { id =>
      get {
        context =>
          for {
            response <- (supervisor ? GetResults(id.toString, user))
              .mapTo[Either[ResponseResult, UnauthorizedResponse]]
          } yield getResponse(context, DebugWorkflowServiceResultsFindById, response, genericError)
      }
    }
  }

}