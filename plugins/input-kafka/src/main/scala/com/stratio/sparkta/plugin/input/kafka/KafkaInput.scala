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
package com.stratio.sparkta.plugin.input.kafka

import com.stratio.sparkta.sdk.Input._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.{Event, Input}
import kafka.serializer.StringDecoder
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils


class KafkaInput(properties: Map[String, Serializable]) extends Input(properties) {

  val DEFAULT_STORAGE_LEVEL = "MEMORY_AND_DISK_SER_2"

  override def setUp(ssc: StreamingContext): DStream[Event] = {

    val submap: Option[Map[String, Serializable]] = properties.getMap("kafkaParams")
    if (submap.isEmpty) {

      KafkaUtils.createStream(
        ssc,
        properties.getString("zkQuorum"),
        properties.getString("groupId"),
        extractTopicsMap,
        storageLevel)
        .map(data => new Event(Map(RAW_DATA_KEY -> data._2.getBytes("UTF-8").asInstanceOf[java.io.Serializable])))

    } else {

      val kafkaParams = submap.get.map(entry => (entry._1, entry._2.toString))
      KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams,
        extractTopicsMap,
        storageLevel)
        .map(data => new Event(Map(RAW_DATA_KEY -> data._2.getBytes("UTF-8").asInstanceOf[java.io.Serializable])))

    }
  }

  private def extractTopicsMap(): Map[String, Int] = {
    var topics : Map[String,Int] = Map()
    properties.getString("topics").split(",").toSeq.map(
      str => str.split(":").toSeq match {
          case Seq(topic) => topics += topic -> 1
          case Seq(topic, partitions) => topics += topic -> partitions.toInt
          case _ => throw new IllegalStateException(s"Invalid conf value for topics : $str")
         }
    )
    topics
  }

  private def storageLevel(): StorageLevel =
    properties.hasKey("storageLevel") match {
      case true => StorageLevel.fromString(properties.getString("storageLevel"))
      case false => StorageLevel.fromString(DEFAULT_STORAGE_LEVEL)
    }

}
