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

import java.net.ConnectException
import java.util

import akka.event.slf4j.SLF4JLogging
import com.rabbitmq.client.{QueueingConsumer, Channel, Connection, ConnectionFactory}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver

/**
 * Created by dcarroza on 4/17/15.
 */
class RabbitMQReceiver(rabbitMQHost: String, rabbitMQPort: Int, rabbitMQQueueName: String, storageLevel: StorageLevel)
  extends Receiver[String](StorageLevel.MEMORY_AND_DISK_2) with SLF4JLogging {

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
    try {
      val factory: ConnectionFactory = new ConnectionFactory
      factory.setHost(rabbitMQHost)
      factory.setPort(rabbitMQPort)
      val connection: Connection = factory.newConnection
      val channel: Channel = connection.createChannel
      channel.queueDeclare(rabbitMQQueueName, false, false, false, new util.HashMap(0))
      log.info("RabbitMQ Input waiting for messages")
      val consumer: QueueingConsumer = new QueueingConsumer(channel)
      channel.basicConsume(rabbitMQQueueName, true, consumer)
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
