/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.actor

import akka.actor.Actor
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.actor.EnvironmentListenerActor._
import com.stratio.sparta.serving.core.actor.EnvironmentPublisherActor.{EnvironmentChange, EnvironmentRemove}
import com.stratio.sparta.serving.core.models.env.Environment

class EnvironmentListenerActor extends Actor with SLF4JLogging {

  private var environmentState = Map.empty[String, String]

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[EnvironmentChange])
    context.system.eventStream.subscribe(self, classOf[EnvironmentRemove])
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[EnvironmentChange])
    context.system.eventStream.unsubscribe(self, classOf[EnvironmentRemove])
  }

  def manageGetEnvironment(): Unit = {
    sender ! environmentState
  }

  def updateEnvironmentState(environmentChange: EnvironmentChange): Unit = {
    val newEnvMap = environmentChange.environment.variables.map { envVariable =>
      envVariable.name -> envVariable.value
    }.toMap
    log.debug(s"Updating environment state with variables: $newEnvMap")
    environmentState = newEnvMap
  }

  def receive: Receive = {
    case environmentChange: EnvironmentChange =>
      updateEnvironmentState(environmentChange)
    case _: EnvironmentRemove =>
      updateEnvironmentState(EnvironmentChange("", Environment(Seq.empty)))
    case GetEnvironment =>
      manageGetEnvironment()
  }

}

object EnvironmentListenerActor {

  case object GetEnvironment

}