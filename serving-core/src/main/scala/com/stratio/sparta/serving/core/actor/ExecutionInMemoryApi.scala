/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import com.stratio.sparta.serving.core.actor.ExecutionInMemoryApi._
import com.stratio.sparta.serving.core.actor.ExecutionPublisherActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.exception.ServerException

import scala.util.Try

class ExecutionInMemoryApi extends InMemoryServicesStatus {

  override def persistenceId: String = AkkaConstant.ExecutionApiActorName

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[ExecutionChange])
    context.system.eventStream.subscribe(self, classOf[ExecutionRemove])
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[ExecutionChange])
    context.system.eventStream.unsubscribe(self, classOf[ExecutionRemove])
  }

  val receiveCommand: Receive = apiReceive.orElse(eventsReceive).orElse(snapshotSaveNotificationReceive)

  def apiReceive: Receive = {
    case FindMemoryExecution(executionId) =>
      log.debug(s"Find execution by id $executionId")
      sender ! Try(executions.getOrElse(
        executionId,
        throw new ServerException(s"No execution with id $executionId")
      ))
    case FindAllMemoryExecution =>
      log.debug(s"Find all executions")
      sender ! Try(executions.values.toSeq)
  }
}

object ExecutionInMemoryApi {

  case object FindAllMemoryExecution

  case class FindMemoryExecution(executionId: String)

}
