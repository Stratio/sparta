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

package com.stratio.sparta.plugin.parser.xpath

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.Parser
import com.stratio.sparta.sdk.properties.PropertiesQueriesModel
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import kantan.xpath._
import kantan.xpath.ops._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

class XPathParser(order: Integer,
                  inputField: String,
                  outputFields: Seq[String],
                  schema: StructType,
                  properties: Map[String, JSerializable])
  extends Parser(order, inputField, outputFields, schema, properties) {

  val queriesModel = properties.getPropertiesQueries("queries")

  override def parse(row: Row, removeRaw: Boolean): Row = {
    val inputValue = Option(row.get(inputFieldIndex))
    val newData = {
      inputValue match {
        case Some(value) =>
          val valuesParsed = XPathParser.xPathParse(value.toString, queriesModel)

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

object XPathParser {

  def xPathParse(xmlData: String, queriesModel: PropertiesQueriesModel): Map[String, Any] =
    queriesModel.queries.map(queryModel => (queryModel.field, parse[String](xmlData, queryModel.query))).toMap

  /**
   * Receives a query and returns the elements found. If there was an error,
   * an exception will be thrown
   *
   * @param query search to apply
   * @tparam T type of the elements returned
   * @return elements found
   */
  private def parse[T: Compiler](source: String, query: String): T =
    applyQuery(source, query).get

  /**
   * Receives a query and returns an XPathResult with the try of apply
   * the query to the source value
   *
   * @param query search to apply
   * @tparam T type of the elements returned.
   * @return a XPathResult with the try of apply the query
   */
  private def applyQuery[T: Compiler](source: String, query: String): XPathResult[T] =
    source.evalXPath[T](query)
}
