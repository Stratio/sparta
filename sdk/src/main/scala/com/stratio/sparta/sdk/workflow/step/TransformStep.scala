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

import com.stratio.sparta.sdk.properties.Parameterizable
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

abstract class TransformStep(
                              val name: String,
                              val outputOptions: OutputOptions,
                              @transient private[sparta] val ssc: StreamingContext,
                              @transient private[sparta] val xDSession: XDSession,
                              properties: Map[String, JSerializable]
                            ) extends Parameterizable(properties) with GraphStep {

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

}

object TransformStep {

  val StepType = "transformation"
}
