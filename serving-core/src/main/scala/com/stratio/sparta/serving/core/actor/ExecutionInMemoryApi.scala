/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
