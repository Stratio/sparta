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

package com.stratio.sparta.plugin.workflow.input.kafka

import org.scalatest.{Matchers, WordSpec}
import com.stratio.sparta.sdk.properties.JsoneyString
import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.workflow.input.kafka.KafkaBase

class DirectExtractTopicsTest extends WordSpec with Matchers {

  class KafkaTestInput(val properties: Map[String, JSerializable]) extends KafkaBase
  
  "Topics match" should {

    "return a tuples (topic,partition)" in {
      val topics =
        """[
          |{
          |   "topic":"test"
          |}
          |]
          |""".stripMargin

      val properties = Map("topics" -> JsoneyString(topics))
      val input = new KafkaTestInput(properties)
      input.extractTopics should be(Set("test"))
    }

    "return a sequence of tuples (topic,partition)" in {
      val topics =
        """[
          |{"topic":"test"},{"topic":"test2"},{"topic":"test3"}
          |]
          |""".stripMargin
      val properties = Map("topics" -> JsoneyString(topics))
      val input = new KafkaTestInput(properties)
      input.extractTopics should be(Set("test","test2","test3"))
    }
  }
}
