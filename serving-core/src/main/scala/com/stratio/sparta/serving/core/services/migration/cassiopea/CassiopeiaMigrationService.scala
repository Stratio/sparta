/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services.migration.cassiopea

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.helpers.ExceptionHelper
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow.migration._
import com.stratio.sparta.serving.core.services.migration.zookeeper.ZkTemplateService
import org.json4s.jackson.Serialization.read

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

//scalastyle:off
class CassiopeiaMigrationService() extends SLF4JLogging with SpartaSerializer {

  import MigrationModelImplicits._

  private val templateService = new ZkTemplateService(CuratorFactoryHolder.getInstance())

  /** TEMPLATES **/

  lazy val explodeFields = List("schema.fromRow", "schema.inputMode", "schema.fields", "schema.sparkSchema")

  private def migrateSelect(cassiopeiaTemplate: TemplateElementOrion): TemplateElementOrion = {
    cassiopeiaTemplate.copy(configuration = cassiopeiaTemplate.configuration.filterNot(kv => kv._1 == "delimiter") ++ Map("selectType" -> JsoneyString("EXPRESSION")),
      versionSparta = Some(AppConstant.AndromedaVersion))
  }

  private def migrateExplode(cassiopeiaTemplate: TemplateElementOrion): TemplateElementOrion = {
    val newConfig =
      cassiopeiaTemplate.configuration.get("schema.inputMode") match {
        case (Some(mode)) if mode.isInstanceOf[JsoneyString] && mode.toString.equals("SPARKFORMAT") =>
          cassiopeiaTemplate.configuration.filterNot(kv => explodeFields.contains(kv._1)) ++
            Map("inputSchemas" -> List(Map("stepName" -> "", "schema" -> cassiopeiaTemplate.configuration("schema.sparkSchema"))))
        case _ => cassiopeiaTemplate.configuration.filterNot(kv => explodeFields.contains(kv._1))
      }
    cassiopeiaTemplate.copy(configuration = newConfig.asInstanceOf[Map[String, JsoneyString]], versionSparta = Some(AppConstant.AndromedaVersion))
  }

  def cassiopeiaTemplatesMigrated(): Try[(Seq[TemplateElementOrion], Seq[TemplateElementOrion])] = {
    log.info(s"Migrating templates from Cassiopeia")
    Try {
      if (CuratorFactoryHolder.existsPath(AppConstant.TemplatesZkPath)) {
        val templatesInZk = templateService.findAll
        val cassiopeiaTemplates = templatesInZk.filterNot(_.versionSparta.isDefined)
        val cassiopeiaTemplatesToAndromeda = templatesInZk.filterNot(_.versionSparta.isDefined).map { template =>
          if (template.classPrettyName == "Select")
            migrateSelect(template)
          else if (template.classPrettyName == "Explode")
            migrateExplode(template)
          else template.copy(versionSparta = Some(AppConstant.AndromedaVersion))
        }

        (cassiopeiaTemplatesToAndromeda, cassiopeiaTemplates)
      } else (Seq.empty, Seq.empty)
    }
  }

  /** WORKFLOWS **/

  def cassiopeaWorkflowsMigrated(): Try[(Seq[WorkflowAndromeda], Seq[WorkflowCassiopeia])] = {
    log.info(s"Migrating workflows from Cassiopeia")
    Try {
      if (CuratorFactoryHolder.existsPath(AppConstant.WorkflowsZkPath)) {
        val children = CuratorFactoryHolder.getInstance().getChildren.forPath(AppConstant.WorkflowsZkPath)
        val cassiopeiaWorkflows = JavaConversions.asScalaBuffer(children).toList.map(id =>
          cassiopieaWorkflowExistsById(id)
        )
        val andromedaWorkflows = cassiopeiaWorkflows.flatMap { cassiopeiaWorkFlow =>
          cassiopeiaWorkFlow.map { workflow =>
            Try {
              val andromedaWorkflow: WorkflowAndromeda = workflow
              andromedaWorkflow
            } match {
              case Success(andromedaWorkflow) =>
                andromedaWorkflow
              case Failure(e) =>
                log.error(s"Workflow ${workflow.name} Cassiopea migration error. ${ExceptionHelper.toPrintableException(e)}", e)
                throw e
            }
          }
        }
        (andromedaWorkflows, cassiopeiaWorkflows.flatten)
      } else (Seq.empty, Seq.empty)
    }
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
