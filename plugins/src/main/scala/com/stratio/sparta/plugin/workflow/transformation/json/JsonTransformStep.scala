/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.json

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.enumerations.{FieldsPreservationPolicy, SchemaInputMode}
import com.stratio.sparta.plugin.helper.SchemaHelper
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step._
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.json.RowJsonHelper.{extractSchemaFromJson, toRow}
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

abstract class JsonTransformStep[Underlying[Row]](
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

  lazy val useRowSchema: Boolean = properties.getBoolean("schema.fromRow", true)

  lazy val schemaInputMode: SchemaInputMode.Value = SchemaInputMode.withName(
    properties.getString("schema.inputMode", "SPARKFORMAT").toUpperCase)

  lazy val schemaProvided: Option[String] = properties.getString("schema.provided", None)

  lazy val jsonSchema: Option[StructType] =
    SchemaHelper.getJsonSparkSchema(useRowSchema, schemaInputMode, schemaProvided)


  override def transform(inputData: Map[String, DistributedMonad[Underlying]]): DistributedMonad[Underlying] =
    applyHeadTransform(inputData) { (_, inputStream) =>
      inputStream flatMap { row =>
        returnSeqDataFromRow {
          val inputSchema = row.schema
          val inputFieldIdx = inputSchema.indexWhere(_.name == inputFieldName)
          assert(inputFieldIdx > -1, s"$inputFieldName should be a field in the input row")

          val value = row(inputFieldIdx).asInstanceOf[String]

          val embeddedRowSchema = jsonSchema getOrElse extractSchemaFromJson(value, Map.empty)
          val embeddedRow = toRow(value, Map.empty, embeddedRowSchema)

          updateRow(row, embeddedRow, inputFieldIdx, preservationPolicy)
        }
      }
    }
}