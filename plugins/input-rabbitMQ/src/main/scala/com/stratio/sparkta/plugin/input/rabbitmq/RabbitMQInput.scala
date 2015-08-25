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

package com.stratio.sparkta.plugin.input.rabbitmq

import java.io.{Serializable => JSerializable}

import com.stratio.receiver.RabbitMQUtils
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import com.stratio.sparkta.sdk.Input._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.{JsoneyString, Event, Input}

class RabbitMQInput(properties: Map[String, JSerializable]) extends Input(properties) {

  val DefaultRabbitMQPort = 5672

  val RabbitMQQueueName = properties.getString("queue")
  val RabbitMQHost = properties.getString("host", "localhost")
  val RabbitMQPort = properties.getInt("port", DefaultRabbitMQPort)
  val ExchangeName = properties.getString("exchangeName", "")
  val RoutingKeys = properties.get("routingKeys")
  val StorageLevelProperty = properties.getString("storageLevel", "MEMORY_AND_DISK_SER_2")

  override def setUp(ssc: StreamingContext): DStream[Event] = {
    RoutingKeys match {
      case Some(_) => createStreamFromRoutingKeys(ssc)
      case None => createStreamFromAQueue(ssc)
    }
  }

  private def createStreamFromRoutingKeys(ssc: StreamingContext): DStream[Event] = {
    RabbitMQUtils.createStreamFromRoutingKeys(ssc,
      RabbitMQHost,
      RabbitMQPort,
      ExchangeName,
      RoutingKeys.get.asInstanceOf[JsoneyString].toSeq,
      StorageLevel.fromString(StorageLevelProperty))
      .map(data => new Event(Map(RawDataKey -> data.getBytes("UTF-8").asInstanceOf[java.io.Serializable])))
  }

  private def createStreamFromAQueue(ssc: StreamingContext): DStream[Event] = {
    RabbitMQUtils.createStreamFromAQueue(ssc,
      RabbitMQHost,
      RabbitMQPort,
      RabbitMQQueueName,
      StorageLevel.fromString(StorageLevelProperty))
      .map(data => new Event(Map(RawDataKey -> data.getBytes("UTF-8").asInstanceOf[java.io.Serializable])))
  }
}
