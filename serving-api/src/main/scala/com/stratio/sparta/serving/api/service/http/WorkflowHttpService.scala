/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.service.http

import java.io.{File, PrintWriter}
import java.util.UUID
import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparta.serving.api.actor.WorkflowActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.models.workflow.migration.WorkflowCassiopeia
import com.wordnik.swagger.annotations._
import org.json4s.jackson.Serialization.write
import spray.http.HttpHeaders.`Content-Disposition`
import spray.http.StatusCodes
import spray.routing._

import scala.concurrent.Future
import scala.util.{Failure, Success}

@Api(value = HttpConstant.WorkflowsPath, description = "Operations over workflows")
trait WorkflowHttpService extends BaseHttpService {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    WorkflowServiceUnexpected,
    ErrorCodesMessages.getOrElse(WorkflowServiceUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route =
    find(user) ~ findAll(user) ~ create(user) ~ run(user) ~ stop(user) ~ reset(user) ~
      update(user) ~ remove(user) ~ removeWithAllVersions(user) ~ download(user) ~ findById(user) ~
      removeAll(user) ~ deleteCheckpoint(user) ~ removeList(user) ~ findList(user) ~ validate(user) ~
      resetAllStatuses(user) ~ createVersion(user) ~ findByIdWithExecutionContext(user) ~ findAllByGroup(user) ~
      findAllMonitoring(user) ~ rename(user) ~ move(user) ~ runWithExecutionContext(user) ~ runWithVariables(user) ~
      migrate(user) ~ validateWithContext(user)

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
  def findById(user: Option[LoggedUser]): Route = {
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

  @Path("/findAllByGroup/{groupID}")
  @ApiOperation(value = "Find all workflows by group id",
    notes = "Find all workflows by group name",
    httpMethod = "GET",
    response = classOf[Array[WorkflowDto]])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "groupID",
      value = "workflow group",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findAllByGroup(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "findAllByGroup" / Segment) { groupID =>
      get {
        context =>
          for {
            response <- (supervisor ? FindAllByGroup(groupID, user))
              .mapTo[Either[ResponseWorkflowsDto, UnauthorizedResponse]]
          } yield getResponse(context, WorkflowServiceFindAllByGroup, response, genericError)
      }
    }
  }

  @Path("/findByIdWithExecutionContext")
  @ApiOperation(value = "Finds a workflow from its id with execution context.",
    notes = "Finds a workflow from its id with context substitution.",
    httpMethod = "POST",
    response = classOf[Workflow])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "workflowIdExecutionContext",
      value = "workflow id execution context model",
      dataType = "WorkflowIdExecutionContext",
      required = true,
      paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByIdWithExecutionContext(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "findByIdWithExecutionContext") { id =>
      pathEndOrSingleSlash {
        post {
          entity(as[WorkflowIdExecutionContext]) { workflowIdExecutionContext =>
            complete {
              for {
                response <- (supervisor ? FindByIdWithExecutionContext(workflowIdExecutionContext, user))
                  .mapTo[Either[ResponseWorkflow, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowServiceFindByIdWithExecutionContext, response, genericError)
            }
          }
        }
      }
    }
  }


  @Path("/find")
  @ApiOperation(value = "Finds a workflow with query.",
    notes = "Finds a workflow from query.",
    httpMethod = "POST",
    response = classOf[Workflow])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "query",
      value = "query model",
      dataType = "WorkflowQuery",
      required = true,
      paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def find(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "find") {
      pathEndOrSingleSlash {
        post {
          entity(as[WorkflowQuery]) { workflowQuery =>
            complete {
              for {
                response <- (supervisor ? Query(workflowQuery, user))
                  .mapTo[Either[ResponseWorkflow, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowServiceFind, response, genericError)
            }
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

  @Path("/findAllMonitoring")
  @ApiOperation(value = "Finds all workflows Dto for monitoring.",
    notes = "Finds all workflows with less fields for monitoring view.",
    httpMethod = "GET",
    response = classOf[Array[WorkflowDto]])
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findAllMonitoring(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "findAllMonitoring") {
      pathEndOrSingleSlash {
        get {
          context =>
            for {
              response <- (supervisor ? FindAllMonitoring(user))
                .mapTo[Either[ResponseWorkflowsDto, UnauthorizedResponse]]
            } yield getResponse(context, WorkflowServiceFindAllMonitoring, response, genericError)
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

  @Path("/removeWithAllVersions")
  @ApiOperation(value = "Removes a workflow and all its versions.",
    notes = "Removes a workflow and all its versions",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "query",
      value = "workflow name",
      dataType = "WorkflowDelete",
      required = true,
      paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def removeWithAllVersions(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "removeWithAllVersions") {
      pathEndOrSingleSlash {
        delete {
          entity(as[WorkflowDelete]) { query =>
            complete {
              for {
                response <- (supervisor ? DeleteWorkflowWithAllVersions(query, user))
                  .mapTo[Either[Response, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowServiceDeleteWithAllVersions, response, genericError, StatusCodes.OK)
            }
          }
        }
      }
    }
  }

  // scalastyle:off cyclomatic.complexity
  @Path("/checkpoint/{id}")
  @ApiOperation(value = "Delete checkpoint associated to a workflow by its id.",
    notes = "Delete checkpoint associated to a workflow by its id.",
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
  def deleteCheckpoint(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "checkpoint" / Segment) { id =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteCheckpoint(id, user))
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
    new ApiImplicitParam(name = "Workflow",
      defaultValue = "",
      value = "workflow in json and the execution context for the workflow",
      dataType = "Workflow",
      required = true,
      paramType = "body")
  ))
  def validate(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "validate") {
      pathEndOrSingleSlash {
        post {
          entity(as[Workflow]) { workflow =>
            complete {
              for {
                response <- (supervisor ? ValidateWorkflow(workflow, user))
                  .mapTo[Either[ResponseFutureWorkflowValidation, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowServiceValidate, response, genericError)
            }
          }
        }
      }
    }
  }

  @Path("/validateWithExecutionContext")
  @ApiOperation(value = "Validate a workflow with execution context.",
    notes = "Validate a workflow with execution context.",
    httpMethod = "POST",
    response = classOf[WorkflowValidation])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "WorkflowIdExecutionContext",
      value = "workflow in json and the execution context for the workflow",
      dataType = "com.stratio.sparta.serving.core.models.workflow.WorkflowIdExecutionContext",
      required = true,
      paramType = "body")
  ))
  def validateWithContext(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "validateWithExecutionContext") {
      pathEndOrSingleSlash {
        post {
          entity(as[WorkflowIdExecutionContext]) { workflowIdExecutionContext =>
            complete {
              for {
                response <- (supervisor ? ValidateWorkflowIdWithExContext(workflowIdExecutionContext, user))
                  .mapTo[Either[ResponseFutureWorkflowValidation, UnauthorizedResponse]]
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
            } yield deletePostPutResponse(WorkflowServiceResetAllStatuses, response, genericError, StatusCodes.OK)
          }
        }
      }
    }
  }

  @Path("/run/{id}")
  @ApiOperation(value = "Runs a workflow from by id.",
    notes = "Runs a workflow by its id.",
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
    path(HttpConstant.WorkflowsPath / "run" / JavaUUID) { id =>
      post {
        complete {
          for {
            response <- (supervisor ? Run(id.toString, user))
              .mapTo[Either[ResponseFutureAny, UnauthorizedResponse]]
          } yield deletePostPutResponseFuture(WorkflowServiceRun, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("/runWithVariables")
  @ApiOperation(value = "Runs a workflow with extra variables.",
    notes = "Runs a workflow by its id and providing execution variables.",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "WorkflowExecutionVariables",
      value = "Workflow id and list of execution variables.",
      dataType = "com.stratio.sparta.serving.core.models.workflow.WorkflowExecutionVariables",
      required = true,
      paramType = "body")))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def runWithVariables(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "runWithVariables") {
      pathEndOrSingleSlash {
        post {
          entity(as[WorkflowExecutionVariables]) { executionWithVariables =>
            complete {
              for {
                response <- (supervisor ? RunWithVariables(executionWithVariables, user))
                  .mapTo[Either[ResponseFutureAny, UnauthorizedResponse]]
              } yield deletePostPutResponseFuture(WorkflowServiceRun, response, genericError)
            }
          }
        }
      }
    }
  }

  @Path("/runWithExecutionContext")
  @ApiOperation(value = "Runs a workflow with execution context.",
    notes = "Runs a workflow by its id and providing execution context.",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "WorkflowIdExecutionContext",
      value = "Workflow id and execution context.",
      dataType = "com.stratio.sparta.serving.core.models.workflow.WorkflowIdExecutionContext",
      required = true,
      paramType = "body")))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def runWithExecutionContext(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "runWithExecutionContext") {
      pathEndOrSingleSlash {
        post {
          entity(as[WorkflowIdExecutionContext]) { workflowIdExecutionContext =>
            complete {
              for {
                response <- (supervisor ? RunWithWorkflowIdExecutionContext(workflowIdExecutionContext, user))
                  .mapTo[Either[ResponseFutureAny, UnauthorizedResponse]]
              } yield deletePostPutResponseFuture(WorkflowServiceRun, response, genericError)
            }
          }
        }
      }
    }
  }

  @Path("/stop/{id}")
  @ApiOperation(value = "Stop a workflow from by id.",
    notes = "Stop a workflow by its id.",
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
  def stop(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "stop" / JavaUUID) { id =>
      post {
        complete {
          for {
            response <- (supervisor ? Stop(id.toString, user))
              .mapTo[Either[ResponseAny, UnauthorizedResponse]]
          } yield deletePostPutResponse(WorkflowServiceStop, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("/reset/{id}")
  @ApiOperation(value = "Reset status workflow by id.",
    notes = "Reset status workflow by id.",
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
  def reset(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "reset" / JavaUUID) { id =>
      post {
        complete {
          for {
            response <- (supervisor ? Reset(id.toString, user))
              .mapTo[Either[ResponseAny, UnauthorizedResponse]]
          } yield deletePostPutResponse(WorkflowServiceReset, response, genericError, StatusCodes.OK)
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
              val printWriter = new PrintWriter(tempFile)
              try {
                printWriter.write(write(workflow))
              } finally {
                printWriter.close()
              }
              getFromFile(tempFile)
            }
          case Failure(ex) => throw ex
        }
      }
    }
  }

  @Path("/version")
  @ApiOperation(value = "Create new workflow version.",
    notes = "Creates new workflow version.",
    httpMethod = "POST",
    response = classOf[Workflow])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "query",
      value = "new version model",
      dataType = "WorkflowVersion",
      required = true,
      paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def createVersion(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "version") {
      pathEndOrSingleSlash {
        post {
          entity(as[WorkflowVersion]) { workflowVersion =>
            complete {
              for {
                response <- (supervisor ? CreateWorkflowVersion(workflowVersion, user))
                  .mapTo[Either[ResponseWorkflow, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowServiceNewVersion, response, genericError)
            }
          }
        }
      }
    }
  }

  @Path("/rename")
  @ApiOperation(value = "Update the workflow names for all workflow versions.",
    notes = "Update the names for all workflow versions",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "query",
      value = "new workflow name",
      dataType = "WorkflowRename",
      required = true,
      paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def rename(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "rename") {
      pathEndOrSingleSlash {
        put {
          entity(as[WorkflowRename]) { query =>
            complete {
              for {
                response <- (supervisor ? RenameWorkflow(query, user))
                  .mapTo[Either[Response, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowServiceRename, response, genericError, StatusCodes.OK)
            }
          }
        }
      }
    }
  }

  @Path("/move")
  @ApiOperation(value = "Move workflow versions between groups.",
    notes = "Move workflow versions between groups",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "query",
      defaultValue = "",
      value = "workflow to move",
      dataType = "WorkflowMove",
      required = true,
      paramType = "body")))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def move(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "move") {
      pathEndOrSingleSlash {
        put {
          entity(as[WorkflowMove]) { query =>
            complete {
              for {
                response <- (supervisor ? MoveWorkflow(query, user))
                  .mapTo[Either[ResponseWorkflowsDto, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowServiceMove, response, genericError)
            }
          }
        }
      }
    }
  }

  @Path("/cassiopeiaMigration")
  @ApiOperation(value = "Migrate workflow between sparta versions.",
    notes = "Migrate workflow between sparta versions.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "workflow cassiopeia",
      defaultValue = "",
      value = "workflow json",
      dataType = "WorkflowCassiopeia",
      required = true,
      paramType = "body")))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def migrate(user: Option[LoggedUser]): Route = {
    path(HttpConstant.WorkflowsPath / "cassiopeiaMigration") {
      pathEndOrSingleSlash {
        put {
          entity(as[WorkflowCassiopeia]) { workflow =>
            complete {
              for {
                response <- (supervisor ? MigrateFromCassiopeia(workflow, user))
                  .mapTo[Either[ResponseWorkflowAndromeda, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowServiceMigration, response, genericError)
            }
          }
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
