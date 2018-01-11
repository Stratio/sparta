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
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import com.stratio.sparta.sdk.DistributedMonad.Implicits._

class TriggerTransformStepBatch(name: String,
                                outputOptions: OutputOptions,
                                ssc: Option[StreamingContext],
                                xDSession: XDSession,
                                properties: Map[String, JSerializable])
  extends TriggerTransformStep[RDD](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  override def transform(inputData: Map[String, DistributedMonad[RDD]]): DistributedMonad[RDD] = {
    val emptySteps = scala.collection.mutable.HashSet[String]()
    val wrongNameSteps = scala.collection.mutable.HashSet[String]()

    inputData.foreach{ case(stepName, stepData) =>
      if(!isCorrectTableName(stepName))
        wrongNameSteps.+=(stepName)
      if (stepData.ds.isEmpty())
        emptySteps.+=(stepName)
      if(emptySteps.isEmpty && wrongNameSteps.isEmpty){
        val schema = stepData.ds.first().schema
        log.debug(s"Registering temporal table in Spark with name: $stepName")
        xDSession.createDataFrame(stepData.ds, schema).createOrReplaceTempView(stepName)
      }
    }

    if(wrongNameSteps.nonEmpty || emptySteps.nonEmpty){
      val wrongNameError = wrongNameSteps.map(stepName =>
        s"The step($stepName) have wrong name and is not possible to register as temporal table."
      ).mkString("\n\t")
      val emptyStepsError = wrongNameSteps.map(stepName =>
        s"The step($stepName) is empty and is not possible to register as temporal table."
      ).mkString("\n\t")
      throw new RuntimeException(
        s"Impossible to execute trigger ($name). With errors: \n\t$wrongNameError\n\t$emptyStepsError")
    } else {
      executeSQL
    }
  }
}

