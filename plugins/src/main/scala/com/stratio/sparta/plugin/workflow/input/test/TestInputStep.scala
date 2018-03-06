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
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, InputStep, OutputOptions}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.test.TestDStream

import scala.util.{Random, Try}

abstract class TestInputStep[Underlying[Row]](
                     name: String,
                     outputOptions: OutputOptions,
                     ssc: Option[StreamingContext],
                     xDSession: XDSession,
                     properties: Map[String, JSerializable]
                   )
  extends InputStep[Underlying](name, outputOptions, ssc, xDSession, properties) with SLF4JLogging {

  lazy val eventType: EventType.Value = EventType.withName(properties.getString("eventType", "STRING").toUpperCase)
  lazy val event = properties.getString("event", None)
  lazy val maxNumber = Try(properties.getInt("maxNumber")).toOption
  lazy val numEvents = Try(properties.getLong("numEvents")).toOption
  lazy val outputField: String = properties.getString("outputField", DefaultRawDataField)
  protected val stopAfterNumbEvents: Option[Long] =
    Try(Option(properties.getString("maxNumbEvents")).notBlank.map(_.toLong)).getOrElse(None)
  lazy val numberSchema = StructType(Seq(StructField(outputField, IntegerType)))
  lazy val stringSchema = StructType(Seq(StructField(outputField, StringType)))
  lazy val outputSchema: StructType = {
    if (eventType == EventType.STRING) stringSchema
    else numberSchema
  }

  lazy val eventsToGenerate : Long = {
    require(numEvents.isDefined, "The field number of events cannot be empty")

    if (stopAfterNumbEvents.isDefined)
      math.min(numEvents.get, stopAfterNumbEvents.get)
    else numEvents.get
  }

  override def validate(options: Map[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if(eventType.equals(EventType.STRING) && event.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name event field cannot be empty"
      )
    else if (eventType.equals(EventType.RANDOM_NUMBER) && maxNumber.isDefined)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name max number field cannot be empty"
      )

    if (properties.getString("numEvents", None).isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name number of events field cannot be empty"
      )
    validation
  }
}

class TestInputStepStreaming(
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
        new GenericRowWithSchema(Array(event.get), stringSchema).asInstanceOf[Row]
      else {
        require(maxNumber.isDefined, "The field max number cannot be empty")
        new GenericRowWithSchema(Array(Random.nextInt(maxNumber.get)), numberSchema).asInstanceOf[Row]
      }
    }

    val defaultRDD = ssc.get.sparkContext.parallelize(registers)

    if(stopAfterNumbEvents.isDefined)
      new TestDStream(ssc.get, defaultRDD, numEvents, stopAfterNumbEvents)
    else new TestDStream(ssc.get, defaultRDD, numEvents)
  }

}

class TestInputStepBatch(
                           name: String,
                           outputOptions: OutputOptions,
                           ssc: Option[StreamingContext],
                           xDSession: XDSession,
                           properties: Map[String, JSerializable]
                         ) extends TestInputStep[RDD](name, outputOptions, ssc, xDSession, properties) {
  /**
    * Create and initialize stream using the Spark Streaming Context.
    *
    * @return The DStream created with spark rows
    */
  override def init(): DistributedMonad[RDD] = {

    val registers = for (_ <- 1L to numEvents.get) yield {
      if (eventType == EventType.STRING)
        new GenericRowWithSchema(Array(event.get), stringSchema).asInstanceOf[Row]
      else new GenericRowWithSchema(Array(Random.nextInt(maxNumber.get)), numberSchema).asInstanceOf[Row]
    }
    val defaultRDD = xDSession.sparkContext.parallelize(registers)

    defaultRDD
  }

}