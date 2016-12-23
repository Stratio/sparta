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

package com.stratio.sparta.plugin.transformation.filter

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.pipeline.filter.Filter
import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import com.stratio.sparta.sdk.pipeline.schema.TypeOp._
import com.stratio.sparta.sdk.pipeline.transformation.Parser
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

class FilterParser(order: Integer,
                   inputField: String,
                   outputFields: Seq[String],
                   val schema: StructType,
                   properties: Map[String, JSerializable])
  extends Parser(order, inputField, outputFields, schema, properties) with Filter {

  def filterInput: Option[String] = properties.getString("filters", None)

  def defaultCastingFilterType: TypeOp = TypeOp.Any

  override def parse(row: Row, removeRaw: Boolean): Option[Row] = {
    applyFilters(row).map { case valuesFiltered =>

      val newData = outputFields.map { outputField =>
        val outputSchemaValid = outputFieldsSchema.find(field => field.name == outputField)
        outputSchemaValid match {
          case Some(outSchema) =>
            valuesFiltered.get(outSchema.name) match {
              case Some(valueParsed) =>
                parseToOutputType(outSchema, valueParsed)
              case None =>
                returnNullValue(new IllegalStateException(
                  s"The values parsed not have the schema field: ${outSchema.name}"))
            }
          case None =>
            returnNullValue(new IllegalStateException(
              s"Impossible to parse outputField: $outputField in the schema"))
        }
      }
      val prevData = if (removeRaw) row.toSeq.drop(1) else row.toSeq

      Row.fromSeq(prevData ++ newData)
    }
  }
}
