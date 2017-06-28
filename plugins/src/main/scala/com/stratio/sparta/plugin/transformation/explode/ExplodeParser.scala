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

package com.stratio.sparta.plugin.transformation.explode

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.pipeline.transformation.Parser
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

import scala.util.Try

class ExplodeParser(order: Integer,
                    inputField: Option[String],
                    outputFields: Seq[String],
                    schema: StructType,
                    properties: Map[String, JSerializable])
  extends Parser(order, inputField, outputFields, schema, properties) {

  override def parse(row: Row): Seq[Row] = {
    val inputValue = Option(row.get(inputFieldIndex))
    inputValue match {
      case Some(value) =>
        val valueTyped = {
          value match {
            case valueCast: Seq[Map[String, _]] => valueCast
            case valueCast: Map[String, _] => Seq(valueCast)
            case _ => throw new RuntimeException(
              s"The input value have incorrect type, Seq(Map()) or Map(). Value:${value.toString}")
          }
        }
        valueTyped.flatMap { valuesMap =>
          val newValues = Try {
            outputFields.map { outputField =>
              val outputSchemaValid = outputFieldsSchema.find(field => field.name == outputField)
              outputSchemaValid match {
                case Some(outSchema) =>
                  valuesMap.get(outSchema.name.replace(s"${inputField.get}_", "")) match {
                    case Some(valueParsed) =>
                      parseToOutputType(outSchema, valueParsed)
                    case None =>
                      returnWhenError(new IllegalStateException(
                        s"The values parsed don't contain the schema field: ${outSchema.name}"))
                  }
                case None =>
                  returnWhenError(new IllegalStateException(
                    s"Impossible to parse outputField: $outputField in the schema"))
              }
            }
          }
          returnData(newValues, removeInputField(row))
        }
      case None =>
        returnWhenError(new IllegalStateException(s"The input value is null or empty"))
    }
  }
}
