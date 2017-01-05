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
package com.stratio.sparta.plugin.input.kafka


import java.io.Serializable

import com.stratio.sparta.sdk.properties.JsoneyString
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class HostPortZkTest extends WordSpec with Matchers {

  class KafkaTestInput(val properties: Map[String, Serializable]) extends KafkaBase
  
  "getHostPortZk" should {

    "return a chain (zookeper:conection , host:port)" in {
      val conn = """[{"host": "localhost", "port": "2181"}]"""
      val props = Map("zookeeper.connect" -> JsoneyString(conn), "zookeeper.path" -> "")
      val input = new KafkaTestInput(props)

      input.getHostPortZk("zookeeper.connect", "localhost", "2181") should
        be(Map("zookeeper.connect" -> "localhost:2181"))
    }

    "return a chain (zookeper:conection , host:port, zookeeper.path:path)" in {
      val conn = """[{"host": "localhost", "port": "2181"}]"""
      val props = Map("zookeeper.connect" -> JsoneyString(conn), "zookeeper.path" -> "/test")
      val input = new KafkaTestInput(props)

      input.getHostPortZk("zookeeper.connect", "localhost", "2181") should
        be(Map("zookeeper.connect" -> "localhost:2181/test"))
    }

    "return a chain (zookeper:conection , host:port,host:port,host:port)" in {
      val conn =
        """[{"host": "localhost", "port": "2181"},{"host": "localhost", "port": "2181"},
          |{"host": "localhost", "port": "2181"}]""".stripMargin
      val props = Map("zookeeper.connect" -> JsoneyString(conn))
      val input = new KafkaTestInput(props)

      input.getHostPortZk("zookeeper.connect", "localhost", "2181") should
        be(Map("zookeeper.connect" -> "localhost:2181,localhost:2181,localhost:2181"))
    }

    "return a chain (zookeper:conection , host:port,host:port,host:port, zookeeper.path:path)" in {
      val conn =
        """[{"host": "localhost", "port": "2181"},{"host": "localhost", "port": "2181"},
          |{"host": "localhost", "port": "2181"}]""".stripMargin
      val props = Map("zookeeper.connect" -> JsoneyString(conn), "zookeeper.path" -> "/test")
      val input = new KafkaTestInput(props)

      input.getHostPortZk("zookeeper.connect", "localhost", "2181") should
        be(Map("zookeeper.connect" -> "localhost:2181,localhost:2181,localhost:2181/test"))
    }

    "return a chain with default port (zookeper:conection , host: defaultport)" in {

      val props = Map("foo" -> "var")
      val input = new KafkaTestInput(props)

      input.getHostPortZk("zookeeper.connect", "localhost", "2181") should
        be(Map("zookeeper.connect" -> "localhost:2181"))
    }

    "return a chain with default port (zookeper:conection , host: defaultport, zookeeper.path:path)" in {
      val props = Map("zookeeper.path" -> "/test")
      val input = new KafkaTestInput(props)

      input.getHostPortZk("zookeeper.connect", "localhost", "2181") should
        be(Map("zookeeper.connect" -> "localhost:2181/test"))
    }

    "return a chain with default host and default porty (zookeeper.connect: ," +
      "defaultHost: defaultport," +
      "zookeeper.path:path)" in {
      val props = Map("foo" -> "var")
      val input = new KafkaTestInput(props)

      input.getHostPortZk("zookeeper.connect", "localhost", "2181") should
        be(Map("zookeeper.connect" -> "localhost:2181"))
    }
  }
}

