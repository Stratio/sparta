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
import com.stratio.sparta.serving.core.actor.EnvironmentStateActor.{EnvironmentChange, GetEnvironment}
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.env.Environment
import com.stratio.sparta.serving.core.services.EnvironmentService
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache._
import org.json4s.jackson.Serialization.read

import scala.util.Try

class EnvironmentStateActor(curatorFramework: CuratorFramework)
  extends Actor with SpartaSerializer with SLF4JLogging {

  private val environmentService = new EnvironmentService(curatorFramework)
  private var environmentState = Map.empty[String, String]

  private case class State(nodeCache: NodeCache)

  override def preStart(): Unit = {
    val environmentPath = AppConstant.EnvironmentZkPath
    val nodeCache = new NodeCache(curatorFramework, environmentPath)
    val nodeListener = new NodeCacheListener {
      override def nodeChanged(): Unit = {
        val eventData = nodeCache.getCurrentData
        Try {
          read[Environment](new String(eventData.getData))
        } foreach {
          self ! EnvironmentChange(eventData.getPath, _)
        }
      }
    }

    nodeCache.getListenable.addListener(nodeListener, context.dispatcher)

    nodeCache.start()

    context.become(receive(State(nodeCache)))

    environmentService.find().foreach(environment => self ! EnvironmentChange(environmentPath, environment))

  }

  def receive(st: State): Receive = {
    {
      case environmentChange: EnvironmentChange => updateEnvironmentState(environmentChange)
      case GetEnvironment => manageGetEnvironment()
    }
  }

  def manageGetEnvironment(): Unit =
    sender ! environmentState

  def updateEnvironmentState(environmentChange: EnvironmentChange): Unit = {
    val newEnvMap = environmentChange.environment.variables.map{envVariable =>
      envVariable.name -> envVariable.value
    }.toMap
    environmentState = newEnvMap
    //context.system.eventStream.publish(environmentChange)
  }

  override def receive: Receive = PartialFunction.empty

}

object EnvironmentStateActor {

  trait Notification

  case class EnvironmentChange(path: String, environment: Environment) extends Notification

  case object GetEnvironment

}