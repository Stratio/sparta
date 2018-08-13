/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import com.stratio.sparta.serving.core.actor.ParameterListInMemoryApi._
import com.stratio.sparta.serving.core.actor.ParameterListPublisherActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.exception.ServerException

import scala.util.Try

class ParameterListInMemoryApi extends InMemoryServicesStatus {

  override def persistenceId: String = AkkaConstant.ParameterListActorName

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[ParameterListChange])
    context.system.eventStream.subscribe(self, classOf[ParameterListRemove])
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[ParameterListChange])
    context.system.eventStream.unsubscribe(self, classOf[ParameterListRemove])
  }

  val receiveCommand: Receive = apiReceive.orElse(eventsReceive).orElse(snapshotSaveNotificationReceive)

  def apiReceive: Receive = {
    case FindByNameMemoryParameterList(parameterListName) =>
      log.debug(s"Find parameter list by name $parameterListName")
      sender ! Try(parameterLists.getOrElse(
        parameterListName,
        throw new ServerException(s"No parameter list with name $parameterListName")
      ))
    case FindByParentMemoryParameterList(parent) =>
      log.debug(s"Find all parameter lists")
      sender ! Try(parameterLists.values.toSeq.filter(parameterList => parameterList.parent.contains(parent)))
    case FindByIdMemoryParameterList(parameterListId) =>
      log.debug(s"Find parameter list by id $parameterListId")
      sender ! Try {
        parameterLists.find { case (_, parameterList) => parameterList.id == Option(parameterListId) } match {
          case Some(parameterList) => parameterList._2
          case None => throw new ServerException(s"No parameter list with id $parameterListId")
        }
      }
    case FindAllMemoryParameterList =>
      log.debug(s"Find all parameter lists")
      sender ! Try(parameterLists.values.toSeq)
  }
}

object ParameterListInMemoryApi {

  case object FindAllMemoryParameterList

  case class FindByIdMemoryParameterList(parameterListId: String)

  case class FindByNameMemoryParameterList(parameterListName: String)

  case class FindByParentMemoryParameterList(parameterListParent: String)

}


