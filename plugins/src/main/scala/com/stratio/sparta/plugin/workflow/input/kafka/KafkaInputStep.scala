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
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.{InputStep, OutputOptions}
import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization._
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010._
import org.json4s.jackson.Serialization._
import org.json4s.{DefaultFormats, Formats}

import scala.util.Try

import DistributedMonad.Implicits._

class KafkaInputStep(
                      name: String,
                      outputOptions: OutputOptions,
                      ssc: Option[StreamingContext],
                      xDSession: XDSession,
                      properties: Map[String, JSerializable]
                    )
  extends InputStep[DStream](name, outputOptions, ssc, xDSession, properties) with KafkaBase with SLF4JLogging {

  lazy val outputField = properties.getString("outputField", DefaultRawDataField)
  lazy val outputSchema = StructType(Seq(StructField(outputField, StringType)))
  lazy val tlsEnabled = Try(properties.getString("tlsEnabled", "false").toBoolean).getOrElse(false)

  def init(): DistributedMonad[DStream] = {
    val brokerList = getHostPort("bootstrap.servers", DefaultHost, DefaultBrokerPort)
    val serializers = Map(
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[RowDeserializer]
    )
    val kafkaSecurityOptions =
      if(tlsEnabled)
        SecurityHelper.getDataStoreSecurityOptions(ssc.get.sparkContext.getConf)
      else Map.empty

    val consumerStrategy = ConsumerStrategies.Subscribe[String, Row](extractTopics, getAutoCommit ++
      getAutoOffset ++ serializers ++ getRowSerializerProperties ++ brokerList ++ getGroupId ++
      getPartitionStrategy ++ kafkaSecurityOptions ++ getCustomProperties, getOffsets)
    val inputDStream = KafkaUtils.createDirectStream[String, Row](ssc.get, getLocationStrategy, consumerStrategy)
    val outputDStream = inputDStream.map(data => data.value())


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

  //scalastyle:off
  def getRowSerializerProperties: Map[String, String] =
    Map(
      "value.deserializer.inputFormat" -> properties.getString("value.deserializer.inputFormat", "STRING"),
      "value.deserializer.json.schema.fromRow" -> properties.getBoolean("value.deserializer.json.schema.fromRow", true).toString,
      "value.deserializer.json.schema.inputMode" -> properties.getString("value.deserializer.json.schema.inputMode", "SPARKFORMAT"),
      "value.deserializer.json.schema.provided" -> properties.getString("value.deserializer.json.schema.provided", ""),
      "value.deserializer.avro.schema" -> properties.getString("value.deserializer.avro.schema", ""),
      "value.deserializer.outputField" -> outputField
    ) ++ properties.mapValues(_.toString)
      .filterKeys(key => key.contains("key.deserializer.json") || key.contains("key.deserializer.avro"))

  //scalastyle:on

  /** OFFSETS MANAGEMENT **/

  def getAutoOffset: Map[String, String] =
    Map("auto.offset.reset" -> properties.getString("auto.offset.reset", "latest"))

  def getAutoCommit: Map[String, java.lang.Boolean] = {
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
    SecurityHelper.dataStoreSecurityConf(configuration)
  }

  def getSparkConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    val maxPollTimeout = "spark.streaming.kafka.consumer.poll.ms"

    Seq((maxPollTimeout, configuration.getString(maxPollTimeout, "512")))
  }

}
