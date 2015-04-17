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

import com.stratio.sparkta.sdk.Input._
import com.stratio.sparkta.sdk.{Event, Input}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream

import com.stratio.sparkta.sdk.ValidatingPropertyMap._

/**
 * Created by dcarroza on 4/17/15.
 */
class RabbitMQInput(properties: Map[String, JSerializable]) extends Input(properties){

  val DefaultRabbitMQPort = 5672

  val rabbitMQQueueName = properties.getString("queue")
  val rabbitMQHost = properties.getString("host", "localhost")
  val rabbitMQPort = properties.getInt("port", DefaultRabbitMQPort)
  val storageLevel = properties.getOrElse("storageLevel", StorageLevel.MEMORY_ONLY)

  override def setUp(ssc: StreamingContext): DStream[Event] = {
    val receiverStream =
      ssc.receiverStream(
        new RabbitMQReceiver(rabbitMQHost,
          rabbitMQPort,
          rabbitMQQueueName,
          storageLevel.asInstanceOf[StorageLevel]))

    receiverStream.
      map(data => new Event(Map(RAW_DATA_KEY -> data.getBytes("UTF-8").asInstanceOf[java.io.Serializable])))

  }
}
