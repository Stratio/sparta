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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class RabbitMQInputTest extends WordSpec with Matchers {

  "RabbitMQInput " should {

    "Add storage level to properties" in {
      val input = new RabbitMQInput(Map.empty[String, String])
      val result = input.propsWithStorageLevel("MEMORY_AND_DISK_SER_2")
      result should contain("storageLevel", "MEMORY_AND_DISK_SER_2")
      result should have size 1
    }

    "Add queue and host to properties" in {
      val props = Map(
        "receiverType" -> "distributed",
        "host" -> "host",
        "queue" -> "queue")
      val input = new RabbitMQInput(props)
      val result = input.propsWithStorageLevel("MEMORY_AND_DISK_SER_2")
      result should contain("host", "host")
      result should contain("queue", "queue")
      result should contain("storageLevel", "MEMORY_AND_DISK_SER_2")
      result should have size 4
    }

    "Flat rabbitmqProperties " in {
      val rabbitmqProperties =
        """
          |[{
          |   "rabbitmqPropertyKey": "host",
          |   "rabbitmqPropertyValue": "host1"
          |  },
          |  {
          |   "rabbitmqPropertyKey": "queue",
          |   "rabbitmqPropertyValue": "queue1"
          |  }
          |]
        """.stripMargin
      val props = Map(
        "rabbitmqProperties" -> rabbitmqProperties
      )
      val input = new RabbitMQInput(props)
      val result = input.propsWithStorageLevel("MEMORY_AND_DISK_SER_2")
      result should contain("host", "host1")
      result should contain("queue", "queue1")
      result should contain("storageLevel", "MEMORY_AND_DISK_SER_2")
      result should have size 4
    }

    "Fail when no rabbitmqPropertyValue in rabbitmqProperties " in {
      val rabbitmqProperties =
        """
          |[{
          |   "rabbitmqPropertyKey": "host"
          |}]
        """.stripMargin
      val props = Map("rabbitmqProperties" -> rabbitmqProperties)
      val input = new RabbitMQInput(props)
      the[IllegalArgumentException] thrownBy {
        input.propsWithStorageLevel("")
      } should have message "The field rabbitmqPropertyValue is mandatory"

    }

    "Fail when no rabbitmqPropertyKey in rabbitmqProperties " in {
      val rabbitmqProperties =
        """
          |[{
          |   "rabbitmqPropertyValue": "host1"
          |}]
        """.stripMargin
      val props = Map("rabbitmqProperties" -> rabbitmqProperties)
      val input = new RabbitMQInput(props)
      the[IllegalArgumentException] thrownBy {
        input.propsWithStorageLevel("")
      } should have message "The field rabbitmqPropertyKey is mandatory"
    }

    "Should preserve the UI fields " in {
      val rabbitmqProperties =
        """
          |[{
          |   "rabbitmqPropertyKey": "host",
          |   "rabbitmqPropertyValue": "host1"
          |  },
          |  {
          |   "rabbitmqPropertyKey": "queue",
          |   "rabbitmqPropertyValue": "queue1"
          |  }
          |]
        """.stripMargin

      val props = Map(
        "receiverType" -> "distributed",
        "host" -> "host",
        "queue" -> "queue",
        "rabbitmqProperties" -> rabbitmqProperties
      )
      val input = new RabbitMQInput(props)
      val result = input.propsWithStorageLevel("MEMORY_AND_DISK_SER_2")
      result should contain("host", "host")
      result should contain("queue", "queue")
      result should contain("storageLevel", "MEMORY_AND_DISK_SER_2")
      result should have size 5
    }

  }
}
