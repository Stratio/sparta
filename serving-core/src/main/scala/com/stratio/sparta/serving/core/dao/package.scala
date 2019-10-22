/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core

import com.stratio.sparta.core.enumerators.QualityRuleResourceTypeEnum.QualityRuleResourceType
import com.stratio.sparta.core.enumerators.QualityRuleTypeEnum.QualityRuleType
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.constants.DatabaseTableConstant._
import com.stratio.sparta.serving.core.dao.CustomColumnTypes
import com.stratio.sparta.serving.core.models.authorization.HeaderAuthUser
import com.stratio.sparta.serving.core.models.enumerators.DataType.DataType
import com.stratio.sparta.serving.core.models.enumerators.ExecutionTypeEnum.ExecutionType
import com.stratio.sparta.serving.core.models.enumerators.ScheduledActionType.ScheduledActionType
import com.stratio.sparta.serving.core.models.enumerators.ScheduledTaskState.ScheduledTaskState
import com.stratio.sparta.serving.core.models.enumerators.ScheduledTaskType.ScheduledTaskType
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine.ExecutionEngine
import com.stratio.sparta.serving.core.models.enumerators.WorkflowStatusEnum.WorkflowStatusEnum
import com.stratio.sparta.serving.core.models.governance.QualityRuleResult
import com.stratio.sparta.serving.core.models.orchestrator.ScheduledWorkflowTask
import com.stratio.sparta.serving.core.models.workflow.migration._
import com.stratio.sparta.serving.core.models.parameters.{ParameterList, ParameterVariable}
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.models.workflow.migration.{SettingsOrion, TemplateElementOrion, WorkflowOrion}
import org.joda.time.DateTime

import scala.util.Try
//scalastyle:off
package object daoTables {

  import CustomColumnTypes._
  import slick.jdbc.PostgresProfile.api._

  val dbSchemaName: Option[String] = {

    lazy val schemaNameFromConfig: Option[String] =
      Try(SpartaConfig.getPostgresConfig().get.getString("schemaName")).toOption.notBlank

    lazy val schemaNameFromExtraParams: Option[String] =
      Try(SpartaConfig.getPostgresConfig().get.getString("extraParams")).toOption.notBlank
        .fold(Option("")) { extraParameters =>
          extraParameters.split("&", -1)
            .find(condition => condition.startsWith("currentSchema"))
            .map(_.replace("currentSchema=", ""))
        }.notBlank

    schemaNameFromConfig.orElse(schemaNameFromExtraParams).orElse(Some("public"))
  }

  class GlobalParametersTable(tag: Tag) extends Table[ParameterVariable](tag, dbSchemaName, GlobalParametersTableName) {

    def name = column[String]("name")

    def value = column[Option[String]]("value")

    def * = (name, value) <> ((ParameterVariable.apply _).tupled, ParameterVariable.unapply)

    def pk = primaryKey(s"pk_$tableName", name)
  }

  class GroupTable(tag: Tag) extends Table[Group](tag, dbSchemaName, GroupTableName) {

    def groupId = column[String]("group_id")

    def name = column[String]("name")

    def * = (groupId.?, name) <> ((Group.apply _).tupled, Group.unapply)

    def pk = primaryKey(s"pk_$tableName", groupId)

    def groupNameIndex = index(s"idx_${tableName}_name", name, unique = true)
  }

  class WorkflowTable(tag: Tag) extends Table[Workflow](tag, dbSchemaName, WorkflowTableName) {

    import CustomColumnTypes._

    def id = column[String]("workflow_id")

    def name = column[String]("name")

    def description = column[String]("description")

    def settings = column[Settings]("settings")

    def pipelineGraph = column[PipelineGraph]("pipeline_graph")

    def executionEngine = column[ExecutionEngine]("execution_engine")

    def uiSettings = column[Option[UiSettings]]("ui_settings")

    def creationDate = column[Option[DateTime]]("creation_date")

    def lastUpdateDate = column[Option[DateTime]]("last_update_date")

    def version = column[Long]("version")

    def group = column[Group]("group")

    def tags = column[Option[Seq[String]]]("tags")

    def debugMode = column[Option[Boolean]]("debug_mode")

    def versionSparta = column[Option[String]]("version_sparta")

    def parametersUsedInExecution = column[Option[Map[String, String]]]("parameters_used_in_execution")

    def executionId = column[Option[String]]("execution_id")

    def groupId = column[Option[String]]("group_id")

    def ciCdLabel = column[Option[String]]("ci_cd_label")

    def * = (id.?, name, description, settings, pipelineGraph, executionEngine, uiSettings, creationDate, lastUpdateDate,
      version, group, tags, debugMode, versionSparta, parametersUsedInExecution, executionId, groupId, ciCdLabel) <> ((Workflow.apply _).tupled, Workflow.unapply _)

    def pk = primaryKey(s"pk_$tableName", id)

    def uniqueWorkflowIndex = index(s"pk_${tableName}_uniqueWorkflow", (name, groupId, version), unique = true)

    def fk = foreignKey(s"fk_${tableName}_group", groupId.get, TableQuery[GroupTable])(_.groupId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    def groupIndex = index(s"idx_${tableName}_group", groupId)
  }

  class WorkflowHydraPegasoTable(tag: Tag) extends Table[WorkflowHydraPegaso](tag, dbSchemaName, WorkflowTableName) {

    import CustomColumnTypes._

    def id = column[String]("workflow_id")

    def name = column[String]("name")

    def description = column[String]("description")

    def settings = column[Settings]("settings")

    def pipelineGraph = column[PipelineGraphHydraPegaso]("pipeline_graph")

    def executionEngine = column[ExecutionEngine]("execution_engine")

    def uiSettings = column[Option[UiSettings]]("ui_settings")

    def creationDate = column[Option[DateTime]]("creation_date")

    def lastUpdateDate = column[Option[DateTime]]("last_update_date")

    def version = column[Long]("version")

    def group = column[Group]("group")

    def tags = column[Option[Seq[String]]]("tags")

    def debugMode = column[Option[Boolean]]("debug_mode")

    def versionSparta = column[Option[String]]("version_sparta")

    def parametersUsedInExecution = column[Option[Map[String, String]]]("parameters_used_in_execution")

    def executionId = column[Option[String]]("execution_id")

    def groupId = column[Option[String]]("group_id")

    def * = (id.?, name, description, settings, pipelineGraph, executionEngine, uiSettings, creationDate, lastUpdateDate,
      version, group, tags, debugMode, versionSparta, parametersUsedInExecution, executionId, groupId) <> ((WorkflowHydraPegaso.apply _).tupled, WorkflowHydraPegaso.unapply _)

    def pk = primaryKey(s"pk_$tableName", id)

    def uniqueWorkflowIndex = index(s"pk_${tableName}_uniqueWorkflow", (name, groupId, version), unique = true)

    def fk = foreignKey(s"fk_${tableName}_group", groupId.get, TableQuery[GroupTable])(_.groupId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    def groupIndex = index(s"idx_${tableName}_group", groupId)
  }

  class WorkflowOrionTable(tag: Tag) extends Table[WorkflowOrion](tag, dbSchemaName, WorkflowTableName) {

    import CustomColumnTypes._

    def id = column[String]("workflow_id")

    def name = column[String]("name")

    def description = column[String]("description")

    def settings = column[SettingsOrion]("settings")

    def pipelineGraph = column[PipelineGraphHydraPegaso]("pipeline_graph")

    def executionEngine = column[ExecutionEngine]("execution_engine")

    def uiSettings = column[Option[UiSettings]]("ui_settings")

    def creationDate = column[Option[DateTime]]("creation_date")

    def lastUpdateDate = column[Option[DateTime]]("last_update_date")

    def version = column[Long]("version")

    def group = column[Group]("group")

    def tags = column[Option[Seq[String]]]("tags")

    def debugMode = column[Option[Boolean]]("debug_mode")

    def versionSparta = column[Option[String]]("version_sparta")

    def parametersUsedInExecution = column[Option[Map[String, String]]]("parameters_used_in_execution")

    def executionId = column[Option[String]]("execution_id")

    def groupId = column[Option[String]]("group_id")

    def * = (id.?, name, description, settings, pipelineGraph, executionEngine, uiSettings, creationDate, lastUpdateDate,
      version, group, tags, debugMode, versionSparta, parametersUsedInExecution, executionId, groupId) <> ((WorkflowOrion.apply _).tupled, WorkflowOrion.unapply _)

    def pk = primaryKey(s"pk_$tableName", id)

    def uniqueWorkflowIndex = index(s"pk_${tableName}_uniqueWorkflow", (name, groupId, version), unique = true)

    def fk = foreignKey(s"fk_${tableName}_group", groupId.get, TableQuery[GroupTable])(_.groupId, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    def groupIndex = index(s"idx_${tableName}_group", groupId)
  }

  class DebugWorkflowTable(tag: Tag) extends Table[DebugWorkflow](tag, dbSchemaName, DebugWorkflowTableName) {

    def id = column[String]("debug_id")

    def workflowOriginal = column[Workflow]("workflow_original")

    def workflowDebug = column[Option[Workflow]]("workflow_debug")

    def result = column[Option[DebugResults]]("result")

    def * = (id.?, workflowOriginal, workflowDebug, result) <> ((DebugWorkflow.apply _).tupled, DebugWorkflow.unapply _)

    def pk = primaryKey(s"pk_$tableName", id)
  }

  class DebugResultStepTable(tag: Tag) extends Table[ResultStep](tag, dbSchemaName, DebugResultStepTableName) {

    def id = column[String]("id")

    def workflowId = column[String]("workflow_id")

    def step = column[String]("step")

    def numEvents = column[Int]("num_events")

    def schema = column[Option[String]]("schema")

    def data = column[Option[Seq[String]]]("data")

    def * = (id, workflowId, step, numEvents, schema, data) <> ((ResultStep.apply _).tupled, ResultStep.unapply _)

    def pk = primaryKey(s"pk_$tableName", id)

    def stepIndex = index(s"idx_${tableName}_step", step)
  }

  class ParameterListTable(tag: Tag) extends Table[ParameterList](tag, dbSchemaName, ParameterListTableName) {

    import CustomColumnTypes._

    def id = column[String]("id")

    def name = column[String]("name")

    def parameters = column[Seq[ParameterVariable]]("parameters")

    def tags = column[Seq[String]]("tags")

    def description = column[Option[String]]("description")

    def creationDate = column[Option[DateTime]]("creation_date")

    def lastUpdateDate = column[Option[DateTime]]("last_update_date")

    def parent = column[Option[String]]("parent")

    def versionSparta = column[Option[String]]("versionSparta")

    def * = (name, id.?, parameters, tags, description, creationDate, lastUpdateDate, parent, versionSparta) <> ((ParameterList.apply _).tupled, ParameterList.unapply _)

    def pk = primaryKey(s"pk_$tableName", id)

    def nameIndex = index(s"idx_${tableName}_name", name, unique = true)
  }

  class TemplateTable(tag: Tag) extends Table[TemplateElement](tag, dbSchemaName, TemplateTableName) {

    import CustomColumnTypes._

    def id = column[String]("id")

    def templateType = column[String]("template_type")

    def name = column[String]("name")

    def description = column[Option[String]]("description")

    def className = column[String]("class_name")

    def classPrettyName = column[String]("class_pretty_name")

    def configuration = column[Map[String, JsoneyString]]("configuration")

    def creationDate = column[Option[DateTime]]("creation_date")

    def lastUpdateDate = column[Option[DateTime]]("last_update_date")

    def supportedEngines = column[Seq[ExecutionEngine]]("supported_engines") //(executionEngineSeqType)

    def executionEngine = column[Option[ExecutionEngine]]("execution_engine")

    def supportedDataRelations = column[Option[Seq[DataType]]]("supported_data_relations")

    def versionSparta = column[Option[String]]("version_sparta")

    def * = (id.?, templateType, name, description, className, classPrettyName, configuration,
      creationDate, lastUpdateDate, supportedEngines, executionEngine, supportedDataRelations, versionSparta) <> ((TemplateElement.apply _).tupled, TemplateElement.unapply _)

    def pk = primaryKey(s"pk_$tableName", id)

    def templateNameIndex = index(s"idx_${tableName}_name", name, unique = true)
  }

  class TemplateOrionTable(tag: Tag) extends Table[TemplateElementOrion](tag, dbSchemaName, TemplateTableName) {

    import CustomColumnTypes._

    def id = column[String]("id")

    def templateType = column[String]("template_type")

    def name = column[String]("name")

    def description = column[Option[String]]("description")

    def className = column[String]("class_name")

    def classPrettyName = column[String]("class_pretty_name")

    def configuration = column[Map[String, JsoneyString]]("configuration")

    def creationDate = column[Option[DateTime]]("creation_date")

    def lastUpdateDate = column[Option[DateTime]]("last_update_date")

    def supportedEngines = column[Seq[ExecutionEngine]]("supported_engines") //(executionEngineSeqType)

    def executionEngine = column[Option[ExecutionEngine]]("execution_engine")

    def versionSparta = column[Option[String]]("version_sparta")

    def * = (id.?, templateType, name, description, className, classPrettyName, configuration,
      creationDate, lastUpdateDate, supportedEngines, executionEngine, versionSparta) <> ((TemplateElementOrion.apply _).tupled, TemplateElementOrion.unapply _)

    def pk = primaryKey(s"pk_$tableName", id)

    def templateNameIndex = index(s"idx_${tableName}_name", name, unique = true)
  }

  class WorkflowExecutionTable(tag: Tag) extends Table[WorkflowExecution](tag, dbSchemaName, WorkflowExecutionTableName) {

    import CustomColumnTypes._

    def id = column[String]("workflow_execution_id")

    def statuses = column[Seq[ExecutionStatus]]("execution_status")

    def genericDataExecution = column[GenericDataExecution]("generic_data_execution")

    def sparkSubmitExecution = column[Option[SparkSubmitExecution]]("spark_submit_execution")

    def sparkExecution = column[Option[SparkExecution]]("spark_execution")

    def sparkDispatcherExecution = column[Option[SparkDispatcherExecution]]("spark_dispatcher_execution")

    def marathonExecution = column[Option[MarathonExecution]]("marathon_execution")

    def localExecution = column[Option[LocalExecution]]("local_execution")

    def archived = column[Option[Boolean]]("archived", O.Default(Option(false)))

    def resumedDate = column[Option[DateTime]]("resumed_date")

    def resumedStatus = column[Option[WorkflowStatusEnum]]("resumed_status")

    def executionEngine = column[Option[ExecutionEngine]]("execution_engine")

    def searchText = column[Option[String]]("search_text")

    def executedFromScheduler = column[Option[String]]("executed_from_scheduler")

    def executedFromExecution = column[Option[String]]("executed_from_execution")

    def executionType = column[Option[ExecutionType]]("execution_type")

    def qualityRules = column[Seq[SpartaQualityRule]]("quality_rules")

    def * = (id.?, statuses, genericDataExecution, sparkSubmitExecution, sparkExecution, sparkDispatcherExecution, marathonExecution, localExecution, archived, resumedDate, resumedStatus, executionEngine, searchText, qualityRules, executedFromScheduler, executedFromExecution, executionType) <>
      ((WorkflowExecution.apply _).tupled, WorkflowExecution.unapply _)

    def pk = primaryKey(s"pk_$tableName", id)

    def workflowExecutionArchivedIndex = index(s"idx_${tableName}_archived", archived)

    def workflowExecutionArchivedDateIndex = index(s"idx_${tableName}_archived_date", (archived, resumedDate))

    def workflowExecutionArchivedStatusIndex = index(s"idx_${tableName}_archived_status", (archived, resumedStatus))

    def workflowExecutionQueryIndex = index(s"idx_${tableName}_query", (archived, resumedDate, resumedStatus, executionEngine, searchText))

  }

  class ScheduledWorkflowTaskTable(tag: Tag) extends Table[ScheduledWorkflowTask](tag, dbSchemaName, ScheduledWorkflowTaskTableName) {

    import CustomColumnTypes._

    def id = column[String]("id")

    def taskType = column[ScheduledTaskType]("task_type")

    def actionType = column[ScheduledActionType]("action_type")

    def entityId = column[String]("entity_id")

    def executionContext = column[Option[ExecutionContext]]("execution_context")

    def active = column[Boolean]("active", O.Default(true))

    def state = column[ScheduledTaskState]("state")

    def duration = column[Option[String]]("duration")

    def initDate = column[Long]("init_date")

    def loggedUser = column[Option[HeaderAuthUser]]("logged_user")

    def * = (id, taskType, actionType, entityId, executionContext, active, state, initDate, duration, loggedUser) <>
      ((ScheduledWorkflowTask.apply _).tupled, ScheduledWorkflowTask.unapply _)

    def pk = primaryKey(s"pk_$tableName", id)

    def scheduledWorkflowTaskActiveIndex = index(s"idx_${tableName}_active_finished", (active, state))

    def fk = foreignKey(s"fk_${tableName}_workflow", entityId, TableQuery[WorkflowTable])(_.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

  }

  class QualityRuleResultTable(tag: Tag) extends Table[QualityRuleResult](tag, dbSchemaName, QualityRuleResultTable){

    def id = column[String]("data_quality_result_id")

    def executionId =  column[String]("workflow_execution_id")

    def dataQualityRuleId =  column[String]("data_quality_rule_id")

    def numTotalEvents = column[Long]("num_total_events")

    def numPassedEvents = column[Long]("num_passed_events")

    def numDiscardedEvents = column[Long]("num_discarded_events")

    def metadataPath = column[String]("metadata_path")

    def transformationStepName = column[String]("transformation_step_name")

    def outputStepName =  column[String]("output_step_name")

    def satisfied = column[Boolean]("rule_satisfied")

    def successfulWriting = column[Boolean]("successful_writing")

    def condition =  column[String]("condition")

    def sentToApi =  column[Boolean]("sent_to_api")

    def warning =  column[Boolean]("warning")

    def qualityRuleName =  column[String]("qualityRuleName")

    def conditionsString =  column[String]("conditionsString")

    def globalAction =  column[String]("globalAction")

    def creationDate = column[Option[DateTime]]("creation_date")

    def * = (id.?, executionId, dataQualityRuleId, numTotalEvents, numPassedEvents, numDiscardedEvents, metadataPath,
      transformationStepName, outputStepName, satisfied, successfulWriting, condition, sentToApi, warning, qualityRuleName,
      conditionsString, globalAction, creationDate) <>
      ((QualityRuleResult.apply _).tupled, QualityRuleResult.unapply _)

    def pk = primaryKey(s"pk_$tableName", id)

    def fk = foreignKey(s"fk_${tableName}_execution", executionId, TableQuery[WorkflowExecutionTable])(_.id, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    def executionIndex = index(s"idx_${tableName}_execution", executionId)
  }

  class PlannedQualityRuleTable(tag: Tag) extends Table[SpartaQualityRule](tag, dbSchemaName, PlannedQualityRuleTable){

    import CustomColumnTypes._

    def id = column[Long]("planned_quality_rule_id")

    def metadataPath = column[String]("metadata_path")

    def name =  column[String]("quality_rule_name")

    def qualityRuleScope = column[String]("quality_rule_scope")

    def logicalOperator =  column[Option[String]]("logical_operator")

    def metadataPathResourceType = column[Option[QualityRuleResourceType]]("metadatapath_resource_type")

    def metadataPathResourceExtraParams = column[Seq[PropertyKeyValue]]("metadatapath_resource_extraparams")

    def enable = column[Boolean]("enabled_quality_rule")

    def threshold = column[SpartaQualityRuleThreshold]("quality_rule_threshold")

    def predicates = column[Seq[SpartaQualityRulePredicate]]("quality_rule_predicates")

    def stepName = column[String]("transformation_step_name")

    def outputName =  column[String]("output_step_name")

    def executionId = column[Option[String]]("execution_id")

    def qualityRuleType = column[QualityRuleType]("quality_rule_type")

    def plannedQuery =  column[Option[PlannedQuery]]("planned_query")

    def tenant =  column[Option[String]]("tenant")

    def creationDate =  column[Option[Long]]("creation_date")

    def modificationDate =  column[Option[Long]]("modification_date")

    def schedulingDetails = column[Option[SchedulingDetails]]("scheduling_details")

    def taskId = column[Option[String]]("task_id")

    def hadoopConfigUri = column[Option[String]]("hadoop_config_uri")

    def * = (id, metadataPath, name, qualityRuleScope, logicalOperator, metadataPathResourceType, metadataPathResourceExtraParams, enable,
      threshold, predicates, stepName, outputName, executionId, qualityRuleType, plannedQuery, tenant, creationDate,
      modificationDate, schedulingDetails, taskId, hadoopConfigUri) <>
      ((SpartaQualityRule.apply _).tupled, SpartaQualityRule.unapply _)

    def pk = primaryKey(s"pk_$tableName", id)

    def fk = foreignKey(s"fk_${tableName}_scheduled_task", taskId, TableQuery[ScheduledWorkflowTaskTable])(_.id.?, onUpdate = ForeignKeyAction.NoAction, onDelete = ForeignKeyAction.Cascade)

    def qrModificationDateIndex = index(s"idx_${tableName}_modification_date", modificationDate)
  }
}