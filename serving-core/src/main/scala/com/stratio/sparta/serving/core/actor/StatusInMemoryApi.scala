/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import com.stratio.sparta.serving.core.actor.StatusInMemoryApi._
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.{StatusChange, StatusRemove}
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.exception.ServerException

import scala.util.Try

class StatusInMemoryApi extends InMemoryServicesStatus {

  override def persistenceId: String = AkkaConstant.StatusApiActorName

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[StatusChange])
    context.system.eventStream.subscribe(self, classOf[StatusRemove])
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[StatusChange])
    context.system.eventStream.unsubscribe(self, classOf[StatusRemove])
  }

  val receiveCommand: Receive = apiReceive.orElse(eventsReceive).orElse(snapshotSaveNotificationReceive)

  def apiReceive: Receive = {
    case FindMemoryStatus(workflowId) =>
      log.debug(s"Find status by id $workflowId")
      sender ! Try(statuses.getOrElse(
        workflowId,
        throw new ServerException(s"No workflow status with id $workflowId")
      ))
    case FindAllMemoryStatus =>
      log.debug(s"Find all statuses")
      sender ! Try(statuses.values.toSeq)
  }
}

object StatusInMemoryApi {

  case object FindAllMemoryStatus

  case class FindMemoryStatus(workflowId: String)

}
