/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.serving.api.helpers

import akka.actor.{ActorSystem, Props}
import akka.event.slf4j.SLF4JLogging
import akka.io.IO
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import com.stratio.sparkta.driver.factory.SparkContextFactory
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.serving.api.actor._
import com.stratio.sparkta.serving.core._
import com.stratio.sparkta.serving.core.actor._
import com.stratio.sparkta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparkta.serving.core.policy.status.PolicyStatusActor
import spray.can.Http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * Helper with common operations used to create a Sparkta context used to run the application.
 */
object SparktaHelper extends SLF4JLogging {

  implicit var system: ActorSystem = _
  implicit val timeout: Timeout = Timeout(15.seconds)

  /**
   * Initializes Sparkta's akka system running an embedded http server with the REST API.
   * @param appName with the name of the application.
   */

  def initAkkaSystem(appName: String): Unit = {
    if (SparktaConfig.mainConfig.isDefined &&
      SparktaConfig.apiConfig.isDefined &&
      SparktaConfig.swaggerConfig.isDefined) {
      val curatorFramework = CuratorFactoryHolder.getInstance()
      log.info("> Initializing akka actors")
      system = ActorSystem(appName)
      val akkaConfig = SparktaConfig.mainConfig.get.getConfig(AppConstant.ConfigAkka)
      val controllerInstances = if (!akkaConfig.isEmpty) akkaConfig.getInt(AkkaConstant.ControllerActorInstances)
      else AkkaConstant.DefaultControllerActorInstances
      val streamingActorInstances = if (!akkaConfig.isEmpty) akkaConfig.getInt(AkkaConstant.ControllerActorInstances)
      else AkkaConstant.DefaultStreamingActorInstances
      val policyStatusActor = system.actorOf(Props(new PolicyStatusActor(curatorFramework)),
        AkkaConstant.PolicyStatusActor)
      val streamingContextService = new StreamingContextService(Some(policyStatusActor), SparktaConfig.mainConfig)
      implicit val actors = Map(
        AkkaConstant.PolicyStatusActor -> policyStatusActor,
        AkkaConstant.FragmentActor ->
          system.actorOf(Props(new FragmentActor(curatorFramework)), AkkaConstant.FragmentActor),
        AkkaConstant.TemplateActor ->
          system.actorOf(Props(new TemplateActor()), AkkaConstant.TemplateActor),
        AkkaConstant.PolicyActor ->
          system.actorOf(Props(new PolicyActor(curatorFramework, policyStatusActor)), AkkaConstant.PolicyActor),
        AkkaConstant.SparkStreamingContextActor -> system.actorOf(RoundRobinPool(streamingActorInstances).props(Props(
          new SparkStreamingContextActor(
            streamingContextService, policyStatusActor))),
          AkkaConstant.SparkStreamingContextActor)
      )
      val swaggerActor = system.actorOf(
        Props(new SwaggerActor(actors)), AkkaConstant.SwaggerActor)
      val controllerActor = system.actorOf(RoundRobinPool(controllerInstances)
        .props(Props(new ControllerActor(streamingContextService, actors))), AkkaConstant.ControllerActor)

      val sparkta = Http.Bind(controllerActor, interface = SparktaConfig.apiConfig.get.getString("host"),
        port = SparktaConfig.apiConfig.get.getInt("port"))

      val swagger = Http.Bind(swaggerActor, interface = SparktaConfig.swaggerConfig.get.getString("host"),
        port = SparktaConfig.swaggerConfig.get.getInt("port"))

      (for {
        _ <- bind(sparkta)(timeout)
        swaggerResult <- bind(swagger)(timeout)
      } yield swaggerResult).onComplete {
        case Success(_) => log.info("> Actors System UP!")
        case Failure(e) => {
          log.error("Sparkta failed because another instance is running !!!!!")
          System.exit(1)
        }
      }
    } else log.info("Config for Sparkta is not defined")
  }

  /**
   * Destroys Spark's context.
   */
  def shutdown: Unit = {
    synchronized {
      SparkContextFactory.destroySparkContext
      CuratorFactoryHolder.resetInstance()
      system.shutdown
    }
  }

  def bind(bind: Http.Bind)(implicit timeout: Timeout): Future[Http.Bound] =
    (IO(Http) ? bind) flatMap {
      case b: Http.Bound => Future.successful(b)
      case failed: Http.CommandFailed => Future.failed(new
          RuntimeException("Binding failed"))
    }
}
