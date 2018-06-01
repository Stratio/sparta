/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.kafka

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.helper.{SchemaHelper, SecurityHelper}
import com.stratio.sparta.plugin.common.kafka.serializers.RowDeserializer
import com.stratio.sparta.plugin.common.kafka.KafkaBase
import com.stratio.sparta.sdk.DistributedMonad
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.workflow.step.InputStep
import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization._
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010._
import org.json4s.jackson.Serialization._
import org.json4s.{DefaultFormats, Formats}

import scala.util.Try
import DistributedMonad.Implicits._
import com.stratio.sparta.plugin.models.TopicModel
import com.stratio.sparta.sdk.models.{ErrorValidations, OutputOptions, WorkflowValidationMessage}
import org.apache.kafka.clients.consumer.ConsumerConfig._

class KafkaInputStepStreaming(
                               name: String,
                               outputOptions: OutputOptions,
                               ssc: Option[StreamingContext],
                               xDSession: XDSession,
                               properties: Map[String, JSerializable]
                             )
  extends InputStep[DStream](name, outputOptions, ssc, xDSession, properties) with KafkaBase with SLF4JLogging {

  lazy val outputField = properties.getString("outputField", DefaultRawDataField)
  lazy val tlsEnabled = Try(properties.getString("tlsEnabled", "false").toBoolean).getOrElse(false)
  lazy val brokerList = getBootstrapServers(BOOTSTRAP_SERVERS_CONFIG)
  lazy val serializers = getSerializers
  lazy val topics = extractTopics
  lazy val autoCommit = getAutoCommit
  lazy val autoOffset = getAutoOffset
  lazy val rowSerializerProps = getRowSerializerProperties
  lazy val groupId = getGroupId
  lazy val partitionStrategy = getPartitionStrategy
  lazy val offsets = getOffsets
  lazy val locationStrategy = getLocationStrategy
  lazy val requestTimeoutMs = Try(propertiesWithCustom.getInt(REQUEST_TIMEOUT_MS_CONFIG)).getOrElse(40 * 1000)
  lazy val heartbeatIntervalMs = Try(propertiesWithCustom.getInt(HEARTBEAT_INTERVAL_MS_CONFIG)).getOrElse(3000)
  lazy val sessionTimeOutMs = Try(propertiesWithCustom.getInt(SESSION_TIMEOUT_MS_CONFIG)).getOrElse(30000)
  lazy val fetchMaxWaitMs = Try(propertiesWithCustom.getInt(FETCH_MAX_WAIT_MS_CONFIG)).getOrElse(500)

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (brokerList.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"the bootstrap server definition is wrong", name)
      )
    if (topics.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"the topic cannot be empty", name)
      )

    if (heartbeatIntervalMs >= sessionTimeOutMs)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+
          WorkflowValidationMessage(s"the $HEARTBEAT_INTERVAL_MS_CONFIG should be lower than $SESSION_TIMEOUT_MS_CONFIG", name)
      )

    if (requestTimeoutMs <= sessionTimeOutMs)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+
          WorkflowValidationMessage(s"the $REQUEST_TIMEOUT_MS_CONFIG should be greater than $SESSION_TIMEOUT_MS_CONFIG", name)
      )

    if (requestTimeoutMs <= fetchMaxWaitMs)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+
          WorkflowValidationMessage(s"the $REQUEST_TIMEOUT_MS_CONFIG should be greater than $FETCH_MAX_WAIT_MS_CONFIG", name)
      )

    if(debugOptions.isDefined && !validDebuggingOptions)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"$errorDebugValidation", name)
      )

    validation
  }

  def init(): DistributedMonad[DStream] = {
    require(topics.nonEmpty, s"The topics can not be empty")
    require(brokerList.nonEmpty, s"The bootstrap server definition is wrong")
    require(requestTimeoutMs >= sessionTimeOutMs,
      s"The $REQUEST_TIMEOUT_MS_CONFIG should be greater than $SESSION_TIMEOUT_MS_CONFIG")
    require(requestTimeoutMs >= fetchMaxWaitMs,
      s"The $REQUEST_TIMEOUT_MS_CONFIG should be greater than $FETCH_MAX_WAIT_MS_CONFIG")
    require(heartbeatIntervalMs <= sessionTimeOutMs,
      s"The $HEARTBEAT_INTERVAL_MS_CONFIG should be lower than $SESSION_TIMEOUT_MS_CONFIG")

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
    val outputDStream = inputDStream.transform { rdd =>
      val newRdd = rdd.map(data => data.value())
      val schema = SchemaHelper.getSchemaFromSessionOrRdd(xDSession, name, newRdd)

      schema.foreach(schema => xDSession.createDataFrame(newRdd, schema).createOrReplaceTempView(name))
      newRdd
    }

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
    Map(GROUP_ID_CONFIG -> properties.getString(GROUP_ID_CONFIG, s"sparta-${System.currentTimeMillis}"))

  /** TOPICS extractions **/

  private[kafka] def extractTopics: Set[String] = {
    val topicsModel = getTopicsPartitions

    if (topicsModel.forall(topicModel => topicModel.topic.nonEmpty))
      topicsModel.map(topicPartitionModel => topicPartitionModel.topic).toSet
    else Set.empty[String]
  }

  private[kafka] def getTopicsPartitions: Seq[TopicModel] = {
    implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
    val topicsKey = s"${properties.getString("topics", None).notBlank.fold("[]") { values => values.toString }}"

    read[Seq[TopicModel]](topicsKey)
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
    Map(AUTO_OFFSET_RESET_CONFIG -> properties.getString(AUTO_OFFSET_RESET_CONFIG, "latest"))

  private[kafka] def getAutoCommit: Map[String, java.lang.Boolean] = {
    val autoCommit = properties.getBoolean(ENABLE_AUTO_COMMIT_CONFIG, default = false)
    Map(ENABLE_AUTO_COMMIT_CONFIG -> autoCommit)
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
    val strategy = properties.getString(PARTITION_ASSIGNMENT_STRATEGY_CONFIG, None) match {
      case Some("range") => classOf[RangeAssignor].getCanonicalName
      case Some("roundrobin") => classOf[RoundRobinAssignor].getCanonicalName
      case _ => classOf[RangeAssignor].getCanonicalName
    }

    Map(PARTITION_ASSIGNMENT_STRATEGY_CONFIG -> strategy)
  }
}

object KafkaInputStepStreaming {

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }

  def getSparkConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    val maxPollTimeout = "spark.streaming.kafka.consumer.poll.ms"
    val cachedKafkaConsumer = "spark.streaming.kafka.consumer.cache.enabled"
    val maxRatePerPartition = "spark.streaming.kafka.maxRatePerPartition"

    Seq(
      (maxPollTimeout, configuration.getString(maxPollTimeout, "512")),
      (maxRatePerPartition, configuration.getString(maxRatePerPartition, "0")),
      (cachedKafkaConsumer, Try(configuration.getBoolean(cachedKafkaConsumer, false)).getOrElse(false).toString)
    )
  }

}
