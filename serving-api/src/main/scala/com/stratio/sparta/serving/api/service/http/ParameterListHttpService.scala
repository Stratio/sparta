/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import javax.ws.rs.Path

import akka.pattern.ask
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.actor.ParameterListActor._
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.parameters.{ParameterList, ParameterListFromWorkflow}
import com.stratio.spray.oauth2.client.OauthClient
import com.wordnik.swagger.annotations._
import spray.http.StatusCodes
import spray.routing.Route

@Api(value = HttpConstant.ParameterListPath, description = "Operations over parameters lists")
trait ParameterListHttpService extends BaseHttpService with OauthClient {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    ParameterListServiceUnexpected,
    ErrorCodesMessages.getOrElse(ParameterListServiceUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route =
    findAll(user) ~ findById(user) ~ findByName(user) ~ findByParent(user) ~ create(user) ~ update(user) ~
      deleteByName(user) ~ deleteAll(user) ~ createFromWorkflow(user)

  @Path("/id/{id}")
  @ApiOperation(value = "Finds a parameter list depending on its id.",
    notes = "Finds a parameter list depending on its id.",
    httpMethod = "GET",
    response = classOf[ParameterList])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the parameter list",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findById(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ParameterListPath / "id" / Segment) { (id) =>
      get {
        context =>
          for {
            response <- (supervisor ? FindByIdParameterList(id, user))
              .mapTo[Either[ResponseParameterList, UnauthorizedResponse]]
          } yield getResponse(context, ParameterListServiceFindById, response, genericError)
      }
    }
  }

  @Path("/name/{name}")
  @ApiOperation(value = "Finds a parameter list depending on its name.",
    notes = "Finds a parameter list depending on its name.",
    httpMethod = "GET",
    response = classOf[ParameterList])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name",
      value = "name of the parameter list",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByName(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ParameterListPath / "name" / Segment) { (name) =>
      get {
        context =>
          for {
            response <- (supervisor ? FindByNameParameterList(name, user))
              .mapTo[Either[ResponseParameterList, UnauthorizedResponse]]
          } yield getResponse(context, ParameterListServiceFindByName, response, genericError)
      }
    }
  }

  @Path("/parent/{parent}")
  @ApiOperation(value = "Finds parameter lists depending on its parent.",
    notes = "Finds parameter lists depending on its parent.",
    httpMethod = "GET",
    response = classOf[Seq[ParameterList]])
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "parent",
      value = "name of the parent parameter list",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findByParent(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ParameterListPath / "parent" / Segment) { (parent) =>
      get {
        context =>
          for {
            response <- (supervisor ? FindByParentParameterList(parent, user))
              .mapTo[Either[ResponseParameterLists, UnauthorizedResponse]]
          } yield getResponse(context, ParameterListServiceFindByParent, response, genericError)
      }
    }
  }

  @ApiOperation(value = "Find all parameter lists",
    notes = "Finds all parameter lists",
    httpMethod = "GET",
    response = classOf[ParameterList],
    responseContainer = "List")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ParameterListPath) {
      get {
        context =>
          for {
            response <- (supervisor ? FindAllParameterList(user))
              .mapTo[Either[ResponseParameterLists, UnauthorizedResponse]]
          } yield getResponse(context, ParameterListServiceFindAll, response, genericError)
      }
    }
  }

  @ApiOperation(value = "Creates a parameter list.",
    notes = "Creates a parameter list.",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "parameterList",
      value = "parameter list to save",
      dataType = "ParameterList",
      required = true,
      paramType = "body")
  ))
  def create(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ParameterListPath) {
      post {
        entity(as[ParameterList]) { parameterList =>
          complete {
            for {
              response <- (supervisor ? CreateParameterList(parameterList, user))
                .mapTo[Either[ResponseParameterList, UnauthorizedResponse]]
            } yield deletePostPutResponse(ParameterListServiceCreate, response, genericError)
          }
        }
      }
    }
  }

  @Path("/createFromWorkflow")
  @ApiOperation(value = "Creates a parameter list from workflow variables.",
    notes = "Creates a parameter list from workflow variables.",
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "ParameterListFromWorkflow",
      value = "parameter list to save with workflow variables",
      dataType = "com.stratio.sparta.serving.core.models.parameters.ParameterListFromWorkflow",
      required = true,
      paramType = "body")
  ))
  def createFromWorkflow(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ParameterListPath / "createFromWorkflow") {
      post {
        entity(as[ParameterListFromWorkflow]) { parameterListFromWorkflow =>
          complete {
            for {
              response <- (supervisor ? CreateParameterListFromWorkflow(parameterListFromWorkflow, user))
                .mapTo[Either[ResponseParameterList, UnauthorizedResponse]]
            } yield deletePostPutResponse(ParameterListServiceCreateFromWorkflow, response, genericError)
          }
        }
      }
    }
  }

  @ApiOperation(value = "Updates a parameter list.",
    notes = "Updates a parameter list.",
    httpMethod = "PUT")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "parameter list",
      value = "parameter list json",
      dataType = "ParameterList",
      required = true,
      paramType = "body")))
  def update(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ParameterListPath) {
      put {
        entity(as[ParameterList]) { parameterList =>
          complete {
            for {
              response <- (supervisor ? UpdateParameterList(parameterList, user))
                .mapTo[Either[Response, UnauthorizedResponse]]
            } yield deletePostPutResponse(ParameterListServiceUpdate, response, genericError, StatusCodes.OK)
          }
        }
      }
    }
  }

  @Path("/name/{name}")
  @ApiOperation(value = "Deletes a parameter list depending on its name",
    notes = "Deletes a parameter list depending on its type and name.",
    httpMethod = "DELETE")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "name",
      value = "name of the parameter list",
      dataType = "string",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteByName(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ParameterListPath / "name" / Segment) { (name) =>
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteByNameParameterList(name, user))
              .mapTo[Either[Response, UnauthorizedResponse]]
          } yield deletePostPutResponse(ParameterListServiceDeleteByName, response, genericError, StatusCodes.OK)
        }
      }
    }
  }

  @ApiOperation(value = "Deletes all parameter list",
    notes = "Deletes all parameter lists.",
    httpMethod = "DELETE")
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)
  ))
  def deleteAll(user: Option[LoggedUser]): Route = {
    path(HttpConstant.ParameterListPath) {
      delete {
        complete {
          for {
            response <- (supervisor ? DeleteAllParameterList(user))
              .mapTo[Either[ResponseParameterLists, UnauthorizedResponse]]
          } yield deletePostPutResponse(ParameterListServiceDeleteAll, response, genericError, StatusCodes.OK)
        }
      }
    }
  }


}