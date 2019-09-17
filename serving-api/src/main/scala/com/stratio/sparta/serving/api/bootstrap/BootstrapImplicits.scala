/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.bootstrap

import java.util.Calendar

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.stratio.sparta.security.SpartaSecurityManager
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper
import com.typesafe.config.ConfigFactory
import com.stratio.sparta.serving.core.models.RocketModes._
import io.netty.bootstrap.ServerBootstrap

object BootstrapImplicits {

  implicit val system: ActorSystem = ActorSystem(AppConstant.ConfigAppName, SpartaConfig.getSpartaConfig()
    .get.withFallback(ConfigFactory.load().getConfig("clusterSparta")))
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  //scalastyle:off
  def logo: Unit = println(


    raw"""
         |       ( )
         |      ( )(        _____ _             _   _          _____                  _
         |   ../ \..)      / ____| |           | | (_)        / ____|                | |
         |  /__ - __\)    | (___ | |_ _ __ __ _| |_ _  ___   | (___  _ __   __ _ _ __| |_ __ _
         |  |\_\_/_/|(     \___ \| __| '__/ _` | __| |/ _ \   \___ \| '_ \ / _` | '__| __/ _` |
         |  \ /|||\ / )    ____) | |_| | | (_| | |_| | (_) |  ____) | |_) | (_| | |  | || (_| |
         |   \\|||//( (   |_____/ \__|_|  \__,_|\__|_|\___/  |_____/| .__/ \__,_|_|   \__\__,_|
         |    -\_/-  v                                              | |
         |                Node on ${retrieveRocketMode}
         |
         |                > Start time     : ${Calendar.getInstance().getTime}
         |                > Total memory   : ${Runtime.getRuntime.totalMemory()}
         |                > Free memory    : ${Runtime.getRuntime.freeMemory}
         |                > Number of CPUs : ${Runtime.getRuntime.availableProcessors}
         |
      """.stripMargin)
  //scalastyle:on

  def boot: Seq[Bootstrap] = retrieveRocketMode match {
    case rocketMode@Local =>
      Seq(
        RocketBootstrap("Sparta"),
        ServerBootstrap("Server"),
        ClusterAPIBootstrap("Cluster Server API", rocketMode),
        LocalValidatorBootstrap("ValidatorLocal"),
        DebugLocalBootstrap("DebugLocal"),
        CatalogLocalBootstrap("CatalogLocal")
      )

    case rocketMode@Remote =>
      Seq(
        RocketBootstrap("Sparta"),
        ServerBootstrap("Server"),
        ClusterAPIBootstrap("Cluster Server API", rocketMode))

    case Debug =>
      Seq(
        RocketBootstrap("Sparta"),
        DebugRemoteBootstrap(Debug.toString)
      )

    case Validator =>
      Seq(
        RocketBootstrap("Sparta"),
        ValidatorRemoteBootstrap(Validator.toString)
      )

    case Catalog =>
      Seq(
        RocketBootstrap("Sparta"),
        CatalogRemoteBootstrap(Catalog.toString)
      )

    case Unknown =>
      throw new RuntimeException("Allowed modes for sparta.bootstrap.mode could be: local, remote, debug, validator or catalog")
  }
}
