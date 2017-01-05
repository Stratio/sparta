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

package com.stratio.sparta.plugin.transformation.json

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.pipeline.transformation.Parser
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.properties.models.PropertiesQueriesModel
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

class JsonParser(order: Integer,
                 inputField: Option[String],
                 outputFields: Seq[String],
                 schema: StructType,
                 properties: Map[String, JSerializable])
  extends Parser(order, inputField, outputFields, schema, properties) {

  val queriesModel = properties.getPropertiesQueries("queries")

  //scalastyle:off
  override def parse(row: Row, removeRaw: Boolean): Option[Row] = {
    val inputValue = Option(row.get(inputFieldIndex))
    val newData = {
      inputValue match {
        case Some(value) =>
          val valuesParsed = value match {
          case valueCast: Array[Byte] => JsonParser.jsonParse(new Predef.String(valueCast), queriesModel)
          case valueCast: String => JsonParser.jsonParse(valueCast, queriesModel)
          case _ => JsonParser.jsonParse(value.toString, queriesModel)
        }

          outputFields.map { outputField =>
            val outputSchemaValid = outputFieldsSchema.find(field => field.name == outputField)
            outputSchemaValid match {
              case Some(outSchema) =>
                valuesParsed.get(outSchema.name) match {
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
        case None =>
          returnNullValue(new IllegalStateException(s"The input value is null or empty"))
      }
    }
    val prevData = if (removeRaw) row.toSeq.drop(1) else row.toSeq

    Option(Row.fromSeq(prevData ++ newData))
  }

  //scalastyle:on
}

object JsonParser {

  def jsonParse(jsonData: String, queriesModel: PropertiesQueriesModel): Map[String, Any] = {
    val jsonPathExtractor = new JsonPathExtractor(jsonData)

    queriesModel.queries.map(queryModel => (queryModel.field, jsonPathExtractor.query(queryModel.query))).toMap
  }
}
