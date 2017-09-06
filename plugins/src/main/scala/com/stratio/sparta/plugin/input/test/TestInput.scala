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

package com.stratio.sparta.plugin.input.test

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.test.{TestDStream, TestReceiver}

import scala.util.{Random, Try}

class TestInput(
                 name: String,
                 ssc: StreamingContext,
                 sparkSession: XDSession,
                 properties: Map[String, JSerializable]
               ) extends Input(name, ssc, sparkSession, properties) with SLF4JLogging {

  private val eventType = EventType.withName(properties.getString("eventType", "STRING").toUpperCase)
  private val event: String = properties.getString("event", "dummyEvent")
  private val maxNumber: Int = Try(properties.getString("maxNumber").toInt).getOrElse(1)
  private val receiverType = ReceiverType.withName(properties.getString("receiverType", "LIMITED").toUpperCase)
  private val numEvents: Long = Try(properties.getString("numEvents").toLong).getOrElse(1L)

  def initStream: DStream[Row] = {
    if (receiverType == ReceiverType.LIMITED) {
      val registers = for (_ <- 1L to numEvents) yield {
        if(eventType == EventType.STRING)
          Row(event)
        else Row(Random.nextInt(maxNumber).toString)
      }
      val defaultRDD = ssc.sparkContext.parallelize(registers)

      new TestDStream(ssc, defaultRDD, Option(numEvents))
    } else {
      ssc.receiverStream(new TestReceiver(event, eventType == EventType.RANDOM_NUMBER, maxNumber, storageLevel))
        .map(data => Row(data))

    }
  }
}
