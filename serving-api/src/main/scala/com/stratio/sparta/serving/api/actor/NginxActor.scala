/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.actor

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Cancellable}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, Unsubscribe}
import akka.stream.ActorMaterializer
import com.stratio.sparta.serving.api.actor.NginxActor._
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangePublisherActor
import com.stratio.sparta.serving.core.actor.ExecutionStatusChangePublisherActor.ExecutionStatusChange
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum._
import com.stratio.sparta.serving.core.models.workflow.WorkflowExecution
import com.stratio.sparta.serving.core.utils.NginxUtils
import com.stratio.sparta.serving.core.utils.NginxUtils._

import scala.concurrent.Future
import scala.concurrent.duration._

class NginxActor extends Actor {

  implicit val ec = context.system.dispatchers.lookup("sparta-actors-dispatcher")

  val mediator = DistributedPubSub(context.system).mediator

  private lazy val scheduledInitialCheck: Cancellable =
    context.system.scheduler.scheduleOnce(30.seconds, self, UpdateAndMakeSureIsRunning)


  override def preStart(): Unit = {
    mediator ! Subscribe(ExecutionStatusChangePublisherActor.ClusterTopicExecutionStatus, self)
    scheduledInitialCheck
  }

  private val nginxService = {
    val materializer: ActorMaterializer = ActorMaterializer()
    val config = NginxMetaConfig()
    new NginxUtils(context.system, materializer, config)
  }

  private lazy val scheduledPeriodicCheck: Cancellable = context.system.scheduler.schedule(
    Duration(3, TimeUnit.MINUTES), //Initial delay
    Duration(3, TimeUnit.MINUTES), //Interval
    self,
    GetServiceStatus)

  scheduledPeriodicCheck

  private def updateAndMakeSureIsRunning: Future[Unit] =
    for {
      haveChanges <- nginxService.updateNginx()
      res <- if (haveChanges) nginxService.reloadNginx() else Future.successful(())
    } yield res

  private def updateIfNecessary(workflowExecution: WorkflowExecution): Unit = {
    val state = workflowExecution.lastStatus.state

    if (state == Started || state == Stopped || state == Failed)
      updateAndMakeSureIsRunning
    else Future.successful(())
  }


  override def receive: Receive = {
    case GetServiceStatus =>
      self ! {
        if (nginxService.isNginxRunning) Up else Down
      }
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
    case workflowExecutionStatusChange: ExecutionStatusChange =>
      updateIfNecessary(workflowExecutionStatusChange.executionChange.newExecution)
  }

  override def postStop(): Unit = {
    mediator ! Unsubscribe(ExecutionStatusChangePublisherActor.ClusterTopicExecutionStatus, self)
    scheduledPeriodicCheck.cancel()
    scheduledInitialCheck.cancel()
    updateAndMakeSureIsRunning
  }
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