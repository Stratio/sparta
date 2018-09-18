/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.api.helpers

import scala.util.{Properties, Try}
import akka.actor.{ActorSystem, Props}
import akka.event.slf4j.SLF4JLogging
import akka.io.IO
import com.typesafe.config.ConfigFactory
import slick.jdbc.PostgresProfile
import spray.can.Http
import com.stratio.sparta.dg.agent.lineage.LineageService
import com.stratio.sparta.serving.api.actor._
import com.stratio.sparta.serving.api.service.ssl.SSLSupport
import com.stratio.sparta.serving.core.actor._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AkkaConstant._
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.AppConstant._
import com.stratio.sparta.serving.core.constants.MarathonConstant.NginxMarathonLBHostEnv
import com.stratio.sparta.serving.core.factory.{CuratorFactoryHolder, PostgresFactory}
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper
import com.stratio.sparta.serving.core.services.migration.CassiopeiaMigrationService
import spray.can.Http

import scala.concurrent.blocking
import scala.util.{Failure, Properties, Success, Try}


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
    if (
      SpartaConfig.getSpartaConfig().isDefined && SpartaConfig.getDetailConfig().isDefined &&
        SpartaConfig.getSparkConfig().isDefined && SpartaConfig.getPostgresConfig().isDefined &&
        SpartaConfig.getCrossdataConfig().isDefined && SpartaConfig.getOauth2Config().isDefined &&
        SpartaConfig.getZookeeperConfig().isDefined && SpartaConfig.getApiConfig().isDefined &&
        SpartaConfig.getSprayConfig().isDefined) {
      log.info("Initializing Sparta Postgres schemas and data ...")
      PostgresFactory.invokeInitializationMethods()

      if (Try(SpartaConfig.getDetailConfig().get.getBoolean("migration.enable")).getOrElse(true)) {
        val migration = new CassiopeiaMigrationService()
        migration.migrateCassiopeiaWorkflows()
        migration.migrateCassiopeiaTemplates()
      }

      log.info("Initializing Dyplon authorization plugins ...")
      implicit val secManager = SecurityManagerHelper.securityManager
      SecurityManagerHelper.initCrossdataSecurityManager()

      log.debug("Initializing Sparta system ...")
      implicit val system = ActorSystem(appName, SpartaConfig.getSpartaConfig())

      val parametersListenerActor = system.actorOf(Props[ParametersListenerActor])
      val executionStatusChangeListenerActor = system.actorOf(Props[ExecutionStatusChangeListenerActor])

      system.actorOf(Props[SchedulerMonitorActor])
      system.actorOf(Props(new ExecutionStatusChangePublisherActor()))

      if (Try(SpartaConfig.getDetailConfig().get.getBoolean("lineage.enable")).getOrElse(false)) {
        log.info("Initializing lineage service ...")
        //TODO lineage
        //system.actorOf(LineageService.props(executionListenerActor, workflowListenerActor))
      }

      val controllerActor = system.actorOf(Props(new ControllerActor(
        executionStatusChangeListenerActor,
        parametersListenerActor
      )), ControllerActorName)

      log.info("Binding Sparta API ...")
      IO(Http) ! Http.Bind(controllerActor,
        interface = SpartaConfig.getApiConfig().get.getString("host"),
        port = SpartaConfig.getApiConfig().get.getInt("port")
      )

      if (Properties.envOrNone(NginxMarathonLBHostEnv).fold(false) { _ => true })
        Option(system.actorOf(Props(new NginxActor()), NginxActorName))

      log.info("Sparta server initiated successfully")
    } else log.info("Sparta configuration is not defined")
  }
}
