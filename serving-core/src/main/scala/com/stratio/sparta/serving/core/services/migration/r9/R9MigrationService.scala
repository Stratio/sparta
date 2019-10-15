/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.migration.r9

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.serving.core.constants.DatabaseTableConstant.{WorkflowExecutionTableName, WorkflowTableName}
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine
import com.stratio.sparta.serving.core.models.workflow.Workflow
import com.stratio.sparta.serving.core.services.dao.{BasicPostgresService, WorkflowPostgresDao}
import com.stratio.sparta.serving.core.services.migration.hydra_pegaso.HydraPegasoMigrationService
import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class R9MigrationService(hydraPegasoMigrationService: HydraPegasoMigrationService) extends SLF4JLogging with SpartaSerializer {

  private lazy val workflowPostgresService = PostgresDaoFactory.workflowPgService
  private lazy val workflowExecutionPostgresService = PostgresDaoFactory.executionPgService
  private lazy val basicPostgresService = new BasicPostgresService()

  val defaultAwaitForMigration: Duration = 20 seconds
  val EpochString: String = "01-01-1970"
  val XDClassName = "CrossdataInputStep"
  val BatchSqlClassName = "SQLInputStepBatch"
  val StreamingSqlClassName = "SQLInputStepStreaming"


  def executeMigration(): Unit = {
    r9WorkflowsMigration()
    r9ExecutionsWithSqlSteps()
  }

  def executePostgresMigration(): Unit =
    Try {
      basicPostgresService.dbSchemaName.foreach { schema =>
        val schemaName = basicPostgresService.profile.quoteIdentifier(schema)
        val createTypeExecutionSql =
          s"ALTER TABLE IF EXISTS $schemaName.$WorkflowExecutionTableName ADD COLUMN IF NOT EXISTS execution_type character varying;"
        basicPostgresService.executeSql(createTypeExecutionSql)
        val createColumnCiCdLabel =
          s"ALTER TABLE IF EXISTS $schemaName.$WorkflowTableName ADD COLUMN IF NOT EXISTS ci_cd_label varchar;"
        basicPostgresService.executeSql(createColumnCiCdLabel)
      }
    } match {
      case Success(_) =>
        log.info("R9 Postgres sentences executed successfully")
      case Failure(e) =>
        log.error(s"Error executing R9 Postgres sentences. ${ExceptionHelper.toPrintableException(e)}", e)
    }

  private def r9WorkflowsMigration(): Unit = {

    import WorkflowPostgresDao._

    val hydraPegasoWorkflowsToR9 = hydraPegasoMigrationService.hydraPegasoWorkflowsMigrated().getOrElse(Seq.empty)
    val newR9Workflows = hydraPegasoWorkflowsToR9.map { workflow =>
      val wfWithSQLStep = fromCrossdataToSqlInput(workflow)
      addUpdateDate(addSpartaVersion(wfWithSQLStep.copy(groupId = wfWithSQLStep.groupId.orElse(wfWithSQLStep.group.id))))
    }.groupBy { workflow =>
      (workflow.name, workflow.groupId, workflow.version)
    }.map { case (key, seq) =>
      (key, seq.sortBy(_.lastUpdateDate.getOrElse(DateTime.parse(EpochString)))(Ordering.by(_.getMillis)).head)
    }.values.toSeq

    Try {
      Await.result(workflowPostgresService.upsertList(newR9Workflows), defaultAwaitForMigration * 2)
    } match {
      case Success(_) =>
        log.info("Workflows migrated to R9")
      case Failure(e) =>
        log.error(s"Error migrating to R9 workflows. ${ExceptionHelper.toPrintableException(e)}", e)
    }
  }

  private def r9ExecutionsWithSqlSteps(): Unit = {
    Try {
      Await.result(workflowExecutionPostgresService.findAllExecutions(), defaultAwaitForMigration * 2)
    } match {
      case Success(executions) =>
        val updatedExecutions = executions.map{ exe =>
          val updatedGenericExe = exe.genericDataExecution.copy(
            workflow = fromCrossdataToSqlInput(exe.getWorkflowToExecute))

          exe.copy(genericDataExecution = updatedGenericExe)
        }

        Try {
          Await.result(workflowExecutionPostgresService.upsertList(updatedExecutions), defaultAwaitForMigration * 2)
        } match {
          case Success(_) =>
            log.debug("Executions updated with new SQL inputs")
          case Failure(e) =>
            log.error(s"Error migration executions to R9. ${ExceptionHelper.toPrintableException(e)}", e)
        }

      case Failure(e) =>
        log.error(s"Error migrating executions to R9. ${ExceptionHelper.toPrintableException(e)}", e)
    }
  }

  private def fromCrossdataToSqlInput(wf: Workflow): Workflow = {

    val updatedNodeGraph = wf.pipelineGraph.nodes.map { node =>
      val exeEngine = node.executionEngine.getOrElse(WorkflowExecutionEngine.Streaming)

      if (node.className.equals(XDClassName))
        node.copy(
          className = if (exeEngine == WorkflowExecutionEngine.Batch)
            BatchSqlClassName else StreamingSqlClassName,
          classPrettyName = if (exeEngine == WorkflowExecutionEngine.Batch) "SQL" else "StreamingSQL"
        )
      else node
    }

    wf.copy(pipelineGraph = wf.pipelineGraph.copy(nodes = updatedNodeGraph))
  }
}
