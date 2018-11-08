/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.serving.core

import scala.util.Try

import org.joda.time.DateTime

import com.stratio.sparta.core.models.{DebugResults, ResultStep}
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.dao.CustomColumnTypes
import com.stratio.sparta.serving.core.models.enumerators.WorkflowExecutionEngine.ExecutionEngine
import com.stratio.sparta.serving.core.models.parameters.{ParameterList, ParameterVariable}
import com.stratio.sparta.serving.core.models.workflow._
import com.stratio.sparta.serving.core.constants.DatabaseTableConstant._
//scalastyle:off
package object daoTables {

  import slick.jdbc.PostgresProfile.api._
  import CustomColumnTypes._

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

    def * = (id.?, name, description, settings, pipelineGraph, executionEngine, uiSettings, creationDate, lastUpdateDate,
      version, group, tags, debugMode, versionSparta, parametersUsedInExecution, executionId, groupId) <> ((Workflow.apply _).tupled, Workflow.unapply _)

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

    def versionSparta = column[Option[String]]("version_sparta")

    def * = (id.?, templateType, name, description, className, classPrettyName, configuration,
      creationDate, lastUpdateDate, supportedEngines, executionEngine, versionSparta) <> ((TemplateElement.apply _).tupled, TemplateElement.unapply _)

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

    def archived = column[Option[Boolean]]("archived")

    def * = (id.?, statuses, genericDataExecution, sparkSubmitExecution, sparkExecution, sparkDispatcherExecution, marathonExecution, localExecution, archived) <>
      ((WorkflowExecution.apply _).tupled, WorkflowExecution.unapply _)

    def pk = primaryKey(s"pk_$tableName", id)

    def workflowExecutionArchivedIndex = index(s"idx_${tableName}_archived", archived)
  }

}

