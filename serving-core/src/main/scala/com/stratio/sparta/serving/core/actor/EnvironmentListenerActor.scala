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