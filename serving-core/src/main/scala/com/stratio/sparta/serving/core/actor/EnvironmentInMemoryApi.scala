/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import com.stratio.sparta.serving.core.actor.EnvironmentInMemoryApi._
import com.stratio.sparta.serving.core.actor.EnvironmentPublisherActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.exception.ServerException

import scala.util.Try

class EnvironmentInMemoryApi extends InMemoryServicesStatus {

  override def persistenceId: String = AkkaConstant.EnvironmentApiActorName

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[EnvironmentChange])
    context.system.eventStream.subscribe(self, classOf[EnvironmentRemove])
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[EnvironmentChange])
    context.system.eventStream.unsubscribe(self, classOf[EnvironmentRemove])
  }

  val receiveCommand: Receive = apiReceive.orElse(eventsReceive).orElse(snapshotSaveNotificationReceive)

  def apiReceive: Receive = {
    case FindMemoryEnvironment =>
      log.debug(s"Find environment")
      sender ! Try(environment.getOrElse(throw new ServerException(s"No environment found")))
    case FindMemoryEnvironmentVariable(name) =>
      log.debug(s"Find environment variable $name")
      sender ! Try {
        environment match {
          case Some(env) =>
            env.variables.find(variable => variable.name == name)
              .getOrElse(throw new ServerException(s"The environment variable doesn't exist"))
          case None =>
            throw new ServerException(s"No environment found")
        }
      }
  }
}

object EnvironmentInMemoryApi {

  case object FindMemoryEnvironment

  case class FindMemoryEnvironmentVariable(name: String)

}
