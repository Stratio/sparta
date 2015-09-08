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


package com.stratio.sparkta.sdk

import org.junit.runner.RunWith
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class getZkConnectionConfSpec extends WordSpec with Matchers {


  "getZkConnectionConf" should {

    "return a chain (zookeper:conection , host:port)" in {
      val conn = """[{"host": "localhost", "port": "2181"}]"""
      val validating: ValidatingPropertyMap[String, JsoneyString] =
        new ValidatingPropertyMap[String, JsoneyString](Map("zookeeper.connect" -> JsoneyString(conn)))
      validating.getZkConnectionConfs("zookeeper.connect", "localhost", "2181") should
        be("zookeeper.connect", "localhost:2181")
    }

    "return a chain (zookeper:conection , host:port,host:port,host:port)" in {
      val conn =
        """[{"host": "localhost", "port": "2181"},{"host": "localhost", "port": "2181"},
          |{"host": "localhost", "port": "2181"}]""".stripMargin
      val validating: ValidatingPropertyMap[String, JsoneyString] =
        new ValidatingPropertyMap[String, JsoneyString](Map("zookeeper.connect" -> JsoneyString(conn)))
      validating.getZkConnectionConfs("zookeeper.connect", "localhost", "2181") should
        be("zookeeper.connect", "localhost:2181,localhost:2181,localhost:2181")
    }

    "return a chain with default port (zookeper:conection , host: defaultport)" in {
      val conn =
        """[{"host": "localhost"}]"""
      val validating: ValidatingPropertyMap[String, JsoneyString] =
        new ValidatingPropertyMap[String, JsoneyString](Map("zookeeper.connect" -> JsoneyString(conn)))
      validating.getZkConnectionConfs("zookeeper.connect", "localhost", "2181") should
        be("zookeeper.connect", "localhost:2181")
    }

    "return a chain with default host (zookeper:conection , defaultHost: port)" in {
      val conn =
        """[{"port": "2181"}]"""
      val validating: ValidatingPropertyMap[String, JsoneyString] =
        new ValidatingPropertyMap[String, JsoneyString](Map("zookeeper.connect" -> JsoneyString(conn)))
      validating.getZkConnectionConfs("zookeeper.connect", "localhost", "2181") should
        be("zookeeper.connect", "localhost:2181")
    }

    "return a chain with default host and default porty (zookeeper.connect: , defaultHost: defaultport)" in {
      val conn =
        """[{}]"""
      val validating: ValidatingPropertyMap[String, JsoneyString] =
        new ValidatingPropertyMap[String, JsoneyString](Map("zookeeper.connect" -> JsoneyString(conn)))
      validating.getZkConnectionConfs("zookeeper.connect", "localhost", "2181") should
        be("zookeeper.connect", "localhost:2181")
    }
  }
}

