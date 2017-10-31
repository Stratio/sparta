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

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.enumerations.FieldsPreservationPolicy._
import com.stratio.sparta.plugin.enumerations.{FieldsPreservationPolicy, SchemaInputMode}
import com.stratio.sparta.plugin.helper.JsonHelper
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{ErrorCheckingStepRow, OutputOptions, TransformStep}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.json.RowJsonHelper.{extractSchemaFromJson, toRow}
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

class JsonTransformStep(
                         name: String,
                         outputOptions: OutputOptions,
                         ssc: StreamingContext,
                         xDSession: XDSession,
                         properties: Map[String, JSerializable]
                       ) extends TransformStep(name, outputOptions, ssc, xDSession, properties)
  with ErrorCheckingStepRow with SLF4JLogging {

  lazy val inputFieldName: String = properties.getString("inputField")

  lazy val preservationPolicy: FieldsPreservationPolicy.Value = FieldsPreservationPolicy.withName(
    properties.getString("fieldsPreservationPolicy", "REPLACE").toUpperCase)

  lazy val useRowSchema: Boolean = properties.getBoolean("schema.fromRow", true)

  lazy val schemaInputMode: SchemaInputMode.Value = SchemaInputMode.withName(
    properties.getString("schema.inputMode", "SPARKFORMAT").toUpperCase)

  lazy val schemaProvided: Option[String] = properties.getString("schema.provided", None)

  lazy val jsonSchema: Option[StructType] = JsonHelper.getJsonSchema(useRowSchema, schemaInputMode, schemaProvided)

  def updateRow(source: Row, extracted: Row, inputFieldIdx: Int): Row =
    preservationPolicy match {
      case APPEND =>
        val values = (source.toSeq ++ extracted.toSeq).toArray
        val schema = StructType(source.schema ++ extracted.schema)
        new GenericRowWithSchema(values, schema)

      case REPLACE =>
        val (leftInputFields, rightInputFields) = source.schema.fields.splitAt(inputFieldIdx)
        val (leftValues, rightValues) = source.toSeq.toArray.splitAt(inputFieldIdx)

        val outputFields = leftInputFields ++ extracted.schema.fields ++ rightInputFields.tail
        val outputValues = leftValues ++ extracted.toSeq.toArray[Any] ++ rightValues.tail

        new GenericRowWithSchema(outputValues, StructType(outputFields))

      case _ =>
        extracted
    }

  override def transform(inputData: Map[String, DStream[Row]]): DStream[Row] =
    applyHeadTransform(inputData)(transformFunction)

  def transformFunction(inputSchema: String, inputStream: DStream[Row]): DStream[Row] = {
    inputStream flatMap { row =>
      returnSeqDataFromRow {
        val inputSchema = row.schema
        val inputFieldIdx = inputSchema.indexWhere(_.name == inputFieldName)
        assert(inputFieldIdx > -1, s"$inputFieldName should be a field in the input row")

        val value = row(inputFieldIdx).asInstanceOf[String]

        val embeddedRowSchema = jsonSchema getOrElse extractSchemaFromJson(value, Map.empty)
        val embeddedRow = toRow(value, Map.empty, embeddedRowSchema)

        updateRow(row, embeddedRow, inputFieldIdx)

      }
    }
  }


}
