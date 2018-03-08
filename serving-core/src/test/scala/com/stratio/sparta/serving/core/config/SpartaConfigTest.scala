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
class SpartaConfigTest extends WordSpec with Matchers {

  "SpartaConfig class" should{

    "initMainConfig should return X" in {

      val config = ConfigFactory.parseString(
        """
          |sparta {
          |
          | "testKey" : "test"
          |}
        """.stripMargin)

      val res = SpartaConfig.initMainConfig(Some(config), SpartaConfigFactory(config)).get.toString
      res should be ("""Config(SimpleConfigObject({"testKey":"test"}))""")

    }
    "initApiConfig should return X" in {
      SpartaConfig.mainConfig = None

      val configApi = ConfigFactory.parseString(
        """
          | api {
          |       "host" : "localhost"
          |       "port" : 9090
          |      }
        """.stripMargin)

      val res = SpartaConfig.initApiConfig(SpartaConfigFactory(configApi)).get.toString
      res should be ("""Config(SimpleConfigObject({"host":"localhost","port":9090}))""")

    }

    "getHdfsConfig(Case: Some(config) should return hdfs config" in {
      SpartaConfig.mainConfig = None
      SpartaConfig.apiConfig = None

      val configHdfs = ConfigFactory.parseString(
        """
          |sparta {
          |  hdfs {
          |    "hadoopUserName" : "stratio"
          |    "hadoopConfDir" : "/home/ubuntu"
          |  }
          |  }
        """.stripMargin
      )

      SpartaConfig.initMainConfig(Some(configHdfs), SpartaConfigFactory(configHdfs))

      val hdfsConf = SpartaConfig.getHdfsConfig.get.toString

      hdfsConf should be ("""Config(SimpleConfigObject({"hadoopConfDir":"/home/ubuntu","hadoopUserName":"stratio"}))""")

    }
    "getHdfsConfig(Case: None) should return hdfs config" in {
      SpartaConfig.mainConfig = None
      SpartaConfig.apiConfig = None

      val hdfsConf = SpartaConfig.getHdfsConfig

      hdfsConf should be (None)

    }

    "getDetailConfig (Case: Some(Config) should return the config" in {
      SpartaConfig.mainConfig = None
      SpartaConfig.apiConfig = None

      val configDetail = ConfigFactory.parseString(
        """
          |sparta {
          |  config {
          |    "executionMode": "local"
          |    "rememberPartitioner": true
          |    "topGracefully": false
          |  }
          |  }
        """.stripMargin
      )

      SpartaConfig.initMainConfig(Some(configDetail), SpartaConfigFactory(configDetail))

      val detailConf = SpartaConfig.getDetailConfig.get.toString

      detailConf should be
      (
        """"Config(SimpleConfigObject({
          |"executionMode":"local",
          |"rememberPartitioner":true,
          |"topGracefully":false
          |}))"""".stripMargin)

    }
    "getDetailConfig (Case: None should return the config" in {
      SpartaConfig.mainConfig = None
      SpartaConfig.apiConfig = None

      val detailConf = SpartaConfig.getDetailConfig

      detailConf should be (None)
    }
    "getZookeeperConfig (Case: Some(config) should return zookeeper conf" in {
      SpartaConfig.mainConfig = None
      SpartaConfig.apiConfig = None

      val configZk = ConfigFactory.parseString(
        """
          |sparta {
          |  zookeeper {
          |    "connectionString" : "localhost:6666"
          |    "connectionTimeout": 15000
          |    "sessionTimeout": 60000
          |    "retryAttempts": 5
          |    "retryInterval": 10000
          |  }
          |  }
        """.stripMargin
      )

      SpartaConfig.initMainConfig(Some(configZk), SpartaConfigFactory(configZk))

      val zkConf = SpartaConfig.getZookeeperConfig.get.toString

      zkConf should be
      (
        """"Config(SimpleConfigObject(
          |{"connectionString":"localhost:6666",
          |"connectionTimeout":15000,
          |"retryAttempts":5,
          |"retryInterval":10000,
          |"sessionTimeout":60000
          |}))"""".stripMargin)

    }
    "getZookeeperConfig (Case: None) should return zookeeper conf" in {
      SpartaConfig.mainConfig = None
      SpartaConfig.apiConfig = None

      val zkConf = SpartaConfig.getZookeeperConfig

      zkConf should be (None)
    }

    "initOptionalConfig should return a config" in {

      val config = ConfigFactory.parseString(
        """
          |sparta {
          | testKey : "testValue"
          |}
        """.stripMargin)

      val spartaConfig = SpartaConfig.initOptionalConfig(
        node = "sparta",
        configFactory = SpartaConfigFactory(config))
      spartaConfig.get.getString("testKey") should be ("testValue")
    }

    "getOptionStringConfig should return None" in {

      val config = ConfigFactory.parseString(
        """
          |sparta {
          | testKey : "testValue"
          |}
        """.stripMargin)
      val res = SpartaConfig.getOptionStringConfig(
        node = "sparta",
        currentConfig = config)

      res should be (None)
    }
  }
}
