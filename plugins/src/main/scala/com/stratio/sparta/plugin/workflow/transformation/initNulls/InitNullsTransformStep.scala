/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.initNulls

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.helper.SchemaHelper.parserInputSchema
import com.stratio.sparta.plugin.models.{DefaultValueToColumn, DefaultValueToType}
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models._
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.TransformStep
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{DataType, StructField}
import org.apache.spark.streaming.StreamingContext
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

abstract class InitNullsTransformStep[Underlying[Row]](
                                                        name: String,
                                                        outputOptions: OutputOptions,
                                                        transformationStepsManagement: TransformationStepManagement,
                                                        ssc: Option[StreamingContext],
                                                        xDSession: XDSession,
                                                        properties: Map[String, JSerializable]
                                                      )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  lazy val defaultValueToColumn: Map[String, String] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val values = s"${properties.getString("defaultValueToColumn", None).notBlank.fold("[]") { values => values.toString }}"

    read[Seq[DefaultValueToColumn]](values)
  }.map(valueToColumn => valueToColumn.columnName -> valueToColumn.value).toMap
  lazy val defaultValueToType: Map[DataType, Any] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val values = s"${properties.getString("defaultValueToType", None).notBlank.fold("[]") { values => values.toString }}"

    read[Seq[DefaultValueToType]](values).map { valueToType =>
      val fieldType = SparkTypes.get(valueToType.`type`) match {
        case Some(sparkType) => sparkType
        case None => schemaFromString(valueToType.`type`)
      }
      val field = StructField(name = "dummy", dataType = fieldType, nullable = true)

      fieldType -> castingToOutputSchema(field, valueToType.value)
    }.toMap
  }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid.", name))

    if (Try(defaultValueToType.nonEmpty).isFailure) {
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"Column value and type do not match.", name))
    }

    if (Try(defaultValueToColumn.nonEmpty).isFailure) {
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The default values for columns are invalid.", name))
    }

    if (defaultValueToColumn.isEmpty && defaultValueToType.isEmpty) {
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The default values in columns and types cannot be empty.", name))
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
    val newValues = row.schema.map { outputField =>
      Try {
        val rowValue = row.get(inputSchema.fieldIndex(outputField.name))
        if (Option(rowValue).isEmpty) {
          defaultValueToColumn.get(outputField.name)
            .map(value => castingToOutputSchema(outputField, value))
            .orElse(defaultValueToType.get(outputField.dataType))
            .getOrElse(new Exception(s"Error generating default value:$rowValue with type ${outputField.dataType}"))
        } else rowValue
      } match {
        case Success(dataRow) =>
          dataRow
        case Failure(e: Exception) =>
          returnWhenFieldError(new Exception(
            s"Impossible to initialize null value in field: $outputField in the schema $inputSchema", e))
      }
    }
    new GenericRowWithSchema(newValues.toArray, inputSchema)
  }

}
