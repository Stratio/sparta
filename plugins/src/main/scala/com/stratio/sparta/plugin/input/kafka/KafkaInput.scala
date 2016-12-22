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
package com.stratio.sparta.plugin.input.kafka

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.Input
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.spark.sql.Row
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils

class KafkaInput(properties: Map[String, JSerializable]) extends Input(properties) with KafkaBase {

  final val DefaultPartition = 1
  final val DefaultPort = "2181"
  final val DefaultHost = "localhost"
  final val DefaultZookeeperPath = ""

  def setUp(ssc: StreamingContext, sparkStorageLevel: String): DStream[Row] = {
    val submap = properties.getMap("kafkaParams.")
    val zookeeperPath = properties.getString("zookeeper.path", DefaultZookeeperPath)
    val connection =
      Map(getZkConnectionConfs(properties, "zookeeper.connect", DefaultHost, DefaultPort, zookeeperPath))
    val kafkaParams = submap.get.map { case (entry, value) => (entry, value.toString) }

    KafkaUtils.createStream[String, Array[Byte], StringDecoder, DefaultDecoder](
      ssc,
      connection ++ kafkaParams,
      extractTopicsMap(),
      storageLevel(sparkStorageLevel))
      .map(data => Row(data._2))
  }

  def extractTopicsMap(): Map[String, Int] = {
    if (!properties.hasKey("topics")) {
      throw new IllegalStateException(s"Invalid configuration, topics must be declared.")
    }
    else {
      val topicPartition: Seq[(String, Int)] = getTopicPartition("topics", DefaultPartition)
      topicPartition.map { case (topic, partition) => topic -> partition }.toMap
    }
  }

  def getTopicPartition(key: String, defaultPartition: Int): Seq[(String, Int)] = {
    val conObj = properties.getMapFromJsoneyString(key)
    conObj.map(c =>
      (c.get("topic") match {
        case Some(value) => value.toString
        case None => throw new IllegalStateException(s"$key is mandatory")
      },
        c.get("partition") match {
          case Some(value) => value.toString.toInt
          case None => defaultPartition
        }))
  }

}