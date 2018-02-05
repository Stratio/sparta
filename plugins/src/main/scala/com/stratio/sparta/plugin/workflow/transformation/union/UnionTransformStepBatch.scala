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

package com.stratio.sparta.plugin.workflow.transformation.union

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext

class UnionTransformStepBatch(
                               name: String,
                               outputOptions: OutputOptions,
                               transformationStepsManagement: TransformationStepManagement,
                               ssc: Option[StreamingContext],
                               xDSession: XDSession,
                               properties: Map[String, JSerializable]
                             ) extends UnionTransformStep[RDD](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) {

  override def transform(inputData: Map[String, DistributedMonad[RDD]]): DistributedMonad[RDD] = {
    val rdds = inputData.map { case (_, rdd) =>
      rdd.ds
    }.toSeq

    rdds.size match {
      case 1 => rdds.head
      case x if x > 1 => xDSession.sparkContext.union(rdds)
      case _ => xDSession.sparkContext.emptyRDD[Row]
    }
  }
}
