/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.migration.orion

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.TemplateElement
import com.stratio.sparta.serving.core.models.workflow.migration._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}


class OrionMigrationService() extends SLF4JLogging with SpartaSerializer {

  import MigrationModelImplicits._

  private lazy val templateOrionPostgresService = Try(new TemplateOrionPostgresDao()) match {
    case Success(service) =>
      Option(service)
    case Failure(e) =>
      log.error(s"Error creating template service for Orion. ${ExceptionHelper.toPrintableException(e)}", e)
      None
  }
  private lazy val workflowOrionPostgresService = Try(new WorkflowOrionPostgresDao()) match {
    case Success(service) =>
      Option(service)
    case Failure(e) =>
      log.warn(s"Error creating workflow service for Orion. ${ExceptionHelper.toPrintableException(e)}", e)
      None
  }

  val defaultAwaitForMigration: Duration = 20 seconds

  private var templatesPgOrion = Seq.empty[TemplateElementOrion]
  private var workflowsPgOrion = Seq.empty[WorkflowOrion]

  def loadOrionPgData(): Unit = {
    templatesPgOrion = templateOrionPostgresService.map { service =>
      Try {
        Await.result(service.findAllTemplates(), defaultAwaitForMigration)
      } match {
        case Success(templates) =>
          log.info(s"Templates in orion: ${templates.map(_.name).mkString(",")}")
          templates
        case Failure(e) =>
          log.warn(s"Error reading templates in Orion. ${ExceptionHelper.toPrintableException(e)}", e)
          Seq.empty[TemplateElementOrion]
      }
    }.getOrElse(Seq.empty[TemplateElementOrion])

    workflowsPgOrion = workflowOrionPostgresService.map { service =>
      Try {
        Await.result(service.findAllWorkflows(), defaultAwaitForMigration)
      } match {
        case Success(workflows) =>
          log.info(s"Workflows in Orion: ${workflows.map(_.name).mkString(",")}")
          workflows
        case Failure(e) =>
          log.warn(s"Error reading workflows in Orion. ${ExceptionHelper.toPrintableException(e)}", e)
          Seq.empty[WorkflowOrion]
      }
    }.getOrElse(Seq.empty[WorkflowOrion])
  }

  def orionWorkflowsMigrated(): Try[Seq[WorkflowHydraPegaso]] =
    Try {
      workflowsPgOrion.map { orionWorkflow =>
        val hydraWorkflow: WorkflowHydraPegaso = orionWorkflow
        hydraWorkflow
      }
    }

  def orionTemplatesMigrated(): Try[Seq[TemplateElement]] = {
    Try {
      templatesPgOrion.map { orionTemplate =>
        val template: TemplateElement = orionTemplate
        template
      }
    }
  }

}
