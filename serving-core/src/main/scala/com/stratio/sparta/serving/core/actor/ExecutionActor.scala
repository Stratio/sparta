/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.{Actor, ActorRef}
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.actor.ExecutionActor._
import com.stratio.sparta.serving.core.actor.ExecutionInMemoryApi._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecution
import com.stratio.sparta.serving.core.services.ExecutionService
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework

class ExecutionActor(val curatorFramework: CuratorFramework, inMemoryApiExecution: ActorRef)
                    (implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  private val executionService = new ExecutionService(curatorFramework)
  private val ResourceType = "Workflows"

  override def receive: Receive = {
    case CreateExecution(workflowExecution, user) => createExecution(workflowExecution, user)
    case Update(workflowExecution, user) => updateExecution(workflowExecution, user)
    case FindAll(user) => findAllExecutions(user)
    case FindById(id, user) => findExecutionById(id, user)
    case DeleteAll(user) => deleteAllExecutions(user)
    case DeleteExecution(id, user) => deleteExecution(id, user)
    case ClearLastError(id) => sender ! executionService.clearLastError(id)
    case _ => log.info("Unrecognized message in Workflow Execution Actor")
  }

  def createExecution(workflowExecution: WorkflowExecution, user: Option[LoggedUser]): Unit = {
    val resourcesId = workflowExecution.authorizationId
    authorizeActionsByResourceId(user, Map(ResourceType -> Status), resourcesId) {
      executionService.create(workflowExecution)
    }
  }

  def updateExecution(workflowExecution: WorkflowExecution, user: Option[LoggedUser]): Unit = {
    val resourcesId = workflowExecution.authorizationId
    authorizeActionsByResourceId(user, Map(ResourceType -> Status), resourcesId) {
      executionService.update(workflowExecution)
    }
  }

  def findAllExecutions(user: Option[LoggedUser]): Unit =
    filterResultsWithAuthorization(user, Map(ResourceType -> Status), Option(inMemoryApiExecution)) {
      FindAllMemoryExecution
    }

  def findExecutionById(id: String, user: Option[LoggedUser]): Unit =
    authorizeResultByResourceId(user, Map(ResourceType -> Status), Option(inMemoryApiExecution)) {
      FindMemoryExecution(id)
    }

  def deleteAllExecutions(user: Option[LoggedUser]): Unit = {
    val authorizationIds = executionService.findAll()
      .map(executions => executions.map(_.authorizationId))
      .getOrElse(Seq.empty)
    authorizeActionsByResourcesIds(user, Map(ResourceType -> Status), authorizationIds) {
      executionService.deleteAll()
    }
  }

  def deleteExecution(id: String, user: Option[LoggedUser]): Unit = {
    val authorizationId = executionService.findById(id).map(_.authorizationId).getOrElse("N/A")
    authorizeActionsByResourceId(user, Map(ResourceType -> Status), authorizationId) {
      executionService.delete(id)
    }
  }

}

object ExecutionActor {

  case class Update(workflowExecution: WorkflowExecution, user: Option[LoggedUser])

  case class CreateExecution(workflowExecution: WorkflowExecution, user: Option[LoggedUser])

  case class DeleteExecution(id: String, user: Option[LoggedUser])

  case class DeleteAll(user: Option[LoggedUser])

  case class FindAll(user: Option[LoggedUser])

  case class FindById(id: String, user: Option[LoggedUser])

  case class ClearLastError(id: String)
}
