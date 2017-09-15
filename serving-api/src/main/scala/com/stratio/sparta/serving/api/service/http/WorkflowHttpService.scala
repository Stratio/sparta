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

import java.io.File
import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparta.serving.api.actor.WorkflowActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.LauncherActor.Launch
import com.stratio.sparta.serving.core.actor.StatusActor
import com.stratio.sparta.serving.core.actor.StatusActor.ResponseDelete
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.{ResponseWorkflow, Workflow, WorkflowValidator}
import com.wordnik.swagger.annotations._
import org.json4s.jackson.Serialization.write
import spray.http.HttpHeaders.`Content-Disposition`
import spray.http.{HttpResponse, StatusCodes}
import spray.routing._

import scala.concurrent.Await
import scala.util.{Failure, Success, Try}

@Api(value = HttpConstant.WorkflowsPath, description = "Operations over workflows")
trait WorkflowHttpService extends BaseHttpService with SpartaSerializer {

  override def routes(user: Option[LoggedUser] = None): Route =
    find(user) ~ findAll(user) ~ create(user) ~
      update(user) ~ remove(user) ~ run(user) ~ download(user) ~ findByName(user) ~
      removeAll(user) ~ deleteCheckpoint(user)

  @Path("/find/{id}")
  @ApiOperation(value = "Finds a workflow from its id.",
    notes = "Finds a workflow from its id.",
    httpMethod = "GET",
    response = classOf[Workflow])
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
    path(HttpConstant.WorkflowsPath / "find" / Segment) { (id) =>
      get {
        complete {
          for {
            response <- (supervisor ? Find(id, user))
              .mapTo[Either[ResponseWorkflow, UnauthorizedResponse]]
          } yield response match {
            case Left(ResponseWorkflow(Failure(exception))) => throw exception
            case Left(ResponseWorkflow(Success(workflow))) => workflow
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in workflows")
          }
        }
      }
    }
  }

  @Path("/findByName/{name}")
  @ApiOperation(value = "Finds a workflow from its name.",
    notes = "Finds a workflow from its name.",
    httpMethod = "GET",
    response = classOf[Workflow])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name",
      value = "name of the workflow",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByName(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "findByName" / Segment) { (name) =>
      get {
        complete {
          for {
            response <- (supervisor ? FindByName(name, user))
              .mapTo[Either[ResponseWorkflow, UnauthorizedResponse]]
          } yield response match {
            case Left(ResponseWorkflow(Failure(exception))) => throw exception
            case Left(ResponseWorkflow(Success(workflow))) => workflow
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in workflows")
          }
        }
      }
    }
  }

  @Path("/all")
  @ApiOperation(value = "Finds all workflows.",
    notes = "Finds all workflows.",
    httpMethod = "GET",
    response = classOf[Workflow])
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "all") {
      get {
        complete {
          for {
            response <- (supervisor ? FindAll(user))
              .mapTo[Either[ResponseWorkflows, UnauthorizedResponse]]
          } yield response match {
            case Left(ResponseWorkflows(Failure(exception))) => throw exception
            case Left(ResponseWorkflows(Success(workflows))) => workflows
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in workflows")
          }
        }
      }
    }
  }

  // scalastyle:off cyclomatic.complexity
  @ApiOperation(value = "Creates a workflow.",
    notes = "Creates a workflow.",
    httpMethod = "POST",
    response = classOf[Workflow])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "workflow",
      defaultValue = "",
      value = "workflow json",
      dataType = "WorkflowModel",
      required = true,
      paramType = "body")))
  def create(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath) {
      post {
        entity(as[Workflow]) { workflow =>
          complete {
            WorkflowValidator.validateDto(workflow)
            for {
              response <- (supervisor ? CreateWorkflow(workflow, user))
                .mapTo[Either[ResponseWorkflow, UnauthorizedResponse]]
            } yield response match {
              case Left(ResponseWorkflow(Failure(exception))) => throw exception
              case Left(ResponseWorkflow(Success(pol))) => pol
              case Right(UnauthorizedResponse(exception)) => throw exception
              case _ => throw new RuntimeException("Unexpected behaviour in workflows")
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Updates a workflow.",
    notes = "Updates a workflow.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "workflow",
      defaultValue = "",
      value = "workflow json",
      dataType = "WorkflowModel",
      required = true,
      paramType = "body")))
  def update(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath) {
      put {
        entity(as[Workflow]) { workflow =>
          complete {
            WorkflowValidator.validateDto(workflow)
            for {
              response <- (supervisor ? Update(workflow, user))
                .mapTo[Either[ResponseWorkflow, UnauthorizedResponse]]
            } yield response match {
              case Left(ResponseWorkflow(Failure(exception))) => throw exception
              case Left(ResponseWorkflow(Success(pol))) => HttpResponse(StatusCodes.OK)
              case Right(UnauthorizedResponse(exception)) => throw exception
              case _ => throw new RuntimeException("Unexpected behaviour in workflows")
            }
          }
        }
      }
    }
  }

  @ApiOperation(value = "Deletes all workflows.",
    notes = "Deletes all workflows.",
    httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def removeAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath) {
      delete {
        complete {
          val statusActor = actors(AkkaConstant.StatusActorName)
          for {
            workflows <- (supervisor ? DeleteAll(user))
              .mapTo[Either[ResponseWorkflows, UnauthorizedResponse]]
          } yield workflows match {
            case Left(ResponseWorkflows(Failure(exception))) =>
              throw exception
            case Left(ResponseWorkflows(Success(_))) =>
              for {
                response <- (statusActor ? StatusActor.DeleteAll(user))
                  .mapTo[Either[ResponseDelete, UnauthorizedResponse]]
              } yield response match {
                case Left(ResponseDelete(Success(_))) => StatusCodes.OK
                case Left(ResponseDelete(Failure(exception))) => throw exception
                case Right(UnauthorizedResponse(exception)) => throw exception
                case _ => throw new RuntimeException("Unexpected behaviour in workflows")
              }
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in workflows")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Deletes a workflow from its id.",
    notes = "Deletes a workflow from its id.",
    httpMethod = "DELETE")
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
  def remove(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / Segment) { (id) =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteWorkflow(id, user))
              .mapTo[Either[Response, UnauthorizedResponse]]
          } yield response match {
            case Left(Response(Failure(ex))) => throw ex
            case Left(Response(Success(_))) => StatusCodes.OK
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in workflows")
          }
        }
      }
    }
  }

  @Path("/checkpoint/{name}")
  @ApiOperation(value = "Delete checkpoint associated to a workflow by its name.",
    notes = "Delete checkpoint associated to a workflow by its name.",
    httpMethod = "DELETE",
    response = classOf[Result])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name",
      value = "name of the workflow",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def deleteCheckpoint(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "checkpoint" / Segment) { (name) =>
      delete {
        complete {
          for {
            responsePolicy <- (supervisor ? FindByName(name, user))
              .mapTo[Either[ResponseWorkflow, UnauthorizedResponse]]
          } yield responsePolicy match {
            case Left(ResponseWorkflow(Failure(exception))) => throw exception
            case Left(ResponseWorkflow(Success(workflow))) => for {
              response <- (supervisor ? DeleteCheckpoint(workflow, user))
                .mapTo[Either[Response, UnauthorizedResponse]]
            } yield response match {
              case Left(Response(Failure(ex))) => throw ex
              case Left(Response(Success(_))) => Result("Checkpoint deleted from workflow: " + workflow.name)
              case Right(UnauthorizedResponse(exception)) => throw exception
              case _ => throw new RuntimeException("Unexpected behaviour in workflows")
            }
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in workflows")
          }
        }
      }
    }
  }

  @Path("/run/{id}")
  @ApiOperation(value = "Runs a workflow from by name.",
    notes = "Runs a workflow by its name.",
    httpMethod = "GET",
    response = classOf[Result])
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
  def run(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "run" / Segment) { (id) =>
      get {
        complete {
          for (result <- supervisor ? Find(id, user)) yield result match {
            case Left((ResponseWorkflow(Failure(exception)))) => throw exception
            case Left(ResponseWorkflow(Success(workflow))) =>
              val launcherActor = actors(AkkaConstant.LauncherActorName)
              for {
                response <- (launcherActor ? Launch(workflow, user))
                  .mapTo[Either[Try[Workflow], UnauthorizedResponse]]
              } yield response match {
                case Left(Failure(ex)) => throw ex
                case Left(Success(workflowModel)) => Result("Launched workflow with name " + workflowModel.name)
                case Right(UnauthorizedResponse(exception)) => throw exception
                case _ => throw new RuntimeException("Unexpected behaviour in workflows")
              }
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in workflows")
          }
        }
      }
    }
  }

  //scalastyle:on cyclomatic.complexity

  @Path("/download/{id}")
  @ApiOperation(value = "Downloads a workflow by its id.",
    notes = "Downloads a workflow by its id.",
    httpMethod = "GET",
    response = classOf[Workflow])
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
  def download(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "download" / Segment) { (id) =>
      get {
        val future = supervisor ? Find(id, user)
        Await.result(future, timeout.duration) match {
          case Left(ResponseWorkflow(Failure(exception))) =>
            throw exception
          case Left(ResponseWorkflow(Success(workflow))) =>
            WorkflowValidator.validateDto(workflow)
            val tempFile = File.createTempFile(s"${workflow.id.get}-${workflow.name}-", ".json")
            tempFile.deleteOnExit()
            respondWithHeader(`Content-Disposition`("attachment", Map("filename" -> s"${workflow.name}.json"))) {
              scala.tools.nsc.io.File(tempFile).writeAll(write(workflow))
              getFromFile(tempFile)
            }
        }
      }
    }
  }

  case class Result(message: String, desc: Option[String] = None)

}
