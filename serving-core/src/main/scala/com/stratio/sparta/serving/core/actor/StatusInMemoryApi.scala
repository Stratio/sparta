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
