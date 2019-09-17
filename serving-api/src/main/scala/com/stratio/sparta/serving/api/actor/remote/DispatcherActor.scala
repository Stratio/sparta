/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor.remote

import akka.actor.{Actor, ActorRef, Terminated}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.api.actor.remote.DispatcherActor._
import com.stratio.sparta.serving.api.actor.remote.WorkerActor._
import com.stratio.sparta.serving.core.models.SpartaSerializer

import scala.concurrent.duration._

trait DispatcherActor extends Actor with SLF4JLogging with SpartaSerializer {

  val workerTopic: String

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  val DispatcherNodeTickInitialDelay = 0 seconds
  val DispatcherNodeTickInterval = 5 seconds

  override def preStart(): Unit = {
    mediator ! Subscribe(workerTopic, self)

    import scala.concurrent.ExecutionContext.Implicits.global
    context.system.scheduler.schedule(DispatcherNodeTickInitialDelay, DispatcherNodeTickInterval, self, DequeueJob)
  }

  override def receive: Receive = workerBehaviour(
    workers = Map.empty[ActorRef, WorkerStatusMessages],
    jobs = Seq.empty[(String, ActorRef)]
  )

  // scalastyle:off
  def workerBehaviour(workers: Map[ActorRef, WorkerStatusMessages], jobs: Seq[(String, ActorRef)]): Receive = {
    case EnqueueJob(job) =>
      context.become(workerBehaviour(
        workers = workers,
        jobs = jobs :+ (job, sender())))
      self ! DequeueJob

    case DequeueJob =>
      jobs match {
        case (job, jobSender) :: xs =>
          workers.find { case (_, status) => status == Free } match {
            case Some((workerRef, _)) =>
              workerRef ! WorkerStartJob(job, jobSender)
              log.debug(s"Sending job to ${workerRef.path} with sender ${jobSender.path}")
              context.become(workerBehaviour(
                workers = workers + (workerRef -> Busy),
                jobs = xs))
            case None =>
              log.warn(s"There are no nodes available for $workerTopic purposes")
          }
        case Nil =>
          log.trace(s"No $workerTopic jobs to run, waiting until next tick")
      }

    case workerStatus: WorkerStatusMessages =>
      val workerRef: ActorRef = sender()
      log.trace(s"${workerRef.path} is [$workerStatus]")

      if(!workers.contains(workerRef)) {
        context.watch(workerRef)
      }

      context.become(workerBehaviour(
        workers = workers + (workerRef -> workerStatus),
        jobs = jobs)
      )

    case Terminated(workerRef) =>
      context.unwatch(workerRef)
      log.warn(s"${workerRef.path} is [DOWN]")
      context.become(workerBehaviour(
        workers = workers - workerRef,
        jobs = jobs)
      )
  }
}

object DispatcherActor {

  sealed trait DispatcherMessages

  case class EnqueueJob(job: String) extends DispatcherMessages

  sealed trait DispatcherInternalMessages

  case object DequeueJob extends DispatcherInternalMessages

}

