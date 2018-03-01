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

package com.stratio.sparta.plugin.workflow.input.websocket

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, InputStep, OutputOptions}
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import DistributedMonad.Implicits._

class WebSocketInputStepStreaming(
                          name: String,
                          outputOptions: OutputOptions,
                          ssc: Option[StreamingContext],
                          xDSession: XDSession,
                          properties: Map[String, JSerializable]
                        ) extends InputStep[DStream](name, outputOptions, ssc, xDSession, properties) {

  lazy val url: String = properties.getString("url", "").trim
  lazy val outputField = properties.getString("outputField", DefaultRawDataField)
  lazy val outputType = properties.getString("outputType", DefaultRawDataType)
  lazy val outputSparkType = SparkTypes.get(outputType) match {
    case Some(sparkType) => sparkType
    case None => schemaFromString(outputType)
  }
  lazy val outputSchema = StructType(Seq(StructField(outputField, outputSparkType)))

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (url.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name input url can not be empty")

    validation
  }

  def init(): DistributedMonad[DStream] = {
    require(url.nonEmpty, "Input url can not be empty")
    ssc.get.receiverStream(new WebSocketReceiver(url, storageLevel, outputSchema))
  }
}
