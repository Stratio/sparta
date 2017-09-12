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

package com.stratio.sparta.plugin.workflow.transformation.json

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.properties.models.PropertiesQueriesModel
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformStep}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.util.{Failure, Success, Try}

class JsonTransformStep(name: String,
                        outputOptions: OutputOptions,
                        ssc: StreamingContext,
                        xDSession: XDSession,
                        properties: Map[String, JSerializable])
  extends TransformStep(name, outputOptions, ssc, xDSession, properties) {

  lazy val queriesModel: PropertiesQueriesModel = properties.getPropertiesQueries("queries")
  lazy val supportNullValues: Boolean = Try(properties.getString("supportNullValues").toBoolean).getOrElse(true)
  lazy val inputField: String = Try(properties.getString("inputField"))
    .getOrElse(throw new IllegalArgumentException("The inputField is mandatory"))
  lazy val addAllInputFields: Boolean = Try(propertiesWithCustom.getBoolean("addAllInputFields")).getOrElse(true)

  assert(inputField.nonEmpty)

  def transformFunction(inputSchema: String, inputStream: DStream[Row]): DStream[Row] =
    inputStream.flatMap(data => parse(data, inputSchema))

  override def transform(inputData: Map[String, DStream[Row]]): DStream[Row] =
    applyHeadTransform(inputData)(transformFunction)

  //scalastyle:off
  def parse(row: Row, schemaName: String): Seq[Row] = {
    returnSeqData(Try {
      val inputSchema = row.schema
      getNewOutputSchema(inputSchema) match {
        case Some(outputSchema) =>
          val inputFieldIndex = inputSchema.fieldIndex(inputField)
          val inputValue = Option(row.get(inputFieldIndex))
          val newValues = inputValue match {
            case Some(value) =>
              if (value.toString.nonEmpty) {
                val valuesParsed = value match {
                  case valueCast: Array[Byte] =>
                    JsonTransformStep.jsonParse(new Predef.String(valueCast), queriesModel, supportNullValues)
                  case valueCast: String =>
                    JsonTransformStep.jsonParse(valueCast, queriesModel, supportNullValues)
                  case _ =>
                    JsonTransformStep.jsonParse(value.toString, queriesModel, supportNullValues)
                }
                outputSchema.map { outputField =>
                  valuesParsed.get(outputField.name) match {
                    case Some(valueParsed) if valueParsed != null | (valueParsed == null && supportNullValues) =>
                      castingToOutputSchema(outputField, valueParsed)
                    case _ =>
                      Try(row.get(inputSchema.fieldIndex(outputField.name))).getOrElse(returnWhenError(
                        new IllegalStateException(s"Impossible to parse outputField: $outputField in the schema")))
                  }
                }
              } else throw new IllegalStateException(s"The input value is empty")
            case None => throw new IllegalStateException(s"The input value is null")
          }
          new GenericRowWithSchema(newValues.toArray, outputSchema)
        case None => row
      }
    })
  }

  //scalastyle:on

  def getNewOutputSchema(inputSchema: StructType): Option[StructType] = {
    val outputFieldsSchema = queriesModel.queries.map { queryField =>
      StructField(
        name = queryField.field,
        dataType = SparkTypes.getOrElse(queryField.`type`.getOrElse("string").toLowerCase, StringType),
        nullable = true
      )
    }
    val inputFieldsSchema = if(addAllInputFields) inputSchema.fields.toSeq else Seq.empty[StructField]
    if (outputFieldsSchema.nonEmpty || inputFieldsSchema.nonEmpty) {
      Option(StructType(inputFieldsSchema ++ outputFieldsSchema))
    } else None
  }
}

object JsonTransformStep {

  def jsonParse(jsonData: String, queriesModel: PropertiesQueriesModel, isLeafToNull: Boolean): Map[String, Any] = {
    val jsonPathExtractor = new JsonPathExtractor(jsonData, isLeafToNull)

    queriesModel.queries.map(queryModel => (queryModel.field, jsonPathExtractor.query(queryModel.query))).toMap
  }
}
