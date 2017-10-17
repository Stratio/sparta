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
package com.stratio.sparta.plugin.workflow.transformation.csv

import java.io.{Serializable => JSerializable}
import java.util.regex.Pattern

import com.stratio.sparta.plugin.enumerations.FieldsPreservationPolicy._
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{ErrorChecking, OutputOptions, SchemaCasting, TransformStep}
import com.stratio.sparta.plugin.enumerations.{FieldsPreservationPolicy, SchemaInputMode}
import com.stratio.sparta.plugin.enumerations.SchemaInputMode._
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.util.Try


class CsvTransformStep(name: String,
                       outputOptions: OutputOptions,
                       ssc: StreamingContext,
                       xDSession: XDSession,
                       properties: Map[String, JSerializable])
  extends TransformStep(name, outputOptions, ssc, xDSession, properties) with ErrorChecking with SchemaCasting {

  lazy val schemaInputMode = SchemaInputMode.withName(properties.getString("schema.inputMode", "HEADER").toUpperCase)

  lazy val fieldsModel = properties.getPropertiesFields("schema.fields")

  lazy val header = properties.getString("schema.header", None)

  lazy val sparkSchema = properties.getString("schema.sparkSchema", None)

  lazy val fieldsSeparator = properties.getString("delimiter", ",")

  lazy val splitLimit = properties.getInt("splitLimit", -1)

  lazy val delimiterType = DelimiterType.withName(properties.getString("delimiterType", "CHARACTER").toUpperCase)

  lazy val inputField = Try(properties.getString("inputField"))
    .getOrElse(throw new Exception("The inputField is mandatory"))

  lazy val preservationPolicy: FieldsPreservationPolicy.Value = FieldsPreservationPolicy.withName(
    properties.getString("fieldsPreservationPolicy", "REPLACE").toUpperCase)

  lazy val providedSchema: Seq[StructField] = {
    (schemaInputMode, header, sparkSchema, fieldsModel) match {
      case (HEADER, Some(headerStr), _, _) =>
        headerStr.split(fieldsSeparator).map(fieldName => StructField(fieldName, StringType, nullable = true)).toSeq
      case (SPARKFORMAT, None, Some(schema), _) =>
        schemaFromString(schema).asInstanceOf[StructType].fields.toSeq
      case (FIELDS, _, _, inputFields) if inputFields.fields.nonEmpty =>
        inputFields.fields.map { fieldModel =>
          val outputType = fieldModel.`type`.notBlank.getOrElse("string")
          StructField(
            name = fieldModel.name,
            dataType = SparkTypes.get(outputType) match {
              case Some(sparkType) => sparkType
              case None => schemaFromString(outputType)
            },
            nullable = fieldModel.nullable.getOrElse(true)
          )
        }
      case _ => throw new Exception("Incorrect schema arguments")

    }
  }

  assert(inputField.nonEmpty)

  def transformationFunction(inputSchema: String, inputStream: DStream[Row]): DStream[Row] =
    inputStream.flatMap(data => parse(data))

  override def transform(inputData: Map[String, DStream[Row]]): DStream[Row] = {
    applyHeadTransform(inputData)(transformationFunction)
  }

  //scalastyle:off
  def parse(row: Row): Seq[Row] =
    returnSeqDataFromRow {
      val inputSchema = row.schema
      val outputSchema = getNewOutputSchema(inputSchema)
      val inputValue = Option(row.get(inputSchema.fieldIndex(inputField)))
      val newValues =
        inputValue match {
          case Some(value) =>
            if (value.toString.nonEmpty) {
              val valuesSplit = {
                val valueStr = value match {
                  case valueCast: Array[Byte] => new Predef.String(valueCast)
                  case valueCast: String => valueCast
                  case _ => value.toString
                }
                delimiterType match {
                  case DelimiterType.REGEX =>
                    valueStr.split(Pattern.compile(fieldsSeparator).toString, splitLimit)
                  case DelimiterType.CHARACTER =>
                    valueStr.split(Pattern.quote(fieldsSeparator), splitLimit)
                  case _ =>
                    valueStr.split(fieldsSeparator, splitLimit)
                }
              }

              if (valuesSplit.length == providedSchema.length) {
                val valuesParsed = providedSchema.map(_.name).zip(valuesSplit).toMap

                outputSchema.map { outputField =>
                  valuesParsed.get(outputField.name) match {
                    case Some(valueParsed) => if (valueParsed != null)
                      castingToOutputSchema(outputField, valueParsed)
                    case None =>
                      Try(row.get(inputSchema.fieldIndex(outputField.name))).getOrElse(returnWhenError(
                        new Exception(s"Impossible to parse outputField: $outputField in the schema")))
                  }
                }
              }
              else returnWhenError(new Exception(s"The number of values splitted does not match the number of " +
                s"fields defined in the schema"))
            }
            else returnWhenError(new Exception(s"The input value is empty"))

          case None =>
            returnWhenError(new Exception(s"The input value is null"))
        }
      new GenericRowWithSchema(newValues.toArray, outputSchema)
    }

  def getNewOutputSchema(inputSchema: StructType): StructType = {
    preservationPolicy match {
      case APPEND =>
        StructType(inputSchema.fields ++ providedSchema)
      case REPLACE =>
        val inputFieldIdx = inputSchema.indexWhere(_.name == inputField)
        assert(inputFieldIdx > -1, s"$inputField should be a field in the input row")
        val (leftInputFields, rightInputFields) = inputSchema.fields.splitAt(inputFieldIdx)
        val outputFields = leftInputFields ++ providedSchema ++ rightInputFields.tail

        StructType(outputFields)
      case _ =>
        StructType(providedSchema)
    }
  }
}
