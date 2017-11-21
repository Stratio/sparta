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
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.recipes.cache.{PathChildrenCache, PathChildrenCacheEvent, PathChildrenCacheListener}
import org.json4s.jackson.Serialization.read

import scala.util.Try

object StatusPublisherActor {
  trait Notification
  case class WorkflowChange(path: String, workflow: WorkflowStatus) extends Notification

}

class StatusPublisherActor(curatorFramework: CuratorFramework) extends Actor with SpartaSerializer with SLF4JLogging  {
  import StatusPublisherActor._

  private case class State(pathCache: PathChildrenCache)

  override def preStart(): Unit = {

    val statusesPath = AppConstant.WorkflowStatusesZkPath
    val pathCache: PathChildrenCache = new PathChildrenCache(curatorFramework, statusesPath, true)


    val nodeListener = new PathChildrenCacheListener {
      override def childEvent(client: CuratorFramework, event: PathChildrenCacheEvent): Unit = {
        val eventData = event.getData
        Try {
          read[WorkflowStatus](new String(eventData.getData))
        } foreach {
          self ! WorkflowChange(eventData.getPath, _)
        }
      }
    }

    pathCache.getListenable.addListener(nodeListener, context.dispatcher)
    pathCache.start()

    context.become(receive(State(pathCache)))

  }

  def receive(st: State): Receive = {
    {
      case cd: WorkflowChange =>
        context.system.eventStream.publish(cd)
    }
  }

  override def receive: Receive = PartialFunction.empty

}