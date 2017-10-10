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
import com.stratio.sparta.sdk.workflow.step.{ErrorChecking, OutputOptions, TransformStep}
import org.apache.spark.sql.json.RowJsonHelper.{toRow, extractSchemaFromJson}
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.catalyst.parser.LegacyTypeStringParser
import org.apache.spark.sql.types.{DataType, StructType}

import scala.util.{Failure, Try}

class JsonTransformStep(
                                 name: String,
                                 outputOptions: OutputOptions,
                                 ssc: StreamingContext,
                                 xDSession: XDSession,
                                 properties: Map[String, JSerializable]
                               ) extends TransformStep(name, outputOptions, ssc, xDSession, properties)
  with ErrorChecking with SLF4JLogging {


  val inputFieldName: String = properties.getString("inputField")
  val rowUpdatePolicy: String = properties.getString("fieldsPreservationPolicy", default = "REPLACE")

  val useRowSchema = properties.getBoolean("schema.fromRow", true)

  val schemaByExample = !useRowSchema &&
    properties.getString("schema.inputMode", None).map(_ == "EXAMPLE").getOrElse(false)

  val providedSchema = properties.getString("schema.provided", None) flatMap { schemaStr =>

    if(useRowSchema) None
    else {
      Try(extractSchemaFromJson(schemaStr, Map.empty)).filter(_ => schemaByExample) orElse
      Try { // Try to deserialize the schema assuming it is in JSON format
        DataType.fromJson(schemaStr)
      } orElse Try { // If it wasn't a JSON, try assuming it is an string serialization of `StructType`
        LegacyTypeStringParser.parse(schemaStr)
      } flatMap { schema =>
        Try(schema.asInstanceOf[StructType])
      } recoverWith {
        case e =>
          log.warn(s"Impossible to parse the schema: $schemaStr, the system infer it from each json event", e)
          Failure(e)
      } toOption
    }

  }

  def updateRow(source: Row, extracted: Row, inputFieldIdx: Int): Row =
    rowUpdatePolicy match {
      case "APPEND" =>
        val values = (source.toSeq ++ extracted.toSeq).toArray
        val schema = StructType(source.schema ++ extracted.schema)
        new GenericRowWithSchema(values , schema)

      case "REPLACE" =>
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
      returnSeqData {
        val inputSchema = row.schema
        val inputFieldIdx = inputSchema.indexWhere(_.name == inputFieldName)
        assert(inputFieldIdx > -1, s"$inputFieldName should be a field in the input row")

        val value = row(inputFieldIdx).asInstanceOf[String]

        val embeddedRowSchema = providedSchema getOrElse extractSchemaFromJson(value, Map.empty)
        val embeddedRow = toRow(value, Map.empty, embeddedRowSchema)

        updateRow(row, embeddedRow, inputFieldIdx)

      }
    }
  }


}
