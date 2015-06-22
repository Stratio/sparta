/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.plugin.input.kafka

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.Input._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.{Event, Input}
import kafka.serializer.StringDecoder
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils


class KafkaDirectInput(properties: Map[String, JSerializable]) extends Input(properties) {

  override def setUp(ssc: StreamingContext): DStream[Event] = {

    val submap: Option[Map[String, JSerializable]] = properties.getMap("kafkaParams")

    if (submap.isDefined) {
      val kafkaParams = submap.get.map(entry => (entry._1, entry._2.toString))
      KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
        ssc,
        kafkaParams,
        extractTopicsSet)
        .map(data => new Event(Map(RawDataKey -> data._2.getBytes("UTF-8").asInstanceOf[java.io.Serializable])))
    } else {
      throw new IllegalStateException(s"kafkaParams is necessary for KafkaDirectInput receiver")
    }
  }

  private def extractTopicsSet(): Set[String] = {

    if(!properties.hasKey("topics")){
      throw new IllegalStateException(s"Invalid configuration, topics must be declared.")
    }

    properties.getString("topics").split(",").toSet
  }

}
