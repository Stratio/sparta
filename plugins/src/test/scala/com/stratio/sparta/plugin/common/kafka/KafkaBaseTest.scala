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
package com.stratio.sparta.plugin.common.kafka

import java.io.Serializable

import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.plugin.workflow.input.kafka.KafkaInputStep
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.apache.spark.SparkConf
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class KafkaBaseTest extends WordSpec with Matchers {

  class KafkaBaseToTest(val properties: Map[String, Serializable]) extends KafkaBase


  "getHostPort" should {
    "return a correct connection string" in {
      val hosts =
        """
          |[
          |{
          |   "host":"kafkaHost",
          |   "port":"9092"
          |},
          |{
          |   "host":"kafkaHost2",
          |   "port":"9092"
          |}
          |]
          |""".stripMargin

      val properties = Map("bootstrap.servers" -> JsoneyString(hosts))
      val input = new KafkaBaseToTest(properties)
      val result = input.getHostPort("bootstrap.servers", "kafka", "9092")
      result should be(Map("bootstrap.servers" -> "kafkaHost:9092,kafkaHost2:9092"))
    }

    "return default connection string" in {
      val hosts =
        """[
          |{"topic":"test"},{"topic":"test2"},{"topic":"test3"}
          |]
          |""".stripMargin
      val properties = Map("bootstrap.servers" -> JsoneyString(hosts))
      val input = new KafkaBaseToTest(properties)
      val result = input.getHostPort("bootstrap.servers", "kafka", "9092")
      result should be(Map("bootstrap.servers" -> "kafka:9092"))
    }
  }

  "securityOptions from map[string, string]" should {
    "return a correct security properties" in {
      val sparkConf = Map(
        "spark.ssl.kafka.enabled" -> "true",
        "spark.ssl.kafka.keyStore" -> "foo",
        "spark.ssl.kafka.keyStorePassword" -> "foo",
        "spark.ssl.kafka.trustStore" -> "foo",
        "spark.ssl.kafka.trustStorePassword" -> "foo",
        "spark.ssl.kafka.keyPassword" -> "foo"
      )
      val properties = Map.empty[String, Serializable]
      val input = new KafkaBaseToTest(properties)
      val result = SecurityHelper.getDataStoreSecurityOptions(sparkConf)
      val expected = Map(
        "security.protocol" -> "SSL",
        "ssl.key.password" -> "foo",
        "ssl.keystore.location" -> "foo",
        "ssl.keystore.password" -> "foo",
        "ssl.truststore.location" -> "foo",
        "ssl.truststore.password" -> "foo"
      )
      result should be(expected)
    }

    "return a empty security properties" in {
      val sparkConf = Map(
        "spark.ssl.kafka.enabled" -> "true",
        "spark.ssl.kafka.keyStore" -> "foo",
        "spark.ssl.kafka.keyStorePassword" -> "foo",
        "spark.ssl.kafka.trustStore" -> "foo"
      )
      val properties = Map.empty[String, Serializable]
      val input = new KafkaBaseToTest(properties)
      val result = SecurityHelper.getDataStoreSecurityOptions(sparkConf)
      val expected = Map.empty[String, Serializable]
      result should be(expected)
    }
  }

  "securityOptions from sparkconf" should {
    "return a correct security properties" in {
      val sparkConf = new SparkConf()
      sparkConf.setAll(Seq(
        ("spark.ssl.kafka.enabled","true"),
        ("spark.ssl.kafka.keyStore","foo"),
        ("spark.ssl.kafka.keyStorePassword","foo"),
        ("spark.ssl.kafka.trustStore","foo"),
        ("spark.ssl.kafka.trustStorePassword","foo"),
        ("spark.ssl.kafka.keyPassword","foo")
      ))
      val properties = Map.empty[String, Serializable]
      val input = new KafkaBaseToTest(properties)
      val result = SecurityHelper.getDataStoreSecurityOptions(sparkConf)
      val expected = Map(
        "security.protocol" -> "SSL",
        "ssl.key.password" -> "foo",
        "ssl.keystore.location" -> "foo",
        "ssl.keystore.password" -> "foo",
        "ssl.truststore.location" -> "foo",
        "ssl.truststore.password" -> "foo"
      )
      result should be(expected)
    }

    "return a empty security properties" in {
      val sparkConf = new SparkConf()
      sparkConf.setAll(Seq(
        ("spark.ssl.kafka.enabled","true"),
        ("spark.ssl.kafka.keyStore","foo"),
        ("spark.ssl.kafka.keyStorePassword","foo"),
        ("spark.ssl.kafka.trustStore","foo")
      ))
      val properties = Map.empty[String, Serializable]
      val input = new KafkaBaseToTest(properties)
      val result = SecurityHelper.getDataStoreSecurityOptions(sparkConf)
      val expected = Map.empty[String, Serializable]
      result should be(expected)
    }
  }

}

