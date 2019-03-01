/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.kafka

import java.io.{Serializable => JSerializable}
import java.util

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.core.DistributedMonad
import com.stratio.sparta.core.DistributedMonad.Implicits._
import com.stratio.sparta.core.helpers.SdkSchemaHelper
import com.stratio.sparta.core.models.{ErrorValidations, OutputOptions, WorkflowValidationMessage}
import com.stratio.sparta.core.properties.JsoneyStringSerializer
import com.stratio.sparta.core.properties.ValidatingPropertyMap._
import com.stratio.sparta.core.workflow.step.{InputStep, OneTransactionOffsetManager}
import com.stratio.sparta.plugin.common.kafka.KafkaBase
import com.stratio.sparta.plugin.common.kafka.serializers.RowDeserializer
import com.stratio.sparta.plugin.enumerations.ConsumerStrategyEnum
import com.stratio.sparta.plugin.enumerations.ConsumerStrategyEnum.ConsumerStrategyEnum
import com.stratio.sparta.plugin.helper.{SchemaHelper, SecurityHelper, SparkStepHelper}
import com.stratio.sparta.plugin.models.{TopicModel, TopicPartitionModel}
import org.apache.kafka.clients.consumer.ConsumerConfig._
import org.apache.kafka.clients.consumer._
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization._
import org.apache.spark.sql.Row
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{HasOffsetRanges, _}
import org.json4s.jackson.Serialization._
import org.json4s.{DefaultFormats, Formats}

import scala.util.Try

//scalastyle:off
class KafkaInputStepStreaming(
                               name: String,
                               outputOptions: OutputOptions,
                               ssc: Option[StreamingContext],
                               xDSession: XDSession,
                               properties: Map[String, JSerializable]
                             )
  extends InputStep[DStream](name, outputOptions, ssc, xDSession, properties) with KafkaBase with SLF4JLogging with OneTransactionOffsetManager {

  import com.stratio.sparta.plugin.models.SerializationImplicits._

  lazy val consumerPollMsKey = "spark.streaming.kafka.consumer.poll.ms"
  lazy val maxRatePerPartitionKey = "spark.streaming.kafka.maxRatePerPartition"

  lazy val outputField = properties.getString("outputField", DefaultRawDataField)
  lazy val tlsEnabled = Try(properties.getString("tlsEnabled", "false").toBoolean).getOrElse(false)
  lazy val brokerList = getBootstrapServers(BOOTSTRAP_SERVERS_CONFIG)
  lazy val serializers = getSerializers
  lazy val topics = extractTopics
  lazy val topicPartitions = extractTopicsPartitions
  lazy val autoCommit = getAutoCommit
  lazy val autoOffset = getAutoOffset
  lazy val rowSerializerProps = getRowSerializerProperties
  lazy val groupId = getGroupId
  lazy val partitionStrategy = getPartitionStrategy
  lazy val offsets = getOffsets
  lazy val locationStrategy = getLocationStrategy
  lazy val consumerStrategy = getConsumerStrategy
  lazy val requestTimeoutMs = Try(propertiesWithCustom.getInt(REQUEST_TIMEOUT_MS_CONFIG)).getOrElse(40000)
  lazy val heartbeatIntervalMs = Try(propertiesWithCustom.getInt(HEARTBEAT_INTERVAL_MS_CONFIG)).getOrElse(10000)
  lazy val sessionTimeOutMs = Try(propertiesWithCustom.getInt(SESSION_TIMEOUT_MS_CONFIG)).getOrElse(30000)
  lazy val fetchMaxWaitMs = Try(propertiesWithCustom.getInt(FETCH_MAX_WAIT_MS_CONFIG)).getOrElse(500)
  lazy val consumerPollMs = Try(propertiesWithCustom.getInt(consumerPollMsKey)).getOrElse(1000)
  lazy val maxRatePerPartition = Try(propertiesWithCustom.getInt(maxRatePerPartitionKey)).getOrElse(0)
  lazy val autoCommitInterval = Try(propertiesWithCustom.getInt(AUTO_COMMIT_INTERVAL_MS_CONFIG)).getOrElse(5000)
  lazy val maxPartitionFetchBytes = Try(propertiesWithCustom.getInt(MAX_PARTITION_FETCH_BYTES_CONFIG)).getOrElse(10485760)
  lazy val retryBackoff = Try(propertiesWithCustom.getInt(RETRY_BACKOFF_MS_CONFIG)).getOrElse(1000)
  lazy val commitOffsetRetries = Try(properties.getInt("commitOffsetsNumRetries", 3)).getOrElse(3)
  lazy val commitOffsetWait = Try(properties.getInt("commitOffsetsWait", 1000)).getOrElse(1000)

  override val executeOffsetCommit: Boolean = !getAutoCommit.head._2 && getAutoCommitInKafka

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (!SdkSchemaHelper.isCorrectTableName(name))
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"The step name $name is not valid.", name)
      )

    if (brokerList.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"the bootstrap server definition is wrong", name)
      )
    if (consumerStrategy == ConsumerStrategyEnum.SUBSCRIBE && topics.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"the topics list cannot be empty", name)
      )

    if (consumerStrategy == ConsumerStrategyEnum.ASSIGN && topicPartitions.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"the topic partitions list cannot be empty", name)
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

    if (consumerPollMs <= fetchMaxWaitMs)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+
          WorkflowValidationMessage(s"the $consumerPollMsKey should be greater than $FETCH_MAX_WAIT_MS_CONFIG", name)
      )

    if (debugOptions.isDefined && !validDebuggingOptions)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ WorkflowValidationMessage(s"$errorDebugValidation", name)
      )

    validation
  }

  def init(): DistributedMonad[DStream] = {
    val validateResult = validate()

    require(validateResult.valid, validateResult.messages.mkString(","))

    val kafkaSecurityOptions = if (tlsEnabled) {
      val securityOptions = SecurityHelper.getDataStoreSecurityOptions(ssc.get.sparkContext.getConf)
      require(securityOptions.nonEmpty,
        "The property TLS is enabled and the sparkConf does not contain security properties")
      securityOptions
    } else Map.empty
    val kafkaConsumerParams = autoCommit ++ autoOffset ++ serializers ++ rowSerializerProps ++ brokerList ++ groupId ++
      partitionStrategy ++ kafkaSecurityOptions ++ consumerProperties
    val strategy = consumerStrategy match {
      case ConsumerStrategyEnum.ASSIGN =>
        ConsumerStrategies.Assign[String, Row](topicPartitions, kafkaConsumerParams, offsets)
      case _ =>
        ConsumerStrategies.Subscribe[String, Row](topics, kafkaConsumerParams, offsets)
    }
    val inputDStream = KafkaUtils.createDirectStream[String, Row](ssc.get, locationStrategy, strategy)
    inputData = Option(inputDStream)
    val outputDStream = inputDStream.transform { rdd =>
      val newRdd = rdd.map(data => data.value())
      val schema = SchemaHelper.getSchemaFromSessionOrRdd(xDSession, name, newRdd)

      schema.foreach(schema => xDSession.createDataFrame(newRdd, schema).createOrReplaceTempView(name))
      newRdd
    }

    outputDStream.asInstanceOf[DStream[Row]]
  }

  override def commitOffsets(): Unit = {
    if (
      executeOffsetCommit &&
        inputData.isDefined &&
        inputData.get.isInstanceOf[InputDStream[ConsumerRecord[String, Row]]] &&
        inputData.get.isInstanceOf[CanCommitOffsets]
    ) {
      val inputDStream = inputData.get.asInstanceOf[InputDStream[ConsumerRecord[String, Row]]]
      inputDStream.foreachRDD { rdd =>
        rdd match {
          case offsets: HasOffsetRanges =>
            val offsetRanges = offsets.offsetRanges
            SparkStepHelper.retry(commitOffsetRetries, commitOffsetWait){
              inputDStream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges, new DisplayOffsetCommits)
            }
          case _ =>
            log.warn("The input DStream don't have offset ranges")
        }
      }
    }
  }

  /** PERFORMANCE SETTINGS **/

  private[kafka] def consumerProperties: Map[String, String] = {
    val performanceProperties = Map(
      consumerPollMsKey -> consumerPollMs.toString,
      maxRatePerPartitionKey -> maxRatePerPartition.toString,
      AUTO_COMMIT_INTERVAL_MS_CONFIG -> autoCommitInterval.toString,
      MAX_PARTITION_FETCH_BYTES_CONFIG -> maxPartitionFetchBytes.toString,
      SESSION_TIMEOUT_MS_CONFIG -> sessionTimeOutMs.toString,
      REQUEST_TIMEOUT_MS_CONFIG -> requestTimeoutMs.toString,
      HEARTBEAT_INTERVAL_MS_CONFIG -> heartbeatIntervalMs.toString,
      FETCH_MAX_WAIT_MS_CONFIG -> fetchMaxWaitMs.toString,
      RETRY_BACKOFF_MS_CONFIG -> retryBackoff.toString
    )

    getCustomProperties ++ performanceProperties
  }

  /** GROUP ID extractions **/

  private[kafka] def getGroupId: Map[String, String] =
    Map(GROUP_ID_CONFIG -> properties.getString(GROUP_ID_CONFIG, s"sparta-${System.currentTimeMillis}"))

  /** TOPICS extractions **/

  private[kafka] def extractTopics: Set[String] = {
    val topicsModel = getTopicsFromProperties

    if (topicsModel.forall(topicModel => topicModel.topic.nonEmpty))
      topicsModel.map(topicPartitionModel => topicPartitionModel.topic).toSet
    else Set.empty[String]
  }

  private[kafka] def extractTopicsPartitions: Seq[TopicPartition] = {
    val topicsKey = properties.getString("topicPartitions", None).notBlank.fold("[]") { values => values.toString }
    val topicPartitionsModel = read[Seq[TopicPartitionModel]](topicsKey)

    topicPartitionsModel.map(topicPartition => new TopicPartition(topicPartition.topic, topicPartition.partition.toInt))
  }

  private[kafka] def getTopicPartitionsFromProperties: Seq[TopicModel] = {
    val topicsKey = properties.getString("topics", None).notBlank.fold("[]") { values => values.toString }

    read[Seq[TopicModel]](topicsKey)
  }

  private[kafka] def getTopicsFromProperties: Seq[TopicModel] = {
    val topicsKey = properties.getString("topics", None).notBlank.fold("[]") { values => values.toString }

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

  private[kafka] def getConsumerStrategy: ConsumerStrategyEnum =
    Try(ConsumerStrategyEnum.withName(properties.getString("consumerStrategy")))
      .getOrElse(ConsumerStrategyEnum.SUBSCRIBE)
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

class DisplayOffsetCommits extends OffsetCommitCallback with Serializable with SLF4JLogging {
  override def onComplete(offsets: util.Map[TopicPartition, OffsetAndMetadata], exception: Exception): Unit = {
    Option(exception) match {
      case Some(ex) =>
        log.warn(s"Error committing offsets in Kafka with exception: ${ex.getLocalizedMessage}")
        throw ex
      case None =>
        import scala.collection.JavaConversions._

        val offsetsMessage = offsets.map { case (topicPartition, offsetAndMetadata) =>
          s"{TopicPartition{${topicPartition.toString}}, ${offsetAndMetadata.toString}}"
        }.mkString(",")
        log.info(s"Committed Kafka offsets and partitions --> [$offsetsMessage]")
    }
  }
}
