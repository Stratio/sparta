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
import com.stratio.sparta.serving.api.actor.CrossdataActor._
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.crossdata.{QueryRequest, TableInfoRequest, TablesRequest}
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.wordnik.swagger.annotations._
import org.apache.spark.sql.catalog.{Column, Database, Table}
import spray.http.StatusCodes
import spray.routing._

import scala.util.Try

@Api(value = HttpConstant.CrossdataPath, description = "Operations over Crossdata catalog")
trait CrossdataHttpService extends BaseHttpService {

  override def routes(user: Option[LoggedUser] = None): Route = findAllDatabases(user) ~ executeQuery(user) ~
    findTables(user) ~ describeTable(user) ~ findAllTables(user)

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    CrossdataServiceUnexpected,
    ErrorCodesMessages.getOrElse(CrossdataServiceUnexpected, UnknownError)
  )

  @Path("/databases")
  @ApiOperation(value = "List Crossdata databases",
    notes = "Returns Crosdata databases",
    httpMethod = "GET",
    response = classOf[Database],
    responseContainer = "List")
  @ApiResponses(Array(new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)))
  def findAllDatabases(user: Option[LoggedUser]): Route = {
    path(HttpConstant.CrossdataPath / "databases") {
      get { context =>
        for {
          response <- (supervisor ? FindAllDatabases(user))
            .mapTo[Either[Try[Array[Database]], UnauthorizedResponse]]
        } yield getResponse(context, CrossdataServiceListDatabases, response, genericError)
      }
    }
  }

  @Path("/tables")
  @ApiOperation(value = "List all Crossdata tables",
    notes = "Returns Crossdata tables available in the catalog",
    httpMethod = "GET",
    response = classOf[Table],
    responseContainer = "List")
  @ApiResponses(Array(new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)))
  def findAllTables(user: Option[LoggedUser]): Route = {
    path(HttpConstant.CrossdataPath / "tables") {
      get { context =>
        for {
          response <- (supervisor ? FindAllTables(user))
            .mapTo[Either[Try[Array[Table]], UnauthorizedResponse]]
        } yield getResponse(context, CrossdataServiceListTables, response, genericError)
      }
    }
  }

  @Path("/tables")
  @ApiOperation(value = "List Crossdata tables based in one query",
    notes = "Returns Crossdata tables available in the catalog",
    httpMethod = "POST",
    response = classOf[Table],
    responseContainer = "List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "tableRequest",
      value = "Table conditions request",
      dataType = "TableRequest",
      required = true,
      paramType = "body")
  ))
  @ApiResponses(Array(new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)))
  def findTables(user: Option[LoggedUser]): Route = {
    path(HttpConstant.CrossdataPath / "tables") {
      post {
        entity(as[TablesRequest]) { tablesRequest =>
          complete {
            for {
              response <- (supervisor ? FindTables(tablesRequest, user))
                .mapTo[Either[Try[Array[Table]], UnauthorizedResponse]]
            } yield deletePostPutResponse(CrossdataServiceListTables, response, genericError)
          }
        }
      }
    }
  }

  @Path("/tables/info")
  @ApiOperation(value = "List Crossdata fields associated to one table",
    notes = "Returns crossdata fields",
    httpMethod = "POST",
    response = classOf[Column],
    responseContainer = "List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "TableInfoRequest",
      value = "Table conditions request",
      dataType = "TableInfoRequest",
      required = true,
      paramType = "body")
  ))
  @ApiResponses(Array(new ApiResponse(code = HttpConstant.NotFound, message = HttpConstant.NotFoundMessage)))
  def describeTable(user: Option[LoggedUser]): Route = {
    path(HttpConstant.CrossdataPath / "tables" / "info") {
      post {
        entity(as[TableInfoRequest]) { tableInfoRequest =>
          complete {
            for {
              response <- (supervisor ? DescribeTable(tableInfoRequest, user))
                .mapTo[Either[Try[Array[Column]], UnauthorizedResponse]]
            } yield deletePostPutResponse(CrossdataServiceListColumns, response, genericError)
          }
        }
      }
    }
  }

  @Path("/queries")
  @ApiOperation(value = "Execute one query in Crossdata",
    notes = "Query executor in crossdata, useful to register tables in the catalog",
    httpMethod = "POST",
    response = classOf[Array[Map[String, Any]]],
    responseContainer = "List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "query",
      value = "Query string",
      dataType = "QueryRequest",
      required = true,
      paramType = "body")
  ))
  def executeQuery(user: Option[LoggedUser]): Route = {
    path(HttpConstant.CrossdataPath / "queries") {
      post {
        entity(as[QueryRequest]) { queryRequest =>
          complete {
            for {
              response <- (supervisor ? ExecuteQuery(queryRequest, user))
                .mapTo[Either[Try[Array[Map[String, Any]]], UnauthorizedResponse]]
            } yield deletePostPutResponse(CrossdataServiceExecuteQuery, response, genericError)
          }
        }
      }
    }
  }
}
