/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.actor.remote

import akka.actor.Actor
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.event.slf4j.SLF4JLogging
import akka.stream.ActorMaterializer
import com.stratio.sparta.serving.api.actor.remote.GuardianActor._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.MarathonConstant
import com.stratio.sparta.serving.core.marathon.MarathonApplication
import com.stratio.sparta.serving.core.marathon.service.{MarathonService, MarathonUpAndDownComponent}
import com.stratio.sparta.serving.core.models.RocketModes.RocketMode
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.typesafe.config.Config

import scala.util.{Failure, Success}

trait GuardianActor extends Actor with SLF4JLogging with SpartaSerializer {

  import scala.concurrent.ExecutionContext.Implicits.global

  val instanceConfig: Config
  val marathonApplication: MarathonApplication
  val rocketMode: RocketMode

  val cluster = Cluster(context.system)
  val mediator = DistributedPubSub(context.system).mediator

  lazy val cpu = instanceConfig.getInt("cpu")
  lazy val mem = instanceConfig.getInt("mem")
  lazy val instances = instanceConfig.getInt("instances")

  lazy val marathonDeploymentGroup = sys.env.get(MarathonConstant.SpartaServerMarathonAppId)
    .map(_.split("/").dropRight(1).mkString("/") + "/" + rocketMode.toString.toLowerCase)
    .getOrElse(throw new RuntimeException("The container should provide a MARATHON_APP_ID environment variable"))
  lazy val marathonDeploymentTaskId = s"$marathonDeploymentGroup/${rocketMode.toString.toLowerCase}-worker"

  lazy val marathonConfig: Config = SpartaConfig.getMarathonConfig().get
  lazy val marathonUpAndDownComponent = MarathonUpAndDownComponent(marathonConfig)(context.system, ActorMaterializer())
  lazy val marathonService = new MarathonService(marathonUpAndDownComponent)

  override def preStart(): Unit = context.system.scheduler.schedule(InitialTickDelay, PeriodicalTickDelay, self, TickCheck)

  override def receive: Receive = checkBehaviour

  def checkBehaviour: Receive = {
    case TickCheck =>
      val deployExists = for {
        deployments <- marathonUpAndDownComponent.marathonAPIUtils.getApplicationDeployments(marathonDeploymentTaskId)
        tasksRunning <- marathonUpAndDownComponent.marathonAPIUtils.getGroupTasks(marathonDeploymentGroup)
      } yield {
        log.debug(s"Deployments for task id [$marathonDeploymentTaskId]: ${deployments.values.mkString(", ")}")
        log.debug(s"Tasks for group id [$marathonDeploymentGroup]: ${tasksRunning.mkString(", ")}")
        deployments.contains(marathonDeploymentTaskId) || tasksRunning.contains(marathonDeploymentTaskId)
      }

      deployExists.onComplete {
        case Success(value) =>
          if (value)
            log.debug(s"Worker instances were deployed previously for task id [$marathonDeploymentTaskId]")
          else deployInstances()
        case Failure(ex) =>
          log.error(ex.getLocalizedMessage, ex)
      }
  }

  def deployInstances(): Unit = {
    log.info(s"Deploying worker ${marathonApplication.id} with instances ${marathonApplication.instances}")
    marathonService.launch(marathonApplication)
  }
}

object GuardianActor {

  import scala.concurrent.duration._

  val InitialTickDelay = 0 seconds
  val PeriodicalTickDelay = 60 seconds

  sealed trait GuardianInternalMessages

  case object TickCheck extends GuardianInternalMessages

}
