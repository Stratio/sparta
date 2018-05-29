/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import akka.actor.Actor
import com.stratio.sparta.security.{SpartaSecurityManager, _}
import com.stratio.sparta.serving.api.actor.StatusHistoryActor._
import com.stratio.sparta.serving.api.services.WorkflowStatusHistoryService
import com.stratio.sparta.serving.core.models.dto.LoggedUser
import com.stratio.sparta.serving.core.utils.ActionUserAuthorize

class StatusHistoryActor()(implicit val secManagerOpt: Option[SpartaSecurityManager]) extends Actor
  with ActionUserAuthorize{

  private val ResourceType = "workflow"
  private val statusHistoryService = new WorkflowStatusHistoryService()

  override def receive: Receive = {
    case FindAll(user) => sender ! findAll(user)
    case FindByWorkflowId(id, user) => sender ! findByWorkflowId(id, user)
  }

  def findAll(user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceType -> View)) {
      statusHistoryService.findAll()
    }

  def findByWorkflowId(id: String, user: Option[LoggedUser]): Unit =
    securityActionAuthorizer(user, Map(ResourceType -> View)) {
      statusHistoryService.findByWorkflowId(id)
    }
}

//scalastyle:off
object StatusHistoryActor {

  case class FindAll(user: Option[LoggedUser])

  case class FindByWorkflowId(id: String, user: Option[LoggedUser])


}
