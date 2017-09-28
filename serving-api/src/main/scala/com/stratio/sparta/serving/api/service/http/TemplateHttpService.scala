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

import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.TemplateActor._
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.TemplateElement
import com.stratio.spray.oauth2.client.OauthClient
import com.wordnik.swagger.annotations._
import spray.http.{HttpResponse, StatusCodes}
import spray.routing.Route

import scala.util.{Failure, Success}

@Api(value = HttpConstant.TemplatePath, description = "Operations over templates (inputs, outputs and transformations)")
trait TemplateHttpService extends BaseHttpService with OauthClient {

  override def routes(user: Option[LoggedUser] = None): Route =
    findAll(user) ~ findByTypeAndId(user) ~ findByTypeAndName(user) ~
      findAllByType(user) ~ create(user) ~ update(user) ~ deleteByTypeAndId(user) ~
      deleteByType(user) ~ deleteByTypeAndName(user) ~ deleteAll(user)

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
        complete {
          for {
            responseTemplate <- (supervisor ? FindByTypeAndId(templateType, id, user))
              .mapTo[Either[ResponseTemplate, UnauthorizedResponse]]
          } yield responseTemplate match {
            case Left(Failure(exception)) => throw exception
            case Left(Success(template)) => template
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in templates")
          }
        }
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
        complete {
          for {
            responseTemplate <- (supervisor ? FindByTypeAndName(templateType, name, user))
              .mapTo[Either[ResponseTemplate, UnauthorizedResponse]]
          } yield responseTemplate match {
            case Left(Failure(exception)) => throw exception
            case Left(Success(template)) => template
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in templates")
          }
        }
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
        complete {
          for {
            responseTemplates <- (supervisor ? FindByType(templateType, user))
              .mapTo[Either[ResponseTemplates, UnauthorizedResponse]]
          } yield responseTemplates match {
            case Left(Failure(exception)) => throw exception
            case Left(Success(templates)) => templates
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in templates")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Find all templates",
    notes = "Finds all templates",
    httpMethod = "GET",
    response = classOf[TemplateElement],
    responseContainer = "List")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.TemplatePath) {
      get {
        complete {
          for {
            responseTemplates <- (supervisor ? FindAllTemplates(user))
              .mapTo[Either[ResponseTemplates, UnauthorizedResponse]]
          } yield responseTemplates match {
            case Left(Failure(exception)) => throw exception
            case Left(Success(templates)) => templates
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in templates")
          }
        }
      }
    }
  }

  @ApiOperation(value = "Creates a template depending of its type.",
    notes = "Creates a template depending on its type.",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "template",
      value = "template to save",
      dataType = "TemplateElementModel",
      required = true,
      paramType = "body")
  ))
  def create(user: Option[LoggedUser]): Route = {
    path(HttpConstant.TemplatePath) {
      post {
        entity(as[TemplateElement]) { template =>
          complete {
            for {
              responseTemplate <- (supervisor ? CreateTemplate(template, user))
                .mapTo[Either[ResponseTemplate, UnauthorizedResponse]]
            } yield responseTemplate match {
              case Left(Failure(exception)) => throw exception
              case Left(Success(template: TemplateElement)) => template
              case Right(UnauthorizedResponse(exception)) => throw exception
              case _ => throw new RuntimeException("Unexpected behaviour in templates")
            }
          }
        }
      }
    }
  }

  // scalastyle:off cyclomatic.complexity
  @ApiOperation(value = "Updates a template.",
    notes = "Updates a template.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "template",
      value = "template json",
      dataType = "TemplateElementModel",
      required = true,
      paramType = "body")))
  def update(user: Option[LoggedUser]): Route = {
    path(HttpConstant.TemplatePath) {
      put {
        entity(as[TemplateElement]) { template =>
          complete {
            for {
              responseTemplates <- (supervisor ? Update(template, user))
                .mapTo[Either[Response, UnauthorizedResponse]]
            } yield responseTemplates match {
              case Left(Success(_)) => HttpResponse(StatusCodes.OK)
              case Left(Failure(exception)) => throw exception
              case Right(UnauthorizedResponse(exception)) => throw exception
              case _ => throw new RuntimeException("Unexpected behaviour in templates")
            }
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
            responseTemplates <- (supervisor ? DeleteByTypeAndId(templateType, id, user))
              .mapTo[Either[Response, UnauthorizedResponse]]
          } yield responseTemplates match {
            case Left(Success(_)) => HttpResponse(StatusCodes.OK)
            case Left(Failure(exception)) => throw exception
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in templates")
          }
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
            responseTemplates <- (supervisor ? DeleteByTypeAndName(templateType, name, user))
              .mapTo[Either[Response, UnauthorizedResponse]]
          } yield responseTemplates match {
            case Left(Success(_)) => HttpResponse(StatusCodes.OK)
            case Left(Failure(exception)) => throw exception
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in templates")
          }
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
            responseTemplates <- (supervisor ? DeleteByType(templateType, user))
              .mapTo[Either[Response, UnauthorizedResponse]]
          } yield responseTemplates match {
            case Left(Success(_)) => HttpResponse(StatusCodes.OK)
            case Left(Failure(exception)) => throw exception
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in templates")
          }
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
            templatesResponse <- (supervisor ? DeleteAllTemplates(user))
              .mapTo[Either[ResponseTemplates, UnauthorizedResponse]]
          } yield templatesResponse match {
            case Left(Failure(exception)) => throw exception
            case Left(Success(_)) => HttpResponse(StatusCodes.OK)
            case Right(UnauthorizedResponse(exception)) => throw exception
            case _ => throw new RuntimeException("Unexpected behaviour in templates")
          }
        }
      }
    }
  }
}