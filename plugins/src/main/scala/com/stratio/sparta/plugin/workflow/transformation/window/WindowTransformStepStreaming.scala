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

package com.stratio.sparta.plugin.workflow.transformation.window

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.utils.AggregationTimeUtils
import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, OutputOptions, TransformStep, TransformationStepManagement}
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Duration, Milliseconds, StreamingContext}

import scala.util.Try

class WindowTransformStepStreaming(
                           name: String,
                           outputOptions: OutputOptions,
                           transformationStepsManagement: TransformationStepManagement,
                           ssc: Option[StreamingContext],
                           xDSession: XDSession,
                           properties: Map[String, JSerializable]
                         )
  extends TransformStep[DStream](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  lazy val overLast: Option[Duration] = Try(properties.getString("overLast", None)
    .notBlank.map(over => Milliseconds(AggregationTimeUtils.parseValueToMilliSeconds(over)))).toOption.flatten
  lazy val computeEvery: Option[Duration] = Try(properties.getString("computeEvery", None)
    .notBlank.map(every => Milliseconds(AggregationTimeUtils.parseValueToMilliSeconds(every)))).toOption.flatten

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (overLast.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the over last time is invalid"
      )

    validation
  }

  override def transform(inputData: Map[String, DistributedMonad[DStream]]): DistributedMonad[DStream] =
    applyHeadTransform(inputData) { (_, inputDistributedMonad) =>
      val inputStream = inputDistributedMonad.ds
      (overLast, computeEvery) match {
        case (Some(over), None) =>
          inputStream.window(over)
        case (Some(over), Some(every)) =>
          inputStream.window(over, every)
        case _ =>
          inputStream
      }
    }
}