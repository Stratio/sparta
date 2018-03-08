/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparta.serving.api.actor.PluginActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.exception.ServerException
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.files.SpartaFile
import com.stratio.spray.oauth2.client.OauthClient
import com.wordnik.swagger.annotations._
import spray.http.HttpHeaders.`Content-Disposition`
import spray.http.{StatusCodes, _}
import spray.httpx.unmarshalling.{FormDataUnmarshallers, Unmarshaller}
import spray.routing.Route

import scala.concurrent.Future
import scala.util.{Failure, Success}

@Api(value = HttpConstant.PluginsPath, description = "Upload or download jars plugins")
trait PluginsHttpService extends BaseHttpService with OauthClient {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    PluginsServiceUnexpected,
    ErrorCodesMessages.getOrElse(PluginsServiceUnexpected, UnknownError)
  )

  implicit def unmarshaller[T: Manifest]: Unmarshaller[MultipartFormData] =
    FormDataUnmarshallers.MultipartFormDataUnmarshaller

  override def routes(user: Option[LoggedUser] = None): Route = upload(user) ~
    download(user) ~ getAll(user) ~ deleteAllFiles(user) ~ deleteFile(user)

  @Path("")
  @ApiOperation(value = "Uploads a file to the plugin directory.",
    notes = "Creates a file in the server filesystem with the uploaded jar.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "file",
      value = "The jar",
      dataType = "file",
      required = true,
      paramType = "formData")
  ))
  def upload(user: Option[LoggedUser]): Route = {
    path(HttpConstant.PluginsPath) {
      pathEndOrSingleSlash {
        put {
          entity(as[MultipartFormData]) { form =>
            complete {
              for {
                response <- (supervisor ? UploadPlugins(form.fields, user))
                  .mapTo[Either[PluginResponse, UnauthorizedResponse]]
              } yield deletePostPutResponse(PluginsServiceUpload, response, genericError, StatusCodes.OK)
            }
          }
        }
      }
    }
  }

  @Path("")
  @ApiOperation(value = "Browses all plugins uploaded",
    notes = "Finds all plugins.",
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def getAll(user: Option[LoggedUser]): Route =
    path(HttpConstant.PluginsPath) {
      pathEndOrSingleSlash {
        get {
          context =>
            for {
              response <- (supervisor ? ListPlugins(user))
                .mapTo[Either[SpartaFilesResponse, UnauthorizedResponse]]
            } yield getResponse(context, PluginsServiceFindAll, response, genericError)
        }
      }
    }

  @Path("")
  @ApiOperation(value = "Deletes all plugins uploaded",
    notes = "Deletes all plugins.",
    httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def deleteAllFiles(user: Option[LoggedUser]): Route =
    path(HttpConstant.PluginsPath) {
      pathEndOrSingleSlash {
        delete {
          complete {
            for {
              response <- (supervisor ? DeletePlugins(user))
                .mapTo[Either[SpartaFilesResponse, UnauthorizedResponse]]
            } yield deletePostPutResponse(PluginsServiceDeleteAll, response, genericError, StatusCodes.OK)
          }
        }
      }
    }

  @Path("/{fileName}")
  @ApiOperation(value = "Deletes one uploaded plugin",
    notes = "Delete one plugin",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fileName",
      value = "Name of the jar",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def deleteFile(user: Option[LoggedUser]): Route = {
    path(HttpConstant.PluginsPath / Segment) { file =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeletePlugin(file, user))
              .mapTo[Either[PluginResponse, UnauthorizedResponse]]
          } yield deletePostPutResponse(PluginsServiceDeleteByName, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("/{fileName}")
  @ApiOperation(value = "Downloads a file from the plugin directory.",
    httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "fileName",
      value = "Name of the jar",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  def download(user: Option[LoggedUser]): Route =
    path(HttpConstant.PluginsPath / "download" / Segment) { file =>
      get {
        onComplete(pluginTempFile(file, user)) {
          case Success(tempFile: SpartaFile) =>
            respondWithHeader(`Content-Disposition`("attachment", Map("filename" -> tempFile.fileName))) {
              getFromFile(tempFile.path)
            }
          case Failure(ex) => throw ex
        }
      }
    }

  private def pluginTempFile(file: String, user: Option[LoggedUser]): Future[SpartaFile] = {
    for {
      response <- (supervisor ? DownloadPlugin(file, user)).mapTo[Either[SpartaFileResponse, UnauthorizedResponse]]
    } yield response match {
      case Left(Failure(e)) =>
        throw new ServerException(ErrorModel.toString(ErrorModel(
          StatusCodes.InternalServerError.intValue,
          PluginsServiceDownload,
          ErrorCodesMessages.getOrElse(PluginsServiceDownload, UnknownError),
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
}
