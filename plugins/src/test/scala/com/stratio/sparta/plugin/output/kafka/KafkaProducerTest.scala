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

import java.io.Serializable
import java.util.Properties

import com.stratio.sparta.plugin.output.kafka.producer.KafkaProducer
import org.apache.log4j.Logger
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.util._

@RunWith(classOf[JUnitRunner])
class KafkaProducerTest extends FlatSpec with Matchers {

  val log = Logger.getRootLogger

  class KafkaOutputTest(val properties: Map[String, Serializable]) extends KafkaProducer

  val mandatoryOptions: Map[String, Serializable] = Map(
    "metadata.broker.list" -> """[{"host":"localhost","port":"9092"}]""",
    "serializer.class" -> "kafka.serializer.StringEncoder",
    "request.required.acks" -> 1,
    "producer.type" -> "sync",
    "batch.num.messages" -> "200")

  val validProperties: Map[String, Serializable] = Map(
    "metadata.broker.list" -> """[{"host":"localhost","port":"9092"},{"host":"localhost2","port":"90922"}]""",
    "serializer.class" -> "kafka.serializer.StringEncoder",
    "request.required.acks" -> "true",
    "producer.type" -> "async",
    "batch.num.messages" -> 200
  )

  val validZkProperties: Map[String, Serializable] = Map(
    "metadata.broker.list" -> """[{"host":"localhost","port":"9092"},{"host":"localhost2","port":"90922"}]""",
    "serializer.class" -> "kafka.serializer.StringEncoder",
    "request.required.acks" -> "true",
    "producer.type" -> "async",
    "zookeeper.connect" -> """[{"host":"localhost","port":"2181"}]""",
    "zookeeper.path" -> "/sparta",
    "batch.num.messages" -> 200
  )

  val noValidProperties: Map[String, Serializable] = Map(
    "metadata.broker.list" -> "",
    "serializer.class" -> "",
    "request.required.acks" -> "",
    "producer.type" -> "",
    "batch.num.messages" -> ""
  )

  "getProducerKey" should "concatenate topic with broker list" in {
    val kafkatest = new KafkaOutputTest(validProperties)

    kafkatest.getProducerConnectionKey shouldBe "localhost:9092,localhost2:90922"
  }

  "getProducerKey" should "return default connection" in {
    val kafkatest = new KafkaOutputTest(noValidProperties)

    kafkatest.getProducerConnectionKey shouldBe "localhost:9092"
  }

  "extractOptions" should "extract mandatory options" in {
    val kafkatest = new KafkaOutputTest(mandatoryOptions)

    val options: Properties = kafkatest.extractMandatoryProperties
    options.size shouldBe 5
    options.get("metadata.broker.list") shouldBe "localhost:9092"
    options.get("serializer.class") shouldBe "kafka.serializer.StringEncoder"
    options.get("request.required.acks") shouldBe "1"
    options.get("producer.type") shouldBe "sync"
    options.get("batch.num.messages") shouldBe "200"
  }

  "extractOptions" should "extract default mandatory options when map is empty" in {

    val kafkatest = new KafkaOutputTest(Map.empty)

    val options: Properties = kafkatest.extractMandatoryProperties
    options.size shouldBe 5
    options.get("metadata.broker.list") shouldBe "localhost:9092"
    options.get("serializer.class") shouldBe "kafka.serializer.StringEncoder"
    options.get("request.required.acks") shouldBe "0"
    options.get("producer.type") shouldBe "sync"
    options.get("batch.num.messages") shouldBe "200"
  }

  "extractOptions" should "create a correct properties file" in {
    val kafkatest = new KafkaOutputTest(mandatoryOptions)

    val options: Properties = kafkatest.createProducerProps
    options.size shouldBe 5
    options.get("metadata.broker.list") shouldBe "localhost:9092"
    options.get("serializer.class") shouldBe "kafka.serializer.StringEncoder"
    options.get("request.required.acks") shouldBe "1"
    options.get("producer.type") shouldBe "sync"
    options.get("batch.num.messages") shouldBe "200"
  }

  "extractOptions" should "create a correct properties file with zookeeper path" in {
    val kafkatest = new KafkaOutputTest(validZkProperties)

    val options: Properties = kafkatest.createProducerProps
    options.size shouldBe 6
    options.get("metadata.broker.list") shouldBe "localhost:9092,localhost2:90922"
    options.get("zookeeper.connect") shouldBe "localhost:2181/sparta"
    options.get("serializer.class") shouldBe "kafka.serializer.StringEncoder"
    options.get("request.required.acks") shouldBe "1"
    options.get("producer.type") shouldBe "async"
    options.get("batch.num.messages") shouldBe "200"
  }

  "createProducer" should "return a valid KafkaProducer without zookeeper config" in {
    val kafkatest = new KafkaOutputTest(mandatoryOptions)

    val options: Properties = kafkatest.extractMandatoryProperties
    val createProducer = Try(KafkaProducer.getProducer("key", options))

    createProducer match {
      case Success(some) => log.info("Test OK!")
      case Failure(e) => log.error("Test KO", e)
    }

    createProducer.isSuccess shouldBe true
  }
}
