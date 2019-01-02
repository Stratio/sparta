/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.migration.orion

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.serving.core.factory.{CuratorFactoryHolder, PostgresDaoFactory}
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.migration._
import com.stratio.sparta.serving.core.models.workflow.{TemplateElement, Workflow}
import com.stratio.sparta.serving.core.services.migration.andromeda.AndromedaMigrationService
import com.stratio.sparta.serving.core.services.migration.cassiopea.CassiopeiaMigrationService
import com.stratio.sparta.serving.core.services.migration.zookeeper.{ZkEnvironmentService, ZkGroupService}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}


class OrionMigrationService() extends SLF4JLogging with SpartaSerializer {

  import MigrationModelImplicits._

  private val groupZkService = new ZkGroupService(CuratorFactoryHolder.getInstance())
  private val environmentZkService = new ZkEnvironmentService(CuratorFactoryHolder.getInstance())

  private val andromedaMigrationService = new AndromedaMigrationService()
  private val cassiopeiaMigrationService = new CassiopeiaMigrationService()

  private val groupPostgresService = PostgresDaoFactory.groupPgService
  private val paramListPostgresService = PostgresDaoFactory.parameterListPostgresDao
  private val globalParameterPostgresService = PostgresDaoFactory.globalParametersService

  def executeMigration(): Unit = {
    orionEnvironmentMigration()
    orionGroupsMigration()
  }

  def orionWorkflowsMigrated(): Try[(Seq[Workflow], Seq[WorkflowOrion], Seq[WorkflowAndromeda], Seq[WorkflowCassiopeia])] =
    Try {
      val (cassiopeiaToAndromedaWorkflows, cassiopeaWorkflows) = cassiopeiaMigrationService.cassiopeaWorkflowsMigrated()
        .getOrElse((Seq.empty, Seq.empty))
      val (andromedaToOrionWorkflows, andromedaWorkflows) = andromedaMigrationService.andromedaWorkflowsMigrated(cassiopeiaToAndromedaWorkflows)
        .getOrElse((Seq.empty, Seq.empty))
      val hydraWorkflows = andromedaToOrionWorkflows.map { orionWorkflow =>
        val hydraWorkflow: Workflow = orionWorkflow
        hydraWorkflow
      }

      (hydraWorkflows, andromedaToOrionWorkflows, andromedaWorkflows ++ cassiopeiaToAndromedaWorkflows, cassiopeaWorkflows)
    }

  def orionTemplatesMigrated(): Try[(Seq[TemplateElement], Seq[TemplateElementOrion], Seq[TemplateElementOrion])] = {
    Try {
      val (cassiopeiaTemplatesToAndromeda, cassiopeiaTemplates) = cassiopeiaMigrationService.cassiopeiaTemplatesMigrated()
        .getOrElse((Seq.empty, Seq.empty))
      val (orionTemplates, andromedaTemplates) = andromedaMigrationService.andromedaTemplatesMigrated(cassiopeiaTemplatesToAndromeda)
        .getOrElse((Seq.empty, Seq.empty))
      val hydraTemplates = orionTemplates.map { orionTemplate =>
        val template: TemplateElement = orionTemplate
        template
      }

      (hydraTemplates, andromedaTemplates ++ cassiopeiaTemplatesToAndromeda, cassiopeiaTemplates)
    }
  }

  private def orionGroupsMigration(): Unit = {

    val andromedaGroups = andromedaMigrationService.andromedaGroupsMigrated().getOrElse(Seq.empty)

    Try {
      Await.result(groupPostgresService.upsertList(andromedaGroups), 20 seconds)
    } match {
      case Success(_) =>
        log.info("Groups migrated to Orion")
        Try {
          andromedaGroups.foreach(group => groupZkService.create(group))
          groupZkService.deletePath()
        } match {
          case Success(_) =>
            log.info("Andromeda groups moved to backup folder in Zookeeper")
          case Failure(e) =>
            log.error(s"Error moving groups to backup folder. ${ExceptionHelper.toPrintableException(e)}", e)
        }
      case Failure(e) =>
        log.error(s"Error migrating Orion groups. ${ExceptionHelper.toPrintableException(e)}", e)
    }
  }

  private def orionEnvironmentMigration(): Unit = {
    andromedaMigrationService.andromedaEnvironmentMigrated().map { result =>
      result.foreach { case (globalParameters, environmentList, defaultList, environmentAndromeda) =>
        Try {
          Await.result(globalParameterPostgresService.updateGlobalParameters(globalParameters), 20 seconds)
          Await.result(paramListPostgresService.update(environmentList), 20 seconds)
          Await.result(paramListPostgresService.update(defaultList), 20 seconds)
        } match {
          case Success(_) =>
            Try {
              environmentZkService.create(environmentAndromeda)
              environmentZkService.deletePath()
            } match {
              case Success(_) =>
                log.info("Andromeda environment moved to backup folder in Zookeeper")
              case Failure(e) =>
                log.error(s"Error moving environment to backup folder. ${ExceptionHelper.toPrintableException(e)}", e)
            }
          case Failure(e) =>
            throw e
        }
      }
    } match {
      case Success(_) => log.info("Environment migrated to Orion")
      case Failure(e) => log.error(s"Error migrating Orion environment. ${ExceptionHelper.toPrintableException(e)}", e)
    }
  }

}
