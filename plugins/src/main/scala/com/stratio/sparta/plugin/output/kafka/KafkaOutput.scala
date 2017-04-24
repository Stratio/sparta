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

package com.stratio.sparta.plugin.output.kafka

import java.io.{Serializable => JSerializable}
import java.util.Properties

import com.stratio.sparta.plugin.input.kafka.KafkaBase
import com.stratio.sparta.sdk.pipeline.output.Output._
import com.stratio.sparta.sdk.pipeline.output.{Output, OutputFormatEnum, SaveModeEnum}
import com.stratio.sparta.sdk.properties.CustomProperties
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.kafka.clients.producer.ProducerConfig._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.spark.sql._

import scala.collection.mutable

class KafkaOutput(name: String, properties: Map[String, JSerializable])
  extends Output(name, properties) with KafkaBase with CustomProperties {

  val DefaultKafkaSerializer = classOf[StringSerializer].getName
  val DefaultAck = "0"
  val DefaultBatchNumMessages = "200"
  val DefaultProducerPort = "9092"

  override val customKey = "KafkaProperties"
  override val customPropertyKey = "kafkaPropertyKey"
  override val customPropertyValue = "kafkaPropertyValue"

  val outputFormat = OutputFormatEnum.withName(properties.getString("format", "json").toUpperCase)
  val rowSeparator = properties.getString("rowSeparator", ",")

  override def supportedSaveModes: Seq[SaveModeEnum.Value] = Seq(SaveModeEnum.Append)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val tableName = getTableNameFromOptions(options)

    validateSaveMode(saveMode)

    outputFormat match {
      case OutputFormatEnum.ROW => dataFrame.rdd.foreachPartition(messages =>
        messages.foreach(message => send(tableName, message.mkString(rowSeparator))))
      case _ => dataFrame.toJSON.foreachPartition { messages =>
        messages.foreach(message => send(tableName, message))
      }
    }
  }

  def send(topic: String, message: String): Unit = {
    val record = new ProducerRecord[String, String](topic, message)
    KafkaOutput.getProducer(getProducerConnectionKey, createProducerProps).send(record)
  }

  private[kafka] def getProducerConnectionKey: String =
    getHostPort(BOOTSTRAP_SERVERS_CONFIG, DefaultHost, DefaultProducerPort)
      .getOrElse(BOOTSTRAP_SERVERS_CONFIG, throw new Exception("Invalid metadata broker list"))

  private[kafka] def createProducerProps: Properties = {
    val props = new Properties()
    properties.foreach { case (key, value) => props.put(key, value.toString) }
    mandatoryOptions.foreach { case (key, value) => props.put(key, value) }
    getCustomProperties.foreach { case (key, value) => props.put(key, value) }
    props
  }

  private[kafka] def mandatoryOptions: Map[String, String] =
    getHostPort(BOOTSTRAP_SERVERS_CONFIG, DefaultHost, DefaultProducerPort) ++
      Map(
        KEY_SERIALIZER_CLASS_CONFIG -> properties.getString(KEY_SERIALIZER_CLASS_CONFIG, DefaultKafkaSerializer),
        VALUE_SERIALIZER_CLASS_CONFIG -> properties.getString(VALUE_SERIALIZER_CLASS_CONFIG, DefaultKafkaSerializer),
        ACKS_CONFIG -> properties.getString(ACKS_CONFIG, DefaultAck),
        "security.protocol" -> "SSL",
        BATCH_SIZE_CONFIG -> properties.getString(BATCH_SIZE_CONFIG, DefaultBatchNumMessages)
      )
}

object KafkaOutput {

  private val producers: mutable.Map[String, KafkaProducer[String, String]] = mutable.Map.empty

  def getProducer(producerKey: String, properties: Properties): KafkaProducer[String, String] = {
    getInstance(producerKey, properties)
  }

  private[kafka] def getInstance(key: String, properties: Properties): KafkaProducer[String, String] = {
    producers.getOrElse(key, {
      val producer = new KafkaProducer[String, String](properties)
      producers.put(key, producer)
      producer
    })
  }
}

