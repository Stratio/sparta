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

package com.stratio.sparta.plugin.workflow.transformation.repartition

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext

class RepartitionTransformStepBatch(
                                     name: String,
                                     outputOptions: OutputOptions,
                                     transformationStepsManagement: TransformationStepManagement,
                                     ssc: Option[StreamingContext],
                                     xDSession: XDSession,
                                     properties: Map[String, JSerializable]
                                   )
  extends RepartitionTransformStep[RDD](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  override def transform(inputData: Map[String, DistributedMonad[RDD]]): DistributedMonad[RDD] =
    applyHeadTransform(inputData) { (inputSchema, inputStream) =>
      partitions.fold(inputStream.ds) { partition => inputStream.ds.repartition(partition)
      }
    }

}
