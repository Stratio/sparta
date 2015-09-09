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


class KafkaInput(properties: Map[String, JSerializable]) extends Input(properties) {

  final val defaultPartition = 1
  final val defaulPort = "2181"
  final val defaultHost = "localhost"

  override def setUp(ssc: StreamingContext, sparkStorageLevel: String): DStream[Event] = {
    val submap: Option[Map[String, JSerializable]] = properties.getMap("kafkaParams")
    val connection = Map(getZkConnectionConfs("zookeeper.connect", defaultHost, defaulPort))
    val kafkaParams = submap.get.map(entry => (entry._1, entry._2.toString))

      KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](
        ssc,
        connection ++ kafkaParams,
        extractTopicsMap,
        storageLevel(sparkStorageLevel))
        .map(data => new Event(Map(RawDataKey ->{
        data._2.getBytes("UTF-8").asInstanceOf[java.io.Serializable]
      })))

  }

  def extractTopicsMap(): Map[String, Int] = {
    if (!properties.hasKey("topics")) {
      throw new IllegalStateException(s"Invalid configuration, topics must be declared.")
    }
    else {
      val topicPartition: Seq[(String, Int)] = getTopicPartition("topics", defaultPartition)
      val topicPartitionMap = topicPartition.map(tuple=>(tuple._1 -> tuple._2)).toMap
      topicPartitionMap
    }
  }

  def getTopicPartition(key: String, defaultPartition: Int): Seq[(String, Int)] ={
    val conObj = properties.getConnectionChain(key)
    conObj.map(c =>
      (c.get("topic") match{
        case Some(value) => value.toString
        case None => throw new IllegalStateException(s"$key is mandatory")
      },
        c.get("partition") match {
          case Some(value) => value.toString.toInt
          case None => defaultPartition
        }))
  }

  def getZkConnectionConfs(key: String, defaultHost: String, defaultPort: String): (String, String) = {
    val conObj = properties.getConnectionChain(key)
    val value = conObj.map(c => {
      val host = c.get("host") match {
        case Some(value) => value.toString
        case None => defaultHost
      }
      val port = c.get("port") match {
        case Some(value) => value.toString
        case None => defaultPort
      }
      s"$host:$port"
    }).mkString(",")
    (key.toString, value)
  }
}