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
import com.stratio.sparta.serving.core.models.workflow.{DebugWorkflow, WorkflowIdExecutionContext}
import com.stratio.sparta.serving.api.actor.DebugWorkflowActor._
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.models.files.SpartaFile
import javax.ws.rs.Path

import com.stratio.spray.oauth2.client.OauthClient
import com.wordnik.swagger.annotations._
import spray.http.HttpHeaders.{`Content-Disposition`, `Content-Type`}
import spray.http.{StatusCodes, _}
import spray.httpx.unmarshalling.{FormDataUnmarshallers, Unmarshaller}
import spray.routing.Route
import HttpConstant._

import scala.concurrent.Future
import scala.util.{Failure, Success}

@Api(value = HttpConstant.DebugWorkflowsPath, description = "Workflow debug utility")
trait DebugWorkflowHttpService extends BaseHttpService with OauthClient {

  implicit def unmarshaller[T: Manifest]: Unmarshaller[MultipartFormData] =
    FormDataUnmarshallers.MultipartFormDataUnmarshaller

  override def routes(user: Option[LoggedUser] = None): Route = upload(user) ~ deleteFile(user) ~ downloadFile(user) ~
    findAll(user) ~ create(user) ~ run(user) ~ remove(user) ~ removeAll(user) ~ findById(user) ~ resultsById(user) ~
    runWithExecutionContext(user)

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
    new ApiImplicitParam(name = "Debug workflow",
      value = "debug workflow json",
      dataType = "DebugWorkflow",
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
      value = "id of the debug workflow",
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

  @Path("/runWithExecutionContext")
  @ApiOperation(value = "Runs a debug with execution context.",
    notes = "Runs a debug by its id and providing execution context.",
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
    path(HttpConstant.DebugWorkflowsPath / "runWithExecutionContext") {
      pathEndOrSingleSlash {
        post {
          entity(as[WorkflowIdExecutionContext]) { workflowIdExecutionContext =>
            complete {
              for {
                response <- (supervisor ? RunWithWorkflowIdExecutionContext(workflowIdExecutionContext, user))
                  .mapTo[Either[ResponseRun, UnauthorizedResponse]]
              } yield deletePostPutResponse(WorkflowServiceRun, response, genericError)
            }
          }
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
      value = "id of the debug workflow",
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
      value = "id of the debug workflow",
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
          } yield {
            response match {
              case Left(Success(data)) =>
                getResponse(context, DebugWorkflowServiceResultsFindByIdNotAvailable, response, genericError)
              case Left(Failure(e: ServerException)) =>
                context.complete( StatusCodes.EnhanceYourCalm,
                  ErrorCodesMessages.getOrElse(DebugWorkflowServiceResultsFindByIdNotAvailable, UnknownError))
              case _=>
                getResponse(context, DebugWorkflowServiceResultsFindByIdNotAvailable, response, genericError)
            }
          }
      }
    }
  }


  @Path("")
  @ApiOperation(value = "Deletes all debug workflows.",
    notes = "Deletes all debug workflows.",
    httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def removeAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.DebugWorkflowsPath) {
      pathEndOrSingleSlash {
        delete {
          complete {
            for {
              response <- (supervisor ? DeleteAll(user))
                .mapTo[Either[Response, UnauthorizedResponse]]
            } yield deletePostPutResponse(DebugWorkflowServiceDeleteAll, response, genericError, StatusCodes.OK)
          }
        }
      }
    }
  }

  @Path("/{id}")
  @ApiOperation(value = "Deletes a debug workflow from its id.",
    notes = "Deletes a debug workflow from its id.",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the debug workflow",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def remove(user: Option[LoggedUser]): Route = {
    path(HttpConstant.DebugWorkflowsPath / JavaUUID) { id =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteById(id.toString, user))
              .mapTo[Either[Response, UnauthorizedResponse]]
          } yield deletePostPutResponse(DebugWorkflowServiceDeleteById, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("/uploadFile/{id}")
  @ApiOperation(value = "Uploads a file to the debug directory inside a folder workflowid",
    notes = "Creates a file in the server filesystem with the uploaded file.",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the debug workflow",
      dataType = "String",
      required = true,
      paramType = "path"),
    new ApiImplicitParam(name = "file",
      value = "The mock input file",
      dataType = "file",
      required = true,
      paramType = "formData")))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def upload(user: Option[LoggedUser]): Route = {
    path(HttpConstant.DebugWorkflowsPath / "uploadFile"/ Segment) { id =>
      post {
        pathEndOrSingleSlash {
          entity(as[MultipartFormData]) { form =>
            complete {
              for {
                response <- (supervisor ? UploadFile(form.fields, id, user))
                  .mapTo[Either[SpartaFilesResponse, UnauthorizedResponse]]
              } yield deletePostPutResponse(DebugWorkflowServiceUpload, response, genericError)
            }
          }
        }
      }
    }
  }

  @Path("/deleteFile/{fileName}")
  @ApiOperation(value = "Deletes an uploaded mock file by its name",
    notes = "Deletes one mock file",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fileName",
      value = "Name of the mock file",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def deleteFile(user: Option[LoggedUser]): Route = {
    path(HttpConstant.DebugWorkflowsPath / "deleteFile" / Rest) { file =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteFile(file, user))
              .mapTo[Either[Response, UnauthorizedResponse]]
          } yield deletePostPutResponse(DebugWorkflowServiceDeleteFile, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  private def debugTempFile(file: String, user: Option[LoggedUser]): Future[SpartaFile] = {
    for {
      response <- (supervisor ? DownloadFile(file, user)).mapTo[Either[SpartaFileResponse, UnauthorizedResponse]]
    } yield response match {
      case Left(Failure(e)) =>
        throw new ServerException(ErrorModel.toString(ErrorModel(
          StatusCodes.InternalServerError.intValue,
          ErrorModel.DebugWorkflowServiceDownload,
          ErrorCodesMessages.getOrElse(ErrorModel.DebugWorkflowServiceDownload, UnknownError),
          None,
          Option(e.getLocalizedMessage)
        )))
      case Left(Success(file: SpartaFile)) =>
        file
      case Right(UnauthorizedResponse(exception)) =>
        throw exception
      case _ =>
        throw new ServerException(ErrorModel.toString(genericError))
    }
  }

  @Path("/downloadFile/{fileName}")
  @ApiOperation(value = "Download the desired mock file",
    notes = "Download the desired mock file",
    httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fileName",
      value = "Name of the mock file",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def downloadFile(user: Option[LoggedUser]): Route = {
    path(HttpConstant.DebugWorkflowsPath / "downloadFile" / Rest) { file =>
      get {
        onComplete(debugTempFile(file, user)) {
          case Success(tempFile: SpartaFile) =>
            respondWithHeaders(`Content-Disposition`("attachment", Map("filename" -> tempFile.fileName)),
              `Content-Type`(MediaTypes.`text/plain`)) {
              getFromFile(tempFile.path)
            }
          case Failure(ex) => throw ex
        }
      }
    }
  }
}