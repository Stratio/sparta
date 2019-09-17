/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.bootstrap

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.factory.PostgresFactory
import com.stratio.sparta.serving.core.services.migration.hydra_pegaso.HydraPegasoMigrationService
import com.stratio.sparta.serving.core.services.migration.orion.OrionMigrationService
import com.stratio.sparta.serving.core.services.migration.r9.R9MigrationService

import scala.util.Try

case class ServerBootstrap(title: String) extends Bootstrap
  with SLF4JLogging {

  def start: Unit = {
    log.info(s"# Bootstraping $title #")
    initConfig()
    initPostgres()
  }

  protected[bootstrap] def initConfig(): Unit = {
    log.info("Bootstrap: Rocket configuration")
    assert(
      SpartaConfig.getSpartaConfig().isDefined && SpartaConfig.getDetailConfig().isDefined &&
        SpartaConfig.getPostgresConfig().isDefined, "Rocket configuration is not defined")

  }

  protected[bootstrap] def initPostgres(): Unit = {
    log.info("Bootstrap: Postgres")
    val InitDatabaseTimeout = 500

    executeDbSchemas()

    if (Try(SpartaConfig.getDetailConfig().get.getBoolean("migration.enable")).getOrElse(true)) {
      log.info("Initializing Postgres schema migration")
      val orionMigrationService = new OrionMigrationService()
      orionMigrationService.loadOrionPgData()
      val hydraPegasoMigrationService = new HydraPegasoMigrationService(orionMigrationService)
      hydraPegasoMigrationService.loadHydraPegasoPgData()
      hydraPegasoMigrationService.executePostgresMigration()
      val r9MigrationService = new R9MigrationService(hydraPegasoMigrationService)
      r9MigrationService.executePostgresMigration()

      executeDbData()

      Thread.sleep(InitDatabaseTimeout)
      hydraPegasoMigrationService.executeMigration()
      r9MigrationService.executeMigration()
    } else {
      executeDbData()
    }
  }

  protected[bootstrap] def executeDbData(): Unit = {
    log.info("Initializing data in Postgres tables")
    PostgresFactory.invokeInitializationDataMethods()
  }

  protected[bootstrap] def executeDbSchemas(): Unit = {
    log.info("Initializing Sparta Postgres schemas ...")
    PostgresFactory.invokeInitializationMethods()
  }
}