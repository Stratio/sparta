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

package com.stratio.sparta.plugin.workflow.transformation.filter

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformStep}
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import scala.util.Try

class FilterTransformStep(name: String,
                          outputOptions: OutputOptions,
                          ssc: StreamingContext,
                          xDSession: XDSession,
                          properties: Map[String, JSerializable])
  extends TransformStep(name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val filterExpression: Option[String] = Try(properties.getString("filterExp")).toOption

  assert(filterExpression.isDefined,
    "It's mandatory one filter expression, such as colA, colB as newName, abs(colC)")

  def transformFunction(inputSchema: String, inputStream: DStream[Row]): DStream[Row] = {
    filterExpression.fold(inputStream) { expression =>
      inputStream.transform { rdd =>
        if (rdd.isEmpty()) rdd
        else {
          val schema = rdd.first().schema
          val df = xDSession.createDataFrame(rdd, schema)

          df.filter(expression).rdd
        }
      }
    }
  }

  override def transform(inputData: Map[String, DStream[Row]]): DStream[Row] =
    applyHeadTransform(inputData)(transformFunction)
}

