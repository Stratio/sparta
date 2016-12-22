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

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.spark.sql.Row
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils

class KafkaDirectInput(properties: Map[String, JSerializable])
  extends Input(properties)
    with KafkaBase
    with SLF4JLogging {

  final val DefaultHost = "localhost"
  final val DefaultPort = "2181"
  final val DefaultZookeeperPath = ""

  def setUp(ssc: StreamingContext, sparkStorageLevel: String): DStream[Row] = {

    val submap = properties.getMap("kafkaParams")

    if (submap.isDefined) {
      val zookeeperPath = properties.getString("zookeeper.path", DefaultZookeeperPath)
      val metaDataBrokerList =
        Map(getMetaDataBrokerList("metadata.broker.list", DefaultHost, DefaultPort))
      val kafkaParams = submap.get.map(entry => (entry._1, entry._2.toString))
      KafkaUtils.createDirectStream[String, Array[Byte], StringDecoder, DefaultDecoder](
        ssc,
        metaDataBrokerList ++ kafkaParams,
        extractTopicsSet())
        .map(data => Row(data._2))
    } else {
      throw new IllegalStateException(s"kafkaParams is necessary for KafkaDirectInput receiver")
    }
  }

  private def extractTopicsSet(): Set[String] = {

    if (!properties.hasKey("topics")) {
      throw new IllegalStateException(s"Invalid configuration, topics must be declared.")
    }
    getDirectTopicPartition("topics", "topic").split(",").toSet
  }

  def getDirectTopicPartition(key: String,
                              firstJsonItem: String): String = {
    val conObj = properties.getMapFromJsoneyString(key)
    conObj.map(c => {
      val topic = c.get(firstJsonItem) match {
        case Some(value) => value.toString
        case None => throw new IllegalStateException(s"$key is mandatory")
      }
      s"$topic"
    }).mkString(",")
  }

  def getMetaDataBrokerList(key: String, defaultHost: String, defaultPort: String):
  (String, String) = {
    val conObj = properties.getMapFromJsoneyString(key)
    val value = conObj.map(c => {
      val host = c.get("broker") match {
        case Some(hostValue) => hostValue.toString
        case None => defaultHost
      }
      val port = c.get("port") match {
        case Some(portValue) => portValue.toString
        case None => defaultPort
      }
      s"$host:$port"
    }).mkString(",")

    (key.toString, value)
  }

}
