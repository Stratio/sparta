/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.plugin.input

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.plugin.input.rabbitmq.{RabbitMQReceiver, RabbitMQInput}
import org.apache.spark.storage.StorageLevel
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.mock.MockitoSugar

/**
 * Created by dcarroza on 4/17/15.
 */
class RabbitMQReceiverSpec extends WordSpecLike
with MockitoSugar
with Matchers {

  "A RabbitMQInput" should {
    "has just queue as mandatory configuration property" in {
      val properties: Map[String, JSerializable]
        = Map(("queue", "test".asInstanceOf[JSerializable]))

      new RabbitMQReceiver(properties, StorageLevel.MEMORY_AND_DISK)
    }

    "fail without queue parameter" in {
      val properties: Map[String, JSerializable] = Map()

      val thrown = intercept[IllegalStateException] {
        new RabbitMQReceiver(properties, StorageLevel.MEMORY_AND_DISK)
      }
      assert(thrown.getMessage === "queue is mandatory")
    }
  }

}
