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

package com.stratio.sparta.plugin.workflow.transformation.casting

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.workflow.transformation.casting.OutputFieldsFrom.OutputFieldsFrom
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformStep}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.util.{Failure, Success, Try}

class CastingTransformStep(name: String,
                           outputOptions: OutputOptions,
                           ssc: StreamingContext,
                           xDSession: XDSession,
                           properties: Map[String, JSerializable])
  extends TransformStep(name, outputOptions, ssc, xDSession, properties) {

  lazy val outputFieldsFrom = OutputFieldsFrom.withName(properties.getString("outputFieldsFrom", "FIELDS").toUpperCase)
  lazy val fieldsString = properties.getString("fieldsString", None).notBlank
  lazy val fieldsModel = properties.getPropertiesFields("fields")
  lazy val outputFieldsSchema: Option[StructType] = {
    outputFieldsFrom match {
      case OutputFieldsFrom.FIELDS =>
        if (fieldsModel.fields.nonEmpty) {
          Option(StructType(fieldsModel.fields.map { outputField =>
            val outputType = outputField.`type`.notBlank.getOrElse("string")
            StructField(
              name = outputField.name,
              dataType = SparkTypes.get(outputType) match {
                case Some(sparkType) => sparkType
                case None => schemaFromString(outputType)
              },
              nullable = outputField.nullable.getOrElse(true)
            )
          }))
        } else None
      case OutputFieldsFrom.STRING =>
        Try(schemaFromString(fieldsString.get).asInstanceOf[StructType]).toOption
      case _ =>
        throw new IllegalArgumentException("It's mandatory to specify the fields format")
    }
  }

  def transformFunction(inputSchema: String, inputStream: DStream[Row]): DStream[Row] =
    castingFields(inputStream)

  override def transform(inputData: Map[String, DStream[Row]]): DStream[Row] =
    applyHeadTransform(inputData)(transformFunction)

  /**
    * Compare input schema and output schema and apply the casting function if it's necessary.
    *
    * @param streamData The stream data to casting
    * @return The casted stream data
    */
  def castingFields(streamData: DStream[Row]): DStream[Row] =
    streamData.flatMap { row =>
      returnSeqData(Try {
        val inputSchema = row.schema
        (compareToOutputSchema(row.schema), outputFieldsSchema) match {
          case (false, Some(outputSchema)) =>
            val newValues = outputSchema.map { outputField =>
              Try {
                inputSchema.find(_.name == outputField.name)
                  .getOrElse(throw new IllegalStateException(
                    s"Output field: ${outputField.name} not found in the schema: $inputSchema"))
                  .dataType
              } match {
                case Success(inputSchemaType) =>
                  Try {
                    val rowValue = row.get(inputSchema.fieldIndex(outputField.name))
                    if (inputSchemaType == outputField.dataType)
                      rowValue
                    else castingToOutputSchema(outputField, rowValue)
                  } match {
                    case Success(dataRow) =>
                      dataRow
                    case Failure(e) =>
                      returnWhenError(new IllegalStateException(
                        s"Impossible to find outputField: $outputField in the schema $inputSchema", e))
                  }
                case Failure(e: Exception) =>
                  returnWhenError(e)
              }
            }
            new GenericRowWithSchema(newValues.toArray, outputSchema)
          case _ => row
        }
      })
    }

  /**
    * Compare schema fields: InputSchema with outputSchema.
    *
    * @param inputSchema The input schema to compare
    * @return If the schemas are equals
    */
  def compareToOutputSchema(inputSchema: StructType): Boolean =
    outputFieldsSchema.isEmpty || (outputFieldsSchema.isDefined && inputSchema == outputFieldsSchema.get)


}