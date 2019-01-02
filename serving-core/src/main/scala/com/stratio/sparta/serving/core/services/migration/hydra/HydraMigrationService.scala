/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.migration.hydra

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.constants.DatabaseTableConstant.TemplateTableName
import com.stratio.sparta.serving.core.factory.{CuratorFactoryHolder, PostgresDaoFactory}
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.services.dao.BasicPostgresService
import com.stratio.sparta.serving.core.services.migration.orion.OrionMigrationService
import com.stratio.sparta.serving.core.services.migration.zookeeper.{ZkTemplateService, ZkWorkflowService}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class HydraMigrationService extends SLF4JLogging with SpartaSerializer {

  private val workflowZkService = new ZkWorkflowService(CuratorFactoryHolder.getInstance())
  private val templateZkService = new ZkTemplateService(CuratorFactoryHolder.getInstance())

  private val workflowPostgresService = PostgresDaoFactory.workflowPgService
  private val templatePostgresService = PostgresDaoFactory.templatePgService
  private val basicPostgresService = new BasicPostgresService()

  private val orionMigrationService = new OrionMigrationService()

  def executeMigration(): Unit = {
    hydraTemplatesMigration()
    hydraWorkflowsMigration()
  }

  def executePostgresMigration(): Unit =
    Try {
      basicPostgresService.dbSchemaName.foreach{schema =>
        val templatesSql = s"ALTER TABLE IF EXISTS ${basicPostgresService.profile.quoteIdentifier(s"$schema.$TemplateTableName")} ADD COLUMN IF NOT EXISTS supported_data_relations character varying;"
        basicPostgresService.executeSql(templatesSql)
      }
    } match {
      case Success(_) =>
        log.info("Hydra Postgres sentences executed successfully")
      case Failure(e) =>
        log.error(s"Error executing Hydra Postgres sentences. ${ExceptionHelper.toPrintableException(e)}", e)
    }

  private def hydraWorkflowsMigration(): Unit = {

    val (hydraWorkflows, orionWorkflows, andromedaWorkflows, cassiopeaWorkflows) = orionMigrationService.orionWorkflowsMigrated()
      .getOrElse((Seq.empty, Seq.empty, Seq.empty, Seq.empty))

    Try {
      Await.result(workflowPostgresService.upsertList(hydraWorkflows), 20 seconds)
    } match {
      case Success(_) =>
        log.info("Workflows migrated to Hydra")
        Try {
          andromedaWorkflows.foreach(workflow => workflowZkService.createAndromeda(workflow, AppConstant.WorkflowsOldZkPath))
          andromedaWorkflows.foreach(workflow => workflowZkService.createAndromeda(workflow, AppConstant.WorkflowsOldZkAndromedaPath))
          cassiopeaWorkflows.foreach(workflow => workflowZkService.createCassiopea(workflow, AppConstant.WorkflowsOldCassiopeiaZkPath))
          workflowZkService.deletePath()
        } match {
          case Success(_) =>
            log.info("Andromeda and Cassiopeia workflows moved to backup folder in Zookeeper")
          case Failure(e) =>
            log.error(s"Error moving workflows to backup folder. ${ExceptionHelper.toPrintableException(e)}", e)
        }
      case Failure(e) =>
        log.error(s"Error migrating to Hydra workflows. ${ExceptionHelper.toPrintableException(e)}", e)
    }
  }

  private def hydraTemplatesMigration(): Unit = {

    val (hydraTemplates, andromedaTemplates, cassiopeiaTemplates) = orionMigrationService.orionTemplatesMigrated()
      .getOrElse((Seq.empty, Seq.empty, Seq.empty))

    Try {
      Await.result(templatePostgresService.upsertList(hydraTemplates), 20 seconds)
    } match {
      case Success(_) =>
        log.info("Templates migrated to Hydra")
        Try {
          andromedaTemplates.foreach(template => templateZkService.create(template, AppConstant.TemplatesOldZkPath))
          andromedaTemplates.foreach(template => templateZkService.create(template, AppConstant.TemplatesOldAndromedaZkPath))
          cassiopeiaTemplates.foreach(template => templateZkService.create(template, AppConstant.TemplatesOldCassiopeiaZkPath))
          templateZkService.deletePath()
        } match {
          case Success(_) =>
            log.info("Andromeda and Cassiopeia templates moved to backup folder in Zookeeper")
          case Failure(e) =>
            log.error(s"Error moving templates to backup folder. ${ExceptionHelper.toPrintableException(e)}", e)
        }
      case Failure(e) =>
        log.error(s"Error migrating to Hydra templates. ${ExceptionHelper.toPrintableException(e)}", e)
    }
  }
}
