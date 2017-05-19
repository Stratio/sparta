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

package com.stratio.sparta.plugin.input.rabbitmq

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.input.rabbitmq.handler.MessageHandler
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.spark.sql.Row
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.rabbitmq.RabbitMQUtils._
import org.apache.spark.streaming.rabbitmq.distributed.RabbitMQDistributedKey
import org.apache.spark.streaming.rabbitmq.models.ExchangeAndRouting

import scala.language.implicitConversions
import scala.util.Try

object RabbitMQDistributedInput {
  //Keys from UI
  val DistributedPropertyKey = "distributedProperties"
  val QueuePropertyKey = "distributedQueue"
  val ExchangeNamePropertyKey = "distributedExchangeName"
  val ExchangeTypePropertyKey = "distributedExchangeType"
  val RoutingKeysPropertyKey = "distributedRoutingKeys"
  val HostPropertyKey = "hosts"

  //Default values
  val QueueDefaultValue = "queue"
  val HostDefaultValue = "localhost"
}

class RabbitMQDistributedInput(properties: Map[String, JSerializable])
  extends Input(properties) with RabbitMQGenericProps {

  import RabbitMQDistributedInput._


  def initStream(ssc: StreamingContext, sparkStorageLevel: String): DStream[Row] = {
    val messageHandler = MessageHandler(properties).handler
    val params = propsWithStorageLevel(sparkStorageLevel)
    createDistributedStream(ssc, getKeys(params), params, messageHandler)
  }

  def getKeys(rabbitMQParams: Map[String, String]): Seq[RabbitMQDistributedKey] = {
    val items = Try(properties.getMapFromJsoneyString(DistributedPropertyKey))
      .getOrElse(Seq.empty[Map[String, String]])
    for (item <- items) yield getKey(item, rabbitMQParams)
  }

  def getKey(params: Map[String, String], rabbitMQParams: Map[String, String]): RabbitMQDistributedKey = {
    val exchangeAndRouting = ExchangeAndRouting(
      params.get(ExchangeNamePropertyKey).notBlank,
      params.get(ExchangeTypePropertyKey).notBlank,
      params.get(RoutingKeysPropertyKey).notBlank
    )
    val hosts = HostPropertyKey -> params.get(HostPropertyKey).notBlankWithDefault(HostDefaultValue)
    val queueName = params.get(QueuePropertyKey).notBlankWithDefault(QueueDefaultValue)

    RabbitMQDistributedKey(
      queueName,
      exchangeAndRouting,
      rabbitMQParams + hosts
    )
  }
}
