/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.service.http

import javax.ws.rs.Path
import scala.concurrent.Future
import scala.util.Try

import akka.pattern.ask
import com.wordnik.swagger.annotations._
import spray.http.StatusCodes
import spray.routing._

import com.stratio.sparta.serving.api.actor.ExecutionHistoryActor.{QueryByUserId, QueryByWorkflowId}
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.history.WorkflowExecutionHistoryDto

@Api(value = HttpConstant.HistoryExecutionsPath, description = "Workflow executions history", position = 0)
trait HistoryExecutionHttpService extends BaseHttpService {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    WorkflowHistoryExecutionUnexpected,
    ErrorCodesMessages.getOrElse(WorkflowHistoryExecutionUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route =
    findByWorkflowId(user) ~ findByUser(user)

  @Path("/findByUserId/{id}")
  @ApiOperation(value = "Finds workflow execution history by User",
    notes = "Returns a history executions list",
    httpMethod = "GET",
    response = classOf[Seq[WorkflowExecutionHistoryDto]],
    responseContainer = "List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the user",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findByUser(user: Option[LoggedUser]): Route = {
    path(HttpConstant.HistoryExecutionsPath / "findByUserId" / Segment) { (id) =>
      get {
        context =>
          for {
            response <- (supervisor ? QueryByUserId(id, user))
              .mapTo[Either[Try[Future[Seq[WorkflowExecutionHistoryDto]]], UnauthorizedResponse]]
          } yield getResponse(context, WorkflowHistoryExecutionFindByUserId, response, genericError)
      }
    }
  }

  @Path("/findByWorkflowId/{id}")
  @ApiOperation(value = "Finds workflow execution history by WorkflowId",
    notes = "Returns an executions list",
    httpMethod = "GET",
    response = classOf[Seq[WorkflowExecutionHistoryDto]],
    responseContainer = "List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "id of the workflow",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findByWorkflowId(user: Option[LoggedUser]): Route = {
    path(HttpConstant.HistoryExecutionsPath / "findByWorkflowId" / JavaUUID) { (id) =>
      get {
        context => {
          for {
            response <- (supervisor ? QueryByWorkflowId(id.toString, user))
              .mapTo[Either[Try[Future[Seq[WorkflowExecutionHistoryDto]]], UnauthorizedResponse]]
          } yield getResponse(context, WorkflowHistoryExecutionFindByWorkflowId, response, genericError)
        }
      }
    }
  }
}
