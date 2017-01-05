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

package com.stratio.sparta.plugin.output.kafka.producer

import java.io.{Serializable => JSerializable}
import java.util.Properties

import com.stratio.sparta.plugin.input.kafka.KafkaBase
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import kafka.producer.{KeyedMessage, Producer, ProducerConfig}

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

trait KafkaProducer extends KafkaBase {

  val DefaultKafkaSerializer = "kafka.serializer.StringEncoder"
  val DefaultProducerType = "sync"
  val DefaultRequiredAck = false
  val DefaultBatchNumMessages = "200"
  val DefaultProducerPort = "9092"

  def send(properties: Map[String, JSerializable], topic: String, message: String): Unit = {
    val keyedMessage: KeyedMessage[String, String] = new KeyedMessage[String, String](topic, message)
    KafkaProducer.getProducer(getProducerConnectionKey, createProducerProps).send(keyedMessage)
  }

  def getProducerConnectionKey: String =
    getHostPort("metadata.broker.list", DefaultHost, DefaultProducerPort)
      .getOrElse("metadata.broker.list", throw new Exception("Invalid metadata broker list"))

  def createProducerProps: Properties = {
    val props = extractMandatoryProperties

    addOptions(getAdditionalOptions(DefaultPropertiesKey, PropertiesKey, PropertiesValue), props)
    props
  }

  def extractMandatoryProperties: Properties = {
    val props = new Properties()

    mandatoryOptions.foreach { case (key, value) => props.put(key, value)}
    props
  }

  def mandatoryOptions: Map[String, String] = {
    def booleanToString(b:Boolean): String = if (b) "1" else "0"

    getHostPort("metadata.broker.list", DefaultHost, DefaultProducerPort) ++
      Map(
        "serializer.class" -> properties.getString("serializer.class", DefaultKafkaSerializer),
        "request.required.acks" -> booleanToString(Try(properties.getBoolean("request.required.acks"))
          .getOrElse(DefaultRequiredAck)),
        "producer.type" -> properties.getString("producer.type", DefaultProducerType),
        "batch.num.messages" ->  properties.getString("batch.num.messages", DefaultBatchNumMessages)
      )
  }

  def addOptions(options: Map[String, String], properties: Properties): Properties = {
    options.foreach { case (key, value) => properties.put(key, value) }
    properties
  }

}

object KafkaProducer {

  val producers: mutable.Map[String, Producer[String, String]] = mutable.Map.empty

  def getProducer(producerKey: String, properties: Properties): Producer[String, String] = {
    getInstance(producerKey, properties)
  }

  private[kafka] def getInstance(key: String, properties: Properties): Producer[String, String] = {
    producers.getOrElse(key, {
      val producer = createProducer(properties)
      producers.put(key, producer)
      producer
    })
  }

  private[kafka] def createProducer(properties: Properties): Producer[String, String] =
    new Producer[String, String](new ProducerConfig(properties))
}