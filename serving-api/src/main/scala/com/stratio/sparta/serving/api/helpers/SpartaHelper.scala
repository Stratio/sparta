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

import akka.actor.{ActorSystem, Props}
import akka.event.slf4j.SLF4JLogging
import akka.io.IO
import spray.can.Http
import com.stratio.sparta.dg.agent.lineage.LineageService
import com.stratio.sparta.serving.api.actor._
import com.stratio.sparta.serving.api.service.ssl.SSLSupport
import com.stratio.sparta.serving.core.actor._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.constants.MarathonConstant.NginxMarathonLBHostEnv
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper
import com.stratio.sparta.serving.core.services.{EnvironmentService, GroupService}
import scala.util.{Properties, Try}

/**
  * Helper with common operations used to create a Sparta context used to run the application.
  */
object SpartaHelper extends SLF4JLogging with SSLSupport {

  //scalastyle:off
  /**
    * Initializes Sparta's akka system running an embedded http server with the REST API.
    *
    * @param appName with the name of the application.
    */
  def initSpartaAPI(appName: String): Unit = {
    if (SpartaConfig.mainConfig.isDefined && SpartaConfig.apiConfig.isDefined) {
      val curatorFramework = CuratorFactoryHolder.getInstance()
      implicit val secManager = SecurityManagerHelper.securityManager

      log.debug("Initializing Sparta system ...")
      implicit val system = ActorSystem(appName, SpartaConfig.mainConfig)

      log.debug("Initializing Sparta data ...")
      new EnvironmentService(curatorFramework).initialize()
      new GroupService(curatorFramework).initialize()

      val envListenerActor = system.actorOf(Props[EnvironmentListenerActor])
      system.actorOf(Props(new EnvironmentPublisherActor(curatorFramework)))

      val groupApiActor = system.actorOf(Props[GroupInMemoryApi])
      val executionApiActor = system.actorOf(Props[ExecutionInMemoryApi])
      val workflowApiActor = system.actorOf(Props[WorkflowInMemoryApi])
      val statusApiActor = system.actorOf(Props[StatusInMemoryApi])
      val stListenerActor = system.actorOf(Props[StatusListenerActor])
      val workflowListenerActor = system.actorOf(Props[WorkflowListenerActor])

      if (Try(SpartaConfig.getDetailConfig.get.getBoolean("lineage.enable")).getOrElse(false)) {
        log.debug("Initializing lineage service ...")
        system.actorOf(LineageService.props(stListenerActor, workflowListenerActor))
      }
      system.actorOf(Props(
        new WorkflowPublisherActor(curatorFramework, Option(system), Option(envListenerActor))))
      system.actorOf(Props(new GroupPublisherActor(curatorFramework)))
      system.actorOf(Props(new WorkflowPublisherActor(curatorFramework)))
      system.actorOf(Props(new ExecutionPublisherActor(curatorFramework)))
      system.actorOf(Props(new StatusPublisherActor(curatorFramework)))

      val inMemoryApiActors = InMemoryApiActors(workflowApiActor, statusApiActor, groupApiActor, executionApiActor)
      val controllerActor = system.actorOf(Props(new ControllerActor(
          curatorFramework,
          stListenerActor,
          envListenerActor,
          inMemoryApiActors
        )), ControllerActorName)

      log.debug("Binding Sparta API ...")
      IO(Http) ! Http.Bind(controllerActor,
        interface = SpartaConfig.apiConfig.get.getString("host"),
        port = SpartaConfig.apiConfig.get.getInt("port")
      )

      if(Properties.envOrNone(NginxMarathonLBHostEnv).fold(false) { _ => true })
        Option(system.actorOf(Props(new NginxActor()), NginxActorName))

      log.info("Sparta System initiated correctly")
    } else log.info("Sparta Configuration is not defined")
  }
}
