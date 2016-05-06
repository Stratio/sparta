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
import kafka.producer
import org.apache.kafka.clients.producer.Producer
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import scala.util.Try


@RunWith(classOf[JUnitRunner])
class KafkaProducerTest extends FlatSpec with Matchers {


  val mandatoryOptions: Map[String, ((Map[String, Serializable], String, String) => AnyRef, String)] = Map(
    "metadata.broker.list" -> ((_, _, _) => "localhost:9092","localhost:9092"),
    "serializer.class" -> ((_, _, _) => "kafka.serializer.StringEncoder","kafka.serializer.StringEncoder"),
    "request.required.acks" -> ((_, _, _) => "1","1"),
    "producer.type" -> ((_, _, _) => "sync","sync"),
    "batch.num.messages" -> ((_, _, _) => "200","200")
  )

  val validProperties: Map[String, Serializable] = Map(
    "metadata.broker.list" -> """[{"host":"localhost","port":"9092"},{"host":"localhost2","port":"90922"}]""",
    "serializer.class" -> "kafka.serializer.StringEncoder",
    "request.required.acks" -> "true",
    "producer.type" -> "async",
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
    KafkaProducer.getProducerKey("myTopic", validProperties) shouldBe "localhost:9092,localhost2:90922"
  }

  "getProducerKey" should "return default connection" in {
    KafkaProducer.getProducerKey("myTopic", noValidProperties) shouldBe "localhost:9092"
  }

  "extractOptions" should "extract mandatory options" in {
    val options: Properties = KafkaProducer.extractOptions(validProperties, mandatoryOptions)
    options.size shouldBe 5
    options.get("metadata.broker.list") shouldBe "localhost:9092"
    options.get("serializer.class") shouldBe "kafka.serializer.StringEncoder"
    options.get("request.required.acks") shouldBe "1"
    options.get("producer.type") shouldBe "sync"
    options.get("batch.num.messages") shouldBe "200"
  }

  "extractOptions" should "extract default mandatory options when map is empty" in {
    val options: Properties = KafkaProducer.extractOptions(Map.empty, mandatoryOptions)
    options.size shouldBe 5
    options.get("metadata.broker.list") shouldBe "localhost:9092"
    options.get("serializer.class") shouldBe "kafka.serializer.StringEncoder"
    options.get("request.required.acks") shouldBe "1"
    options.get("producer.type") shouldBe "sync"
    options.get("batch.num.messages") shouldBe "200"
  }

  "createProducer" should "return a valid KafkaProducer" in {
    Try(KafkaProducer.createProducer(validProperties)).isSuccess shouldBe true
  }

  "createProducer" should "return exception with no valid properties" in {
    Try(KafkaProducer.createProducer(noValidProperties)).isSuccess shouldBe false
  }

  "getInstance" should "create new instance the first time" in {
    Try(KafkaProducer.getInstance("myTopic", validProperties)).isInstanceOf[Producer[String, String]]
  }
  "getInstance" should "return the same instance always" in {
    val instance = KafkaProducer.getInstance("myTopic", validProperties)

    instance should equal(KafkaProducer.getInstance("myTopic", validProperties))
  }

  "getProducer" should "return a KafKAProducer" in {
    Try(KafkaProducer.getProducer("myTopic", validProperties)).isSuccess shouldBe true

  }
}
