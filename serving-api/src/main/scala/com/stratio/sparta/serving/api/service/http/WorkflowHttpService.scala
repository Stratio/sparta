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
import java.util.UUID
import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparta.serving.api.actor.WorkflowActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.LauncherActor.Launch
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

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Api(value = HttpConstant.WorkflowsPath, description = "Operations over workflows")
trait WorkflowHttpService extends BaseHttpService with SpartaSerializer {

  override def routes(user: Option[LoggedUser] = None): Route =
    find(user) ~ findAll(user) ~ create(user) ~ createList(user) ~
      update(user) ~ updateList(user) ~  remove(user) ~ run(user) ~ download(user) ~ findByName(user) ~
      removeAll(user) ~ deleteCheckpoint(user) ~ removeList(user) ~ findList(user)

  @Path("/findById/{id}")
  @ApiOperation(value = "Finds a workflow from its id.",
    notes = "Finds a workflow from its id.",
    httpMethod = "GET",
    response = classOf[Workflow])
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
  def find(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "findById" / JavaUUID) { id =>
      get {
        complete {
          for {
            response <- (supervisor ? Find(id.toString, user))
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
      dataType = "String",
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

  @Path("/findByIds")
  @ApiOperation(value = "Finds a workflow list.",
    notes = "Finds workflows from workflow ids.",
    httpMethod = "POST",
    response = classOf[Array[Workflow]])
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "workflows",
      defaultValue = "",
      value = "workflows ids",
      dataType = "Array[String]",
      required = true,
      paramType = "body")))
  def findList(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "findByIds") {
      post {
        entity(as[Seq[String]]) { workflowIds =>
          complete {
            for {
              response <- (supervisor ? FindByIdList(workflowIds, user))
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
  }

  @Path("")
  @ApiOperation(value = "Finds all workflows.",
    notes = "Finds all workflows.",
    httpMethod = "GET",
    response = classOf[Array[Workflow]])
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath) {
      pathEndOrSingleSlash {
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
  }

  @Path("")
  @ApiOperation(value = "Creates a workflow.",
    notes = "Creates a workflow.",
    httpMethod = "POST",
    response = classOf[Workflow])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "workflow",
      defaultValue = "",
      value = "workflow json",
      dataType = "Workflow",
      required = true,
      paramType = "body")))
  def create(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath) {
      pathEndOrSingleSlash {
        post {
          entity(as[Workflow]) { workflow =>
            complete {
              WorkflowValidator.validateDto(workflow)
              for {
                response <- (supervisor ? CreateWorkflow(workflow, user))
                  .mapTo[Either[ResponseWorkflow, UnauthorizedResponse]]
              } yield response match {
                case Left(ResponseWorkflow(Failure(exception))) => throw exception
                case Left(ResponseWorkflow(Success(workflowCreated))) => workflowCreated
                case Right(UnauthorizedResponse(exception)) => throw exception
                case _ => throw new RuntimeException("Unexpected behaviour in workflows")
              }
            }
          }
        }
      }
    }
  }

  @Path("/list")
  @ApiOperation(value = "Creates a workflow list.",
    notes = "Creates a workflow list.",
    httpMethod = "POST",
    response = classOf[Array[Workflow]])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "workflow",
      defaultValue = "",
      value = "workflow json list",
      dataType = "Workflow",
      required = true,
      paramType = "body")))
  def createList(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "list") {
      pathEndOrSingleSlash {
        post {
          entity(as[Seq[Workflow]]) { workflows =>
            complete {
              workflows.foreach(WorkflowValidator.validateDto)
              for {
                response <- (supervisor ? CreateWorkflows(workflows, user))
                  .mapTo[Either[ResponseWorkflows, UnauthorizedResponse]]
              } yield response match {
                case Left(ResponseWorkflows(Failure(exception))) => throw exception
                case Left(ResponseWorkflows(Success(workflowsCreated))) => workflowsCreated
                case Right(UnauthorizedResponse(exception)) => throw exception
                case _ => throw new RuntimeException("Unexpected behaviour in workflows")
              }
            }
          }
        }
      }
    }
  }

  @Path("")
  @ApiOperation(value = "Updates a workflow.",
    notes = "Updates a workflow.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "workflow",
      defaultValue = "",
      value = "workflow json",
      dataType = "Workflow",
      required = true,
      paramType = "body")))
  def update(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath) {
      pathEndOrSingleSlash {
        put {
          entity(as[Workflow]) { workflow =>
            complete {
              WorkflowValidator.validateDto(workflow)
              for {
                response <- (supervisor ? Update(workflow, user))
                  .mapTo[Either[ResponseWorkflow, UnauthorizedResponse]]
              } yield response match {
                case Left(ResponseWorkflow(Failure(exception))) => throw exception
                case Left(ResponseWorkflow(Success(pol))) => StatusCodes.OK
                case Right(UnauthorizedResponse(exception)) => throw exception
                case _ => throw new RuntimeException("Unexpected behaviour in workflows")
              }
            }
          }
        }
      }
    }
  }

  @Path("/list")
  @ApiOperation(value = "Updates a workflow list.",
    notes = "Updates a workflow list.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "workflows",
      defaultValue = "",
      value = "workflow json",
      dataType = "Workflow",
      required = true,
      paramType = "body")))
  def updateList(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "list") {
      pathEndOrSingleSlash {
        put {
          entity(as[Seq[Workflow]]) { workflows =>
            complete {
              workflows.foreach(WorkflowValidator.validateDto)
              for {
                response <- (supervisor ? UpdateList(workflows, user))
                  .mapTo[Either[ResponseWorkflows, UnauthorizedResponse]]
              } yield response match {
                case Left(ResponseWorkflows(Failure(exception))) => throw exception
                case Left(ResponseWorkflows(Success(pol))) => StatusCodes.OK
                case Right(UnauthorizedResponse(exception)) => throw exception
                case _ => throw new RuntimeException("Unexpected behaviour in workflows")
              }
            }
          }
        }
      }
    }
  }

  @Path("")
  @ApiOperation(value = "Deletes all workflows.",
    notes = "Deletes all workflows.",
    httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def removeAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath) {
      pathEndOrSingleSlash {
        delete {
          complete {
            for {
              workflows <- (supervisor ? DeleteAll(user))
                .mapTo[Either[Response, UnauthorizedResponse]]
            } yield workflows match {
              case Left(Response(Failure(exception))) => throw exception
              case Left(Response(Success(_))) => StatusCodes.OK
              case Right(UnauthorizedResponse(exception)) => throw exception
              case _ => throw new RuntimeException("Unexpected behaviour in workflows")
            }
          }
        }
      }
    }
  }

  @Path("/list")
  @ApiOperation(value = "Delete workflows.",
    notes = "Deletes workflows from id list.",
    httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "workflows",
      defaultValue = "",
      value = "workflows ids",
      dataType = "Array[String]",
      required = true,
      paramType = "body")))
  def removeList(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "list") {
      delete {
        entity(as[Seq[String]]) { workflowIds =>
          complete {
            for {
              workflows <- (supervisor ? DeleteList(workflowIds, user))
                .mapTo[Either[Response, UnauthorizedResponse]]
            } yield workflows match {
              case Left(Response(Failure(exception))) => throw exception
              case Left(Response(Success(_))) => StatusCodes.OK
              case Right(UnauthorizedResponse(exception)) => throw exception
              case _ => throw new RuntimeException("Unexpected behaviour in workflows")
            }
          }
        }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "Deletes a workflow from its id.",
    notes = "Deletes a workflow from its id.",
    httpMethod = "DELETE")
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
  def remove(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / JavaUUID) { id =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteWorkflow(id.toString, user))
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

  // scalastyle:off cyclomatic.complexity
  @Path("/checkpoint/{name}")
  @ApiOperation(value = "Delete checkpoint associated to a workflow by its name.",
    notes = "Delete checkpoint associated to a workflow by its name.",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name",
      value = "name of the workflow",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def deleteCheckpoint(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "checkpoint" / Segment) { name =>
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
              case Left(Response(Success(_))) => StatusCodes.OK
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
    httpMethod = "GET")
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
    path(HttpConstant.WorkflowsPath / "run" / JavaUUID) { id =>
      get {
        complete {
          for (result <- supervisor ? Find(id.toString, user)) yield result match {
            case Left((ResponseWorkflow(Failure(exception)))) => throw exception
            case Left(ResponseWorkflow(Success(workflow))) =>
              val launcherActor = actors(AkkaConstant.LauncherActorName)
              for {
                response <- (launcherActor ? Launch(workflow, user))
                  .mapTo[Either[Try[Workflow], UnauthorizedResponse]]
              } yield response match {
                case Left(Failure(ex)) => throw ex
                case Left(Success(workflowModel)) => StatusCodes.OK
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
    path(HttpConstant.WorkflowsPath / "download" / JavaUUID) { id =>
      get {
        onComplete(workflowTempFile(id, user)) {
          case Success((workflow, tempFile)) =>
            respondWithHeader(`Content-Disposition`("attachment", Map("filename" -> s"${workflow.name}.json"))) {
              scala.tools.nsc.io.File(tempFile).writeAll(write(workflow))
              getFromFile(tempFile)
            }
          case Failure(ex) => throw ex
        }
      }
    }
  }

  private def workflowTempFile(id: UUID, user: Option[LoggedUser]): Future[(Workflow, File)] = {
    for {
      response <- (supervisor ? Find(id.toString, user))
        .mapTo[Either[ResponseWorkflow, UnauthorizedResponse]]
    } yield response match {
      case Left(ResponseWorkflow(Failure(ex))) => throw ex
      case Left(ResponseWorkflow(Success(workflow))) =>
        val tempFile = File.createTempFile(s"${workflow.id.get}-${workflow.name}-", ".json")
        tempFile.deleteOnExit()
        (workflow, tempFile)
      case Right(UnauthorizedResponse(exception)) => throw exception
      case _ => throw new RuntimeException("Unexpected behaviour in workflows")
    }
  }

}
