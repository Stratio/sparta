/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core.services

import scala.collection.JavaConversions
import scala.util.{Failure, Success, Try}

import akka.event.slf4j.SLF4JLogging
import org.apache.curator.framework.CuratorFramework
import org.json4s.jackson.Serialization.{read, write}

import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.stratio.sparta.serving.core.models.SpartaSerializer
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.models.workflow.migration._

//scalastyle:off
class CassiopeiaMigrationService(val curatorFramework: CuratorFramework) extends SLF4JLogging with SpartaSerializer {

  private val templateService = new TemplateService(curatorFramework)
  private val workflowService = new WorkflowService(curatorFramework)
  private val statusService = new WorkflowStatusService(curatorFramework)
  private val executionService = new ExecutionService(curatorFramework)

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
            templateService.update(migrateSelect(cassiopeiaTemplate))
          } match {
            case Success(_) => log.info(s"Template (Select) ${cassiopeiaTemplate.name} migrated")
            case Failure(f) => log.error(s"Template (Select) ${cassiopeiaTemplate.name} migration error", f)
          }
        )
      templatesToMigrate.find(template => template.classPrettyName == "Explode")
        .foreach(old =>
          Try {
            templateService.update(migrateExplode(old))
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
    val children = curatorFramework.getChildren.forPath(AppConstant.WorkflowsZkPath)
    val cassiopeiaWorkflows: List[Option[WorkflowCassiopeia]] = JavaConversions.asScalaBuffer(children).toList.map(id => cassiopieaWorkflowExistsById(id))
    cassiopeiaWorkflows.filter(_.isDefined).foreach(cassiopeiaWorkFlow => {
      Try {
        var workflowAndromeda = cassiopieaWorkflowToAndromeda(cassiopeiaWorkFlow.get)
        val workflowAndromedaStatus = workflowAndromeda.status
        val workFlowAndromedaExecution = cassiopeiaExecutionToAndromeda(workflowAndromeda, cassiopeiaWorkFlow.get)

        if (workFlowAndromedaExecution.isDefined) {
          val workFlowExecution = write(workFlowAndromedaExecution.get)
          executionService.update(read[WorkflowExecution](workFlowExecution))
          workflowAndromeda = workflowAndromeda.copy(execution = workFlowAndromedaExecution)
        }
        if (workflowAndromedaStatus.isDefined) {
          val workFlowStatus = write(workflowAndromedaStatus.get)
          statusService.update(read[WorkflowStatus](workFlowStatus))
        }
        val workflowtoUpdate = write(workflowAndromeda)
        workflowService.update(read[Workflow](workflowtoUpdate))
      } match {
        case Success(_) => log.info(s"Workflow ${cassiopeiaWorkFlow.get.name} migrated")
        case Failure(f) => log.error(s"Workflow ${cassiopeiaWorkFlow.get.name} migration error", f)
      }
    }
    )
  }

  def migrateWorkflowFromCassiopeia(workflowCassiopeia: WorkflowCassiopeia): WorkflowAndromeda = {
    log.info(s"Migrate workflowCassiopeia with id = ${workflowCassiopeia.id}")
    var workflowAndromeda = cassiopieaWorkflowToAndromeda(workflowCassiopeia)
    val workFlowExecutionAndromeda = cassiopeiaExecutionToAndromeda(workflowAndromeda, workflowCassiopeia)
    if (workFlowExecutionAndromeda.isDefined) {
      workflowAndromeda = workflowAndromeda.copy(execution = workFlowExecutionAndromeda)
    }
    workflowAndromeda
  }

  private[sparta] def addAndromedaSpartaVersion(workflowAndromeda: WorkflowAndromeda): WorkflowAndromeda =
    workflowAndromeda.versionSparta match {
      case None => workflowAndromeda.copy(versionSparta = Some(AppConstant.version))
      case Some(_) => workflowAndromeda
    }

  private[sparta] def cassiopieaWorkflowToAndromeda(workflowCassiopeia: WorkflowCassiopeia): WorkflowAndromeda = {
    val workflowStatus = workflowCassiopeia.status.map(s => WorkflowStatusAndromeda(id = s.id, status = s.status, statusInfo = s.statusInfo, creationDate = s.creationDate,
      lastUpdateDate = s.lastUpdateDate, lastUpdateDateWorkflow = s.lastUpdateDateWorkflow))

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
        status = workflowStatus,
        execution = None
      )
    )
  }

  private[sparta] def cassiopeiaExecutionToAndromeda(workflowAndromeda: WorkflowAndromeda, workflowCassiopeia: WorkflowCassiopeia): Option[WorkflowExecutionAndromeda] = {
    Try {
      val id = workflowCassiopeia.id.get
      val executionPath = s"${AppConstant.WorkflowExecutionsZkPath}/$id"
      read[WorkflowExecutionCassiopeia](new String(curatorFramework.getData.forPath(executionPath)))
    } match {
      case Success(cassiopeiaExecution) => Option(WorkflowExecutionAndromeda(id = cassiopeiaExecution.id,
        sparkSubmitExecution = Option(SparkSubmitExecutionAndromeda(driverClass = cassiopeiaExecution.sparkSubmitExecution.driverClass,
          driverFile = cassiopeiaExecution.sparkSubmitExecution.driverFile,
          pluginFiles = cassiopeiaExecution.sparkSubmitExecution.pluginFiles,
          master = cassiopeiaExecution.sparkSubmitExecution.master,
          submitArguments = cassiopeiaExecution.sparkSubmitExecution.submitArguments,
          sparkConfigurations = cassiopeiaExecution.sparkSubmitExecution.sparkConfigurations,
          driverArguments = cassiopeiaExecution.sparkSubmitExecution.driverArguments,
          sparkHome = cassiopeiaExecution.sparkSubmitExecution.sparkHome)),
        sparkExecution = cassiopeiaExecution.sparkExecution.map(sparkExecution => SparkExecutionAndromeda(applicationId = sparkExecution.applicationId)),
        sparkDispatcherExecution = cassiopeiaExecution.sparkDispatcherExecution,
        marathonExecution = cassiopeiaExecution.marathonExecution.map(me => MarathonExecutionAndromeda(marathonId = me.marathonId)),
        localExecution = None,
        genericDataExecution = Option(GenericDataExecutionAndromeda(
          workflow = workflowAndromeda,
          executionMode = workflowCassiopeia.settings.global.executionMode,
          executionId = cassiopeiaExecution.id))
      ))
      case Failure(f) => {
        log.error(s"Error on migration workflowExecution ${workflowCassiopeia.id.get}", f)
        None
      }
    }
  }

  private[sparta] def cassiopieaWorkflowExistsById(id: String): Option[WorkflowCassiopeia] =
    Try {
      if (CuratorFactoryHolder.existsPath(s"${AppConstant.WorkflowsZkPath}/$id")) {
        val data = new Predef.String(curatorFramework.getData.forPath(s"${AppConstant.WorkflowsZkPath}/$id"))
        if (!data.contains("versionSparta")) {
          val workFlow = read[WorkflowCassiopeia](data)
          Option(workFlow.copy(status =
            findCassiopeiaWorkflowStatusById(id) recoverWith {
              case e =>
                log.error(s"Error finding workflowStatus with id $id", e)
                Failure(e)
            } toOption
          ))
        } else
          None
      } else None
    } match {
      case Success(result) => result
      case Failure(exception) =>
        log.error(exception.getLocalizedMessage, exception)
        None
    }

  private[sparta] def findCassiopeiaWorkflowStatusById(id: String): Try[WorkflowStatusCassiopeia] = {
    Try {
      val statusPath = s"${AppConstant.WorkflowStatusesZkPath}/$id"
      read[WorkflowStatusCassiopeia](new String(curatorFramework.getData.forPath(statusPath)))
    }
  }

  private[sparta] def findCassiopeiaWorkflowExecutionsById(id: String): Try[WorkflowExecutionCassiopeia] = {
    Try {
      val executionPath = s"${AppConstant.WorkflowExecutionsZkPath}/$id"
      read[WorkflowExecutionCassiopeia](new String(curatorFramework.getData.forPath(executionPath)))
    }
  }
}
