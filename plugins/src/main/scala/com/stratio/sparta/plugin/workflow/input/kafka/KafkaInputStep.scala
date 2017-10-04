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

package com.stratio.sparta.plugin.workflow.input.kafka

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.plugin.common.kafka.serializers.RowDeserializer
import com.stratio.sparta.plugin.common.kafka.KafkaBase
import com.stratio.sparta.plugin.common.kafka.models.TopicsModel
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{InputStep, OutputOptions}
import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization._
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010._
import org.json4s.jackson.Serialization._
import org.json4s.{DefaultFormats, Formats}

import scala.util.Try

class KafkaInputStep(
                      name: String,
                      outputOptions: OutputOptions,
                      ssc: StreamingContext,
                      xDSession: XDSession,
                      properties: Map[String, JSerializable]
                    )
  extends InputStep(name, outputOptions, ssc, xDSession, properties) with KafkaBase with SLF4JLogging {

  override lazy val customKey = "kafkaProperties"
  override lazy val customPropertyKey = "kafkaPropertyKey"
  override lazy val customPropertyValue = "kafkaPropertyValue"

  lazy val outputField = properties.getString("outputField", DefaultRawDataField)
  lazy val outputSchema = StructType(Seq(StructField(outputField, StringType)))

  //scalastyle:off
  def initStream(): DStream[Row] = {
    val brokerList = getHostPort("bootstrap.servers", DefaultHost, DefaultBrokerPort)
    val serializerProperty = properties.getString("value.deserializer", "row")
    val serializers = Map(
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> getSerializerByKey(serializerProperty)
    )
    val kafkaSecurityOptions = securityOptions(ssc.sparkContext.getConf)

    val (inputDStream, outputDStream) = serializerProperty match {
      case "arraybyte" =>
        val consumerStrategy = ConsumerStrategies.Subscribe[String, Array[Byte]](extractTopics, getAutoCommit ++
          getAutoOffset ++ serializers ++ brokerList ++ getGroupId ++ getPartitionStrategy ++
          kafkaSecurityOptions ++ getCustomProperties, getOffsets)
        val inputStream = KafkaUtils.createDirectStream[String, Array[Byte]](ssc, getLocationStrategy, consumerStrategy)
        val outputStream = inputStream.map(data =>
          new GenericRowWithSchema(Array(new String(data.value())), outputSchema))

        (inputStream, outputStream)
      case "row" =>
        val consumerStrategy = ConsumerStrategies.Subscribe[String, Row](extractTopics, getAutoCommit ++
          getAutoOffset ++ serializers ++ getRowSerializerProperties ++ brokerList ++ getGroupId ++
          getPartitionStrategy ++ kafkaSecurityOptions ++ getCustomProperties, getOffsets)
        val inputStream = KafkaUtils.createDirectStream[String, Row](ssc, getLocationStrategy, consumerStrategy)
        val outputStream = inputStream.map(data => data.value())

        (inputStream, outputStream)
      case _ =>
        val consumerStrategy = ConsumerStrategies.Subscribe[String, String](extractTopics, getAutoCommit ++
          getAutoOffset ++ serializers ++ brokerList ++ getGroupId ++ getPartitionStrategy ++
          kafkaSecurityOptions ++ getCustomProperties, getOffsets)
        val inputStream = KafkaUtils.createDirectStream[String, String](ssc, getLocationStrategy, consumerStrategy)
        val outputStream = inputStream.map(data => new GenericRowWithSchema(Array(data.value()), outputSchema))

        (inputStream, outputStream)
    }

    if (!getAutoCommit.head._2 && getAutoCommitInKafka) {
      inputDStream.foreachRDD { rdd =>
        val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
        inputDStream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
        log.info(s"Committed Kafka offsets --> ${
          offsetRanges.map(offset =>
            s"\tTopic: ${offset.topic}, Partition: ${offset.partition}, From: ${offset.fromOffset}, until: " +
              s"${offset.untilOffset}"
          ).mkString("\n")
        }")
      }
    }

    outputDStream.asInstanceOf[DStream[Row]]
  }

  //scalastyle:on

  /** GROUP ID extractions **/

  def getGroupId: Map[String, String] =
    Map("group.id" -> properties.getString("group.id", s"sparta-${System.currentTimeMillis}"))

  /** TOPICS extractions **/

  def extractTopics: Set[String] =
    if (properties.contains("topics"))
      getTopicsPartitions.topics.map(topicPartitionModel => topicPartitionModel.topic).toSet
    else throw new IllegalArgumentException(s"Invalid configuration, topics must be declared in direct approach")

  def getTopicsPartitions: TopicsModel = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val topicsModel = read[TopicsModel](
      s"""{"topics": ${properties.get("topics").fold("[]") { values => values.toString }}}""""
    )

    if (topicsModel.topics.isEmpty)
      throw new IllegalArgumentException(s"At least one topic must be defined")
    else topicsModel
  }

  /** OFFSETS **/

  def getOffsets: Map[TopicPartition, Long] = {
    Try(properties.getMapFromArrayOfValues("offsets"))
      .getOrElse(Seq.empty[Map[String, String]])
      .flatMap(offsetSequence => getOffset(offsetSequence)).toMap
  }

  def getOffset(fields: Map[String, String]): Option[(TopicPartition, Long)] = {
    val topic = fields.get("topic").notBlank
    val partition = fields.getInt("partition", None)
    val offsetValue = fields.getLong("offsetValue", None)

    (topic, partition, offsetValue) match {
      case (Some(tp), Some(part), Some(off)) =>
        Option((new TopicPartition(tp, part), off))
      case _ => None
    }
  }

  /** SERIALIZERS **/

  def getSerializerByKey(serializerKey: String): Class[_ >: StringDeserializer with ByteArrayDeserializer
    with RowDeserializer <: Deserializer[_ >: String with Array[Byte] with Row]] =
    serializerKey match {
      case "string" => classOf[StringDeserializer]
      case "arraybyte" => classOf[ByteArrayDeserializer]
      case "row" => classOf[RowDeserializer]
      case _ => classOf[StringDeserializer]
    }

  def getRowSerializerProperties: Map[String, String] =
    Map(
      "value.deserializer.inputFormat" -> properties.getString("value.deserializer.inputFormat", "STRING"),
      "value.deserializer.schema" -> properties.getString("value.deserializer.schema", "NONE"),
      "value.deserializer.outputField" -> outputField
    ) ++ properties.mapValues(_.toString).filterKeys(key => key.contains("key.deserializer.json"))

  /** OFFSETS MANAGEMENT **/

  def getAutoOffset: Map[String, String] =
    Map("auto.offset.reset" -> properties.getString("auto.offset.reset", "latest"))

  def getAutoCommit: Map[String, java.lang.Boolean] ={
    val autoCommit = properties.getBoolean("enable.auto.commit", default = false)
    Map("enable.auto.commit" -> autoCommit)
  }

  def getAutoCommitInKafka: Boolean =
    Try(properties.getBoolean("storeOffsetInKafka")).getOrElse(true)

  /** LOCATION STRATEGY **/

  def getLocationStrategy: LocationStrategy =
    properties.getString("locationStrategy", None) match {
      case Some("preferbrokers") => LocationStrategies.PreferBrokers
      case Some("preferconsistent") => LocationStrategies.PreferConsistent
      case _ => LocationStrategies.PreferConsistent
    }

  /** PARTITION ASSIGNMENT STRATEGY **/

  def getPartitionStrategy: Map[String, String] = {
    val strategy = properties.getString("partition.assignment.strategy", None) match {
      case Some("range") => classOf[RangeAssignor].getCanonicalName
      case Some("roundrobin") => classOf[RoundRobinAssignor].getCanonicalName
      case _ => classOf[RangeAssignor].getCanonicalName
    }

    Map("partition.assignment.strategy" -> strategy)
  }
}

object KafkaInputStep {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.kafkaSecurityConf(configuration)
  }
}
