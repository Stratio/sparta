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
package com.stratio.sparta.sdk.pipeline.aggregation.operator

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.pipeline.filter.Filter
import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

import scala.util.{Failure, Success, Try}

trait OperatorProcessMapAsNumber extends SLF4JLogging {

  self: Filter =>

  val inputSchema: StructType

  val inputField: Option[String]

  def processMap(inputFieldsValues: Row): Option[Number] =
    if (inputField.isDefined && inputSchema.fieldNames.contains(inputField.get))
      applyFilters(inputFieldsValues)
        .map(filteredFields => getNumberFromAny(filteredFields.get(inputField.get).get))
    else None

  /**
   * This method tries to cast a value to Number, if it's possible.
   *
   * Serializable -> String -> Number
   * Serializable -> Number
   *
   */
  def getNumberFromAny(value: Any): Number =
    Try(TypeOp.castingToSchemaType(TypeOp.Double, value).asInstanceOf[Number]) match {
      case Success(number) => number
      case Failure(ex) => log.info(s"Impossible to parse as double number inside operator: ${value.toString}")
        throw new Exception(ex.getMessage)
    }
}
