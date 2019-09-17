/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.bootstrap

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.SparkContextFactory
import com.stratio.sparta.serving.core.helpers.SecurityManagerHelper
import com.stratio.sparta.serving.core.utils.SpartaIgnite
import org.apache.ignite.Ignition

import scala.util.Try

case class RocketBootstrap(title: String) extends Bootstrap
  with SLF4JLogging {

  def start: Unit = {
    log.info(s"# Bootstraping $title #")
    initConfig()
    initIgnite()
    initVault()
    initSpartaSecurityManager()
    initCrossdataSecurityManager()
    initShutdownHooks()
  }

  protected[bootstrap] def initConfig(): Unit = {
    log.info("Bootstrap: Sparta configuration")
    assert(SpartaConfig.getSpartaConfig().isDefined && SpartaConfig.getDetailConfig().isDefined &&
      SpartaConfig.getSparkConfig().isDefined && SpartaConfig.getPostgresConfig().isDefined &&
      SpartaConfig.getCrossdataConfig().isDefined && SpartaConfig.getOauth2Config().isDefined &&
      SpartaConfig.getZookeeperConfig().isDefined && SpartaConfig.getApiConfig().isDefined &&
      SpartaConfig.getSprayConfig().isDefined, "Sparta configuration is not defined")
  }

  protected[bootstrap] def initIgnite(): Unit =
    if (igniteEnabled) {
      log.info("Bootstrap: Ignite")
      SpartaIgnite.getAndOrCreateInstance()
    }

  protected[bootstrap] def initVault(): Unit = {
    //This strange call solves the bug described in [SPARTA-2648]
    log.info("Bootstrap: Vault")
    SparkContextFactory.triggerRenovationVaultToken()
  }

  protected[bootstrap] def initCrossdataSecurityManager(): Unit = {
    log.info("Bootstrap: Crossdata security manager")
    SecurityManagerHelper.initCrossdataSecurityManager()
  }

  protected[bootstrap] def initSpartaSecurityManager(): Unit = {
    log.info("Bootstrap: Sparta security manager")
    SecurityManagerHelper.initSpartaSecurityManager()
  }

  protected[bootstrap] def initShutdownHooks(): Unit =
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        if (igniteEnabled) {
          SpartaIgnite.stopOrphanedNodes()
          SpartaIgnite.closeIgniteConnection()
          Ignition.stop(true)
        }
      }
    })

  private def igniteEnabled: Boolean = Try(SpartaConfig.getIgniteConfig().get.getBoolean(AppConstant.IgniteEnabled)).getOrElse(false)
}
