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
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream


abstract class InputStep(
                          val name: String,
                          val outputOptions: OutputOptions,
                          @transient private[sparta] val ssc: StreamingContext,
                          @transient private[sparta] val sparkSession: XDSession,
                          properties: Map[String, JSerializable]
                        ) extends Parameterizable(properties) with GraphStep {

  /* GLOBAL VARIABLES */

  lazy val StorageDefaultValue = "MEMORY_ONLY"
  lazy val DefaultRawDataField = "raw"
  lazy val DefaultRawDataType = "string"
  lazy val DefaultSchema: StructType = StructType(Seq(StructField(DefaultRawDataField, StringType)))
  lazy val storageLevel: StorageLevel = {
    val storageLevel = properties.getString("storageLevel", StorageDefaultValue)
    StorageLevel.fromString(storageLevel)
  }

  /* METHODS TO IMPLEMENT */

  /**
   * Create and initialize stream using the Spark Streaming Context.
   *
   * @return The DStream created with spark rows
   */
  def initStream(): DStream[Row]

}

object InputStep {

  val StepType = "input"

}