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

package com.stratio.sparta.plugin.workflow.transformation.cube.sdk

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.workflow.enumerators.WhenError.WhenError
import com.stratio.sparta.sdk.workflow.step.ErrorCheckingOption
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.DataType

import scala.util.Try

abstract class Operator(
                         val name: String,
                         whenErrorDo: WhenError,
                         inputField: Option[String]
                       ) extends SLF4JLogging with Serializable with ErrorCheckingOption {

  val defaultOutputType : DataType

  def processMap(inputRow: Row): Option[Any]

  def processReduce(values: Iterable[Option[Any]]): Option[Any]

  val isAssociative: Boolean = false // `Associative` trait will override it to `true`

  def processMapFromInputField(inputRow: Row): Option[Any] =
    inputField.flatMap { field =>
      returnFromTry(s"Error in process map with operator: $name, inputRow: $inputRow and field: $field") {
        Try(inputRow.get(inputRow.schema.fieldIndex(field)))
      }
    }

  def extractValues(values: Iterable[(String, Option[Any])], filterKey: Option[String]): Iterable[Any] =
    values.flatMap { case (key, value) =>
      filterKey match {
        case Some(filter) => if (key == filter) value else None
        case None => value
      }
    }
}

object Operator {

  final val ClassSuffix = "Operator"
  final val OldValuesKey = "old"
  final val NewValuesKey = "new"

}