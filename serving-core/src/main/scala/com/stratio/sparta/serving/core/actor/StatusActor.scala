/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.{Actor, _}
import com.stratio.sparta.security._
import com.stratio.sparta.serving.core.actor.StatusActor._
import com.stratio.sparta.serving.core.actor.StatusInMemoryApi._
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import com.stratio.sparta.serving.core.services.{ListenerService, WorkflowStatusService}
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize
import org.apache.curator.framework.CuratorFramework

import scala.util.Try

class StatusActor(
                   curatorFramework: CuratorFramework,
                   statusListenerActor: ActorRef,
                   inMemoryStatusApi: ActorRef
                 )(implicit val secManagerOpt: Option[SpartaSecurityManager])
  extends Actor with ActionUserAuthorize {

  private val ResourceType = "status"
  private val statusService = new WorkflowStatusService(curatorFramework)

  //scalastyle:off cyclomatic.complexity
  override def receive: Receive = {
    case CreateStatus(policyStatus, user) => createStatus(policyStatus, user)
    case Update(policyStatus, user) => update(policyStatus, user)
    case ClearLastError(id) => sender ! statusService.clearLastError(id)
    case FindAll(user) => findAll(user)
    case FindById(id, user) => findById(id, user)
    case DeleteAll(user) => deleteAll(user)
    case DeleteStatus(id, user) => deleteStatus(id, user)
    case _ => log.info("Unrecognized message in Status Actor")
  }

  //scalastyle:on cyclomatic.complexity

  def createStatus(policyStatus: WorkflowStatus, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceType -> Create)) {
      statusService.create(policyStatus)
    }

  def update(policyStatus: WorkflowStatus, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceType -> Edit)) {
      statusService.update(policyStatus)
    }


  def findAll(user: Option[LoggedUser]): Unit = {
    securityActionAuthorizer(
      user,
      Map(ResourceType -> View),
      Option(inMemoryStatusApi)
    ) {
      FindAllMemoryStatus
    }
  }


  def findById(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(
      user,
      Map(ResourceType -> View),
      Option(inMemoryStatusApi)
    ) {
      FindMemoryStatus(id)
    }

  def deleteAll(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[Response](user, Map(ResourceType -> Delete)) {
      statusService.deleteAll()
    }

  def deleteStatus(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer[Response](user, Map(ResourceType -> Delete)) {
      statusService.delete(id)
    }
}

object StatusActor {

  case class Update(policyStatus: WorkflowStatus, user: Option[LoggedUser])

  case class CreateStatus(policyStatus: WorkflowStatus, user: Option[LoggedUser])

  case class DeleteStatus(id: String, user: Option[LoggedUser])

  case class DeleteAll(user: Option[LoggedUser])

  case class FindAll(user: Option[LoggedUser])

  case class FindById(id: String, user: Option[LoggedUser])

  case class ClearLastError(id: String)

  type Response = Try[Unit]

  type ResponseStatuses = Try[Seq[WorkflowStatus]]

  type ResponseStatus = Try[WorkflowStatus]

}