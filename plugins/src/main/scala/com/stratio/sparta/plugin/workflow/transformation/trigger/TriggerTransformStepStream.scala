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

package com.stratio.sparta.plugin.workflow.transformation.trigger

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

class TriggerTransformStepStream(
                                  name: String,
                                  outputOptions: OutputOptions,
                                  transformationStepsManagement: TransformationStepManagement,
                                  ssc: Option[StreamingContext],
                                  xDSession: XDSession,
                                  properties: Map[String, JSerializable]
                                )
  extends TriggerTransformStep[DStream](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  override def transform(inputData: Map[String, DistributedMonad[DStream]]): DistributedMonad[DStream] = {
    assert(inputData.size == 2 || inputData.size == 1,
      s"The trigger $name must have one or two input steps, now have: ${inputData.keys}")

    if (inputData.size == 1) {
      val (firstStep, firstStream) = inputData.head

      assert(isCorrectTableName(firstStep),
        s"The step($firstStep) have wrong name and is not possible to register as temporal table. ${inputData.keys}")

      firstStream.ds.transform { rdd =>
        if (rdd.isEmpty()) rdd
        else {
          val schema = rdd.first().schema
          log.debug(s"Registering temporal table in Spark with name: $firstStep")
          xDSession.createDataFrame(rdd, schema).createOrReplaceTempView(firstStep)

          executeSQL
        }
      }
    } else {
      val (firstStep, firstStream) = inputData.head
      val (secondStep, secondStream) = inputData.drop(1).head

      assert(isCorrectTableName(firstStep) && isCorrectTableName(secondStep),
        s"The input steps have incorrect names and is not possible to register as temporal table in Spark." +
          s" ${inputData.keys}")

      val transformFunc: (RDD[Row], RDD[Row]) => RDD[Row] = {
        case (rdd1, rdd2) =>
          log.debug(s"Registering temporal tables in Spark with names: $firstStep, $secondStep")
          if (!rdd1.isEmpty() && !rdd2.isEmpty()) {
            val firstSchema = rdd1.first().schema
            xDSession.createDataFrame(rdd1, firstSchema).createOrReplaceTempView(firstStep)
            val secondSchema = rdd2.first().schema
            xDSession.createDataFrame(rdd2, secondSchema).createOrReplaceTempView(secondStep)
            executeSQL
          } else rdd1
      }

      firstStream.ds.transformWith(secondStream.ds, transformFunc)
    }
  }
}

