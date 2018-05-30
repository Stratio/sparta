/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.Actor
import org.joda.time.DateTime
import org.json4s.jackson.Serialization._

import com.stratio.sparta.security.{SpartaSecurityManager, _}
import com.stratio.sparta.serving.api.services.WorkflowExecutionHistoryService
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.history.{WorkflowExecutionHistory, WorkflowExecutionHistoryDto}
import com.stratio.sparta.serving.core.models.workflow.{GenericDataExecution, WorkflowDtoImplicit}
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize

//scalastyle:off
class ExecutionHistoryActor()(implicit val secManagerOpt: Option[SpartaSecurityManager]) extends Actor with ActionUserAuthorize {

  import ExecutionHistoryActor._

  private lazy val workflowExecutionHistoryService = new WorkflowExecutionHistoryService()
  private val ResourceType = "workflow"

  override def receive: Receive = {
    case QueryByWorkflowId(id, user) => queryByWorkflowId(id, user)
    case QueryByUserId(id, user) => queryByUserId(id, user)
  }

  def queryByWorkflowId(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceType -> View)) {
        val result = workflowExecutionHistoryService.queryByWorkflowId(id)
        val list = result.map(f => f.map(eh => eh.map(dbToExecution)))
        list
    }

  def queryByUserId(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceType -> View)) {
      val result = workflowExecutionHistoryService.queryByUserId(id)
      val list = result.map(f => f.map(eh => eh.map(dbToExecution)))
      list
    }
}

//scalastyle:off
object ExecutionHistoryActor extends SpartaSerializer {

  case class QueryByWorkflowId(workflowId: String, user: Option[LoggedUser])

  case class QueryByUserId(userId: String, user: Option[LoggedUser])

  implicit def dbToExecution(workflowExecutionHistory: WorkflowExecutionHistory): WorkflowExecutionHistoryDto = {
    WorkflowExecutionHistoryDto(
      executionId = workflowExecutionHistory.executionId,
      workflowId = workflowExecutionHistory.workflowId,
      executionMode = workflowExecutionHistory.executionMode,
      launchDate = workflowExecutionHistory.launchDate.map(d => new DateTime(d)),
      startDate = workflowExecutionHistory.startDate.map(d => new DateTime(d)),
      endDate = workflowExecutionHistory.endDate.map(d => new DateTime(d)),
      userId = workflowExecutionHistory.userId,
      lastError = workflowExecutionHistory.lastError,
      genericExecution = WorkflowDtoImplicit.genericExecutionToDto(read[GenericDataExecution](workflowExecutionHistory.genericExecution)))
  }
}
