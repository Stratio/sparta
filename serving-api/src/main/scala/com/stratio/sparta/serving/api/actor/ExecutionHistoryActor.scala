/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Try}

import akka.actor.Actor
import org.joda.time.DateTime
import org.json4s.jackson.Serialization._

import com.stratio.sparta.security.{SpartaSecurityManager, _}
import com.stratio.sparta.serving.api.services.WorkflowExecutionHistoryService
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.history.{WorkflowExecutionHistory, WorkflowExecutionHistoryDto}
import com.stratio.sparta.serving.core.models.workflow.{GenericDataExecution, WorkflowDtoImplicit}
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize

//scalastyle:off
class ExecutionHistoryActor()(implicit val secManagerOpt: Option[SpartaSecurityManager]) extends Actor with ActionUserAuthorize {

  import ExecutionHistoryActor._

  private lazy val workflowExecutionHistoryService = new WorkflowExecutionHistoryService()
  private val ResourceType = "Workflows"

  private lazy val enabled = Try(SpartaConfig.getSpartaPostgres.get.getBoolean("historyEnabled")).getOrElse(false)

  override def preStart(): Unit = {
    if (!enabled) {
      log.warn(s"History is not enabled. Impossible to execute queries")
    }
  }

  override def receive: Receive = {
    case QueryByWorkflowId(id, user) => queryByWorkflowId(id, user)
    case QueryByUserId(id, user) => queryByUserId(id, user)
  }

  def queryByWorkflowId(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceType -> View)) {
      if (enabled)
        Try {
          validateSlickFuture(workflowExecutionHistoryService.queryByWorkflowId(id))
        }.getOrElse(
          Failure(new RuntimeException(s"Unable to obtain workflow execution history data from database"))
        )
      else
        Failure(new RuntimeException(s"History is not enabled to execute queries"))
    }

  def queryByUserId(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceType -> View)) {
      if (enabled)
        Try {
          validateSlickFuture(workflowExecutionHistoryService.queryByUserId(id))
        }.getOrElse(
          Failure(new RuntimeException(s"Unable to obtain workflow execution history data from database"))
        )
      else
        Failure(new RuntimeException(s"History is not enabled. Impossible to execute queries"))
    }

  private def validateSlickFuture(result: Try[Future[List[WorkflowExecutionHistory]]]) = {
    val list = result.map(f => f.map(eh => eh.map(dbToExecution)).recover {
      case e: Exception => {
        log.warn(s"Unable to obtain data from database ${e.getMessage}")
        throw new RuntimeException(s"Unable to obtain data from database ${e.getMessage}", e)
      }
    })
    list
  }
}

//scalastyle:off
object ExecutionHistoryActor extends SpartaSerializer {

  class QueryByHistory(user: Option[LoggedUser])

  case class QueryByWorkflowId(workflowId: String, user: Option[LoggedUser]) extends QueryByHistory(user)

  case class QueryByUserId(userId: String, user: Option[LoggedUser]) extends QueryByHistory(user)

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
