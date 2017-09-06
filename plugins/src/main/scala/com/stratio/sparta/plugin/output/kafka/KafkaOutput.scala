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

  override def supportedSaveModes: Seq[SaveModeEnum.Value] = Seq(SaveModeEnum.Append)

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    val tableName = getTableNameFromOptions(options)
    val kafkaSecurityProperties = {
      if (securityProtocol == "SSL") getKafkaSecurityProperties(dataFrame.sparkSession.conf.getAll)
      else Map.empty[String, String]
    }

    log.debug(s"Kafka security options included in Spark Context: $kafkaSecurityProperties")

    validateSaveMode(saveMode)

    outputFormat match {
      case OutputFormatEnum.ROW => dataFrame.rdd.foreachPartition(messages =>
        messages.foreach(message => send(tableName, message.mkString(rowSeparator), kafkaSecurityProperties)))
      case _ => dataFrame.toJSON.foreachPartition { messages =>
        messages.foreach(message => send(tableName, message, kafkaSecurityProperties))
      }
    }
  }

  def send(topic: String, message: String, securityProperties: Map[String, String]): Unit = {
    val record = new ProducerRecord[String, String](topic, message)
    KafkaOutput.getProducer(getProducerConnectionKey, createProducerProps(securityProperties)).send(record)
  }

  private[kafka] def getKafkaSecurityProperties(sparkConfs: Map[String, String]): Map[String, String] =
    sparkConfs.flatMap { case (key, value) =>
      if (key.contains("spark.secret.kafka") && value.nonEmpty)
        Option((key.replace("spark.secret.kafka.", "").toLowerCase(), value))
      else None
    }

  private[kafka] def getProducerConnectionKey: String =
    getHostPort(BOOTSTRAP_SERVERS_CONFIG, DefaultHost, DefaultProducerPort)
      .getOrElse(BOOTSTRAP_SERVERS_CONFIG, throw new Exception("Invalid metadata broker list"))

  private[kafka] def createProducerProps(securityProperties: Map[String, String]): Properties = {
    val props = new Properties()
    properties.filter(_._1 != customKey).foreach { case (key, value) =>
      if (value.toString.nonEmpty) props.put(key, value.toString)
    }
    securityProperties.foreach { case (key, value) =>
      if (value.nonEmpty) {
        props.remove(key)
        props.put(key, value)
      }
    }
    mandatoryOptions.foreach { case (key, value) =>
      if (value.nonEmpty) {
        props.remove(key)
        props.put(key, value)
      }
    }
    getCustomProperties.foreach { case (key, value) =>
      if (value.nonEmpty) {
        props.remove(key)
        props.put(key, value)
      }
    }

    props
  }

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

  def getProducer(producerKey: String, properties: Properties): KafkaProducer[String, String] = {
    getInstance(producerKey, properties)
  }

  def closeProducers(): Unit = {
    producers.values.foreach(producer => producer.close())
  }

  private[kafka] def getInstance(key: String, properties: Properties): KafkaProducer[String, String] = {
    producers.getOrElse(key, {
      log.info(s"Creating Kafka Producer with properties:\t$properties")
      val producer = new KafkaProducer[String, String](properties)
      producers.put(key, producer)
      producer
    })
  }

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.kafkaSecurityConf(configuration)
  }
}

