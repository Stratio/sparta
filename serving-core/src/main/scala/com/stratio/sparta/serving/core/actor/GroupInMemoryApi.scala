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

import com.stratio.sparta.serving.core.actor.GroupInMemoryApi._
import com.stratio.sparta.serving.core.actor.GroupPublisherActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.exception.ServerException

import scala.util.Try

class GroupInMemoryApi extends InMemoryServicesStatus {

  override def persistenceId: String = AkkaConstant.GroupApiActorName

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[GroupChange])
    context.system.eventStream.subscribe(self, classOf[GroupRemove])
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[GroupChange])
    context.system.eventStream.unsubscribe(self, classOf[GroupRemove])
  }

  val receiveCommand: Receive = apiReceive.orElse(eventsReceive).orElse(snapshotSaveNotificationReceive)

  def apiReceive: Receive = {
    case FindMemoryGroup(groupId) =>
      log.debug(s"Find group by id $groupId")
      sender ! Try(groups.getOrElse(
        groupId,
        throw new ServerException(s"No group with id $groupId")
      ))
    case FindMemoryGroupByName(groupName) =>
      log.debug(s"Find group by name $groupName")
      sender ! Try {
        val groupFound = groups.find(group => group._2.name == groupName)
          .getOrElse(throw new ServerException(s"No group with name $groupName"))
        groupFound._2
      }
    case FindAllMemoryGroup =>
      log.debug(s"Find all groups")
      sender ! Try(groups.values.toSeq)
  }
}

object GroupInMemoryApi {

  case object FindAllMemoryGroup

  case class FindMemoryGroup(groupId: String)

  case class FindMemoryGroupByName(groupName: String)

}
