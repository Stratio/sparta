/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.output.kafka

import java.io.{Serializable => JSerializable}
import java.util.Properties

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.plugin.common.kafka.KafkaBase
import com.stratio.sparta.plugin.common.kafka.serializers.RowSerializer
import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.sdk.models.ErrorValidations
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputStep
import org.apache.kafka.clients.producer.ProducerConfig._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.spark.sql._
import org.apache.spark.sql.crossdata.XDSession

import scala.util.Try

class KafkaOutputStep(name: String, xDSession: XDSession, properties: Map[String, JSerializable])
  extends OutputStep(name, xDSession, properties) with KafkaBase {

  lazy val tlsEnabled: Boolean = Try(properties.getString("tlsEnabled", "false").toBoolean).getOrElse(false)
  lazy val brokerList: Map[String, String] = getBootstrapServers(BOOTSTRAP_SERVERS_CONFIG)
  lazy val keySeparator: String = properties.getString("keySeparator", ",")
  lazy val producerConnectionKey: String = name + brokerList
  lazy val mandatoryOptions: Map[String, String] = {
    getBootstrapServers(BOOTSTRAP_SERVERS_CONFIG) ++
      Map(
        KEY_SERIALIZER_CLASS_CONFIG -> classOf[StringSerializer].getName,
        VALUE_SERIALIZER_CLASS_CONFIG -> classOf[RowSerializer].getName,
        ACKS_CONFIG -> properties.getString(ACKS_CONFIG, "0"),
        BATCH_SIZE_CONFIG -> properties.getString(BATCH_SIZE_CONFIG, "200")
      )
  }

  val sparkConf: Map[String, String] = xDSession.conf.getAll
  val securityOpts: Map[String, AnyRef] = {
    if (tlsEnabled)
      SecurityHelper.getDataStoreSecurityOptions(sparkConf)
    else Map.empty
  }

  override def validate(options: Map[String, String] = Map.empty[String, String]): ErrorValidations = {
    var validation = ErrorValidations(valid = true, messages = Seq.empty)

    if (brokerList.isEmpty)
      validation = ErrorValidations(
        valid = false,
        messages = validation.messages :+ s"$name: the bootstrap server definition is wrong"
      )

    validation
  }

  override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {
    require(brokerList.nonEmpty, s"The bootstrap server definition is wrong")

    val tableName = getTableNameFromOptions(options)
    val partitionKey = options.get(PartitionByKey).notBlank

    dataFrame.rdd.foreachPartition { rows =>
      val producer = KafkaOutput.getProducer(
        producerConnectionKey,
        properties,
        securityOpts,
        mandatoryOptions ++ getCustomProperties
      )
      rows.foreach { row =>
        val recordToSend = partitionKey.map(_ => extractKeyValues(row, partitionKey))
          .map(new ProducerRecord[String, Row](tableName, _, row))
          .getOrElse(new ProducerRecord[String, Row](tableName, row))

        producer.send(recordToSend)
      }
    }
  }

  override def cleanUp(options: Map[String, String]): Unit = {
    log.info(s"Closing Kafka producer in Kafka Output: $name")
    KafkaOutput.closeProducers()
  }

  private[kafka] def extractKeyValues(row: Row, partitionKey: Option[String]): String = {
    partitionKey.get.split(",").flatMap { key =>
      Try(row.get(row.fieldIndex(key)).toString).toOption
    }.mkString(keySeparator)
  }
}

object KafkaOutput extends SLF4JLogging {

  private val producers: scala.collection.concurrent.TrieMap[String, KafkaProducer[String, Row]] =
    scala.collection.concurrent.TrieMap.empty


  /** PUBLIC METHODS **/

  def getProducer(
                   producerKey: String,
                   properties: Map[String, JSerializable],
                   securityOptions: Map[String, AnyRef],
                   additionalProperties: Map[String, String]
                 ): KafkaProducer[String, Row] =
  synchronized(getInstance(producerKey, securityOptions, properties, additionalProperties))

  def closeProducers(): Unit = {
    producers.values.foreach(producer => producer.close())
    producers.clear()
  }


  /** PRIVATE METHODS **/

  private[kafka] def createProducerProps(
                                          properties: Map[String, JSerializable],
                                          calculatedProperties: Map[String, String]
                                        ): Properties = {
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

  private[kafka] def getInstance(
                                  key: String,
                                  securityOptions: Map[String, AnyRef],
                                  properties: Map[String, JSerializable],
                                  additionalProperties: Map[String, String]
                                ): KafkaProducer[String, Row] =
    producers.getOrElse(key, {
      val propertiesProducer = createProducerProps(properties,
        additionalProperties ++ securityOptions.mapValues(_.toString))
      log.debug(s"Creating Kafka Producer with properties:\t$propertiesProducer")
      val producer = new KafkaProducer[String, Row](propertiesProducer)
      producers.put(key, producer)
      producer
    })
}

object KafkaOutputStep{

  def getSparkSubmitConfiguration(configuration: Map[String, JSerializable]): Seq[(String, String)] = {
    SecurityHelper.dataStoreSecurityConf(configuration)
  }
}
