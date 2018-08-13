/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.{Actor, _}
import com.stratio.sparta.security.{Status => SpartaStatus, _}
import com.stratio.sparta.serving.core.actor.StatusInMemoryApi._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import com.stratio.sparta.serving.core.services.{WorkflowService, WorkflowStatusService}
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework
import StatusActor._

import scala.util.Try

class StatusActor(
                   curatorFramework: CuratorFramework,
                   statusListenerActor: ActorRef,
                   inMemoryStatusApi: ActorRef
                 )(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  private val ResourceType = "Workflows"
  private val statusService = new WorkflowStatusService(curatorFramework)
  private val workflowService = new WorkflowService(curatorFramework)

  //scalastyle:off cyclomatic.complexity
  def receiveApiActions(action : Any): Unit = action match {
    case CreateStatus(workflowStatus, user) => createStatus(workflowStatus, user)
    case Update(workflowStatus, user) => update(workflowStatus, user)
    case FindAll(user) => findAll(user)
    case FindById(id, user) => findById(id, user)
    case DeleteAll(user) => deleteAll(user)
    case DeleteStatus(id, user) => deleteStatus(id, user)
    case _ => log.info("Unrecognized message in Status Actor")
  }

  //scalastyle:on cyclomatic.complexity
  def createStatus(workflowStatus: WorkflowStatus, user: Option[LoggedUser]): Unit = {
    val authorizationId = workflowService.findById(workflowStatus.id).authorizationId
    authorizeActionsByResourceId(user, Map(ResourceType -> SpartaStatus), authorizationId) {
      statusService.create(workflowStatus)
    }
  }

  def update(workflowStatus: WorkflowStatus, user: Option[LoggedUser]): Unit = {
    val authorizationId = workflowService.findById(workflowStatus.id).authorizationId
    authorizeActionsByResourceId(user, Map(ResourceType -> SpartaStatus), authorizationId) {
      statusService.update(workflowStatus)
    }
  }

  def findAll(user: Option[LoggedUser]): Unit = {
    val authorizationIds = workflowService.findAll.map(_.authorizationId)
    authorizeActionsByResourcesIds(
      user,
      Map(ResourceType -> SpartaStatus),
      authorizationIds, Option(inMemoryStatusApi)
    ) {
      FindAllMemoryStatus
    }
  }

  def findById(id: String, user: Option[LoggedUser]): Unit = {
    val authorizationId = workflowService.findById(id).authorizationId
    authorizeActionsByResourceId(user, Map(ResourceType -> SpartaStatus), authorizationId, Option(inMemoryStatusApi)) {
      FindMemoryStatus(id)
    }
  }

  def deleteAll(user: Option[LoggedUser]): Unit = {
    val authorizationIds = workflowService.findAll.map(_.authorizationId)
    authorizeActionsByResourcesIds[Response](user, Map(ResourceType -> SpartaStatus), authorizationIds) {
      statusService.deleteAll()
    }
  }

  def deleteStatus(id: String, user: Option[LoggedUser]): Unit = {
    val authorizationId = workflowService.findById(id).authorizationId
    authorizeActionsByResourceId[Response](user, Map(ResourceType -> SpartaStatus), authorizationId) {
      statusService.delete(id)
    }
  }
}

object StatusActor {

  case class Update(workflowStatus: WorkflowStatus, user: Option[LoggedUser])

  case class CreateStatus(workflowStatus: WorkflowStatus, user: Option[LoggedUser])

  case class DeleteStatus(id: String, user: Option[LoggedUser])

  case class DeleteAll(user: Option[LoggedUser])

  case class FindAll(user: Option[LoggedUser])

  case class FindById(id: String, user: Option[LoggedUser])

  type Response = Try[Unit]

  type ResponseStatuses = Try[Seq[WorkflowStatus]]

  type ResponseStatus = Try[WorkflowStatus]

}