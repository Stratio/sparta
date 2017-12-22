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

import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.properties.Parameterizable
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import com.stratio.sparta.sdk.utils.ClasspathUtils

abstract class TransformStep[Underlying[Row]](
                              val name: String,
                              val outputOptions: OutputOptions,
                              @transient private[sparta] val ssc: Option[StreamingContext],
                              @transient private[sparta] val xDSession: XDSession,
                              properties: Map[String, JSerializable]
                            ) extends Parameterizable(properties) with GraphStep with DistributedMonadImplicits {

  override lazy val customKey = "transformationOptions"
  override lazy val customPropertyKey = "transformationOptionsKey"
  override lazy val customPropertyValue = "transformationOptionsValue"

  /* METHODS TO IMPLEMENT */

  /**
   * Transformation function that all the transformation plugins must implements.
   *
   * @param inputData Input steps data that the function receive. The key is the name of the step and the value is
   *                  the collection ([[DistributedMonad]])
   * @return The output [[DistributedMonad]] generated after apply the function
   */
  def transform(inputData: Map[String, DistributedMonad[Underlying]]): DistributedMonad[Underlying]

  /* METHODS IMPLEMENTED */

  /**
   * Execute the transform function passed as parameter over the first data of the map.
   *
   * @param inputData       Input data that must contains only one distributed collection.
   * @param generateDistributedMonad Function to apply
   * @return The transformed distributed collection [[DistributedMonad]]
   */
  def applyHeadTransform[Underlying[Row]](inputData: Map[String, DistributedMonad[Underlying]])
                        (
                          generateDistributedMonad: (String, DistributedMonad[Underlying]) =>
                            DistributedMonad[Underlying]
                        ): DistributedMonad[Underlying] = {
    assert(inputData.size == 1, s"The step $name must have one input, now have: ${inputData.keys}")

    val (firstStep, firstStream) = inputData.head

    generateDistributedMonad(firstStep, firstStream)
  }

}

object TransformStep {
  val StepType = "transformation"
}