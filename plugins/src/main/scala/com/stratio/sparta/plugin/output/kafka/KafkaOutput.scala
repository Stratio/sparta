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

import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.plugin.workflow.input.kafka.KafkaBase
import com.stratio.sparta.sdk.pipeline.output.Output._
import com.stratio.sparta.sdk.pipeline.output.{Output, OutputFormatEnum, SaveModeEnum}
import com.stratio.sparta.sdk.properties.CustomProperties
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import org.apache.kafka.clients.producer.ProducerConfig._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.spark.sql._
import org.apache.spark.sql.crossdata.XDSession

import scala.collection.mutable

class KafkaOutput(
                   name: String,
                   sparkSession: XDSession,
                   properties: Map[String, JSerializable]
                 ) extends Output(name, sparkSession, properties) with KafkaBase with CustomProperties {

  val DefaultKafkaSerializer = classOf[StringSerializer].getName
  val DefaultAck = "0"
  val DefaultBatchNumMessages = "200"
  val DefaultProducerPort = "9092"

  override val customKey = "KafkaProperties"
  override val customPropertyKey = "kafkaPropertyKey"
  override val customPropertyValue = "kafkaPropertyValue"

  val outputFormat = OutputFormatEnum.withName(properties.getString("format", "json").toUpperCase)
  val rowSeparator = properties.getString("rowSeparator", ",")
  val securityProtocol = properties.getString("security.protocol", "PLAINTEXT")
  val sparkConf = sparkSession.conf.getAll
  val securityOpts = securityOptions(sparkConf)

  override def supportedSaveModes: Seq[SaveModeEnum.Value] = Seq(SaveModeEnum.Append)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    validateSaveMode(saveMode)

    val tableName = getTableNameFromOptions(options)

    outputFormat match {
      case OutputFormatEnum.ROW => dataFrame.rdd.foreachPartition { messages =>
        messages.foreach(message => send(tableName, message.mkString(rowSeparator), securityOpts))
      }
      case _ => dataFrame.toJSON.foreachPartition { messages =>
        messages.foreach(message => send(tableName, message, securityOpts))
      }
    }
  }

  def send(topic: String, message: String, securityProperties: Map[String, AnyRef]): Unit = {
    val record = new ProducerRecord[String, String](topic, message)
    KafkaOutput.getProducer(getProducerConnectionKey, properties,
      securityProperties, mandatoryOptions ++ getCustomProperties).send(record)
  }

  private[kafka] def getProducerConnectionKey: String =
    getHostPort(BOOTSTRAP_SERVERS_CONFIG, DefaultHost, DefaultProducerPort)
      .getOrElse(BOOTSTRAP_SERVERS_CONFIG, throw new Exception("Invalid metadata broker list"))


  private[kafka] def mandatoryOptions: Map[String, String] =
    getHostPort(BOOTSTRAP_SERVERS_CONFIG, DefaultHost, DefaultProducerPort) ++
      Map(
        KEY_SERIALIZER_CLASS_CONFIG -> properties.getString(KEY_SERIALIZER_CLASS_CONFIG, DefaultKafkaSerializer),
        VALUE_SERIALIZER_CLASS_CONFIG -> properties.getString(VALUE_SERIALIZER_CLASS_CONFIG, DefaultKafkaSerializer),
        ACKS_CONFIG -> properties.getString(ACKS_CONFIG, DefaultAck),
        BATCH_SIZE_CONFIG -> properties.getString(BATCH_SIZE_CONFIG, DefaultBatchNumMessages)
      )

  override def cleanUp(options: Map[String, String]): Unit = {
    log.info(s"Closing Kafka producer in Kafka Output: $name")
    KafkaOutput.closeProducers()
  }
}

object KafkaOutput {

  private val producers: mutable.Map[String, KafkaProducer[String, String]] = mutable.Map.empty

  def getProducer(producerKey: String,
                  properties: Map[String, JSerializable],
                  securityOptions: Map[String, AnyRef],
                  additionalProperties: Map[String, String]): KafkaProducer[String, String] =
    getInstance(producerKey, securityOptions, properties, additionalProperties)


  def createProducerProps(properties: Map[String, JSerializable],
                          calculatedProperties: Map[String, String]): Properties = {
    val props = new Properties()
    properties.foreach { case (key, value) =>
      if (value.toString.nonEmpty) props.put(key, value.toString)
    }
    calculatedProperties.foreach { case (key, value) =>
      if (value.nonEmpty) {
        props.remove(key)
        props.put(key, value)
      }
    }

    props
  }

  def closeProducers(): Unit = producers.values.foreach(producer => producer.close())

  private[kafka] def getInstance(key: String,
                                 securityOptions: Map[String, AnyRef],
                                 properties: Map[String, JSerializable],
                                 additionalProperties: Map[String, String]): KafkaProducer[String, String] =
    producers.getOrElse(key, {
      val propertiesProducer= createProducerProps(properties,
        additionalProperties ++ securityOptions.mapValues(_.toString))
      log.info(s"Creating Kafka Producer with properties:\t$propertiesProducer")
      val producer = new KafkaProducer[String, String](propertiesProducer)
      producers.put(key, producer)
      producer
    })

}

