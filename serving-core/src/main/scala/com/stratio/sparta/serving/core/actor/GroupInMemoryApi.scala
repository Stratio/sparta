/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
