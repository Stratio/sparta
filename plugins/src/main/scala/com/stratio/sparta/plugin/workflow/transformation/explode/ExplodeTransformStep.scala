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
package com.stratio.sparta.plugin.workflow.transformation.explode

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.enumerations.FieldsPreservationPolicy.{APPEND, REPLACE}
import com.stratio.sparta.plugin.enumerations.SchemaInputMode.{FIELDS, SPARKFORMAT}
import com.stratio.sparta.plugin.enumerations.{FieldsPreservationPolicy, SchemaInputMode}
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{ErrorCheckingStepRow, OutputOptions, SchemaCasting, TransformStep}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.util.Try


class ExplodeTransformStep(name: String,
                           outputOptions: OutputOptions,
                           ssc: StreamingContext,
                           xDSession: XDSession,
                           properties: Map[String, JSerializable])
  extends TransformStep(name, outputOptions, ssc, xDSession, properties) with ErrorCheckingStepRow with SchemaCasting {

  lazy val inputField: String = Try(properties.getString("inputField"))
    .getOrElse(throw new IllegalArgumentException("The inputField is mandatory"))

  lazy val preservationPolicy: FieldsPreservationPolicy.Value = FieldsPreservationPolicy.withName(
    properties.getString("fieldsPreservationPolicy", "REPLACE").toUpperCase)

  lazy val providedSchema: Option[Seq[StructField]] = {
    if (properties.getBoolean("schema.fromRow", default = true)) None
    else {
      val fieldsModel = properties.getPropertiesFields("schema.fields")
      val sparkSchema = properties.getString("schema.sparkSchema", None)
      val schemaInputMode = SchemaInputMode.withName(properties.getString("schema.inputMode", "FIELDS").toUpperCase)
      (schemaInputMode, sparkSchema, fieldsModel) match {
        case (SPARKFORMAT, Some(schema), _) =>
          Option(schemaFromString(schema).asInstanceOf[StructType].fields.toSeq)
        case (FIELDS, _, inputFields) if inputFields.fields.nonEmpty =>
          Option(inputFields.fields.map { fieldModel =>
            val outputType = fieldModel.`type`.notBlank.getOrElse("string")
            StructField(
              name = fieldModel.name,
              dataType = SparkTypes.get(outputType) match {
                case Some(sparkType) => sparkType
                case None => schemaFromString(outputType)
              },
              nullable = fieldModel.nullable.getOrElse(true)
            )
          })
        case _ => throw new Exception("Incorrect schema arguments")

      }
    }
  }

  def transformationFunction(inputSchema: String, inputStream: DStream[Row]): DStream[Row] =
    inputStream.flatMap(data => parse(data))

  override def transform(inputData: Map[String, DStream[Row]]): DStream[Row] = {
    applyHeadTransform(inputData)(transformationFunction)
  }

  //scalastyle:off
  def parse(row: Row): Seq[Row] =
    returnSeqDataFromRows {
      val inputSchema = row.schema
      Option(row.get(inputSchema.fieldIndex(inputField))) match {
        case Some(value) =>
          val (rowFieldValues, rowFieldSchema) = value match {
            case valueCast: Map[String, _] =>
              (Seq(valueCast), valueCast.map { case (key, _) => StructField(key, StringType) }.toSeq)
            case valueCast: GenericRowWithSchema =>
              (Seq(valueCast.getValuesMap(valueCast.schema.fieldNames)), valueCast.schema.fields.toSeq)
            case valueCast: Seq[_] =>
              Try {
                val valueInstance = valueCast.asInstanceOf[Seq[Map[String, _]]]
                (valueInstance, valueInstance.head.map { case (key, _) => StructField(key, StringType) }.toSeq)
              } orElse Try {
                val valueInstance = valueCast.asInstanceOf[Seq[GenericRowWithSchema]]
                (valueInstance.map(row => row.getValuesMap(row.schema.fieldNames)), valueInstance.head.schema.fields.toSeq)
              } getOrElse {
                throw new Exception(s"The input value has incorrect type Seq(Map()) or Seq(Row). Value:${value.toString}")
              }
            case _ => throw new Exception(
              s"The input value has incorrect type, Seq(Map()),  Map() or Rows with schema. Value:${value.toString}")
          }
          val outputSchema = getNewOutputSchema(inputSchema, rowFieldSchema)

          rowFieldValues.map { valuesMap =>
            val newValues = outputSchema.map { outputField =>
              valuesMap.get(outputField.name) match {
                case Some(valueParsed) => if (valueParsed != null)
                  castingToOutputSchema(outputField, valueParsed)
                case None =>
                  Try(row.get(inputSchema.fieldIndex(outputField.name))).getOrElse {
                    returnWhenError(new Exception(s"Impossible to parse outputField: $outputField in the schema"))
                  }
              }
            }
            new GenericRowWithSchema(newValues.toArray, outputSchema)
          }
        case None =>
          throw new Exception(s"The input value is null")
      }
    }

  def getNewOutputSchema(inputSchema: StructType, rowFieldSchema: Seq[StructField]): StructType = {
    val extractedSchema = providedSchema.getOrElse(rowFieldSchema)
    preservationPolicy match {
      case APPEND =>
        StructType(inputSchema.fields ++ extractedSchema)
      case REPLACE =>
        val inputFieldIdx = inputSchema.indexWhere(_.name == inputField)
        assert(inputFieldIdx > -1, s"$inputField should be a field in the input row")
        val (leftInputFields, rightInputFields) = inputSchema.fields.splitAt(inputFieldIdx)
        val outputFields = leftInputFields ++ extractedSchema ++ rightInputFields.tail

        StructType(outputFields)
      case _ =>
        StructType(extractedSchema)
    }
  }
}
