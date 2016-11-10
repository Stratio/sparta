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

package com.stratio.sparta.serving.api.helpers

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.io.IO
import akka.routing.RoundRobinPool
import com.stratio.sparta.driver.factory.SparkContextFactory
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.api.actor._
import com.stratio.sparta.serving.api.utils.PolicyStatusUtils
import com.stratio.sparta.serving.core.actor.FragmentActor
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.{AkkaConstant, AppConstant}
import com.stratio.sparta.serving.core.curator.CuratorFactoryHolder
import com.stratio.sparta.serving.core.policy.status.{PolicyStatusActor, PolicyStatusEnum}
import com.stratio.sparta.serving.core.utils.PolicyUtils
import spray.can.Http

/**
 * Helper with common operations used to create a Sparta context used to run the application.
 */
object SpartaHelper extends PolicyStatusUtils with PolicyUtils {

  implicit var system: ActorSystem = _

  /**
   * Initializes Sparta's akka system running an embedded http server with the REST API.
   *
   * @param appName with the name of the application.
   */
  def initAkkaSystem(appName: String): Unit = {
    if (SpartaConfig.mainConfig.isDefined &&
      SpartaConfig.apiConfig.isDefined &&
      SpartaConfig.swaggerConfig.isDefined) {
      val curatorFramework = CuratorFactoryHolder.getInstance()
      log.info("> Initializing akka actors")
      system = ActorSystem(appName)
      val akkaConfig = SpartaConfig.mainConfig.get.getConfig(AppConstant.ConfigAkka)
      val controllerInstances = if (!akkaConfig.isEmpty) akkaConfig.getInt(AkkaConstant.ControllerActorInstances)
      else AkkaConstant.DefaultControllerActorInstances
      val policyStatusActor = system.actorOf(Props(new PolicyStatusActor(curatorFramework)),
        AkkaConstant.PolicyStatusActor)
      val policyActor = system.actorOf(Props(new PolicyActor(curatorFramework, policyStatusActor)),
        AkkaConstant.PolicyActor)
      val streamingContextService = new StreamingContextService(Some(policyStatusActor), SpartaConfig.mainConfig)
      implicit val actors = Map(
        AkkaConstant.PolicyStatusActor -> policyStatusActor,
        AkkaConstant.FragmentActor ->
          system.actorOf(Props(new FragmentActor(curatorFramework)), AkkaConstant.FragmentActor),
        AkkaConstant.TemplateActor ->
          system.actorOf(Props(new TemplateActor()), AkkaConstant.TemplateActor),
        AkkaConstant.PolicyActor -> policyActor,
        AkkaConstant.SparkStreamingContextActor -> system.actorOf(Props(
          new SparkStreamingContextActor(streamingContextService, policyActor, policyStatusActor, curatorFramework)),
          AkkaConstant.SparkStreamingContextActor
        )
      )
      val swaggerActor = system.actorOf(
        Props(new SwaggerActor(actors, curatorFramework)), AkkaConstant.SwaggerActor)
      val controllerActor = system.actorOf(RoundRobinPool(controllerInstances)
        .props(Props(new ControllerActor(actors, curatorFramework))), AkkaConstant.ControllerActor)

      if (SpartaConfig.isHttpsEnabled()) loadSpartaWithHttps(controllerActor, swaggerActor)
      else loadSpartaWithHttp(controllerActor, swaggerActor)

      if(isLocalMode) updateAll(policyStatusActor, PolicyStatusEnum.NotStarted)
    } else log.info("Config for Sparta is not defined")
  }

  def loadSpartaWithHttps(controllerActor: ActorRef, swaggerActor: ActorRef): Unit = {
    import com.stratio.sparkta.serving.api.ssl.SSLSupport._
    IO(Http) ! Http.Bind(controllerActor,
      interface = SpartaConfig.apiConfig.get.getString("host"),
      port = SpartaConfig.apiConfig.get.getInt("port")
    )
    IO(Http) ! Http.Bind(swaggerActor, interface = SpartaConfig.swaggerConfig.get.getString("host"),
      port = SpartaConfig.swaggerConfig.get.getInt("port"))

    log.info("> Actors System UP!")
  }

  def loadSpartaWithHttp(controllerActor: ActorRef, swaggerActor: ActorRef): Unit = {
    IO(Http) ! Http.Bind(controllerActor,
      interface = SpartaConfig.apiConfig.get.getString("host"),
      port = SpartaConfig.apiConfig.get.getInt("port")
    )
    IO(Http) ! Http.Bind(swaggerActor, interface = SpartaConfig.swaggerConfig.get.getString("host"),
      port = SpartaConfig.swaggerConfig.get.getInt("port"))

    log.info("> Actors System UP!")
  }

  def shutdown(destroySparkContext: Boolean = true): Unit = {
    synchronized {
      try {
        SparkContextFactory.destroySparkStreamingContext()
      } finally {
        if (destroySparkContext)
          SparkContextFactory.destroySparkContext()
        CuratorFactoryHolder.resetInstance()
        system.shutdown
      }
    }
  }

  def getExecutionMode: String = {
    val detailConfig = SpartaConfig.getDetailConfig.getOrElse(throw new RuntimeException("Error getting Spark config"))
    detailConfig.getString(AppConstant.ExecutionMode)
  }

  def isClusterMode: Boolean = {
    val executionMode = getExecutionMode
    executionMode == AppConstant.ConfigMesos || executionMode == AppConstant.ConfigYarn
  }
}
