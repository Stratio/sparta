/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.services.migration.r9

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.serving.core.factory.PostgresDaoFactory
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.services.dao.{BasicPostgresService, WorkflowPostgresDao}
import com.stratio.sparta.serving.core.services.migration.hydra_pegaso.HydraPegasoMigrationService
import org.joda.time.DateTime

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class R9MigrationService(hydraPegasoMigrationService: HydraPegasoMigrationService) extends SLF4JLogging with SpartaSerializer {

  private lazy val workflowPostgresService = PostgresDaoFactory.workflowPgService

  private lazy val basicPostgresService = new BasicPostgresService()

  val defaultAwaitForMigration: Duration = 20 seconds

  val epochString: String = "01-01-1970"

  def executeMigration(): Unit = {
    r9WorkflowsMigration()
  }

  def executePostgresMigration(): Unit =
    Try {
      basicPostgresService.dbSchemaName.foreach { schema =>
        val schemaName = basicPostgresService.profile.quoteIdentifier(schema)

        //TODO with new fields and updates
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
      addUpdateDate(addSpartaVersion(workflow.copy(groupId = workflow.groupId.orElse(workflow.group.id))))
    }.groupBy { workflow =>
      (workflow.name, workflow.groupId, workflow.version)
    }.map { case (key, seq) =>
      (key, seq.sortBy(_.lastUpdateDate.getOrElse(DateTime.parse(epochString)))(Ordering.by(_.getMillis)).head)
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

}
