/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

  /* Workflow Service 700-724 */
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
  val WorkflowServiceFindByIdWithEnv = "719"
  val WorkflowServiceFindByNameWithEnv = "720"
  val WorkflowServiceFindAllWithEnv = "721"
  val WorkflowServiceNewVersion = "722"

  /* Metadata Service 725-749 */
  val MetadataServiceUnexpected = "725"
  val MetadataServiceBuildBackup = "726"
  val MetadataServiceExecuteBackup = "727"
  val MetadataServiceUploadBackup = "728"
  val MetadataServiceFindAllBackups = "729"
  val MetadataServiceDeleteAllBackups = "730"
  val MetadataServiceDeleteBackup = "731"
  val MetadataServiceCleanAll = "732"

  /* Environment Service 750-760 */
  val EnvironmentServiceUnexpected = "750"
  val EnvironmentServiceFindEnvironment = "751"
  val EnvironmentServiceCreateEnvironment = "752"
  val EnvironmentServiceDeleteEnvironment = "753"
  val EnvironmentServiceUpdateEnvironment = "754"
  val EnvironmentServiceFindEnvironmentVariable = "755"
  val EnvironmentServiceCreateEnvironmentVariable = "756"
  val EnvironmentServiceUpdateEnvironmentVariable = "757"
  val EnvironmentServiceDeleteEnvironmentVariable = "758"
  val EnvironmentServiceExportData = "759"
  val EnvironmentServiceImportData = "760"

  /* Group Service 750-760 */
  val GroupServiceUnexpected = "770"
  val GroupServiceFindGroup = "771"
  val GroupServiceFindAllGroups = "772"
  val GroupServiceCreateGroup = "773"
  val GroupServiceDeleteGroup = "774"
  val GroupServiceDeleteAllGroups = "775"
  val GroupServiceUpdateGroup = "776"

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
    WorkflowServiceFindByIdWithEnv -> "Error finding workflows by ID with environment",
    WorkflowServiceFindByNameWithEnv -> "Error finding workflows by name with environment",
    WorkflowServiceFindAllWithEnv -> "Error finding all workflows with environment",
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
    EnvironmentServiceUnexpected -> "Unexpected behaviour in environment service",
    EnvironmentServiceFindEnvironment -> "Error obtaining environment",
    EnvironmentServiceCreateEnvironment -> "Error creating environment",
    EnvironmentServiceDeleteEnvironment -> "Error deleting environment",
    EnvironmentServiceFindEnvironmentVariable -> "Error obtaining environment variable",
    EnvironmentServiceCreateEnvironmentVariable -> "Error creating environment variable",
    EnvironmentServiceUpdateEnvironmentVariable -> "Error updating environment variable",
    EnvironmentServiceDeleteEnvironmentVariable -> "Error deleting environment variable",
    EnvironmentServiceExportData -> "Error exporting environment data",
    EnvironmentServiceImportData -> "Error importing environment data",
    GroupServiceUnexpected -> "Unexpected behaviour in group service",
    GroupServiceFindGroup -> "Error obtaining group",
    GroupServiceCreateGroup -> "Error creating group",
    GroupServiceDeleteGroup -> "Error deleting group",
    GroupServiceFindAllGroups -> "Error obtaining all groups",
    GroupServiceDeleteAllGroups -> "Error deleting all groups"
  )

  def toString(errorModel: ErrorModel): String = write(errorModel)

  def toErrorModel(json: String): ErrorModel = read[ErrorModel](json)
}