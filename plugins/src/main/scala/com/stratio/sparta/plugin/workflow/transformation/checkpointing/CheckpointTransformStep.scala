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

package com.stratio.sparta.plugin.workflow.transformation.checkpointing

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.utils.AggregationTime
import com.stratio.sparta.sdk.workflow.step.{OutputFields, OutputOptions, TransformStep}
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Duration, Milliseconds, StreamingContext}

import scala.util.Try

class CheckpointTransformStep(name: String,
                              inputSchemas: Map[String, StructType],
                              outputFields: Seq[OutputFields],
                              outputOptions: OutputOptions,
                              ssc: StreamingContext,
                              xDSession: XDSession,
                              properties: Map[String, JSerializable])
  extends TransformStep(name, inputSchemas, outputFields, outputOptions, ssc, xDSession, properties) {

  lazy val interval: Option[Duration] = Try(properties.getString("interval", None)).getOrElse(None).map(time =>
    Milliseconds(AggregationTime.parseValueToMilliSeconds(time)))

  def transformFunction(inputSchema: String, inputStream: DStream[Row]): DStream[Row] = {
    interval match {
      case Some(time) => inputStream.checkpoint(time)
      case None => inputStream
    }
  }

  override def transform(inputData: Map[String, DStream[Row]]): DStream[Row] =
    applyHeadTransform(inputData)(transformFunction)
}