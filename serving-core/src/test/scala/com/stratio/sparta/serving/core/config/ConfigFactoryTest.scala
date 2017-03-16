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
package com.stratio.sparta.serving.core.config

import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class ConfigFactoryTest extends WordSpec with Matchers with Serializable {

  "ConfigFactory" should {

    " getConfig returns an Option[Config] " in {

      val config = ConfigFactory.parseString(
        """
          |sparta {
          | testKey : "testValue"
          |}
        """.stripMargin)

      val spartaConfig = SpartaConfig.initConfig(node = "sparta", configFactory = SpartaConfigFactory(config))
      spartaConfig.get.getString("testKey") should be("testValue")
    }
    " getConfig returns a Config(SimpleConfigObject({ Key, Value }) " in {

      val config = ConfigFactory.parseString(
        """
          |sparta {
          | testKey : "test"
          |}
        """.stripMargin)

      val configFactory = SpartaConfigFactory()

      val res = configFactory.getConfig("sparta", Some(config)).get.toString
      res should be("""Config(SimpleConfigObject({"testKey":"test"}))""")
    }

    "getConfig returns None due to an exception " in {

      val config = ConfigFactory.parseString(
        """
          |sparta {
          | testKey : "test"
          |}
        """.stripMargin)

      val configFactory = SpartaConfigFactory()

      val conf = configFactory.getConfig(None.orNull, Some(config))
      conf should be(None)
    }

    "getConfig returns this: Config(SimpleConfigObject({ Key, Value }) when both " +
      "parameters are Null or None " in {

      val config = ConfigFactory.parseString(
        """
          |sparta {
          | testKey : "test"
          |}
        """.stripMargin)

      val configFactory = SpartaConfigFactory()

      val conf = configFactory.getConfig(None.orNull, None)
      conf should be(None)
    }

    "init a config from a given config and a Null Node" in {
      val config = ConfigFactory.parseString(
        """
          |sparta {
          |  testNode {
          |    testKey : "testValue"
          |  }
          |}
        """.stripMargin)

      val configFactory = SpartaConfigFactory(config)

      val testNodeConfig = configFactory.getConfig(None.orNull, Some(config))

      testNodeConfig should be(None)

    }
  }

  "init a config from a given config when the config is null" in {
    val config = ConfigFactory.parseString(
      """
        |sparta {
        |  testNode {
        |    testKey : "testValue"
        |  }
        |}
      """.stripMargin)

    val configFactory = SpartaConfigFactory(config)

    val testNodeConfig = configFactory.getConfig("sparta", None).get.toString

    testNodeConfig should be("""Config(SimpleConfigObject({"testNode":{"testKey":"testValue"}}))""")

  }
}
