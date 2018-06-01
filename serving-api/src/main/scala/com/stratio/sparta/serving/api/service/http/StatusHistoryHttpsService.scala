/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.service.http

import javax.ws.rs.Path

import com.stratio.sparta.serving.api.constants.HttpConstant
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.ErrorModel.{ErrorCodesMessages, UnknownError, WorkflowHistoryExecutionUnexpected}
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.history.WorkflowStatusHistoryDto
import com.wordnik.swagger.annotations.{Api, ApiOperation, ApiResponse, ApiResponses}
import spray.http.StatusCodes
import spray.routing._

@Api(value = HttpConstant.StatusHistoryPath, description = "Workflow status history", position = 0)
trait StatusHistoryHttpsService extends BaseHttpService {

  val genericError = ErrorModel(
    StatusCodes.InternalServerError.intValue,
    WorkflowHistoryExecutionUnexpected,
    ErrorCodesMessages.getOrElse(WorkflowHistoryExecutionUnexpected, UnknownError)
  )

  override def routes(user: Option[LoggedUser] = None): Route =
    findAll(user) ~ findByWorkflowId(user)

  @Path("/findAll")
  @ApiOperation(value = "Finds all workflow status stored in Postgres.",
    notes = "Returns a history of workflow statuses",
    httpMethod = "GET",
    response = classOf[Seq[WorkflowStatusHistoryDto]])
  @ApiResponses(Array(
    new ApiResponse(code = HttpConstant.NotFound,
      message = HttpConstant.NotFoundMessage)
  ))
  def findAll(user: Option[LoggedUser]) = ???


  def findByWorkflowId(user: Option[LoggedUser]) = ???


}
