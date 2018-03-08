/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
