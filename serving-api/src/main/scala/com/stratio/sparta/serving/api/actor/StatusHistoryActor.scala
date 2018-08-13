/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.Actor
import scala.concurrent.ExecutionContext.Implicits.global
import com.stratio.sparta.security.{SpartaSecurityManager, _}
import com.stratio.sparta.serving.api.actor.StatusHistoryActor._
import com.stratio.sparta.serving.api.services.WorkflowStatusHistoryService
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.history.{WorkflowStatusHistory, WorkflowStatusHistoryDto}
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.joda.time.DateTime

import scala.concurrent.Future
import scala.util.{Failure, Try}

class StatusHistoryActor()(implicit val secManagerOpt: Option[SpartaSecurityManager]) extends Actor
  with ActionUserAuthorize{

  private val ResourceType = "History"
  private lazy val statusHistoryService = new WorkflowStatusHistoryService()

  private lazy val enabled = Try(SpartaConfig.getSpartaPostgres.get.getBoolean("historyEnabled")).getOrElse(false)

  override def preStart(): Unit = {
    if (!enabled) {
      log.warn("History is not enabled. Impossible to execute queries")
    }
  }

  def receiveApiActions(action : Any): Unit = action match {
    case FindByWorkflowId(id, user) => findByWorkflowId(id, user)
  }

  def findByWorkflowId(id: String, user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> View)) {
      if(enabled)
        Try{
          validateSlickFuture(statusHistoryService.findAllStatusByWorkflowId(id))
        }.getOrElse(
          Failure(new RuntimeException("Unable to obtain workflow status history data from database"))
        )
      else
          Failure(new RuntimeException("History is not enable. Impossible to execute queries"))
    }

  private def validateSlickFuture(result: Try[Future[List[WorkflowStatusHistory]]]) = {
    val list = result.map(f => f.map(sh => sh.map(dbToStatus)).recover {
      case e: Exception => {
        log.warn(s"Unable to obtain data from database ${e.getMessage}")
        throw new RuntimeException(s"Unable to obtain data from database ${e.getMessage}", e)
      }
    })
    list
  }
}

//scalastyle:off
object StatusHistoryActor {

  case class FindByWorkflowId(id: String, user: Option[LoggedUser])

  implicit def dbToStatus(workflowStatusHistory: WorkflowStatusHistory): WorkflowStatusHistoryDto = {
    WorkflowStatusHistoryDto(
      workflowId = workflowStatusHistory.workflowId,
      status = workflowStatusHistory.status,
      statusId = workflowStatusHistory.statusId,
      statusInfo = workflowStatusHistory.statusInfo,
      creationDate = workflowStatusHistory.creationDate.map(d => new DateTime(d)),
      lastUpdateDate = workflowStatusHistory.lastUpdateDate.map(d => new DateTime(d)),
      lastUpdateDateWorkflow = workflowStatusHistory.creationDate.map(d => new DateTime(d))
    )
  }
}