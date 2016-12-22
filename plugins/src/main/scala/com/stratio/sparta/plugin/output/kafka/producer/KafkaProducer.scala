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
import scala.util.Try

trait KafkaProducer {

  def send(properties: Map[String, JSerializable], topic: String, message: String): Unit = {
    val keyedMessage: KeyedMessage[String, String] = new KeyedMessage[String, String](topic, message)
    KafkaProducer.getProducer(topic, properties).send(keyedMessage)
  }
}

object KafkaProducer extends KafkaBase {

  private val DefaultHostPort: String = "localhost:9092"
  private val DefaultKafkaSerializer: String = "kafka.serializer.StringEncoder"
  private val DefaultRequiredAcks: String = "0"
  private val DefaultProducerType = "sync"
  private val DefaultBatchNumMessages = "200"
  private val DefaultPropertiesKey = "kafkaProperties"
  private val DefaultZookeeperPath = ""
  private val DefaultHost = "localhost"
  private val DefaultPort = "2181"
  private val PropertiesKey = "kafkaPropertyKey"
  private val PropertiesValue = "kafkaPropertyValue"

  private val HostKey = "host"
  private val PortKey = "port"

  private val producers: mutable.Map[String, Producer[String, String]] = mutable.Map.empty

  private val getString: ((Map[String, JSerializable], String, String) => String) = (properties, key, default) => {
    properties.get(key) match {
      case Some(v) => v.toString
      case None => throw new IllegalStateException(s"The field $key is mandatory")
    }
  }

  private val getBoolean: ((Map[String, JSerializable], String, String) => String) = (properties, key, default) => {
    properties.getString(key).toBoolean match {
      case true => "1"
      case _ => "0"
    }
  }

  private val getList: ((Map[String, JSerializable], String, String) => String) = (properties, key, default) => {
    val values = getAdditionalOptions(key, HostKey, PortKey, properties)

    if(values.nonEmpty) (for (elem <- values) yield s"${elem._1}:${elem._2}").mkString(",")
    else default
  }

  private[kafka] def getAdditionalOptions(key: String,
                                   propertyKey: String,
                                   propertyValue: String,
                                   properties: Map[String, JSerializable]): Map[String, JSerializable] =
    Try(properties.getMapFromJsoneyString(key)).getOrElse(Seq.empty[Map[String, String]])
      .map(c =>
        (c.get(propertyKey) match {
          case Some(value) => value.toString
          case None => throw new IllegalStateException(s"The field $propertyKey is mandatory")
        },
          c.get(propertyValue) match {
            case Some(value) => value.toString
            case None => throw new IllegalStateException(s"The field $propertyValue is mandatory")
          })).toMap

  private val mandatoryOptions: Map[String, ((Map[String, JSerializable], String, String) => AnyRef, String)] = Map(
    "metadata.broker.list" ->(getList, DefaultHostPort),
    "serializer.class" ->(getString, DefaultKafkaSerializer),
    "request.required.acks" ->(getBoolean, DefaultRequiredAcks),
    "producer.type" ->(getString, DefaultProducerType),
    "batch.num.messages" ->(getString, DefaultBatchNumMessages))

  private[kafka] def extractOptions(properties: Map[String, JSerializable],
                     map: Map[String, ((Map[String, JSerializable], String, String) => AnyRef, String)]): Properties = {
    val props = new Properties()
    map.foreach {
      case (key, (func, default)) =>
        properties.get(key) match {
          case Some(value) => props.put(key, func(properties, key, default))
          case None => props.put(key, default)
        }
    }
    props
  }

  private[kafka] def addOptions(options: Map[String, JSerializable], properties: Properties): Properties = {
    options.foreach { case (key, value) => properties.put(key, value.toString) }
    properties
  }

  private[kafka] def getProducer(topic: String, properties: Map[String, JSerializable]): Producer[String, String] = {
    getInstance(getProducerKey(topic, properties), properties)
  }

  private[kafka] def getInstance(key: String, properties: Map[String, JSerializable]): Producer[String, String] = {
    producers.getOrElse(key, {
      val producer = createProducer(properties)
      producers.put(key, producer)
      producer
    })
  }

  private[kafka] def createProducer(properties: Map[String, JSerializable]): Producer[String, String] = {
    val props = extractOptions(properties, mandatoryOptions)
    addOptions(getAdditionalOptions(DefaultPropertiesKey, PropertiesKey, PropertiesValue, properties), props)
    if (properties.contains("zookeeper.connect")) {
      val zookeeperPath = properties.getString("zookeeper.path", DefaultZookeeperPath)
      addOptions(
        Map(getZkConnectionConfs(properties, "zookeeper.connect", DefaultHost, DefaultPort, zookeeperPath)), props
      )
    }

    val producerConfig = new ProducerConfig(props)
    new Producer[String, String](producerConfig)
  }

  private[kafka] def getProducerKey(topic: String, properties: Map[String, JSerializable]): String = {
    s"${getList(properties, "metadata.broker.list", DefaultHostPort)}"
  }
}