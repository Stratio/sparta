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

package com.stratio.sparta.serving.api.actor

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.stream.ActorMaterializer
import com.stratio.sparta.serving.core.utils.NginxUtils
import com.stratio.sparta.serving.core.utils.NginxUtils._
import com.stratio.sparta.serving.api.actor.NginxActor._
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.WorkflowStatusChange

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Success


class NginxActor extends Actor {

  import context.dispatcher

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[WorkflowStatusChange])
    context.system.scheduler.scheduleOnce(Duration(30, TimeUnit.SECONDS),
      self,
      UpdateAndMakeSureIsRunning)
  }

  private val nginxService = {
    val materializer: ActorMaterializer = ActorMaterializer()
    val config = NginxMetaConfig()
    new NginxUtils(context.system, materializer, config)
  }

  context.system.scheduler.schedule(
    Duration(3, TimeUnit.MINUTES), //Initial delay
    Duration(3, TimeUnit.MINUTES), //Interval
    self,
    GetServiceStatus)

  private def updateAndMakeSureIsRunning: Future[Unit] =
    for {
      haveChanges <- nginxService.updateNginx()
      res <- if(haveChanges) nginxService.reloadNginx() else Future.successful(())
    } yield res

  override def receive: Receive = {
    case GetServiceStatus =>
      self ! { if(nginxService.isNginxRunning) Up else Down }
    case Start =>
      nginxService.startNginx()
    case Stop =>
      nginxService.stopNginx()
    case Restart =>
      nginxService.reloadNginx()
    case Update =>
      nginxService.updateNginx()
    case Down =>
      nginxService.startNginx()
      updateAndMakeSureIsRunning
    case UpdateAndMakeSureIsRunning =>
      updateAndMakeSureIsRunning
    case _: WorkflowStatusChange =>
      updateAndMakeSureIsRunning
  }

  override def postStop(): Unit =
    updateAndMakeSureIsRunning
}

object NginxActor {

  trait ServiceStatus
  object Up extends ServiceStatus
  object Down extends ServiceStatus

  object GetServiceStatus
  object Start
  object Stop
  object Restart
  object Update
  object UpdateAndMakeSureIsRunning

}