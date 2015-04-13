/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.plugin.output.print

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.WriteOp.WriteOp
import com.stratio.sparkta.sdk._
import org.apache.spark.streaming.dstream.DStream

import scala.util.Try

class PrintOutput(properties: Map[String, JSerializable], schema: Option[Map[String, WriteOp]])
  extends Output(properties, schema) {

  override val supportedWriteOps = Seq(WriteOp.Inc, WriteOp.Set, WriteOp.Max, WriteOp.Min)

  override val multiplexer = Try(
    properties("multiplexer").asInstanceOf[String].toLowerCase().toBoolean).getOrElse(false)

  override def timeBucket: String = Try(properties("timeDimension").asInstanceOf[String]).getOrElse("")

  override val granularity = Try(properties("granularity").asInstanceOf[String]).getOrElse("")

  override def persist(streams: Seq[DStream[UpdateMetricOperation]]): Unit = {
    streams.foreach(persist)
  }

  override def persist(stream: DStream[UpdateMetricOperation]): Unit = {
    stream.print()
  }

}
