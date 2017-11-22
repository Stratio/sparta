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
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.{Workflow, WorkflowValidation}
import com.stratio.sparta.serving.core.models.{ErrorModel, SpartaSerializer}
import com.wordnik.swagger.annotations._
import org.json4s.jackson.Serialization.write
import spray.http.HttpHeaders.`Content-Disposition`
import spray.http.StatusCodes
import spray.routing._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Api(value = HttpConstant.WorkflowsPath, description = "Operations over workflows")
trait WorkflowHttpService extends BaseHttpService with SpartaSerializer {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    WorkflowServiceUnexpected,
    ErrorCodesMessages.getOrElse(WorkflowServiceUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route =
    find(user) ~ findAll(user) ~ create(user) ~ createList(user) ~
      update(user) ~ updateList(user) ~ remove(user) ~ run(user) ~ download(user) ~ findByName(user) ~
      removeAll(user) ~ deleteCheckpoint(user) ~ removeList(user) ~ findList(user) ~ validate(user) ~
      resetAllStatuses(user)

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
        context =>
          for {
            response <- (supervisor ? Find(id.toString, user))
              .mapTo[Either[ResponseWorkflow, UnauthorizedResponse]]
          } yield getResponse(context, WorkflowServiceFindById, response, genericError)
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
        context =>
          for {
            response <- (supervisor ? FindByName(name, user))
              .mapTo[Either[ResponseWorkflow, UnauthorizedResponse]]
          } yield getResponse(context, WorkflowServiceFindByName, response, genericError)
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
            } yield deletePostPutResponse(WorkflowServiceFindByIds, response, genericError)
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
          context =>
            for {
              response <- (supervisor ? FindAll(user))
                .mapTo[Either[ResponseWorkflows, UnauthorizedResponse]]
            } yield getResponse(context, WorkflowServiceFindAll, response, genericError)
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
              for {
                response <- (supervisor ? CreateWorkflow(workflow, user))
                  .mapTo[Either[ResponseWorkflow, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowServiceCreate, response, genericError)
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
              for {
                response <- (supervisor ? CreateWorkflows(workflows, user))
                  .mapTo[Either[ResponseWorkflows, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowServiceCreateList, response, genericError)
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
              for {
                response <- (supervisor ? Update(workflow, user))
                  .mapTo[Either[ResponseWorkflow, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowServiceUpdate, response, genericError, StatusCodes.OK)
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
              for {
                response <- (supervisor ? UpdateList(workflows, user))
                  .mapTo[Either[ResponseWorkflows, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowServiceUpdateList, response, genericError, StatusCodes.OK)

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
              response <- (supervisor ? DeleteAll(user))
                .mapTo[Either[Response, UnauthorizedResponse]]
            } yield deletePostPutResponse(WorkflowServiceDeleteAll, response, genericError, StatusCodes.OK)
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
              response <- (supervisor ? DeleteList(workflowIds, user))
                .mapTo[Either[Response, UnauthorizedResponse]]
            } yield {
              deletePostPutResponse(WorkflowServiceDeleteList, response, genericError, StatusCodes.OK)
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
          } yield deletePostPutResponse(WorkflowServiceDeleteById, response, genericError, StatusCodes.OK)
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
            response <- (supervisor ? DeleteCheckpoint(name, user))
              .mapTo[Either[Response, UnauthorizedResponse]]
          } yield {
            deletePostPutResponse(WorkflowServiceDeleteCheckpoint, response, genericError, StatusCodes.OK)
          }
        }
      }
    }
  }

  @Path("/validate")
  @ApiOperation(value = "Validate a workflow.",
    notes = "Validate a workflow.",
    httpMethod = "POST",
    response = classOf[WorkflowValidation])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "workflow",
      defaultValue = "",
      value = "workflow json",
      dataType = "Workflow",
      required = true,
      paramType = "body")))
  def validate(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "validate") {
      pathEndOrSingleSlash {
        post {
          entity(as[Workflow]) { workflow =>
            complete {
              for {
                response <- (supervisor ? ValidateWorkflow(workflow, user))
                  .mapTo[Either[ResponseWorkflowValidation, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowServiceValidate, response, genericError)
            }
          }
        }
      }
    }
  }

  @Path("/resetAllStatuses")
  @ApiOperation(value = "Reset all workflow statuses.",
    notes = "Reset all statuses",
    httpMethod = "POST",
    response = classOf[WorkflowValidation])
  def resetAllStatuses(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "resetAllStatuses") {
      pathEndOrSingleSlash {
        post {
          complete {
            for {
              response <- (supervisor ? ResetAllStatuses(user))
                .mapTo[Either[Response, UnauthorizedResponse]]
            } yield deletePostPutResponse(WorkflowServiceResetAllStatuses, response, genericError)
          }
        }
      }
    }
  }

  @Path("/run/{id}")
  @ApiOperation(value = "Runs a workflow from by id.",
    notes = "Runs a workflow by its id.",
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
        context =>
          val launcherActor = actors(AkkaConstant.LauncherActorName)
          for {
            response <- (launcherActor ? Launch(id.toString, user))
              .mapTo[Either[Try[Workflow], UnauthorizedResponse]]
          } yield getResponse(context, WorkflowServiceRun, response, genericError)
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
      case Left(Failure(e)) =>
        throw new ServerException(ErrorModel.toString(ErrorModel(
          StatusCodes.InternalServerError.intValue,
          WorkflowServiceDownload,
          ErrorCodesMessages.getOrElse(WorkflowServiceDownload, UnknownError),
          None,
          Option(e.getLocalizedMessage)
        )))
      case Left(Success(workflow: Workflow)) =>
        val tempFile = File.createTempFile(s"${workflow.id.get}-${workflow.name}-", ".json")
        tempFile.deleteOnExit()
        (workflow, tempFile)
      case Right(UnauthorizedResponse(exception)) =>
        throw exception
      case _ =>
        throw new ServerException(ErrorModel.toString(genericError))
    }
  }

}
