/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.migration

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.models.workflow.migration._
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization.{read, write}

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

//scalastyle:off
class CassiopeiaMigrationService() extends SLF4JLogging with SpartaSerializer {

  private val templateService = new TemplateService(CuratorFactoryHolder.getInstance())

  /** TEMPLATES **/

  lazy val explodeFields = List("schema.fromRow", "schema.inputMode", "schema.fields", "schema.sparkSchema")

  def migrateTemplateFromCassiopeia(cassiopeiaTemplate: TemplateElement): TemplateElement = {
    cassiopeiaTemplate match {
      case cassiopeiaTemplate: TemplateElement if cassiopeiaTemplate.classPrettyName == "Select" => migrateSelect(cassiopeiaTemplate)
      case cassiopeiaTemplate: TemplateElement if cassiopeiaTemplate.classPrettyName == "Explode" => migrateExplode(cassiopeiaTemplate)
      case cassiopeiaTemplate: TemplateElement => cassiopeiaTemplate //no changes
    }
  }

  private def migrateSelect(cassiopeiaTemplate: TemplateElement): TemplateElement = {
    cassiopeiaTemplate.copy(configuration = cassiopeiaTemplate.configuration.filterNot(kv => kv._1 == "delimiter") ++ Map("selectType" -> JsoneyString("EXPRESSION")),
      versionSparta = Some(AppConstant.version))
  }

  private def migrateExplode(cassiopeiaTemplate: TemplateElement): TemplateElement = {
    val newConfig =
      cassiopeiaTemplate.configuration.get("schema.inputMode") match {
        case (Some(mode)) if mode.isInstanceOf[JsoneyString] && mode.toString.equals("SPARKFORMAT") => {
          cassiopeiaTemplate.configuration.filterNot(kv => explodeFields.contains(kv._1)) ++
            Map("inputSchemas" -> List(Map("stepName" -> "", "schema" -> cassiopeiaTemplate.configuration.get("schema.sparkSchema").get)))
        }
        case _ => cassiopeiaTemplate.configuration.filterNot(kv => explodeFields.contains(kv._1))
      }
    cassiopeiaTemplate.copy(configuration = newConfig.asInstanceOf[Map[String, JsoneyString]], versionSparta = Some(AppConstant.version))
  }

  def migrateCassiopeiaTemplates(): Unit = {
    log.info(s"Migrating templates from cassiopeia")
    Try {
      val templatesToMigrate = templateService.findByType("transformation").filterNot(_.versionSparta.isDefined)
      templatesToMigrate.find(template => template.classPrettyName == "Select")
        .foreach(cassiopeiaTemplate =>
          Try {
            //TODO remove!!!
            //templateService.update(migrateSelect(cassiopeiaTemplate))
          } match {
            case Success(_) => log.info(s"Template (Select) ${cassiopeiaTemplate.name} migrated")
            case Failure(f) => log.error(s"Template (Select) ${cassiopeiaTemplate.name} migration error", f)
          }
        )
      templatesToMigrate.find(template => template.classPrettyName == "Explode")
        .foreach(old =>
          Try {
            //TODO remove!!!
            //templateService.update(migrateExplode(old))
          } match {
            case Success(_) => log.info(s"Template (Explode) ${old.name} migrated")
            case Failure(f) => log.error(s"Template (Explode) ${old.name} migration error", f)
          }
        )
    }
  }

  /** WORKFLOWS **/
  def migrateCassiopeiaWorkflows(): Unit = {
    log.info(s"Migrating workflows from cassiopeia")
    Option(CuratorFactoryHolder.existsPath(AppConstant.WorkflowsZkPath))
      .fold(log.info("There are no workflows to migrate")) { _ =>
        val children = CuratorFactoryHolder.getInstance().getChildren.forPath(AppConstant.WorkflowsZkPath)
        val cassiopeiaWorkflows: List[Option[WorkflowCassiopeia]] = JavaConversions.asScalaBuffer(children).toList.map(id => cassiopieaWorkflowExistsById(id))
        cassiopeiaWorkflows.filter(_.isDefined).foreach(cassiopeiaWorkFlow => {
          Try {
            val workflowAndromeda = cassiopieaWorkflowToAndromeda(cassiopeiaWorkFlow.get)
            val workflowtoUpdate = write(workflowAndromeda)
            //TODO remove!!!!
            //workflowService.update(read[Workflow](workflowtoUpdate))
          } match {
            case Success(_) => log.info(s"Workflow ${cassiopeiaWorkFlow.get.name} migrated")
            case Failure(f) => log.error(s"Workflow ${cassiopeiaWorkFlow.get.name} migration error", f)
          }
        }
        )
      }
  }

  def migrateWorkflowFromCassiopeia(workflowCassiopeia: WorkflowCassiopeia): WorkflowAndromeda = {
    log.info(s"Migrate workflowCassiopeia with id = ${workflowCassiopeia.id}")
    cassiopieaWorkflowToAndromeda(workflowCassiopeia)
  }

  private[sparta] def addAndromedaSpartaVersion(workflowAndromeda: WorkflowAndromeda): WorkflowAndromeda =
    workflowAndromeda.versionSparta match {
      case None => workflowAndromeda.copy(versionSparta = Some(AppConstant.version))
      case Some(_) => workflowAndromeda
    }

  private[sparta] def cassiopieaWorkflowToAndromeda(workflowCassiopeia: WorkflowCassiopeia): WorkflowAndromeda = {
    addAndromedaSpartaVersion(
      WorkflowAndromeda(id = workflowCassiopeia.id,
        name = workflowCassiopeia.name,
        description = workflowCassiopeia.name,
        settings = workflowCassiopeia.settings,
        pipelineGraph = workflowCassiopeia.pipelineGraph,
        executionEngine = workflowCassiopeia.executionEngine,
        uiSettings = workflowCassiopeia.uiSettings,
        creationDate = workflowCassiopeia.creationDate,
        lastUpdateDate = workflowCassiopeia.lastUpdateDate,
        version = workflowCassiopeia.version,
        group = workflowCassiopeia.group,
        tags = workflowCassiopeia.tags,
        status = None,
        execution = None
      )
    )
  }

  private[sparta] def cassiopieaWorkflowExistsById(id: String): Option[WorkflowCassiopeia] =
    Try {
      if (CuratorFactoryHolder.existsPath(s"${AppConstant.WorkflowsZkPath}/$id")) {
        val data = new Predef.String(CuratorFactoryHolder.getInstance().getData.forPath(s"${AppConstant.WorkflowsZkPath}/$id"))
        if (!data.contains("versionSparta")) {
          val workFlow = read[WorkflowCassiopeia](data)
          Option(workFlow.copy(status = None))
        } else None
      } else None
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        None
    }
}
