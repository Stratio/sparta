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

package com.stratio.sparta.sdk.workflow.step

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import com.stratio.sparta.sdk.properties.Parameterizable
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.WhenError
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types._
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

abstract class TransformStep(
                              val name: String,
                              val inputSchemas: Map[String, StructType],
                              val outputFields: Seq[OutputFields],
                              val outputOptions: OutputOptions,
                              @transient private[sparta] val ssc: StreamingContext,
                              @transient private[sparta] val xDSession: XDSession,
                              properties: Map[String, JSerializable]
                            ) extends Parameterizable(properties) with GraphStep {

  /* GLOBAL VARIABLES */

  lazy val addAllInputFields: Boolean = Try(propertiesWithCustom.getBoolean("addAllInputFields")).getOrElse(false)

  /* METHODS TO IMPLEMENT */

  /**
   * Transformation function that all the transformation plugins must implements.
   *
   * @param inputData Input steps data that the function receive. The key is the name of the step and the value is
   *                  the stream
   * @return The output stream generated after apply the function
   */
  def transform(inputData: Map[String, DStream[Row]]): DStream[Row]

  /* METHODS IMPLEMENTED */

  /**
   * Default parsing function to apply inside the transform function.
   *
   * By default make one casting of the input fields based on the output fields. It's mandatory that the input fields
   * and the output fields have the same name.
   *
   * @param row        The data to parse
   * @param schemaName The schema name of the data to search in the input schemas
   * @return One or more rows that the parsing function generates
   */
  def parse(row: Row, schemaName: String): Seq[Row] = {
    returnData(Try {
      outputSchema.map { outputField =>
        Try {
          val inputSchema = inputSchemas
            .getOrElse(schemaName, throw new IllegalStateException("Incorrect input schema name"))
          val inputSchemaType = inputSchema.find(_.name == outputField.name)
            .getOrElse(throw new IllegalStateException(
              s"Output field: ${outputField.name} not found in the schema: $inputSchema"))
            .dataType
          (inputSchema, inputSchemaType)
        } match {
          case Success((inputSchema, inputSchemaType)) =>
            Try {
              if (inputSchemaType == outputField.dataType)
                row.get(inputSchema.fieldIndex(outputField.name))
              else castingToOutputSchema(outputField, row.get(inputSchema.fieldIndex(outputField.name)))
            } match {
              case Success(dataRow) =>
                dataRow
              case Failure(e) =>
                returnWhenError(new IllegalStateException(
                  s"Impossible to find outputField: $outputField in the schema $inputSchema", e))
            }
          case Failure(e: Exception) =>
            returnWhenError(e)
        }
      }
    })
  }

  /**
   * Execute the transform function passed as parameter over the first data of the map.
   *
   * @param inputData       Input data that must contains only one DStream
   * @param generateDStream Function to apply
   * @return The transformed stream
   */
  def applyHeadTransform(inputData: Map[String, DStream[Row]])
                        (generateDStream: (String, DStream[Row]) => DStream[Row]): DStream[Row] = {
    assert(inputData.size == 1, s"The step $name must have one input, now have: ${inputData.keys}")

    val (firstStep, firstStream) = inputData.head
    val streamTransformed = generateDStream(firstStep, firstStream)

    castingFields(firstStep, streamTransformed)
  }

  /**
   * Compare input schema and output schema and apply the parsing function if it's necessary.
   *
   * @param schemaKey  The key associated to the schema input stream (the step name)
   * @param streamData The stream data to casting
   * @return The casted stream data
   */
  def castingFields(schemaKey: String, streamData: DStream[Row]): DStream[Row] =
    if (compareToOutputSchema(inputSchemas(schemaKey)))
      streamData
    else streamData.flatMap(data => parse(data, schemaKey))

  /**
   * Calculate the output schema based to the output fields and the variable addAllInputFields.
   *
   * @return The calculated schema
   */
  def getOutputSchema: StructType = {
    if (outputFields.nonEmpty) {
      val newFields = outputFields.map { outputField =>
        StructField(
          name = outputField.name,
          dataType = sparkTypes.getOrElse(outputField.`type`.toLowerCase, StringType),
          nullable = true
        )
      }
      if (addAllInputFields)
        StructType(inputSchemas.flatMap { case (_, schema) => schema.fields }.toSeq ++ newFields)
      else StructType(newFields)
    } else StructType(inputSchemas.flatMap { case (_, schema) => schema.fields }.toSeq)
  }
}

object TransformStep {

  val ClassSuffix = "TransformStep"
}
