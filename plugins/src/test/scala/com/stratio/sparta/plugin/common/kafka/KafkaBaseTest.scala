/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.kafka

import java.io.Serializable

import com.stratio.sparta.plugin.helper.SecurityHelper
import com.stratio.sparta.plugin.workflow.input.kafka.KafkaInputStepStreaming
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
      val result = input.getBootstrapServers("bootstrap.servers")
      result should be(Map("bootstrap.servers" -> "kafkaHost:9092,kafkaHost2:9092"))
    }

    "return empty connection string" in {
      val hosts =
        """[
          |{"topic":"test"},{"topic":"test2"},{"topic":"test3"}
          |]
          |""".stripMargin
      val properties = Map("bootstrap.servers" -> JsoneyString(hosts))
      val input = new KafkaBaseToTest(properties)
      val result = input.getBootstrapServers("bootstrap.servers")
      result should be(Map())
    }
  }

  "securityOptions from map[string, string]" should {
    "return a correct security properties" in {
      val sparkConf = Map(
        "spark.ssl.datastore.enabled" -> "true",
        "spark.ssl.datastore.keyStore" -> "foo",
        "spark.ssl.datastore.keyStorePassword" -> "foo",
        "spark.ssl.datastore.trustStore" -> "foo",
        "spark.ssl.datastore.trustStorePassword" -> "foo",
        "spark.ssl.datastore.keyPassword" -> "foo"
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
        "spark.ssl.datastore.enabled" -> "false",
        "spark.ssl.datastore.keyStore" -> "foo",
        "spark.ssl.datastore.keyStorePassword" -> "foo",
        "spark.ssl.datastore.trustStore" -> "foo"
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
        ("spark.ssl.datastore.enabled","true"),
        ("spark.ssl.datastore.keyStore","foo"),
        ("spark.ssl.datastore.keyStorePassword","foo"),
        ("spark.ssl.datastore.trustStore","foo"),
        ("spark.ssl.datastore.trustStorePassword","foo"),
        ("spark.ssl.datastore.keyPassword","foo")
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
        ("spark.ssl.datastore.keyStore","foo"),
        ("spark.ssl.datastore.keyStorePassword","foo"),
        ("spark.ssl.datastore.trustStore","foo")
      ))
      val properties = Map.empty[String, Serializable]
      val input = new KafkaBaseToTest(properties)
      val result = SecurityHelper.getDataStoreSecurityOptions(sparkConf)
      val expected = Map.empty[String, Serializable]
      result should be(expected)
    }
  }

}

