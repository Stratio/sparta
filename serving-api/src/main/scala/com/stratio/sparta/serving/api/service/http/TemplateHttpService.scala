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
import spray.routing.Route

import com.stratio.sparta.serving.api.constants.HttpConstant._
import com.stratio.sparta.serving.api.actor.TemplateActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.TemplateElement
import com.stratio.spray.oauth2.client.OauthClient


@Api(value = HttpConstant.TemplatePath, description = "Operations over templates (inputs, outputs and transformations)")
trait TemplateHttpService extends BaseHttpService with OauthClient {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    TemplateServiceUnexpected,
    ErrorCodesMessages.getOrElse(TemplateServiceUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route =
    findAll(user) ~ findByTypeAndId(user) ~ findByTypeAndName(user) ~ //findById(user) ~
  findAllByType(user) ~ create(user) ~ update(user) ~ deleteByTypeAndId(user) ~
      deleteByType(user) ~ deleteByTypeAndName(user) ~ deleteAll(user) ~ migrate(user)

  @Path("/id/{id}")
  @ApiOperation(value = "Finds a template depending on its id.",
    notes = "Finds a template depending on its id.",
    httpMethod = "GET",
    response = classOf[TemplateElement])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the template",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findById(user: Option[LoggedUser]): Route = {
    path(HttpConstant.TemplatePath / "id" / Segment) { (id) =>
      get {
        context =>
          for {
            response <- (supervisor ? FindById(id, user))
              .mapTo[Either[ResponseTemplate, UnauthorizedResponse]]
          } yield getResponse(context, TemplateServiceFindById, response, genericError)
      }
    }
  }

  @Path("/{templateType}/id/{templateId}")
  @ApiOperation(value = "Finds a template depending on its type and id.",
    notes = "Finds a template depending on its type and id.",
    httpMethod = "GET",
    response = classOf[TemplateElement])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "templateType",
      value = "type of template (input/output/transformation)",
      dataType = "string",
      required = true,
      paramType = "path"),
    new ApiImplicitParam(name = "templateId",
      value = "id of the template",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByTypeAndId(user: Option[LoggedUser]): Route = {
    path(HttpConstant.TemplatePath / Segment / "id" / Segment) { (templateType, id) =>
      get {
        context =>
          for {
            response <- (supervisor ? FindByTypeAndId(templateType, id, user))
              .mapTo[Either[ResponseTemplate, UnauthorizedResponse]]
          } yield getResponse(context, TemplateServiceFindByTypeId, response, genericError)
      }
    }
  }

  @Path("/{templateType}/name/{templateName}")
  @ApiOperation(value = "Finds a template depending on its type and name.",
    notes = "Finds a template depending on its type and name.",
    httpMethod = "GET",
    response = classOf[TemplateElement])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "templateType",
      value = "type of template (input/output/transformation)",
      dataType = "string",
      required = true,
      paramType = "path"),
    new ApiImplicitParam(name = "templateName",
      value = "name of the template",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByTypeAndName(user: Option[LoggedUser]): Route = {
    path(HttpConstant.TemplatePath / Segment / "name" / Segment) { (templateType, name) =>
      get {
        context =>
          for {
            response <- (supervisor ? FindByTypeAndName(templateType, name, user))
              .mapTo[Either[ResponseTemplate, UnauthorizedResponse]]
          } yield getResponse(context, TemplateServiceFindByTypeName, response, genericError)
      }
    }
  }

  @Path("/{templateType}")
  @ApiOperation(value = "Finds a list of templates depending on its type.",
    notes = "Finds a list of templates depending on its type.",
    httpMethod = "GET",
    response = classOf[TemplateElement],
    responseContainer = "List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "templateType",
      value = "type of template (input/output/transformation)",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findAllByType(user: Option[LoggedUser]): Route = {
    path(HttpConstant.TemplatePath / Segment) { (templateType) =>
      get {
        context =>
          for {
            response <- (supervisor ? FindByType(templateType, user))
              .mapTo[Either[ResponseTemplates, UnauthorizedResponse]]
          } yield getResponse(context, TemplateServiceFindAllByType, response, genericError)
      }
    }
  }

  @ApiOperation(value = "Find all templates",
    notes = "Finds all templates",
    httpMethod = "GET",
    response = classOf[Seq[TemplateElement]],
    responseContainer = "List")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.TemplatePath) {
      get {
        context =>
          for {
            response <- (supervisor ? FindAllTemplates(user))
              .mapTo[Either[ResponseTemplates, UnauthorizedResponse]]
          } yield getResponse(context, TemplateServiceFindAll, response, genericError)
      }
    }
  }

  @ApiOperation(value = "Creates a template depending of its type.",
    notes = "Creates a template depending on its type.",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "template",
      value = "template to save",
      dataType = "com.stratio.sparta.serving.core.models.workflow.TemplateElement",
      required = true,
      paramType = "body")
  ))
  def create(user: Option[LoggedUser]): Route = {
    path(HttpConstant.TemplatePath) {
      post {
        entity(as[TemplateElement]) { template =>
          complete {
            for {
              response <- (supervisor ? CreateTemplate(template, user))
                .mapTo[Either[ResponseTemplate, UnauthorizedResponse]]
            } yield deletePostPutResponse(TemplateServiceCreate, response, genericError)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Updates a template.",
    notes = "Updates a template.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "template",
      value = "template json",
      dataType = "com.stratio.sparta.serving.core.models.workflow.TemplateElement",
      required = true,
      paramType = "body")))
  def update(user: Option[LoggedUser]): Route = {
    path(HttpConstant.TemplatePath) {
      put {
        entity(as[TemplateElement]) { template =>
          complete {
            for {
              response <- (supervisor ? Update(template, user))
                .mapTo[Either[Response, UnauthorizedResponse]]
            } yield deletePostPutResponse(TemplateServiceUpdate, response, genericError, StatusCodes.OK)
          }
        }
      }
    }
  }

  @Path("/{templateType}/id/{templateId}")
  @ApiOperation(value = "Deletes a template depending of its type and id",
    notes = "Deletes a template depending on its type and id.",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "templateType",
      value = "type of template (input/output/transformation))",
      dataType = "string",
      required = true,
      paramType = "path"),
    new ApiImplicitParam(name = "templateId",
      value = "id of the template",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteByTypeAndId(user: Option[LoggedUser]): Route = {
    path(HttpConstant.TemplatePath / Segment / "id" / Segment) { (templateType, id) =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteByTypeAndId(templateType, id, user))
              .mapTo[Either[ResponseBoolean, UnauthorizedResponse]]
          } yield deletePostPutResponse(TemplateServiceDeleteByTypeId, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("/{templateType}/name/{templateName}")
  @ApiOperation(value = "Deletes a template depending on its type and name",
    notes = "Deletes a template depending on its type and name.",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "templateType",
      value = "type of template (input/output/transformation)",
      dataType = "string",
      required = true,
      paramType = "path"),
    new ApiImplicitParam(name = "templateName",
      value = "name of the template",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteByTypeAndName(user: Option[LoggedUser]): Route = {
    path(HttpConstant.TemplatePath / Segment / "name" / Segment) { (templateType, name) =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteByTypeAndName(templateType, name, user))
              .mapTo[Either[ResponseBoolean, UnauthorizedResponse]]
          } yield deletePostPutResponse(TemplateServiceDeleteByTypeName, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @ApiOperation(value = "Deletes a template depending on its type",
    notes = "Deletes a template depending of its type.",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "templateType",
      value = "type of template (input/output/transformation)",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteByType(user: Option[LoggedUser]): Route = {
    path(HttpConstant.TemplatePath / Segment) { (templateType) =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteByType(templateType, user))
              .mapTo[Either[ResponseBoolean, UnauthorizedResponse]]
          } yield deletePostPutResponse(TemplateServiceDeleteByType, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @ApiOperation(value = "Deletes all templates",
    notes = "Deletes all templates.",
    httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.TemplatePath) {
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteAllTemplates(user))
              .mapTo[Either[ResponseBoolean, UnauthorizedResponse]]
          } yield deletePostPutResponse(TemplateServiceDeleteAll, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @Path("/cassiopeiaMigration")
  @ApiOperation(value = "Migrate template from sparta cassiopeia to andromeda.",
    notes = "Migrate template from sparta cassiopeia to andromeda.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "template",
      value = "template json",
      dataType = "com.stratio.sparta.serving.core.models.workflow.TemplateElement",
      required = true,
      paramType = "body")))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def migrate(user: Option[LoggedUser]): Route = {
    path(HttpConstant.TemplatePath / "cassiopeiaMigration") {
      put {
        entity(as[TemplateElement]) { template =>
          context =>
            for {
              response <- (supervisor ? Migrate(template, user))
                .mapTo[Either[ResponseTemplate, UnauthorizedResponse]]
            } yield getResponse(context, TemplateMigration, response, genericError)
        }
      }
    }
  }
}