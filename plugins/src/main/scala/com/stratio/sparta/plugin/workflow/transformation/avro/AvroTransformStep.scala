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

package com.stratio.sparta.plugin.workflow.transformation.avro

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.databricks.spark.avro.RowAvroHelper
import com.stratio.sparta.plugin.enumerations.FieldsPreservationPolicy
import com.stratio.sparta.plugin.helper.SchemaHelper
import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{ErrorCheckingStepRow, OutputOptions, TransformStep}
import com.twitter.bijection.avro.GenericAvroCodecs
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.{GenericRow, GenericRowWithSchema}
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

abstract class AvroTransformStep[Underlying[Row]](
                         name: String,
                         outputOptions: OutputOptions,
                         ssc: Option[StreamingContext],
                         xDSession: XDSession,
                         properties: Map[String, JSerializable]
                       )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, ssc, xDSession, properties)
  with ErrorCheckingStepRow with SLF4JLogging {

  lazy val inputFieldName: String = properties.getString("inputField")

  lazy val preservationPolicy: FieldsPreservationPolicy.Value = FieldsPreservationPolicy.withName(
    properties.getString("fieldsPreservationPolicy", "REPLACE").toUpperCase)

  lazy val schemaProvided: String = properties.getString("schema.provided")

  override def transform(inputData: Map[String, DistributedMonad[Underlying]]): DistributedMonad[Underlying] =
    applyHeadTransform(inputData) { (inputSchema, inputStream) =>
      inputStream flatMap { row =>
        returnSeqDataFromRow {
          val inputSchema = row.schema
          val inputFieldIdx = inputSchema.indexWhere(_.name == inputFieldName)

          assert(inputFieldIdx > -1, s"$inputFieldName should be a field in the input row")

          val avroSchema = AvroTransformStep.getAvroSchema(schemaProvided)
          val expectedSchema = AvroTransformStep.getExpectedSchema(avroSchema)
          val converter = RowAvroHelper.getAvroConverter(avroSchema, expectedSchema)
          val value = row(inputFieldIdx).asInstanceOf[String].getBytes()
          val recordInjection = GenericAvroCodecs.toBinary[GenericRecord](avroSchema)
          val record = recordInjection.invert(value).get
          val safeDataRow = converter(record).asInstanceOf[GenericRow]
          val newRow = new GenericRowWithSchema(safeDataRow.toSeq.toArray, expectedSchema)

          updateRow(row, newRow, inputFieldIdx, preservationPolicy)
        }
      }
    }

}

object AvroTransformStep {

  private var schema: Option[Schema] = None
  private var expectedSchema: Option[StructType] = None

  def getAvroSchema(schemaProvided: String): Schema =
    schema.getOrElse {
      val newSchema = SchemaHelper.getAvroSchemaFromString(schemaProvided)
      schema = Option(newSchema)
      newSchema
    }

  def getExpectedSchema(avroSchema: Schema): StructType =
    expectedSchema.getOrElse {
      val newSchema = SchemaHelper.getSparkSchemaFromAvroSchema(avroSchema)
      expectedSchema = Option(newSchema)
      newSchema
    }

}
