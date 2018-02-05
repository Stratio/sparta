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

package com.stratio.sparta

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{ErrorsManagement, OutputOptions, OutputStep, TransformStep}
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import org.apache.spark.sql._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.functions._
import com.stratio.sparta.sdk.workflow.step._

import scala.util.{Failure, Success, Try}
import org.apache.spark.sql._

import scala.util.Try

class RepartitionCustomTransformStepBatch(name: String,
                                          outputOptions: OutputOptions,
                                          transformationStepsManagement: TransformationStepManagement,
                                          ssc: Option[StreamingContext],
                                          xDSession: XDSession,
                                          properties: Map[String, JSerializable])
  extends TransformStep[RDD](name, outputOptions, transformationStepsManagement, ssc, xDSession, properties) with SLF4JLogging {

  lazy val partitions = Try(propertiesWithCustom.getInt("partitions")).getOrElse(
    throw new Exception("Property partitions is mandatory"))


  override def transform(inputData: Map[String, DistributedMonad[RDD]]): DistributedMonad[RDD] =
    applyHeadTransform(inputData) { (_, inputDistributedMonad) =>
      val inputStream = inputDistributedMonad.ds
      inputStream.repartition(partitions)
    }
}
