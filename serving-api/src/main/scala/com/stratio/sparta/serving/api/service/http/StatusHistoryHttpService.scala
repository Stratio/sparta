/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import javax.ws.rs.Path

import com.stratio.sparta.serving.api.actor.StatusHistoryActor.FindByWorkflowId
import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.history.WorkflowStatusHistoryDto
import com.wordnik.swagger.annotations._
import spray.http.StatusCodes
import spray.routing._
import akka.pattern.ask
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper.UnauthorizedResponse

import scala.concurrent.Future
import scala.util.Try


@Api(value = HttpConstant.StatusHistoryPath, description = "Workflow status history")
trait StatusHistoryHttpService extends BaseHttpService {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    WorkflowHistoryStatusUnexpected,
    ErrorCodesMessages.getOrElse(WorkflowHistoryStatusUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route = findByWorkflowId(user)

  @Path("/findByWorkflowId/{id}")
  @ApiOperation(value = "Finds workflow status history by WorkflowId",
    notes = "Returns an status list",
    httpMethod = "GET",
    response = classOf[Seq[WorkflowStatusHistoryDto]],
    responseContainer = "List")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id",
      value = "Id of the workflow",
      dataType = "String",
      required = true,
      paramType = "path")
  ))
  @ApiResponses(
    Array(new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)))
  def findByWorkflowId(user: Option[LoggedUser]): Route = {
    path(HttpConstant.StatusHistoryPath / "findByWorkflowId" / JavaUUID) { (id) =>
      get {
        context => {
          for {
            response <- (supervisor ? FindByWorkflowId(id.toString, user))
              .mapTo[Either[Try[Future[Seq[WorkflowStatusHistoryDto]]], UnauthorizedResponse]]
          } yield getResponse(context, WorkflowHistoryStatusFindByWorkflowId, response, genericError)
        }
      }
    }
  }
}
