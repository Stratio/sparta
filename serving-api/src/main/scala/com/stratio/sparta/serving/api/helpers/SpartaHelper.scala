/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
import com.stratio.sparta.serving.core.constants.AppConstant._
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

      log.debug("Initializing Dyplon authorization plugins ...")
      implicit val secManager = SecurityManagerHelper.securityManager
      SecurityManagerHelper.initCrossdataSecurityManager()

      log.debug("Initializing Sparta system ...")
      implicit val system = ActorSystem(appName, SpartaConfig.mainConfig)

      val envListenerActor = system.actorOf(Props[EnvironmentListenerActor])

      Thread.sleep(Try(SpartaConfig.getDetailConfig.get.getLong("awaitEnvInit")).getOrElse(DefaultEnvSleep) / 5)

      system.actorOf(Props(new EnvironmentPublisherActor(curatorFramework)))

      Thread.sleep(Try(SpartaConfig.getDetailConfig.get.getLong("awaitEnvInit")).getOrElse(DefaultEnvSleep))

      val groupApiActor = system.actorOf(Props[GroupInMemoryApi])
      val executionApiActor = system.actorOf(Props[ExecutionInMemoryApi])
      val workflowApiActor = system.actorOf(Props[WorkflowInMemoryApi])
      val statusApiActor = system.actorOf(Props[StatusInMemoryApi])
      val debugWorkflowApiActor = system.actorOf(Props[DebugWorkflowInMemoryApi])
      val stListenerActor = system.actorOf(Props[StatusListenerActor])
      val workflowListenerActor = system.actorOf(Props[WorkflowListenerActor])
      val inMemoryApiActors = InMemoryApiActors(workflowApiActor, statusApiActor, groupApiActor, executionApiActor, debugWorkflowApiActor)

      system.actorOf(Props[SchedulerMonitorActor])

      if (Try(SpartaConfig.getDetailConfig.get.getBoolean("lineage.enable")).getOrElse(false)) {
        log.debug("Initializing lineage service ...")
        system.actorOf(LineageService.props(stListenerActor, workflowListenerActor))
      }

      log.debug("Initializing Sparta data ...")
      new EnvironmentService(curatorFramework).initialize()
      new GroupService(curatorFramework).initialize()

      Thread.sleep(Try(SpartaConfig.getDetailConfig.get.getLong("awaitRecovery")).getOrElse(DefaultRecoverySleep))

      system.actorOf(Props(new ExecutionPublisherActor(curatorFramework)))
      system.actorOf(Props(
        new WorkflowPublisherActor(curatorFramework, Option(system), Option(envListenerActor))))
      system.actorOf(Props(new WorkflowPublisherActor(curatorFramework)))
      system.actorOf(Props(new GroupPublisherActor(curatorFramework)))
      system.actorOf(Props(new StatusPublisherActor(curatorFramework)))
      system.actorOf(Props(new DebugWorkflowPublisherActor(curatorFramework)))
      system.actorOf(Props(new DebugStepDataPublisherActor(curatorFramework)))
      system.actorOf(Props(new DebugStepErrorPublisherActor(curatorFramework)))

      val controllerActor = system.actorOf(Props(new ControllerActor(
          curatorFramework,
          stListenerActor,
          envListenerActor,
          inMemoryApiActors
        )), ControllerActorName)

      log.info("Binding Sparta API ...")
      IO(Http) ! Http.Bind(controllerActor,
        interface = SpartaConfig.apiConfig.get.getString("host"),
        port = SpartaConfig.apiConfig.get.getInt("port")
      )

      if(Properties.envOrNone(NginxMarathonLBHostEnv).fold(false) { _ => true })
        Option(system.actorOf(Props(new NginxActor()), NginxActorName))

      log.info("Sparta server initiated successfully")
    } else log.info("Sparta configuration is not defined")
  }
}
