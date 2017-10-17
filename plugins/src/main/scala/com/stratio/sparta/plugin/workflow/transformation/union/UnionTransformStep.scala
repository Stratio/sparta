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

import com.stratio.sparta.sdk.workflow.step.{OutputFields, OutputOptions, TransformStep}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.collection.mutable

class UnionTransformStep(name: String,
                         outputOptions: OutputOptions,
                         ssc: StreamingContext,
                         xDSession: XDSession,
                         properties: Map[String, JSerializable])
  extends TransformStep(name, outputOptions, ssc, xDSession, properties) {

  override def transform(inputData: Map[String, DStream[Row]]): DStream[Row] = {
    val streams = inputData.map { case (_, dStream) =>
      dStream
    }.toSeq

    streams.size match {
      case 1 => streams.head
      case x if x > 1 => ssc.union(streams)
      case _ => ssc.queueStream(new mutable.Queue[RDD[Row]])
    }
  }
}