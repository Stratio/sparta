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

  /* Unkown error */
  val UnknownErrorCode = "560"

  /* App Info Service */
  val AppInfo = "561"

  /* App Info Service */
  val AppStatus = "562"

  /* Authorization Service 550-559 */
  val UserNotFound = "550"

  /* Template Service 650-699 */
  val TemplateServiceNotFound = "650"

  /* Crossdata Service 600-649 */
  val CrossdataServiceUnexpected = "600"
  val CrossdataServiceListDatabases = "601"
  val CrossdataServiceListTables = "602"
  val CrossdataServiceListColumns = "603"
  val CrossdataServiceExecuteQuery = "604"

  /* Map with all error codes and messages */
  val ErrorCodesMessages = Map(
    UnknownErrorCode -> UnknownError,
    StatusCodes.Unauthorized.toString() -> "Unauthorized action",
    UserNotFound -> "User not found",
    CrossdataServiceUnexpected -> "Unexpected behaviour in Crossdata catalog",
    CrossdataServiceListDatabases -> "Impossible to list databases in Crossdata Context",
    CrossdataServiceListTables -> "Impossible to list tables in Crossdata Context",
    CrossdataServiceListColumns -> "Impossible to list columns in Crossdata Context",
    TemplateServiceNotFound -> "No templates found",
    AppInfo -> "Impossible to extract server information",
    AppStatus -> "Zookeeper is not connected"
  )

  def toString(errorModel: ErrorModel): String = write(errorModel)

  def toErrorModel(json: String): ErrorModel = read[ErrorModel](json)
}