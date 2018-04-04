/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.casting

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.helper.SchemaHelper
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step._
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
        SchemaHelper.getSparkSchemaFromString(fieldsString.get).toOption
      case _ =>
        throw new IllegalArgumentException("It's mandatory to specify the fields format")
    }
  }

  def transformFunction(inputSchema: String,
                        inputStream: DistributedMonad[Underlying]): DistributedMonad[Underlying] =
    castingFields(inputStream)

  override def transform(inputData: Map[String, DistributedMonad[Underlying]]): DistributedMonad[Underlying] =
    applyHeadTransform(inputData)(transformFunction)

  /**
    * Compare input schema and output schema and apply the casting function if it's necessary.
    *
    * @param streamData The stream data to casting
    * @return The casted stream data
    */
  def castingFields(streamData: DistributedMonad[Underlying]): DistributedMonad[Underlying] =
    streamData.flatMap { row =>
      returnSeqDataFromRow {
        val inputSchema = row.schema
        (compareToOutputSchema(row.schema), outputFieldsSchema) match {
          case (false, Some(outputSchema)) =>
            val newValues = outputSchema.map { outputField =>
              Try {
                inputSchema.find(_.name == outputField.name)
                  .getOrElse(throw new Exception(
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
                      returnWhenFieldError(new Exception(
                        s"Impossible to cast outputField: $outputField in the schema $inputSchema", e))
                  }
                case Failure(e: Exception) =>
                  returnWhenFieldError(e)
              }
            }
            new GenericRowWithSchema(newValues.toArray, outputSchema)
          case _ => row
        }
      }
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