/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.migration.hydra_pegaso

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.serving.core.constants.DatabaseTableConstant._
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.Workflow
import com.stratio.sparta.serving.core.models.workflow.migration._
import com.stratio.sparta.serving.core.services.dao.BasicPostgresService
import com.stratio.sparta.serving.core.services.migration.orion.OrionMigrationService

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

//scalastyle:off
class HydraPegasoMigrationService(orionMigrationService: OrionMigrationService) extends SLF4JLogging with SpartaSerializer {

  import MigrationModelImplicits._

  private lazy val templatePostgresService = PostgresDaoFactory.templatePgService
  private lazy val basicPostgresService = new BasicPostgresService()

  private lazy val workflowHydraPegasoPostgresService = Try(new WorkflowHydraPegasoPostgresDao()) match {
    case Success(service) =>
      Option(service)
    case Failure(e) =>
      log.warn(s"Error creating workflow service for Hydra-Pegaso. ${ExceptionHelper.toPrintableException(e)}", e)
      None
  }

  val defaultAwaitForMigration: Duration = 20 seconds

  private var workflowsPgHydraPegaso = Seq.empty[WorkflowHydraPegaso]

  def loadHydraPegasoPgData(): Unit = {
    workflowsPgHydraPegaso = workflowHydraPegasoPostgresService.map { service =>
      Try {
        Await.result(service.findAllWorkflows(), defaultAwaitForMigration)
      } match {
        case Success(workflows) =>
          log.info(s"Workflows in Hydra-Pegaso: ${workflows.map(_.name).mkString(",")}")
          workflows
        case Failure(e) =>
          log.warn(s"Error reading workflows in Hydra-Pegaso. ${ExceptionHelper.toPrintableException(e)}", e)
          Seq.empty[WorkflowHydraPegaso]
      }
    }.getOrElse(Seq.empty[WorkflowHydraPegaso])
  }

  def executeMigration(): Unit = {
    hydraPegasoTemplatesMigration()
  }

  def executePostgresMigration(): Unit =
    Try {
      basicPostgresService.dbSchemaName.foreach { schema =>
        val schemaName = basicPostgresService.profile.quoteIdentifier(schema)
        val templatesSql = s"ALTER TABLE IF EXISTS $schemaName.$TemplateTableName ADD COLUMN IF NOT EXISTS supported_data_relations character varying;"
        basicPostgresService.executeSql(templatesSql)
        val createQualityRulesExecutionSql =
          s"ALTER TABLE IF EXISTS $schemaName.$WorkflowExecutionTableName ADD COLUMN IF NOT EXISTS quality_rules character varying;"
        basicPostgresService.executeSql(createQualityRulesExecutionSql)
        val defaultQualityRulesExecutionSql =
          s"UPDATE $schemaName.$WorkflowExecutionTableName SET quality_rules='[]' WHERE quality_rules IS NULL;"
        basicPostgresService.executeSql(defaultQualityRulesExecutionSql)
        val notNullQualityRulesExecutionSql =
          s"ALTER TABLE IF EXISTS $schemaName.$WorkflowExecutionTableName ALTER COLUMN quality_rules SET NOT NULL;"
        basicPostgresService.executeSql(notNullQualityRulesExecutionSql)
        val createFromSchedulerInExecutionSql =
          s"ALTER TABLE IF EXISTS $schemaName.$WorkflowExecutionTableName ADD COLUMN IF NOT EXISTS executed_from_scheduler character varying;"
        basicPostgresService.executeSql(createFromSchedulerInExecutionSql)
        val createFromExecutionInExecutionSql =
          s"ALTER TABLE IF EXISTS $schemaName.$WorkflowExecutionTableName ADD COLUMN IF NOT EXISTS executed_from_execution character varying;"
        basicPostgresService.executeSql(createFromExecutionInExecutionSql)
      }
    } match {
      case Success(_) =>
        log.info("Hydra-Pegaso Postgres sentences executed successfully")
      case Failure(e) =>
        log.error(s"Error executing Hydra-Pegaso Postgres sentences. ${ExceptionHelper.toPrintableException(e)}", e)
    }

  def hydraPegasoWorkflowsMigrated(): Try[(Seq[Workflow])] = {
    Try {
      val orionWorkflowsToHydraPegasoWorkflows = orionMigrationService.orionWorkflowsMigrated().getOrElse(Seq.empty)
      val newHydraPegasoWorkflows = workflowsPgHydraPegaso ++ orionWorkflowsToHydraPegasoWorkflows

      newHydraPegasoWorkflows.map(workflow => workflow: Workflow)
    }
  }

  private def hydraPegasoTemplatesMigration(): Unit = {
    val hydraPegasoTemplates = orionMigrationService.orionTemplatesMigrated()
      .getOrElse(Seq.empty)

    Try {
      Await.result(templatePostgresService.upsertList(hydraPegasoTemplates), defaultAwaitForMigration)
    } match {
      case Success(_) =>
        log.info("Templates migrated to Hydra")
      case Failure(e) =>
        log.error(s"Error migrating to Hydra-Pegaso templates. ${ExceptionHelper.toPrintableException(e)}", e)
    }
  }
}
