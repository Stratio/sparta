/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.stream.ActorMaterializer
import com.stratio.sparta.serving.core.utils.NginxUtils
import com.stratio.sparta.serving.core.utils.NginxUtils._
import com.stratio.sparta.serving.api.actor.NginxActor._
import com.stratio.sparta.serving.core.actor.StatusPublisherActor.StatusChange
import com.stratio.sparta.serving.core.models.workflow.WorkflowStatus
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._


import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Success


class NginxActor extends Actor {

  import context.dispatcher

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[StatusChange])
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

  private def updateIfNecessary(workflowStatus: WorkflowStatus): Unit =
    if(workflowStatus.status == Started || workflowStatus.status == Stopped || workflowStatus.status == Failed)
      updateAndMakeSureIsRunning
    else Future.successful(())


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
    case workflowStatusChange: StatusChange =>
      updateIfNecessary(workflowStatusChange.workflowStatus)
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