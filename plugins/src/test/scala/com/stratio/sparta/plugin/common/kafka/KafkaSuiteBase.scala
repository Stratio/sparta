/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.kafka

import java.util.Properties

import akka.event.slf4j.SLF4JLogging
import com.typesafe.config.ConfigFactory
import kafka.admin.AdminUtils
import kafka.utils.ZkUtils
import org.I0Itec.zkclient.exception.ZkMarshallingError
import org.I0Itec.zkclient.serialize.ZkSerializer
import org.I0Itec.zkclient.{ZkClient, ZkConnection}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.{Minute, Span}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpec}

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

abstract class KafkaSuiteBase extends WordSpec with Matchers with SLF4JLogging with TimeLimitedTests
  with BeforeAndAfter with BeforeAndAfterAll {

  private lazy val config = ConfigFactory.load()
  val timeLimit = Span(1, Minute)

  /**
    * Spark Properties
    */
  val DefaultSparkTimeOut = 10000L
  val SparkTimeOut = DefaultSparkTimeOut
  val conf = new SparkConf()
    .setAppName("KafkaIntegrationTest")
    .setIfMissing("spark.master", "local[*]")
  //Total messages to send and receive
  val totalRegisters = 1000

  /**
    * Kafka Properties
    */

  val keySerializer = classOf[StringSerializer]
  val hosts = Try(config.getString("kafka.hosts")) match {
    case Success(configHosts) =>
      log.info(s"Kafka hosts from config: $configHosts")
      s"$configHosts"
    case Failure(e) =>
      log.info(s"Kafka hosts from default")
      "127.0.0.1"
  }
  val zkHosts = Try(config.getString("sparta.zookeeper.connectionString")) match {
    case Success(configHosts) =>
      log.info(s"Zookeeper hosts from config: $configHosts")
      configHosts
    case Failure(e) =>
      log.info(s"Zookeeper hosts from default")
      "127.0.0.1:2181"
  }
  /**
    * Schema Registry Properties
    * */
  val schemaRegistry = Try(config.getString("schemaregistry.host")) match {
    case Success(configHost) =>
      log.info(s"Schema Registry host from config: $configHost")
      s"http://$configHost:8081"
    case Failure(e) =>
      log.info(s"Schema Registry host from default")
      "http://127.0.0.1:8081"
  }

  var sc: Option[SparkContext] = None
  var ssc: Option[StreamingContext] = None
  var sparkSession: Option[XDSession] = None

  // Create a ZooKeeper client
  val sessionTimeoutMs = 10000
  val connectionTimeoutMs = 10000
  val zkClient = new ZkClient(zkHosts, sessionTimeoutMs, connectionTimeoutMs, ZKStringSerializer)
  val numPartitions = 1
  val replicationFactor = 1
  val topicConfig = new Properties
  val zkUtils = new ZkUtils(zkClient, new ZkConnection(zkHosts), false)

  def initSpark(): Unit = {
    sc = Some(new SparkContext(conf))
    ssc = Some(new StreamingContext(sc.get, Seconds(2)))
    sparkSession = Some(XDSession.builder().config(conf).create("dummyUser"))
  }

  def stopSpark(): Unit = {
    ssc.foreach(_.stop(false, true))
    sc.foreach(_.stop())

    System.gc()
  }

  def produceEvents(topics: Seq[String]): Unit = {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, s"$hosts:9092")
    props.put("key.serializer", keySerializer)
    props.put("value.serializer", keySerializer)

    val producer = new KafkaProducer[String, String](props)
    log.info(s"Producer created")

    topics.foreach { topic =>
      for (register <- 1 to totalRegisters) yield {
        val record = new ProducerRecord[String, String](topic, register.toString)
        producer.send(record)
      }
      log.info(s"Registers produced in topic $topic")
    }

    producer.close()
  }

  def createTopics(topics: Seq[String]): Unit = {
    topics.foreach { topic =>
      AdminUtils.createTopic(zkUtils, topic, numPartitions, replicationFactor, topicConfig)
      log.info(s"Topic created: $topic")
    }
  }

  def resetTopics(topics: Seq[String]): Unit = {
    topics.foreach { topic =>
      AdminUtils.deleteTopic(zkUtils, topic)
      log.info(s"Topic deleted: $topic")
    }
  }

  before {
    log.info("Init spark")
    initSpark()
  }

  after {
    log.info("Stop spark")
    stopSpark()
  }

  override def beforeAll(): Unit = {}

  override def afterAll(): Unit = {}

  private object ZKStringSerializer extends ZkSerializer {

    @throws(classOf[ZkMarshallingError])
    def serialize(data: Object): Array[Byte] = data.asInstanceOf[String].getBytes("UTF-8")

    @throws(classOf[ZkMarshallingError])
    def deserialize(bytes: Array[Byte]): Object = {
      if (bytes == null)
        null
      else
        new String(bytes, "UTF-8")
    }
  }

}
