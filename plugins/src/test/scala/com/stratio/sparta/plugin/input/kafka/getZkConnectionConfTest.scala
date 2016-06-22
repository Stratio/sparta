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

import com.stratio.sparta.sdk.JsoneyString
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class getZkConnectionConfTest extends WordSpec with Matchers {


  "getZkConnectionConf" should {

    "return a chain (zookeper:conection , host:port)" in {
      val conn = """[{"host": "localhost", "port": "2181"}]"""
      val props = Map("zookeeper.connect" -> JsoneyString(conn), "zookeeper.path" -> "/")
      val input = new KafkaInput(props)

      input.getZkConnectionConfs(props, "zookeeper.connect", "localhost", "2181", "") should
        be("zookeeper.connect", "localhost:2181")
    }

    "return a chain (zookeper:conection , host:port, zookeeper.path:path)" in {
      val conn = """[{"host": "localhost", "port": "2181"}]"""
      val props = Map("zookeeper.connect" -> JsoneyString(conn), "zookeeper.path" -> "/test")
      val input = new KafkaInput(props)

      input.getZkConnectionConfs(props, "zookeeper.connect", "localhost", "2181", "/test") should
        be("zookeeper.connect", "localhost:2181/test")
    }

    "return a chain (zookeper:conection , host:port,host:port,host:port)" in {
      val conn =
        """[{"host": "localhost", "port": "2181"},{"host": "localhost", "port": "2181"},
          |{"host": "localhost", "port": "2181"}]""".stripMargin
      val props = Map("zookeeper.connect" -> JsoneyString(conn))
      val input = new KafkaInput(props)

      input.getZkConnectionConfs(props, "zookeeper.connect", "localhost", "2181", "") should
        be("zookeeper.connect", "localhost:2181,localhost:2181,localhost:2181")
    }

    "return a chain (zookeper:conection , host:port,host:port,host:port, zookeeper.path:path)" in {
      val conn =
        """[{"host": "localhost", "port": "2181"},{"host": "localhost", "port": "2181"},
          |{"host": "localhost", "port": "2181"}]""".stripMargin
      val props = Map("zookeeper.connect" -> JsoneyString(conn), "zookeeper.path" -> "/test")
      val input = new KafkaInput(props)

      input.getZkConnectionConfs(props, "zookeeper.connect", "localhost", "2181", "/test") should
        be("zookeeper.connect", "localhost:2181,localhost:2181,localhost:2181/test")
    }

    "return a chain with default port (zookeper:conection , host: defaultport)" in {
      val conn =
        """[{"host": "localhost"}]"""
      val props = Map("zookeeper.connect" -> JsoneyString(conn))
      val input = new KafkaInput(props)

      input.getZkConnectionConfs(props, "zookeeper.connect", "localhost", "2181", "") should
        be("zookeeper.connect", "localhost:2181")
    }

    "return a chain with default port (zookeper:conection , host: defaultport, zookeeper.path:path)" in {
      val conn =
        """[{"host": "localhost"}]"""
      val props = Map("zookeeper.connect" -> JsoneyString(conn))
      val input = new KafkaInput(props)

      input.getZkConnectionConfs(props, "zookeeper.connect", "localhost", "2181", "/test") should
        be("zookeeper.connect", "localhost:2181/test")
    }

    "return a chain with default host and default porty (zookeeper.connect: ," +
      "defaultHost: defaultport," +
      "zookeeper.path:path)" in {
      val conn =
        """[{}]"""
      val props = Map("zookeeper.connect" -> JsoneyString(conn))
      val input = new KafkaInput(props)

      input.getZkConnectionConfs(props, "zookeeper.connect", "localhost", "2181", "") should
        be("zookeeper.connect", "localhost:2181")
    }
  }
}

