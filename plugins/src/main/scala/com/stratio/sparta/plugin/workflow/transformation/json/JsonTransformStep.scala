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
import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step._
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.json.RowJsonHelper.{extractSchemaFromJson, toRow}
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext

import scala.util.Try

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

  lazy val inputField = properties.getString("inputField", None)
  lazy val preservationPolicy: FieldsPreservationPolicy.Value = FieldsPreservationPolicy.withName(
    properties.getString("fieldsPreservationPolicy", "REPLACE").toUpperCase)
  lazy val useRowSchema: Boolean = properties.getBoolean("schema.fromRow", true)
  lazy val schemaInputMode: SchemaInputMode.Value = SchemaInputMode.withName(
    properties.getString("schema.inputMode", "SPARKFORMAT").toUpperCase)
  lazy val schemaProvided: Option[String] = properties.getString("schema.provided", None)
  lazy val jsonSchema: Option[StructType] =
    SchemaHelper.getJsonSparkSchema(useRowSchema, schemaInputMode, schemaProvided)

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid.", name))

    if (inputField.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The input field cannot be empty.", name))

    if (!useRowSchema && jsonSchema.isEmpty) {
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The output schema cannot be generated.", name))
    }

    //If contains schemas, validate if it can be parsed
    if (inputsModel.inputSchemas.nonEmpty) {
      inputsModel.inputSchemas.foreach { input =>
        if (parserInputSchema(input.schema).isFailure)
          validation = ErrorValidations(
            valid = false,
            messages = validation.messages :+ WorkflowValidationMessage(s"The input schema from step ${input.stepName} is not valid.", name))
      }

      inputsModel.inputSchemas.filterNot(is => SdkSchemaHelper.isCorrectTableName(is.stepName)).foreach { is =>
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ WorkflowValidationMessage(s"The input table name ${is.stepName} is not valid.", name))
      }
    }

    validation
  }

  def generateNewRow(row: Row): Row = {
    val inputSchema = row.schema
    val inputFieldName = inputField.get
    val inputFieldIdx = inputSchema.indexWhere(_.name == inputFieldName)

    assert(inputFieldIdx > -1, s"$inputFieldName should be a field in the input row with schema: $inputSchema")

    val value = row(inputFieldIdx).asInstanceOf[String]

    val embeddedRowSchema = jsonSchema getOrElse extractSchemaFromJson(value, Map.empty)
    val embeddedRow = toRow(value, Map.empty, embeddedRowSchema)

    updateRow(row, embeddedRow, inputFieldIdx, preservationPolicy)
  }
}