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
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{InputStep, OutputOptions}
import org.apache.spark.sql.{Dataset, Row}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.test.TestDStream

import scala.util.{Random, Try}
import DistributedMonad.Implicits._

abstract class TestInputStep[Underlying[Row]](
                     name: String,
                     outputOptions: OutputOptions,
                     ssc: Option[StreamingContext],
                     xDSession: XDSession,
                     properties: Map[String, JSerializable]
                   )
  extends InputStep[Underlying](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val eventType: EventType.Value = EventType.withName(properties.getString("eventType", "STRING").toUpperCase)
  lazy val event: String = properties.getString("event", "dummyEvent")
  lazy val maxNumber: Int = properties.getInt("maxNumber", 1)
  lazy val numEvents: Long = properties.getLong("numEvents", 1L)
  lazy val outputField: String = properties.getString("outputField", DefaultRawDataField)
  protected val stopAfterNumbEvents: Option[Long] =
    Try(Option(properties.getString("maxNumbEvents")).notBlank.map(_.toLong)).getOrElse(None)
  lazy val numberSchema = StructType(Seq(StructField(outputField, IntegerType)))
  lazy val stringSchema = StructType(Seq(StructField(outputField, StringType)))
  lazy val outputSchema: StructType = {
    if (eventType == EventType.STRING) stringSchema
    else numberSchema
  }

  val eventsToGenerate : Long =
    if(stopAfterNumbEvents.isDefined)
      math.min(numEvents, stopAfterNumbEvents.get)
    else numEvents

}

class TestInputStepStream(
                           name: String,
                           outputOptions: OutputOptions,
                           ssc: Option[StreamingContext],
                           xDSession: XDSession,
                           properties: Map[String, JSerializable]
                         ) extends TestInputStep[DStream](name, outputOptions, ssc, xDSession, properties) {

  /**
    * Create and initialize stream using the Spark Streaming Context.
    *
    * @return The DStream created with spark rows
    */
  override def init(): DistributedMonad[DStream] = {

    val registers = for (_ <- 1L to eventsToGenerate) yield {
      if (eventType == EventType.STRING)
        new GenericRowWithSchema(Array(event), stringSchema).asInstanceOf[Row]
      else new GenericRowWithSchema(Array(Random.nextInt(maxNumber)), numberSchema).asInstanceOf[Row]
    }

    val defaultRDD = ssc.get.sparkContext.parallelize(registers)

    if(stopAfterNumbEvents.isDefined)
      new TestDStream(ssc.get, defaultRDD, Option(numEvents), stopAfterNumbEvents)
    else new TestDStream(ssc.get, defaultRDD, Option(numEvents))
  }

}

class TestInputStepBatch(
                           name: String,
                           outputOptions: OutputOptions,
                           ssc: Option[StreamingContext],
                           xDSession: XDSession,
                           properties: Map[String, JSerializable]
                         ) extends TestInputStep[Dataset](name, outputOptions, ssc, xDSession, properties) {
  /**
    * Create and initialize stream using the Spark Streaming Context.
    *
    * @return The DStream created with spark rows
    */
  override def init(): DistributedMonad[Dataset] = {

    val registers = for (_ <- 1L to numEvents) yield {
      if (eventType == EventType.STRING)
        new GenericRowWithSchema(Array(event), stringSchema).asInstanceOf[Row]
      else new GenericRowWithSchema(Array(Random.nextInt(maxNumber)), numberSchema).asInstanceOf[Row]
    }
    val defaultRDD = xDSession.sparkContext.parallelize(registers)

    xDSession.createDataFrame(defaultRDD, outputSchema)
  }

}