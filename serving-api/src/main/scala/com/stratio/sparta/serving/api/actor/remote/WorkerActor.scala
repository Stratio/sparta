/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor.remote

import akka.actor.{Actor, ActorRef}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.event.slf4j.SLF4JLogging
import akka.pattern.pipe
import com.stratio.sparta.serving.api.actor.remote.WorkerActor._
import com.stratio.sparta.serving.core.constants.AkkaConstant
import com.stratio.sparta.serving.core.models.RocketModes.RocketMode
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.utils.AkkaClusterUtils

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait WorkerActor extends Actor with SLF4JLogging with SpartaSerializer {

  implicit val actorSystem = context.system
  implicit val messageDispatcher = context.system.dispatchers.lookup("sparta-actors-dispatcher")
  implicit val ec: ExecutionContext = context.dispatcher

  val dispatcherActorName: String
  val rocketMode: RocketMode
  val workerTopic: String

  lazy val dispatcherActor = AkkaClusterUtils.proxyInstanceForName(dispatcherActorName,  AkkaConstant.MasterRole)

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  lazy val WorkerTickInitialDelay = 0 seconds
  lazy val WorkerNodeTickInterval = 500 milli

  def workerPreStart(): Unit

  def executeJob(job: String, jobSender: ActorRef): Future[Unit]

  override def preStart(): Unit = {
    workerPreStart()
    context.system.scheduler.schedule(WorkerTickInitialDelay, WorkerNodeTickInterval, self, StatusTick)
    log.info("Worker actor initiated")
  }

  override def receive: Receive = freeBehaviour

  def freeBehaviour: Receive = {
    case StatusTick =>
      log.trace(s"Publishing free message from ${self.path}")
      mediator ! Publish(workerTopic, Free)

    case WorkerStartJob(job, jobSender) =>
      mediator ! Publish(workerTopic, Busy)
      context.become(busyBehaviour)
      executeJob(job, jobSender).map(_ => JobFinished) pipeTo self

  }

  def busyBehaviour: Receive = {
    case StatusTick =>
      log.trace(s"Publishing busy message from ${self.path}")
      mediator ! Publish(workerTopic, Busy)
    case JobFinished =>
      mediator ! Publish(workerTopic, Free)
      context.become(freeBehaviour)
  }

}

object WorkerActor {

  sealed trait WorkerMessages

  case class WorkerStartJob(job: String, jobSender: ActorRef) extends WorkerMessages

  sealed trait WorkerStatusMessages

  case object Busy extends WorkerStatusMessages

  case object Free extends WorkerStatusMessages

  sealed trait WorkerInternalMessages

  case object StatusTick

  case object JobFinished

}