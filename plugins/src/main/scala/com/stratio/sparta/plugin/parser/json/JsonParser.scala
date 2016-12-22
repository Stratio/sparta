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

package com.stratio.sparta.plugin.parser.json

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.parser.json.models.{JsonQueriesModel, JsonQueryModel}
import com.stratio.sparta.sdk.{JsoneyStringSerializer, Parser}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.json4s._
import org.json4s.jackson.Serialization._

class JsonParser(order: Integer,
                 inputField: String,
                 outputFields: Seq[String],
                 schema: StructType,
                 properties: Map[String, JSerializable])
  extends Parser(order, inputField, outputFields, schema, properties) {

  override def parse(row: Row, removeRaw: Boolean): Row = {
    val inputValue = Option(row.get(inputFieldIndex))
    val newData = {
      inputValue match {
        case Some(value) =>
          val valuesParsed = JsonParser.jsonParse(value.toString, properties)

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

    Row.fromSeq(prevData ++ newData)
  }
}

object JsonParser {

  implicit val json4sJacksonFormats: Formats =
    DefaultFormats +
      new JsoneyStringSerializer()

  var queriesByField: Option[JsonQueriesModel] = None

  def jsonParse(jsonData: String, properties: Map[String, JSerializable]): Map[String, Any] = {
    val jsonPathExtractor = new JsonPathExtractor(jsonData)

    queriesByField.getOrElse {
      queriesByField = Option(read[JsonQueriesModel](
        s"""{"queries": ${properties.get("queries").fold("[]") { values => values.toString }}}""""
      ))
      queriesByField.get
    }.queries.map(queryModel => (queryModel.field, jsonPathExtractor.query(queryModel.query))).toMap
  }
}
