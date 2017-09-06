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

/**
 * An ErrorDto represents an error that will be sent as response to the frontend.
 *
 * @param i18nCode          with the code of the error that will be translated.
 * @param message           that describes the exception.
 * @param stackTraceElement with the stackTrace of the exception.
 * @param params            with values that could be needed by the frontend.
 */
case class ErrorModel(i18nCode: String,
                      message: String,
                      subErrorModels: Option[Seq[ErrorModel]] = None,
                      stackTraceElement: Option[Seq[StackTraceElement]] = None,
                      params: Option[Map[Any, Any]] = None) {}

object ErrorModel extends SpartaSerializer {

  val CodeExistsTemplateWithName = "100"
  val CodeNotExistsTemplateWithId = "101"
  val CodeErrorDeletingAllTemplates = "105"
  val CodeExistsWorkflowWithName = "200"
  val CodeNotExistsWorkflowWithId = "201"
  val CodeNotExistsWorkflowWithName = "202"
  val CodeErrorCreatingWorkflow = "203"
  val CodeErrorDeletingWorkflow = "204"
  val CodeErrorUpdatingWorkflow = "205"
  val CodeErrorUpdatingExecution = "206"
  val UnauthorizedAction = "403"
  val UserNotFound = "501"
  val CodeUnknown = "666"
  val CrossdataService = 550
  StatusCodes.registerCustom(ErrorModel.CrossdataService, "Errors in CrossData Context")


  def toString(errorModel: ErrorModel): String = write(errorModel)

  def toErrorModel(json: String): ErrorModel = read[ErrorModel](json)
}