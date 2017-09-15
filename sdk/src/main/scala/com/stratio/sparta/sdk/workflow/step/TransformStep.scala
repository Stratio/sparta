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
import com.stratio.sparta.sdk.workflow.enumerators.WhenError._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types._
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

abstract class TransformStep(
                              val name: String,
                              val outputOptions: OutputOptions,
                              @transient private[sparta] val ssc: StreamingContext,
                              @transient private[sparta] val xDSession: XDSession,
                              properties: Map[String, JSerializable]
                            ) extends Parameterizable(properties) with GraphStep {

  /* GLOBAL VARIABLES */

  lazy val whenErrorDo: WhenError = Try(WhenError.withName(propertiesWithCustom.getString("whenError")))
    .getOrElse(WhenError.Error)


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

    generateDStream(firstStep, firstStream)
  }


  //scalastyle:off
  def returnWhenError(exception: Exception): Null =
    whenErrorDo match {
      case WhenError.Null => null
      case _ => throw exception
    }

  //scalastyle:on

  def castingToOutputSchema(outSchema: StructField, inputValue: Any): Any =
    Try {
      TypeOp.castingToSchemaType(outSchema.dataType, inputValue.asInstanceOf[Any])
    } match {
      case Success(result) => result
      case Failure(e) => returnWhenError(new IllegalStateException(
        s"Error casting to output type the value: ${inputValue.toString}", e))
    }

  def returnSeqData(newData: Try[_]): Seq[Row] =
    newData match {
      case Success(data: GenericRowWithSchema) => Seq(data)
      case Success(_) => whenErrorDo match {
        case WhenError.Discard => Seq.empty[GenericRowWithSchema]
        case _ => throw new IllegalStateException("Invalid new data in step")
      }
      case Failure(e) => whenErrorDo match {
        case WhenError.Discard => Seq.empty[GenericRowWithSchema]
        case _ => throw e
      }
    }
}

object TransformStep {

  val StepType = "transformation"
}
