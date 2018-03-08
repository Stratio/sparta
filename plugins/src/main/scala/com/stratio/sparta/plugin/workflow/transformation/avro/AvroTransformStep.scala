/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
import com.stratio.sparta.sdk.workflow.step._
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
                                                   transformationStepsManagement: TransformationStepManagement,
                                                   ssc: Option[StreamingContext],
                                                   xDSession: XDSession,
                                                   properties: Map[String, JSerializable]
                                                 )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

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
