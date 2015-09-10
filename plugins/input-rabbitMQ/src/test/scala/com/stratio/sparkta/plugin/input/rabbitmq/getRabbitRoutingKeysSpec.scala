/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.plugin.input.rabbitmq


import com.stratio.sparkta.sdk.JsoneyString
import org.scalatest.{Matchers, WordSpec}


class getRabbitRoutingKeysSpec extends WordSpec with Matchers {

  "getRabbitRoutingKeys" should {

    "return a sequence with a routingKey" in {
      val conn = """[{"routingKey": "routingKey1"}]"""
      val input = new RabbitMQInput(Map("routingKeys" -> JsoneyString(conn)))
      input.getRabbitRoutingKeys("routingKeys") should be(Seq("routingKey1"))
    }

    "return a sequence of routingKeys" in {
      val conn = """[{"routingKey": "routingKey1"},{"routingKey": "routingKey2"}]"""
      val input = new RabbitMQInput(Map("routingKeys" -> JsoneyString(conn)))
      input.getRabbitRoutingKeys("routingKeys") should be(Seq("routingKey1", "routingKey2"))
    }
  }
}
