/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.migration

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.utils.PostgresDaoFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}


class OrionMigrationService() extends SLF4JLogging with SpartaSerializer {

  private val templateZkService = new ZkTemplateService(CuratorFactoryHolder.getInstance())
  private val groupZkService = new ZkGroupService(CuratorFactoryHolder.getInstance())
  private val workflowZkService = new ZkWorkflowService(CuratorFactoryHolder.getInstance())
  private val environmentZkService = new ZkEnvironmentService(CuratorFactoryHolder.getInstance())

  private val andromedaMigrationService = new AndromedaMigrationService()
  private val cassiopeiaMigrationService = new CassiopeiaMigrationService()

  private val groupPostgresService = PostgresDaoFactory.groupPgService
  private val workflowPostgresService = PostgresDaoFactory.workflowPgService
  private val templatePostgresService = PostgresDaoFactory.templatePgService
  private val paramListPostgresService = PostgresDaoFactory.parameterListPostgresDao
  private val globalParameterPostgresService = PostgresDaoFactory.globalParametersService

  def executeMigration(): Unit = {

    orionEnvironmentMigration()
    orionTemplatesMigration()
    orionGroupsMigration()
    orionWorkflowsMigration()
  }

  private def orionTemplatesMigration(): Unit = {

    val cassiopeaTemplates = cassiopeiaMigrationService.cassiopeiaTemplatesMigrated().getOrElse(Seq.empty)
    val andromedaTemplates = andromedaMigrationService.andromedaTemplatesMigrated(cassiopeaTemplates).getOrElse(Seq.empty)

    Try {
      Await.result(templatePostgresService.upsertList(andromedaTemplates), 20 seconds)
    } match {
      case Success(_) =>
        log.info("Templates migrated to Orion")
        Try {
          andromedaTemplates.foreach(template => templateZkService.create(template))
          templateZkService.deletePath()
        } match {
          case Success(_) =>
            log.info("Andromeda templates moved to backup folder in Zookeeper")
          case Failure(e) =>
            log.error(s"Error moving templates to backup folder. ${ExceptionHelper.toPrintableException(e)}", e)
        }
      case Failure(e) =>
        log.error(s"Error migrating Orion templates. ${ExceptionHelper.toPrintableException(e)}", e)
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

  private def orionWorkflowsMigration(): Unit = {

    val andromedaWorkflows = cassiopeiaMigrationService.cassiopeaWorkflowsMigrated().getOrElse(Seq.empty)
    val orionWorkflows = andromedaMigrationService.andromedaWorkflowsMigrated(andromedaWorkflows).getOrElse(Seq.empty)

    Try {
      Await.result(workflowPostgresService.upsertList(orionWorkflows), 20 seconds)
    } match {
      case Success(_) =>
        log.info("Workflows migrated to Orion")
        Try {
          orionWorkflows.foreach(workflow => workflowZkService.create(workflow))
          workflowZkService.deletePath()
        } match {
          case Success(_) =>
            log.info("Andromeda workflows moved to backup folder in Zookeeper")
          case Failure(e) =>
            log.error(s"Error moving workflows to backup folder. ${ExceptionHelper.toPrintableException(e)}", e)
        }
      case Failure(e) =>
        log.error(s"Error migrating workflows. ${ExceptionHelper.toPrintableException(e)}", e)
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
