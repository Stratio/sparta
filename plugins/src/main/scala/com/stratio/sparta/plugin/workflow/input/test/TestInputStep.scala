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

package com.stratio.sparta.plugin.workflow.input.test

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{InputStep, OutputOptions}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.test.TestDStream

import scala.util.{Random, Try}

class TestInputStep(
                     name: String,
                     outputOptions: OutputOptions,
                     ssc: StreamingContext,
                     xDSession: XDSession,
                     properties: Map[String, JSerializable]
                   )
  extends InputStep(name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val eventType: EventType.Value = EventType.withName(properties.getString("eventType", "STRING").toUpperCase)
  lazy val event: String = properties.getString("event", "dummyEvent")
  lazy val maxNumber: Int = Try(properties.getString("maxNumber").toInt).getOrElse(1)
  lazy val numEvents: Long = Try(properties.getString("numEvents").toLong).getOrElse(1L)
  lazy val outputField: String = properties.getString("outputField", DefaultRawDataField)
  lazy val numberSchema = StructType(Seq(StructField(outputField, IntegerType)))
  lazy val stringSchema = StructType(Seq(StructField(outputField, StringType)))
  lazy val outputSchema: StructType = {
    if (eventType == EventType.STRING) stringSchema
    else numberSchema
  }

  def initStream(): DStream[Row] = {
    val registers = for (_ <- 1L to numEvents) yield {
      if (eventType == EventType.STRING)
        new GenericRowWithSchema(Array(event), stringSchema).asInstanceOf[Row]
      else new GenericRowWithSchema(Array(Random.nextInt(maxNumber)), numberSchema).asInstanceOf[Row]
    }
    val defaultRDD = ssc.sparkContext.parallelize(registers)

    new TestDStream(ssc, defaultRDD, Option(numEvents))
  }
}
