/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor

import scala.concurrent.Future
import scala.util.Try

import akka.actor.Actor

import com.stratio.sparta.security._
import com.stratio.sparta.serving.api.actor.ExecutionActor._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.{DashboardView, DtoModelImplicits, WorkflowExecution, WorkflowExecutionDto}
import com.stratio.sparta.serving.core.utils.{ActionUserAuthorize, PostgresDaoFactory}

class ExecutionActor()
                    (implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  import DtoModelImplicits._

  private val executionPgService = PostgresDaoFactory.executionPgService
  private val ResourceType = "Workflows"

  //scalastyle:off
  def receiveApiActions(action: Any): Any = action match {
    case CreateExecution(workflowExecution, user) => createExecution(workflowExecution, user)
    case Update(workflowExecution, user) => updateExecution(workflowExecution, user)
    case CreateDashboardView(user) => createDashboardView(user)
    case FindAll(user) => findAllExecutions(user)
    case FindAllDto(user) => findAllExecutionsDto(user)
    case FindById(id, user) => findExecutionById(id, user)
    case DeleteAll(user) => deleteAllExecutions(user)
    case DeleteExecution(id, user) => deleteExecution(id, user)
    case Stop(id, user) => stopExecution(id, user)
    case _ => log.info("Unrecognized message in Workflow Execution Actor")
  }

  //scalastyle:on

  def createExecution(workflowExecution: WorkflowExecution, user: Option[LoggedUser]): Unit = {
    val resourcesId = workflowExecution.authorizationId
    authorizeActionsByResourceId(user, Map(ResourceType -> Status), resourcesId) {
      executionPgService.createExecution(workflowExecution)
    }
  }

  def updateExecution(workflowExecution: WorkflowExecution, user: Option[LoggedUser]): Unit = {
    val resourcesId = workflowExecution.authorizationId
    authorizeActionsByResourceId(user, Map(ResourceType -> Status), resourcesId) {
      executionPgService.updateExecution(workflowExecution)
    }
  }

  def stopExecution(id: String, user: Option[LoggedUser]): Future[Any] = {
    val sendResponseTo = Option(sender)
    for {
      execById <- executionPgService.findExecutionById(id)
    } yield {
      val authorizationId = execById.authorizationId
      authorizeActionsByResourceId[WorkflowExecution](user, Map(ResourceType -> Status), authorizationId, sendResponseTo) {
        executionPgService.stopExecution(id)
      }
    }
  }

  def createDashboardView(user: Option[LoggedUser]): Unit =
    authorizeActions(user, Map(ResourceType -> View)) {
      executionPgService.createDashboardView()
    }

  def findAllExecutions(user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceType -> Status)) {
      executionPgService.findAllExecutions()
    }

  def findAllExecutionsDto(user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceType -> Status), Option(sender)) {
      executionPgService.findAllExecutions().map { executions =>
        executions.map { execution =>
          val executionDto: WorkflowExecutionDto = execution
          executionDto
        }
      }
    }

  def findExecutionById(id: String, user: Option[LoggedUser]): Unit =
    authorizeActionResultResources(user, Map(ResourceType -> Status)) {
      executionPgService.findExecutionById(id)
    }

  def deleteAllExecutions(user: Option[LoggedUser]): Future[Any] = {
    val sendResponseTo = Option(sender)
    for {
      ids <- executionPgService.findAllExecutions()
    } yield {
      val authorizationIds = ids.map(executions => executions.authorizationId)
      authorizeActionsByResourcesIds(user, Map(ResourceType -> Status), authorizationIds, sendResponseTo) {
        executionPgService.deleteAllExecutions()
      }
    }
  }

  def deleteExecution(id: String, user: Option[LoggedUser]): Future[Any] = {
    val sendResponseTo = Option(sender)
    for {
      execById <- executionPgService.findExecutionById(id)
    } yield {
      val authorizationId = execById.authorizationId
      authorizeActionsByResourceId(user, Map(ResourceType -> Status), authorizationId, sendResponseTo) {
        executionPgService.deleteByID(id)
      }
    }
  }
}

object ExecutionActor {

  case class Update(workflowExecution: WorkflowExecution, user: Option[LoggedUser])

  case class CreateExecution(workflowExecution: WorkflowExecution, user: Option[LoggedUser])

  case class DeleteExecution(id: String, user: Option[LoggedUser])

  case class DeleteAll(user: Option[LoggedUser])

  case class CreateDashboardView(user: Option[LoggedUser])

  case class FindAll(user: Option[LoggedUser])

  case class FindAllDto(user: Option[LoggedUser])

  case class FindById(id: String, user: Option[LoggedUser])

  case class Stop(id: String, user: Option[LoggedUser])

  type ResponseWorkflowExecution = Try[WorkflowExecution]

  type ResponseWorkflowExecutions = Try[Seq[WorkflowExecution]]

  type ResponseWorkflowExecutionsDto = Try[Seq[WorkflowExecutionDto]]

  type ResponseDashboardView = Try[DashboardView]
}
