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
import akka.stream.ActorMaterializer
import com.stratio.sparta.serving.api.actor.remote.WorkerActor._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.{AkkaConstant, MarathonConstant}
import com.stratio.sparta.serving.core.marathon.service.{MarathonService, MarathonUpAndDownComponent}
import com.stratio.sparta.serving.core.models.RocketModes.{Remote, RocketMode}
import com.stratio.sparta.serving.core.models.{RocketModes, SpartaSerializer}
import com.stratio.sparta.serving.core.utils.AkkaClusterUtils
import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait WorkerActor extends Actor with SLF4JLogging with SpartaSerializer {

  implicit val actorSystem = context.system
  implicit val messageDispatcher = context.system.dispatchers.lookup("sparta-actors-dispatcher")
  implicit val ec: ExecutionContext = context.dispatcher

  val dispatcherActorName: String
  val rocketMode: RocketMode
  val workerTopic: String

  lazy val dispatcherActor = AkkaClusterUtils.proxyInstanceForName(dispatcherActorName,  AkkaConstant.MasterRole)

  lazy val spartaServerMarathonAppId = sys.env.getOrElse(MarathonConstant.SpartaServerMarathonAppId,
    throw new RuntimeException("The container should provide a MARATHON_APP_ID environment variable"))

  lazy val marathonDeploymentGroup = spartaServerMarathonAppId.split("/")
    .dropRight(1)
    .mkString("/") + "/" + rocketMode.toString.toLowerCase

  lazy val marathonDeploymentTaskId = s"$marathonDeploymentGroup/${rocketMode.toString.toLowerCase}-worker"

  lazy val marathonConfig: Config = SpartaConfig.getMarathonConfig().get
  lazy val marathonUpAndDownComponent = MarathonUpAndDownComponent(marathonConfig)(context.system, ActorMaterializer())
  lazy val marathonService = new MarathonService(marathonUpAndDownComponent)

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  val WorkerTickInitialDelay = 0 seconds
  val WorkerNodeTickInterval = 500 milli

  val WorkerAutokillTickInitialDelay = 0 seconds
  val WorkerAutokillNodeTickInterval = 20 seconds

  def workerPreStart(): Unit

  def executeJob(job: String, jobSender: ActorRef): Future[Unit]

  override def preStart(): Unit = {
    workerPreStart()
    context.system.scheduler.schedule(WorkerTickInitialDelay, WorkerNodeTickInterval, self, StatusTick)

    if(RocketModes.retrieveRocketMode != RocketModes.Local)
      context.system.scheduler.schedule(WorkerAutokillTickInitialDelay, WorkerAutokillNodeTickInterval, self, AutokillTick)

    log.info("Worker actor initiated")
  }

  override def receive: Receive = freeBehaviour

  def freeBehaviour: Receive = {
    case StatusTick =>
      log.trace(s"Publishing free message from ${self.path}")
      mediator ! Publish(workerTopic, Free)

    case AutokillTick =>
      autoKillIfNotExists

    case WorkerStartJob(job, jobSender) =>
      mediator ! Publish(workerTopic, Busy)
      context.become(busyBehaviour)
      executeJob(job, jobSender).map(_ => JobFinished) pipeTo self

  }

  def busyBehaviour: Receive = {
    case StatusTick =>
      log.trace(s"Publishing busy message from ${self.path}")
      mediator ! Publish(workerTopic, Busy)

    case AutokillTick =>
      autoKillIfNotExists

    case JobFinished =>
      mediator ! Publish(workerTopic, Free)
      context.become(freeBehaviour)
  }

  def autoKillIfNotExists: Unit = {
    val futureApplicationDeployments: Future[String] = for {
      applicationDeployments <- marathonUpAndDownComponent.marathonAPIUtils.retrieveApps(spartaServerMarathonAppId)
    } yield { applicationDeployments }

    futureApplicationDeployments.onComplete {
      case Success(value) =>
        val isRunning: Boolean = marathonUpAndDownComponent.marathonAPIUtils.isRunning(value)
        if(isRunning) {
          log.trace(s"There are a running deployment for id: $spartaServerMarathonAppId. Nothing to do")
        } else {
          log.info(s"There are not any deployment for id : $spartaServerMarathonAppId. Autokilling this instance!")
          marathonService.kill(marathonDeploymentTaskId)
        }
      case Failure(ex) =>
        log.error(ex.getLocalizedMessage, ex)
    }
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

  case object AutokillTick

  case object JobFinished

}