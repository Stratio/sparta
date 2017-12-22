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

package com.stratio.sparta.plugin.workflow.input.filesystem

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.DistributedMonad
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.sql.Row
import org.apache.spark.streaming.dstream.DStream
import org.apache.hadoop.fs.Path
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{InputStep, OutputOptions}
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}

import DistributedMonad.Implicits._

class FileSystemInputStep(
                           name: String,
                           outputOptions: OutputOptions,
                           ssc: Option[StreamingContext],
                           xDSession: XDSession,
                           properties: Map[String, JSerializable]
                         ) extends InputStep[DStream](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  protected def defaultFilter(path: Path): Boolean =
    !path.getName.startsWith(".") && !path.getName.endsWith("_COPYING_") &&
      !path.getName.startsWith("_")

  def init(): DistributedMonad[DStream] = {

    val directory = properties.getString("directory", "")
    val filters = properties.getString("filterString", None).notBlank
    val flagNewFiles = properties.getBoolean("newFilesOnly")
    val outputField = properties.getString("outputField", DefaultRawDataField)

    val outputSchema = StructType(Seq(StructField(outputField, StringType)))

    val applyFilters = (path: Path) =>
      defaultFilter(path) && filters.forall(_.split(",").forall(!path.getName.contains(_)))

    ssc.get.fileStream[LongWritable, Text, TextInputFormat](directory, applyFilters, flagNewFiles) map[Row] {
      case (_, text) => new GenericRowWithSchema(Array(text.toString), outputSchema)
    }

  }

}
