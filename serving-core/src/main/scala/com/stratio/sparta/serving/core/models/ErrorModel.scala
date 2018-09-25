/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.core.models

import org.json4s.native.Serialization._
import spray.http.StatusCodes

case class ErrorModel(
                       statusCode: Int,
                       errorCode: String,
                       message: String,
                       detailMessage: Option[String] = None,
                       exception: Option[String] = None
                     )

object ErrorModel extends SpartaSerializer {

  /* Generic error messages */
  val UnknownError = "Unknown error"
  val UnAuthorizedError = "Unauthorized error"

  /* Authorization Service 550-559 */
  val UserNotFound = "550"

  /* Unkown error */
  val UnknownErrorCode = "560"

  /* App Info Service */
  val AppInfo = "561"

  /* App Info Service */
  val AppStatus = "562"

  /* Config Service */
  val ConfigurationUnexpected = "563"
  val ConfigurationFind = "564"

  /* Workflow Executions Service 575-599 */
  val WorkflowExecutionUnexpected = "575"
  val WorkflowExecutionFindAll = "576"
  val WorkflowExecutionFindById = "577"
  val WorkflowExecutionDeleteAll = "578"
  val WorkflowExecutionDeleteById = "579"
  val WorkflowExecutionUpdate = "580"
  val WorkflowExecutionCreate = "581"
  val WorkflowExecutionFindAllDto = "582"
  val WorkflowExecutionDashboard = "583"

  /* Crossdata Service 600-624 */
  val CrossdataServiceUnexpected = "600"
  val CrossdataServiceListDatabases = "601"
  val CrossdataServiceListTables = "602"
  val CrossdataServiceListColumns = "603"
  val CrossdataServiceExecuteQuery = "604"

  /* Workflow Status Service 625-649 */
  val WorkflowStatusUnexpected = "625"
  val WorkflowStatusFindAll = "626"
  val WorkflowStatusFindById = "627"
  val WorkflowStatusDeleteAll = "628"
  val WorkflowStatusDeleteById = "629"
  val WorkflowStatusUpdate = "630"

  /* Template Service 650-675 */
  val TemplateServiceUnexpected = "650"
  val TemplateServiceFindByTypeId = "651"
  val TemplateServiceFindByTypeName = "652"
  val TemplateServiceFindAllByType = "653"
  val TemplateServiceFindAll = "654"
  val TemplateServiceCreate = "655"
  val TemplateServiceUpdate = "656"
  val TemplateServiceDeleteByTypeId = "657"
  val TemplateServiceDeleteByTypeName = "658"
  val TemplateServiceDeleteByType = "659"
  val TemplateServiceDeleteAll = "660"
  val TemplateMigration = "661"

  /* Driver Service 675-684 */
  val DriverServiceUnexpected = "675"
  val DriverServiceUpload = "676"
  val DriverServiceFindAll = "677"
  val DriverServiceDeleteAll = "678"
  val DriverServiceDeleteByName = "679"

  /* Plugins Service 685-699 */
  val PluginsServiceUnexpected = "685"
  val PluginsServiceUpload = "686"
  val PluginsServiceFindAll = "687"
  val PluginsServiceDeleteAll = "688"
  val PluginsServiceDeleteByName = "689"
  val PluginsServiceDownload = "690"

  /* Workflow Service 700-749 */
  val WorkflowServiceUnexpected = "700"
  val WorkflowServiceFindById = "701"
  val WorkflowServiceFind = "702"
  val WorkflowServiceFindByIds = "703"
  val WorkflowServiceFindAll = "704"
  val WorkflowServiceCreate = "705"
  val WorkflowServiceCreateList = "706"
  val WorkflowServiceUpdate = "707"
  val WorkflowServiceUpdateList = "708"
  val WorkflowServiceDeleteAll = "709"
  val WorkflowServiceDeleteList = "710"
  val WorkflowServiceDeleteById = "711"
  val WorkflowServiceDeleteCheckpoint = "712"
  val WorkflowServiceRun = "713"
  val WorkflowServiceStop = "714"
  val WorkflowServiceReset = "715"
  val WorkflowServiceDownload = "716"
  val WorkflowServiceValidate = "717"
  val WorkflowServiceResetAllStatuses = "718"
  val WorkflowServiceFindByIdWithExecutionContext = "719"
  val WorkflowServiceFindByNameWithEnv = "720"
  val WorkflowServiceFindAllWithEnv = "721"
  val WorkflowServiceNewVersion = "722"
  val WorkflowServiceFindAllByGroup = "723"
  val WorkflowServiceFindAllMonitoring = "724"
  val WorkflowServiceRename = "725"
  val WorkflowServiceDeleteWithAllVersions = "726"
  val WorkflowServiceMove = "727"
  val WorkflowServiceMigration = "728"
  val WorkflowServiceRunWithExecutionContextView = "729"

  /* Environment Service 750-769 */
  val GlobalParametersServiceUnexpected = "750"
  val GlobalParametersServiceFindEnvironment = "751"
  val GlobalParametersServiceCreateEnvironment = "752"
  val GlobalParametersServiceDeleteEnvironment = "753"
  val GlobalParametersServiceUpdateEnvironment = "754"
  val GlobalParametersServiceFindEnvironmentVariable = "755"
  val GlobalParametersServiceCreateEnvironmentVariable = "756"
  val GlobalParametersServiceUpdateEnvironmentVariable = "757"
  val GlobalParametersDeleteEnvironmentVariable = "758"

  /* Group Service 770-799 */
  val GroupServiceUnexpected = "770"
  val GroupServiceFindGroup = "771"
  val GroupServiceFindAllGroups = "772"
  val GroupServiceCreateGroup = "773"
  val GroupServiceDeleteGroup = "774"
  val GroupServiceDeleteAllGroups = "775"
  val GroupServiceUpdateGroup = "776"

  /* Metadata Service 800-825 */
  val MetadataServiceUnexpected = "800"
  val MetadataServiceBuildBackup = "801"
  val MetadataServiceExecuteBackup = "802"
  val MetadataServiceUploadBackup = "803"
  val MetadataServiceFindAllBackups = "804"
  val MetadataServiceDeleteAllBackups = "805"
  val MetadataServiceDeleteBackup = "806"
  val MetadataServiceCleanAll = "807"

  /* Repository ML Models 826-850 */
  val MlModelsServiceFindAll = "826"

  /* Debug Service 900-949 */
  val DebugWorkflowServiceUnexpected = "900"
  val DebugWorkflowServiceFindById = "901"
  val DebugWorkflowServiceFind = "902"
  val DebugWorkflowServiceFindAll = "904"
  val DebugWorkflowServiceCreate = "905"
  val DebugWorkflowServiceUpdate = "907"
  val DebugWorkflowServiceDeleteAll = "909"
  val DebugWorkflowServiceDeleteById = "911"
  val DebugWorkflowServiceRun = "913"
  val DebugWorkflowServiceResultsFindById = "921"
  val DebugWorkflowServiceResultsFindByIdNotAvailable = "922"
  val DebugWorkflowServiceUpload = "923"
  val DebugWorkflowServiceDeleteFile = "924"
  val DebugWorkflowServiceDownload = "925"

  /* history Service 950 - 974 */
  val WorkflowHistoryExecutionUnexpected = "950"
  val WorkflowHistoryExecutionFindByUserId = "951"
  val WorkflowHistoryExecutionFindByWorkflowId = "952"
  val WorkflowHistoryStatusUnexpected = "953"
  val WorkflowHistoryStatusFindByWorkflowId = "954"

  /* Parameter lists Service 975 - 999 */
  val ParameterListServiceUnexpected = "975"
  val ParameterListServiceFindById = "976"
  val ParameterListServiceFindByName = "977"
  val ParameterListServiceFindAll = "978"
  val ParameterListServiceCreate = "979"
  val ParameterListServiceUpdate = "980"
  val ParameterListServiceDeleteAll = "981"
  val ParameterListServiceDeleteByName = "982"
  val ParameterListServiceFindByParent = "983"
  val ParameterListServiceCreateFromWorkflow = "984"
  val ParameterListServiceFindAllContexts = "985"
  val ParameterListServiceFindEnvironment = "986"
  val ParameterListServiceFindEnvironmentContexts = "987"
  val ParameterListServiceFindEnvironmentAndContexts = "988"
  val ParameterListServiceFindGroupAndContexts = "989"
  val ParameterListServiceDeleteById = "990"

  /* Map with all error codes and messages */
  val ErrorCodesMessages = Map(
    UnknownErrorCode -> UnknownError,
    StatusCodes.Unauthorized.toString() -> "Unauthorized action",
    UserNotFound -> "User not found",
    CrossdataServiceUnexpected -> "Unexpected behaviour in Crossdata catalog",
    CrossdataServiceListDatabases -> "Impossible to list databases in Crossdata Context",
    CrossdataServiceListTables -> "Impossible to list tables in Crossdata Context",
    CrossdataServiceListColumns -> "Impossible to list columns in Crossdata Context",
    AppInfo -> "Impossible to extract server information",
    AppStatus -> "Zookeeper is not connected",
    WorkflowServiceUnexpected -> "Unexpected behaviour in Workflow service",
    WorkflowServiceFindById -> "Error finding workflow by ID",
    WorkflowServiceFind -> "Error executing workflow query",
    WorkflowServiceFindByIds -> "Error finding workflows by ID's",
    WorkflowServiceFindAll -> "Error obtaining all workflows",
    WorkflowServiceCreate -> "Error creating workflow",
    WorkflowServiceCreateList -> "Error creating workflows",
    WorkflowServiceUpdate -> "Error updating workflow",
    WorkflowServiceUpdateList -> "Error updating workflows",
    WorkflowServiceDeleteAll -> "Error deleting all workflows",
    WorkflowServiceDeleteWithAllVersions -> "Error deleting the workflow and its versions",
    WorkflowServiceDeleteList -> "Error deleting workflows",
    WorkflowServiceDeleteById -> "Error deleting workflows by ID",
    WorkflowServiceDeleteCheckpoint -> "Error deleting checkpoint",
    WorkflowServiceRun -> "Error running workflow",
    WorkflowServiceStop -> "Error stopping workflow",
    WorkflowServiceReset -> "Error resetting workflow",
    WorkflowServiceDownload -> "Error downloading workflow",
    WorkflowServiceValidate -> "Error validating workflow",
    WorkflowServiceResetAllStatuses -> "Error resetting all workflow statuses",
    WorkflowServiceNewVersion -> "Error creating new workflow version",
    WorkflowServiceFindByIdWithExecutionContext -> "Error finding workflows by ID with execution context",
    WorkflowServiceFindByNameWithEnv -> "Error finding workflows by name with environment",
    WorkflowServiceFindAllWithEnv -> "Error finding all workflows with environment",
    WorkflowServiceFindAllByGroup -> "Error finding all workflows by group",
    WorkflowServiceFindAllMonitoring -> "Error finding all workflows for monitoring",
    WorkflowServiceRename -> "Error renaming all workflow versions",
    WorkflowServiceMove -> "Error moving workflow between groups",
    WorkflowServiceRunWithExecutionContextView -> "Error creating run with parameters view",
    WorkflowStatusUnexpected -> "Unexpected behaviour in Workflow status service",
    WorkflowStatusFindAll -> "Error obtaining all workflow statuses",
    WorkflowStatusFindById -> "Error obtaining workflow status",
    WorkflowStatusDeleteAll -> "Error deleting all workflow statuses",
    WorkflowStatusDeleteById -> "Error deleting workflow status",
    WorkflowStatusUpdate -> "Error updating workflow status",
    TemplateServiceUnexpected -> "Unexpected behaviour in templates service",
    TemplateServiceFindByTypeId -> "Error obtaining template by ID",
    TemplateServiceFindByTypeName -> "Error obtaining template by name",
    TemplateServiceFindAllByType -> "Error obtaining templates by type",
    TemplateServiceFindAll -> "Error obtaining templates",
    TemplateServiceCreate -> "Error creating template",
    TemplateServiceUpdate -> "Error updating template",
    TemplateServiceDeleteByTypeId -> "Error deleting template by ID",
    TemplateServiceDeleteByTypeName -> "Error deleting template by name",
    TemplateServiceDeleteByType -> "Error deleting templates by type",
    TemplateServiceDeleteAll -> "Error deleting all templates",
    WorkflowExecutionUnexpected -> "575",
    WorkflowExecutionFindAll -> "Error obtaining all workflow executions",
    WorkflowExecutionFindAllDto -> "Error obtaining all workflow executions dto",
    WorkflowExecutionDashboard -> "Error obtaining dashboard",
    WorkflowExecutionFindById -> "Error obtaining workflow execution",
    WorkflowExecutionDeleteAll -> "Error deleting all workflow executions",
    WorkflowExecutionDeleteById -> "Error deleting workflow execution",
    WorkflowExecutionUpdate -> "Error updating workflow execution",
    WorkflowExecutionCreate -> "Error creating workflow execution",
    DriverServiceUnexpected -> "Unexpected behaviour in driver service",
    DriverServiceUpload -> "Error uploading driver",
    DriverServiceFindAll -> "Error obtaining all drivers",
    DriverServiceDeleteAll -> "Error deleting all drivers",
    DriverServiceDeleteByName -> "Error deleting driver by name",
    PluginsServiceUnexpected -> "Unexpected behaviour in plugins service",
    PluginsServiceUpload -> "Error uploading plugins",
    PluginsServiceFindAll -> "Error obtaining all plugins",
    PluginsServiceDeleteAll -> "Error deleting all plugins",
    PluginsServiceDeleteByName -> "Error deleting plugins by name",
    PluginsServiceDownload -> "Error downloading plugin",
    MetadataServiceUnexpected -> "Unexpected behaviour in metadata service",
    MetadataServiceBuildBackup -> "Error building backup",
    MetadataServiceExecuteBackup -> "Error executing backup",
    MetadataServiceUploadBackup -> "Error uploading backup",
    MetadataServiceFindAllBackups -> "Error obtaining all backups",
    MetadataServiceDeleteAllBackups -> "Error deleting all backups",
    MetadataServiceDeleteBackup -> "Error deleting backup",
    MetadataServiceCleanAll -> "Error cleaning all metadata",
    GlobalParametersServiceUnexpected -> "Unexpected behaviour in global parameters service",
    GlobalParametersServiceFindEnvironment -> "Error obtaining global parameters",
    GlobalParametersServiceCreateEnvironment -> "Error creating global parameters",
    GlobalParametersServiceDeleteEnvironment -> "Error deleting global parameters",
    GlobalParametersServiceFindEnvironmentVariable -> "Error obtaining global parameters variable",
    GlobalParametersServiceCreateEnvironmentVariable -> "Error creating global parameters variable",
    GlobalParametersServiceUpdateEnvironmentVariable -> "Error updating global parameters variable",
    GlobalParametersDeleteEnvironmentVariable -> "Error deleting global parameters variable",
    GroupServiceUnexpected -> "Unexpected behaviour in group service",
    GroupServiceFindGroup -> "Error obtaining group",
    GroupServiceCreateGroup -> "Error creating group",
    GroupServiceDeleteGroup -> "Error deleting group",
    GroupServiceFindAllGroups -> "Error obtaining all groups",
    GroupServiceDeleteAllGroups -> "Error deleting all groups",
    DebugWorkflowServiceUnexpected -> "Unexpected behaviour in Debug Workflow service",
    DebugWorkflowServiceFindById -> "Error finding debug workflow by ID",
    DebugWorkflowServiceFind -> "Error executing debug workflow query",
    DebugWorkflowServiceFindAll -> "Error obtaining all debug workflows",
    DebugWorkflowServiceCreate -> "Error creating debug workflow",
    DebugWorkflowServiceUpdate -> "Error updating debug workflow",
    DebugWorkflowServiceDeleteAll -> "Error deleting all debug workflows",
    DebugWorkflowServiceDeleteById -> "Error deleting debug workflows by ID",
    DebugWorkflowServiceRun -> "Error running debug workflow",
    DebugWorkflowServiceResultsFindByIdNotAvailable -> "Not found any debug result for that ID. Try again later",
    DebugWorkflowServiceResultsFindById -> "Error finding the debug results for that workflow ID",
    DebugWorkflowServiceUpload -> "Error while uploading a mock file",
    DebugWorkflowServiceDownload -> "Error while downloading a mock file",
    DebugWorkflowServiceDeleteFile -> "Error while deleting a mock file",
    WorkflowHistoryExecutionUnexpected -> "Unexpected behaviour in Workflow History Execution service",
    WorkflowHistoryExecutionFindByUserId -> "Error finding workflow History Execution by userId",
    WorkflowHistoryExecutionFindByWorkflowId -> "Error finding workflow History Execution by workflowId",
    WorkflowHistoryStatusUnexpected -> "Unexpected behaviour in Workflow Status History service",
    WorkflowHistoryStatusFindByWorkflowId -> "Error finding workflow status history by workflow ID",
    ParameterListServiceUnexpected -> "Unexpected behaviour in parameter list service",
    ParameterListServiceFindById -> "Error finding parameter list by ID",
    ParameterListServiceFindByName -> "Error finding parameter list by Name",
    ParameterListServiceFindAll -> "Error obtaining all parameter list",
    ParameterListServiceCreate -> "Error creating parameter list",
    ParameterListServiceUpdate -> "Error updating parameter list",
    ParameterListServiceDeleteAll -> "Error deleting all parameter lists",
    ParameterListServiceDeleteByName -> "Error deleting parameter list by Name",
    ParameterListServiceFindByParent -> "Error finding parameter lists by Parent",
    ParameterListServiceCreateFromWorkflow -> "Error creating parameter list from workflow",
    ParameterListServiceFindAllContexts -> "Error finding contexts by parameter group",
    ParameterListServiceFindEnvironment -> "Error finding environment",
    ParameterListServiceFindEnvironmentContexts -> "Error finding environment contexts",
    ParameterListServiceFindEnvironmentAndContexts -> "Error finding environment and their contexts",
    ParameterListServiceFindGroupAndContexts -> "Error finding group and their contexts",
    ParameterListServiceDeleteById -> "Error deleting parameter list by id",
    MlModelsServiceFindAll -> "Error finding all machine learning models"

  )

  def toString(errorModel: ErrorModel): String = write(errorModel)

  def toErrorModel(json: String): ErrorModel = read[ErrorModel](json)
}