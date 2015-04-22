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
import java.net.ConnectException
import java.util

import akka.event.slf4j.SLF4JLogging
import com.rabbitmq.client.{QueueingConsumer, Channel, Connection, ConnectionFactory}
import com.stratio.sparkta.sdk.{JsoneyStringSerializer, JsoneyString}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import org.json4s.{JsonMethods, DefaultFormats}
import org.json4s.jackson.JsonMethods._

/**
 * Created by dcarroza on 4/17/15.
 *
 * Example of this input at https://github.com/Stratio/SpaRkTA-examples
 *
 */
class RabbitMQReceiver(properties: Map[String, JSerializable], storageLevel: StorageLevel)
  extends Receiver[String](storageLevel) with SLF4JLogging {

  val DirectExchangeType: String = "direct"
  val DefaultRabbitMQPort = 5672

  val rabbitMQQueueName = properties.getString("queue")
  val rabbitMQHost = properties.getString("host", "localhost")
  val rabbitMQPort = properties.getInt("port", DefaultRabbitMQPort)
  val exchangeName = properties.getString("exchangeName", "")
  val routingKeys = properties.get("routingKeys")

  def onStart() {
    // Start the thread that receives data over a connection
    new Thread("Socket Receiver") {
      override def run() { receive() }
    }.start()
  }

  def onStop() {
    // There is nothing much to do as the thread calling receive()
    // is designed to stop by itself isStopped() returns false
  }

  /** Create a socket connection and receive data until receiver is stopped */
  private def receive() {

    implicit val json4sJacksonFormats = DefaultFormats + new JsoneyStringSerializer()

    try {
      val factory: ConnectionFactory = new ConnectionFactory
      factory.setHost(rabbitMQHost)
      factory.setPort(rabbitMQPort)
      val connection: Connection = factory.newConnection
      val channel: Channel = connection.createChannel

      var queueName = ""
      if (routingKeys.isDefined){
        channel.exchangeDeclare(exchangeName, DirectExchangeType)
        queueName = channel.queueDeclare().getQueue()

        for (routingKey: String <- routingKeys.get.asInstanceOf[JsoneyString].toSeq) {
          channel.queueBind(queueName, exchangeName, routingKey)
        }
      }else{
        channel.queueDeclare(rabbitMQQueueName, false, false, false, new util.HashMap(0))
        queueName = rabbitMQQueueName
      }

      log.info("RabbitMQ Input waiting for messages")
      val consumer: QueueingConsumer = new QueueingConsumer(channel)
      channel.basicConsume(queueName, true, consumer)
      while (!isStopped) {
        val delivery: QueueingConsumer.Delivery = consumer.nextDelivery
        store(new String(delivery.getBody))
      }

      log.info("it has been stopped")
      channel.close
      connection.close
      restart("Trying to connect again")
    }
    catch {
      case ce: ConnectException => {
        restart("Could not connect", ce)
      }
      case t: Throwable => {
        restart("Error receiving data", t)
      }
    }
  }
}
