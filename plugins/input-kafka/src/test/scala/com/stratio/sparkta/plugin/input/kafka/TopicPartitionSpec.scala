/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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

package com.stratio.sparkta.plugin.input.kafka

import com.stratio.sparkta.sdk.{JsoneyString, ValidatingPropertyMap}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class TopicPartitionSpec extends WordSpec with Matchers{

  "getTopicPartition" should {
    "return a tuples (topic,partition)" in {
      val conn = """[{"topic":"test","partition":"1"}]"""
      val input = new KafkaInput(Map("topics" -> JsoneyString(conn)))
      input.getTopicPartition("topics", 1) should be (List(("test", 1)))
    }

    "return a sequence of tuples (topic,partition)" in {

      val conn =
        """[{"topic":"test","partition":"1"},
          |{"topic":"test2","partition":"2"},{"topic":"test3","partition":"3"}]""".stripMargin
      val input = new KafkaInput(Map("topics" -> JsoneyString(conn)))
      input.getTopicPartition("topics", 1) should be (List(("test", 1),("test2", 2),("test3", 3)))
    }


    "return a sequence of tuples with partition setted by default (topic,partition)" in {

      val conn =
        """[{"topic":"test"},
          |{"topic":"test2"},{"topic":"test3"}]""".stripMargin
      val input = new KafkaInput(Map("topics" -> JsoneyString(conn)))
      input.getTopicPartition("topics", 1) should be (List(("test", 1),("test2", 1),("test3", 1)))
    }
  }

}
