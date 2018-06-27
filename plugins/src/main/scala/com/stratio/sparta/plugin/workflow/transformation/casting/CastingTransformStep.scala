/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.casting

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.enumerations.OutputFieldsFrom
import com.stratio.sparta.plugin.helper.SchemaHelper
import com.stratio.sparta.plugin.helper.SchemaHelper.parserInputSchema
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, TransformationStepManagement, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step._
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.streaming.StreamingContext

import scala.util.{Failure, Success, Try}

abstract class CastingTransformStep[Underlying[Row]](
                                                      name: String,
                                                      outputOptions: OutputOptions,
                                                      transformationStepsManagement: TransformationStepManagement,
                                                      ssc: Option[StreamingContext],
                                                      xDSession: XDSession,
                                                      properties: Map[String, JSerializable]
                                                    )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  lazy val outputFieldsFrom = OutputFieldsFrom.withName(properties.getString("outputFieldsFrom", "FIELDS").toUpperCase)
  lazy val fieldsString = properties.getString("fieldsString", None).notBlank
  lazy val fieldsModel = properties.getPropertiesFields("fields")
  lazy val outputFieldsSchema: StructType = {
    outputFieldsFrom match {
      case OutputFieldsFrom.FIELDS =>
        if (fieldsModel.fields.nonEmpty) {
          StructType(fieldsModel.fields.map { outputField =>
            val outputType = outputField.`type`.notBlank.getOrElse("string")
            StructField(
              name = outputField.name,
              dataType = SparkTypes.get(outputType) match {
                case Some(sparkType) => sparkType
                case None => schemaFromString(outputType)
              },
              nullable = outputField.nullable.getOrElse(true)
            )
          })
        } else throw new Exception("The input fields cannot be empty.")
      case OutputFieldsFrom.STRING =>
        SchemaHelper.parserInputSchema(fieldsString.get).get
      case _ =>
        throw new Exception("It's mandatory to specify the fields format.")
    }
  }

  //scalastyle:off
  def generateNewRow(inputRow: Row): Row = {
    if (!compareToOutputSchema(inputRow.schema)) {
      val inputSchema = inputRow.schema
      val newValues = outputFieldsSchema.map { outputField =>
        Try {
          inputSchema.find(_.name == outputField.name)
            .getOrElse(throw new Exception(s"Output field: ${outputField.name} not found in the schema: $inputSchema"))
            .dataType
        } match {
          case Success(inputSchemaType) =>
            Try {
              val rowValue = inputRow.get(inputSchema.fieldIndex(outputField.name))
              if (inputSchemaType == outputField.dataType)
                rowValue
              else castingToOutputSchema(outputField, rowValue)
            } match {
              case Success(dataRow) => dataRow
              case Failure(e) => returnWhenFieldError(new Exception(s"Impossible to cast outputField: $outputField in the schema $inputSchema", e))
            }
          case Failure(e: Exception) =>
            returnWhenFieldError(e)
        }
      }
      new GenericRowWithSchema(newValues.toArray, outputFieldsSchema)
    } else inputRow
  }


  /**
    * Compare schema fields: InputSchema with outputSchema.
    *
    * @param inputSchema The input schema to compare
    * @return If the schemas are equals
    */
  def compareToOutputSchema(inputSchema: StructType): Boolean = inputSchema == outputFieldsSchema

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"the step name $name is not valid", name))

    if (outputFieldsFrom == OutputFieldsFrom.FIELDS && Try(fieldsModel.fields.nonEmpty).isFailure) {
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"the output fields are not valid", name))
    }

    if (outputFieldsFrom == OutputFieldsFrom.STRING && fieldsString.isEmpty) {
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"the output schema in string format cannot be empty", name))
    }

    if (Try(outputFieldsSchema).isFailure) {
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"the output fields definition is not valid. See more info in logs", name))
    }

    //If contains schemas, validate if it can be parsed
    if (inputsModel.inputSchemas.nonEmpty) {
      inputsModel.inputSchemas.foreach { input =>
        if (parserInputSchema(input.schema).isFailure)
          validation = ErrorValidations(
            valid = false,
            messages = validation.messages :+ WorkflowValidationMessage(s"the input schema from step ${input.stepName} is not valid", name))
      }

      inputsModel.inputSchemas.filterNot(is => SdkSchemaHelper.isCorrectTableName(is.stepName)).foreach { is =>
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ WorkflowValidationMessage(s"the input table name ${is.stepName} is not valid", name))
      }
    }

    validation
  }
}