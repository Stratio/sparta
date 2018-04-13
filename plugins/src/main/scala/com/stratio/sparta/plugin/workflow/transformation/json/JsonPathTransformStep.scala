/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.json

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.enumerations.FieldsPreservationPolicy
import com.stratio.sparta.plugin.helper.SchemaHelper._
import com.stratio.sparta.plugin.models.{PropertyQuery}
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.helpers.SdkSchemaHelper
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step._
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructField
import org.apache.spark.streaming.StreamingContext
import org.json4s.jackson.Serialization.read
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

abstract class JsonPathTransformStep[Underlying[Row]](
                                                       name: String,
                                                       outputOptions: OutputOptions,
                                                       transformationStepsManagement: TransformationStepManagement,
                                                       ssc: Option[StreamingContext],
                                                       xDSession: XDSession,
                                                       properties: Map[String, JSerializable]
                                                     )(implicit dsMonadEvidence: Underlying[Row] => DistributedMonad[Underlying])
  extends TransformStep[Underlying](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  lazy val queriesModel: Seq[PropertyQuery] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val queries = s"${properties.getString("queries", None).notBlank.fold("[]") { values => values.toString }}"

    read[Seq[PropertyQuery]](queries)
  }
  lazy val supportNullValues: Boolean = properties.getBoolean("supportNullValues", default = true)
  lazy val inputField = properties.getString("inputField", None)
  lazy val preservationPolicy: FieldsPreservationPolicy.Value = FieldsPreservationPolicy.withName(
    properties.getString("fieldsPreservationPolicy", "REPLACE").toUpperCase)
  lazy val outputFieldsSchema: Seq[StructField] = queriesModel.map { queryField =>
    val outputType = queryField.`type`.notBlank.getOrElse("string")
    StructField(
      name = queryField.field,
      dataType = SparkTypes.get(outputType) match {
        case Some(sparkType) => sparkType
        case None => schemaFromString(outputType)
      },
      nullable = queryField.nullable.getOrElse(true)
    )
  }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the step name $name is not valid")

    if (inputField.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the input field cannot be empty")

    //If contains schemas, validate if it can be parsed
    if (inputsModel.inputSchemas.nonEmpty) {
      inputsModel.inputSchemas.foreach { input =>
        if (parserInputSchema(input.schema).isFailure)
          validation = ErrorValidations(
            valid = false,
            messages = validation.messages :+ s"$name: the input schema from step ${input.stepName} is not valid")
      }

      inputsModel.inputSchemas.filterNot(is => SdkSchemaHelper.isCorrectTableName(is.stepName)).foreach { is =>
        validation = ErrorValidations(
          valid = false,
          messages = validation.messages :+ s"$name: the input table name ${is.stepName} is not valid")
      }
    }

    validation
  }

  def transformFunc(inputData: Map[String, DistributedMonad[Underlying]]): DistributedMonad[Underlying] =
    applyHeadTransform(inputData) { (_, inputStream) =>
      inputStream.flatMap(data => parse(data))
    }

  //scalastyle:off
  def parse(row: Row): Seq[Row] = returnSeqDataFromRow {
    val inputSchema = row.schema
    val inputFieldName = inputField.get
    val outputSchema = getNewOutputSchema(inputSchema, preservationPolicy, outputFieldsSchema, inputFieldName)
    val inputFieldIndex = inputSchema.fieldIndex(inputFieldName)
    val inputValue = Option(row.get(inputFieldIndex))
    val newValues = inputValue match {
      case Some(value) =>
        if (value.toString.nonEmpty) {
          val valuesParsed = value match {
            case valueCast: Array[Byte] =>
              JsonPathTransformStep.jsonParse(new Predef.String(valueCast), queriesModel, supportNullValues)
            case valueCast: String =>
              JsonPathTransformStep.jsonParse(valueCast, queriesModel, supportNullValues)
            case valueCast if valueCast == null =>
              Map.empty[String, Any]
            case _ =>
              JsonPathTransformStep.jsonParse(value.toString, queriesModel, supportNullValues)
          }
          outputSchema.map { outputField =>
            Try {
              valuesParsed.get(outputField.name) match {
                case Some(valueParsed) if valueParsed != null | (valueParsed == null && supportNullValues) =>
                  castingToOutputSchema(outputField, valueParsed)
                case _ =>
                  row.get(inputSchema.fieldIndex(outputField.name))
              }
            } match {
              case Success(newValue) =>
                newValue
              case Failure(e) =>
                returnWhenFieldError(new Exception(s"Impossible to parse outputField: $outputField " +
                  s"from extracted values: ${valuesParsed.keys.mkString(",")}", e))
            }
          }
        } else throw new Exception(s"The input value is empty")
      case None => throw new Exception(s"The input value is null")
    }
    new GenericRowWithSchema(newValues.toArray, outputSchema)
  }

  //scalastyle:on
}

object JsonPathTransformStep {

  def jsonParse(jsonData: String, queriesModel: Seq[PropertyQuery], isLeafToNull: Boolean): Map[String, Any] = {
    val jsonPathExtractor = new JsonPathExtractor(jsonData, isLeafToNull)

    //Delegate the error management to next step, when the outputFields is transformed
    queriesModel.flatMap { queryModel =>
      Try((queryModel.field, jsonPathExtractor.query(queryModel.query))).toOption
    }.toMap
  }
}
