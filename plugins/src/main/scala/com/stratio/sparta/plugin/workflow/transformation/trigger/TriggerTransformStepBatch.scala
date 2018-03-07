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
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext

class TriggerTransformStepBatch(
                                 name: String,
                                 outputOptions: OutputOptions,
                                 transformationStepsManagement: TransformationStepManagement,
                                 ssc: Option[StreamingContext],
                                 xDSession: XDSession,
                                 properties: Map[String, JSerializable]
                               )
  extends TriggerTransformStep[RDD](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties)
    with SLF4JLogging {

  //scalastyle:off
  override def transform(inputData: Map[String, DistributedMonad[RDD]]): DistributedMonad[RDD] = {
    require(sql.nonEmpty, "The input query can not be empty")
    validateSchemas(inputData)

    inputData.foreach { case (stepName, stepData) =>
      if (!isCorrectTableName(stepName)) {
        throw new RuntimeException(s"The step($stepName) have wrong name and is not possible to register as temporal table.")
      }
      val schema = inputsModel.inputSchemas.filter(is => is.stepName == stepName) match {
        case Nil => if (!stepData.ds.isEmpty()) Some(stepData.ds.first().schema) else None
        case x :: Nil => parserInputSchema(x.schema).toOption
      }
      schema.foreach { s =>
        log.debug(s"Registering temporal table in Spark with name: $stepName")
        xDSession.createDataFrame(stepData.ds, s).createOrReplaceTempView(stepName)
      }
    }
    executeSQL
  }
}

