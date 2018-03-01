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
import com.stratio.sparta.sdk.workflow.step.{ErrorValidations, InputStep, OutputOptions}
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

class KafkaInputStepStreaming(
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
  lazy val brokerList = getHostPort("bootstrap.servers")
  lazy val serializers = getSerializers
  lazy val topics = extractTopics
  lazy val autoCommit = getAutoCommit
  lazy val autoOffset = getAutoOffset
  lazy val rowSerializerProps = getRowSerializerProperties
  lazy val groupId = getGroupId
  lazy val partitionStrategy = getPartitionStrategy
  lazy val offsets = getOffsets
  lazy val locationStrategy = getLocationStrategy

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (brokerList.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name bootstrap servers list can not be empty"
      )
    if (topics.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name the topics can not be empty"
      )

    validation
  }

  def init(): DistributedMonad[DStream] = {
    require(topics.nonEmpty, s"The topics can not be empty")
    require(brokerList.nonEmpty, s"The bootstrap servers can not be empty")

    val kafkaSecurityOptions = if (tlsEnabled) {
      val securityOptions = SecurityHelper.getDataStoreSecurityOptions(ssc.get.sparkContext.getConf)
      require(securityOptions.nonEmpty,
        "The property TLS is enabled and the sparkConf does not contain security properties")
      securityOptions
    } else Map.empty
    val consumerStrategy = ConsumerStrategies.Subscribe[String, Row](
      topics, autoCommit ++ autoOffset ++ serializers ++
        rowSerializerProps ++ brokerList ++ groupId ++ partitionStrategy ++ kafkaSecurityOptions ++ getCustomProperties,
      offsets
    )
    val inputDStream = KafkaUtils.createDirectStream[String, Row](ssc.get, locationStrategy, consumerStrategy)
    val outputDStream = inputDStream.map(data => data.value())

    if (!getAutoCommit.head._2 && getAutoCommitInKafka) {
      inputDStream.foreachRDD { rdd =>
        val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
        inputDStream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
        log.debug(s"Committed Kafka offsets --> ${
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

  private[kafka] def getGroupId: Map[String, String] =
    Map("group.id" -> properties.getString("group.id", s"sparta-${System.currentTimeMillis}"))

  /** TOPICS extractions **/

  private[kafka] def extractTopics: Set[String] =
    getTopicsPartitions.topics.map(topicPartitionModel => topicPartitionModel.topic).toSet

  private[kafka] def getTopicsPartitions: TopicsModel = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val topicsKey =
      s"""{"topics": ${properties.getString("topics", None).notBlank.fold("[]") { values => values.toString }}}"""
    read[TopicsModel](topicsKey)
  }

  /** OFFSETS **/

  private[kafka] def getOffsets: Map[TopicPartition, Long] = {
    Try(properties.getMapFromArrayOfValues("offsets"))
      .getOrElse(Seq.empty[Map[String, String]])
      .flatMap(offsetSequence => getOffset(offsetSequence)).toMap
  }

  private[kafka] def getOffset(fields: Map[String, String]): Option[(TopicPartition, Long)] = {
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
  private[kafka] def getSerializers = Map(
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[RowDeserializer]
  )

  private[kafka] def getRowSerializerProperties: Map[String, String] =
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

  private[kafka] def getAutoOffset: Map[String, String] =
    Map("auto.offset.reset" -> properties.getString("auto.offset.reset", "latest"))

  private[kafka] def getAutoCommit: Map[String, java.lang.Boolean] = {
    val autoCommit = properties.getBoolean("enable.auto.commit", default = false)
    Map("enable.auto.commit" -> autoCommit)
  }

  private[kafka] def getAutoCommitInKafka: Boolean =
    Try(properties.getBoolean("storeOffsetInKafka")).getOrElse(true)

  /** LOCATION STRATEGY **/

  private[kafka] def getLocationStrategy: LocationStrategy =
    properties.getString("locationStrategy", None) match {
      case Some("preferbrokers") => LocationStrategies.PreferBrokers
      case Some("preferconsistent") => LocationStrategies.PreferConsistent
      case _ => LocationStrategies.PreferConsistent
    }

  /** PARTITION ASSIGNMENT STRATEGY **/

  private[kafka] def getPartitionStrategy: Map[String, String] = {
    val strategy = properties.getString("partition.assignment.strategy", None) match {
      case Some("range") => classOf[RangeAssignor].getCanonicalName
      case Some("roundrobin") => classOf[RoundRobinAssignor].getCanonicalName
      case _ => classOf[RangeAssignor].getCanonicalName
    }

    Map("partition.assignment.strategy" -> strategy)
  }
}

object KafkaInputStepStreaming {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }

  def getSparkConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    val maxPollTimeout = "spark.streaming.kafka.consumer.poll.ms"

    Seq((maxPollTimeout, configuration.getString(maxPollTimeout, "512")))
  }

}
