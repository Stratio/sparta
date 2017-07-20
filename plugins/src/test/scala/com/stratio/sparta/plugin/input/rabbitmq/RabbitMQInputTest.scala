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
package com.stratio.sparta.plugin.input.rabbitmq

import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class RabbitMQInputTest extends WordSpec with Matchers with MockitoSugar{

  val DefaultStorageLevel = "MEMORY_AND_DISK_SER_2"
  val customKey = "inputOptions"
  val customPropertyKey = "inputOptionsKey"
  val customPropertyValue = "inputOptionsValue"
  val sparkSession = mock[XDSession]
  val ssc = mock[StreamingContext]

  "RabbitMQInput " should {

    "Add storage level to properties" in {
      val input = new RabbitMQInput("rabbitmq", ssc, sparkSession, Map.empty[String, String])
      val result = input.propsWithStorageLevel(DefaultStorageLevel)
      result should contain("storageLevel", DefaultStorageLevel)
      result should have size 1
    }

    "Add queue and host to properties" in {
      val props = Map(
        "host" -> "host",
        "queue" -> "queue")
      val input = new RabbitMQInput("rabbitmq", ssc, sparkSession, props)
      val result = input.propsWithStorageLevel(DefaultStorageLevel)
      result should contain("host", "host")
      result should contain("queue", "queue")
      result should contain("storageLevel", DefaultStorageLevel)
      result should have size 3
    }

    "Flat rabbitmqProperties " in {
      val rabbitmqProperties =
        s"""
          |[{
          |   "$customPropertyKey": "host",
          |   "$customPropertyValue": "host1"
          |  },
          |  {
          |   "$customPropertyKey": "queue",
          |   "$customPropertyValue": "queue1"
          |  }
          |]
        """.stripMargin
      val props = Map(customKey -> rabbitmqProperties)
      val input = new RabbitMQInput("rabbitmq", ssc, sparkSession, props)
      val result = input.propsWithStorageLevel(DefaultStorageLevel)
      result should contain("host", "host1")
      result should contain("queue", "queue1")
      result should contain("storageLevel", DefaultStorageLevel)
      result should have size 4
    }

    "Fail when no inputOptionsValue in customProperties " in {
      val rabbitmqProperties =
        s"""
          |[{
          |   "$customPropertyKey": "host"
          |}]
        """.stripMargin
      val props = Map(customKey -> rabbitmqProperties)
      the[IllegalStateException] thrownBy {
        new RabbitMQInput("rabbitmq", ssc, sparkSession, props)
      } should have message "The field inputOptionsValue is mandatory"

    }

    "Fail when no inputOptionsKey in customProperties" in {
      val rabbitmqProperties =
        s"""
          |[{
          |   "$customPropertyValue": "host1"
          |}]
        """.stripMargin
      val props = Map(customKey -> rabbitmqProperties)
      the[IllegalStateException] thrownBy {
        new RabbitMQInput("rabbitmq", ssc, sparkSession, props)
      } should have message "The field inputOptionsKey is mandatory"
    }

    "Should preserve the UI fields " in {
      val rabbitmqProperties =
        s"""
           |[{
           |   "$customPropertyKey": "host",
           |   "$customPropertyValue": "host1"
           |  },
           |  {
           |   "$customPropertyKey": "queue",
           |   "$customPropertyValue": "queue1"
           |  }
           |]
        """.stripMargin

      val props = Map(
        "host" -> "host",
        "queue" -> "queue",
        customKey -> rabbitmqProperties
      )
      val input = new RabbitMQInput("rabbitmq", ssc, sparkSession, props)
      val result = input.propsWithStorageLevel(DefaultStorageLevel)
      result should contain("host", "host")
      result should contain("queue", "queue")
      result should contain("storageLevel", DefaultStorageLevel)
      result should have size 4
    }
  }
}
