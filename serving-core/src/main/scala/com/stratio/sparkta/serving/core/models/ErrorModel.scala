/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.serving.core.models

import org.json4s.DefaultFormats
import org.json4s.native.Serialization._

/**
 * An ErrorDto represents an error that will be sent as response to the frontend.
 * @param i18nCode with the code of the error that will be translated.
 * @param message that describes the exception.
 * @param stackTraceElement with the stackTrace of the exception.
 * @param params with values that could be needed by the frontend.
 */
case class ErrorModel(i18nCode: String,
                    message: String,
                    stackTraceElement: Option[Seq[StackTraceElement]] = None,
                    params: Option[Map[Any,Any]]= None) {

}

object ErrorModel extends SparktaSerializer{

  val CodeExistsFragmentWithName      = "100"
  val CodeNotExistsFragmentWithId     = "101"
  val CodeNotExistsFragmentWithName   = "102"
  val CodeExistsPolicyWithName        = "200"
  val CodeNotExistsPolicyWithId       = "201"
  val CodeNotExistsPolicyWithName     = "202"
  val CodeErrorCreatingPolicy         = "203"
  val CodeNotExistsTemplateWithName   = "300"
  val CodePolicyIsWrong               = "305"
  val CodeUnknown                     = "666"

  def toString(errorModel: ErrorModel): String = {
    write(errorModel)
  }

  def toErrorModel(json: String): ErrorModel = {
    read[ErrorModel](json)
  }
}