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

  /* Crossdata Service 600-624 */
  val CrossdataServiceUnexpected = "600"
  val CrossdataServiceListDatabases = "601"
  val CrossdataServiceListTables = "602"
  val CrossdataServiceListColumns = "603"
  val CrossdataServiceExecuteQuery = "604"

  /* Workflow Status 625-649 */
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


  /* Workflow Service 700-749 */
  val WorkflowServiceUnexpected = "700"
  val WorkflowServiceFindById = "701"
  val WorkflowServiceFindByName = "702"
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
  val WorkflowServiceDownload = "714"


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
    WorkflowServiceFindByName -> "Error finding workflow by Name",
    WorkflowServiceFindByIds -> "Error finding workflows by ID's",
    WorkflowServiceFindAll -> "Error obtaining all workflows",
    WorkflowServiceCreate -> "Error creating workflow",
    WorkflowServiceCreateList -> "Error creating workflows",
    WorkflowServiceUpdate -> "Error updating workflow",
    WorkflowServiceUpdateList -> "Error updating workflows",
    WorkflowServiceDeleteAll -> "Error deleting all workflows",
    WorkflowServiceDeleteList -> "Error deleting workflows",
    WorkflowServiceDeleteById -> "Error deleting workflows by ID's",
    WorkflowServiceDeleteCheckpoint -> "Error deleting checkpoint",
    WorkflowServiceRun -> "Error running workflow",
    WorkflowServiceDownload -> "Error downloading workflow",
    WorkflowStatusUnexpected -> "Unexpected behaviour in Workflow status service",
    WorkflowStatusFindAll -> "Error obtaining all workflow statuses",
    WorkflowStatusFindById -> "Error obtaining workflow status",
    WorkflowStatusDeleteAll -> "Error deleting all workflow statuses",
    WorkflowStatusDeleteById -> "Error deleting workflow status",
    WorkflowStatusUpdate -> "Error updating workflow status",
    TemplateServiceUnexpected -> "Unexpected behaviour in templates service",
    TemplateServiceFindByTypeId -> "Error obtaining template by id",
    TemplateServiceFindByTypeName -> "Error obtaining template by name",
    TemplateServiceFindAllByType -> "Error obtaining templates by type",
    TemplateServiceFindAll -> "Error obtaining templates",
    TemplateServiceCreate -> "Error creating template",
    TemplateServiceUpdate -> "Error updating template",
    TemplateServiceDeleteByTypeId -> "Error deleting template by id",
    TemplateServiceDeleteByTypeName -> "Error deleting template by name",
    TemplateServiceDeleteByType -> "Error deleting templates by type",
    TemplateServiceDeleteAll -> "Error deleting all templates"
  )

  def toString(errorModel: ErrorModel): String = write(errorModel)

  def toErrorModel(json: String): ErrorModel = read[ErrorModel](json)
}