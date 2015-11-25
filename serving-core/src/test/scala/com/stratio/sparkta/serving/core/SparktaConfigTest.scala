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

package com.stratio.sparkta.serving.core

import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class SparktaConfigTest extends WordSpec with Matchers {

  "SparktaConfig class" should{

    "initMainConfig should return X" in {

      val config = ConfigFactory.parseString(
        """
          |sparkta {
          |
          | "testKey" : "test"
          |}
        """.stripMargin)

      val res = SparktaConfig.initMainConfig(Some(config), new MockConfigFactory(config)).get.toString
      res should be ("""Config(SimpleConfigObject({"testKey":"test"}))""")

    }
    "initApiConfig should return X" in {
      SparktaConfig.mainConfig = None

      val configApi = ConfigFactory.parseString(
        """
          | api {
          |       "host" : "localhost"
          |       "port" : 9090
          |      }
        """.stripMargin)

      val res = SparktaConfig.initApiConfig(new MockConfigFactory(configApi)).get.toString
      res should be ("""Config(SimpleConfigObject({"host":"localhost","port":9090}))""")

    }
    "initSwaggerConfig should return X" in {
      SparktaConfig.mainConfig = None

      val configSwagger = ConfigFactory.parseString(
        """
          | swagger {
          |       "host" : "localhost"
          |       "port" : 9090
          |      }
        """.stripMargin)

      val res = SparktaConfig.initSwaggerConfig(new MockConfigFactory(configSwagger)).get.toString
      res should be ("""Config(SimpleConfigObject({"host":"localhost","port":9090}))""")

    }

    "getClusterConfig(Case: Success) should return cluster config" in {
      SparktaConfig.mainConfig = None
      SparktaConfig.swaggerConfig = None
      SparktaConfig.apiConfig = None

      val configCluster = ConfigFactory.parseString(
        """
          |sparkta {
          |  config {
          |    executionMode = mesos
          |    rememberPartitioner = true
          |    topGracefully = false
          |  }
          |  mesos {
          |   deployMode = cluster
          |   numExecutors = 2
          |  }
          |  }
        """.stripMargin
      )

      SparktaConfig.initMainConfig(Some(configCluster), new MockConfigFactory(configCluster))

      val clusterConf = SparktaConfig.getClusterConfig.get.toString

      clusterConf should be ("""Config(SimpleConfigObject({"deployMode":"cluster","numExecutors":2}))""")

    }
    "getClusterConfig(Case: Success and executionMode = local) should return None" in {
      SparktaConfig.mainConfig = None
      SparktaConfig.swaggerConfig = None
      SparktaConfig.apiConfig = None

      val configCluster = ConfigFactory.parseString(
        """
          |sparkta {
          |  config {
          |    executionMode = local
          |    rememberPartitioner = true
          |    topGracefully = false
          |  }
          |  }
        """.stripMargin
      )

      SparktaConfig.initMainConfig(Some(configCluster), new MockConfigFactory(configCluster))

      val clusterConf = SparktaConfig.getClusterConfig

      clusterConf should be (None)

    }

    "getClusterConfig(Case: _) should return None" in {
      SparktaConfig.mainConfig = None
      SparktaConfig.swaggerConfig = None
      SparktaConfig.apiConfig = None

      val clusterConf = SparktaConfig.getClusterConfig

      clusterConf should be (None)

    }


    "getHdfsConfig(Case: Some(config) should return hdfs config" in {
      SparktaConfig.mainConfig = None
      SparktaConfig.swaggerConfig = None
      SparktaConfig.apiConfig = None

      val configHdfs = ConfigFactory.parseString(
        """
          |sparkta {
          |  hdfs {
          |    "hadoopUserName" : "stratio"
          |    "hadoopConfDir" : "/home/ubuntu"
          |  }
          |  }
        """.stripMargin
      )

      SparktaConfig.initMainConfig(Some(configHdfs), new MockConfigFactory(configHdfs))

      val hdfsConf = SparktaConfig.getHdfsConfig.get.toString

      hdfsConf should be ("""Config(SimpleConfigObject({"hadoopConfDir":"/home/ubuntu","hadoopUserName":"stratio"}))""")

    }
    "getHdfsConfig(Case: None) should return hdfs config" in {
      SparktaConfig.mainConfig = None
      SparktaConfig.swaggerConfig = None
      SparktaConfig.apiConfig = None

      val hdfsConf = SparktaConfig.getHdfsConfig

      hdfsConf should be (None)

    }

    "getDetailConfig (Case: Some(Config) should return the config" in {
      SparktaConfig.mainConfig = None
      SparktaConfig.swaggerConfig = None
      SparktaConfig.apiConfig = None

      val configDetail = ConfigFactory.parseString(
        """
          |sparkta {
          |  config {
          |    "executionMode": "local"
          |    "rememberPartitioner": true
          |    "topGracefully": false
          |  }
          |  }
        """.stripMargin
      )

      SparktaConfig.initMainConfig(Some(configDetail), new MockConfigFactory(configDetail))

      val detailConf = SparktaConfig.getDetailConfig.get.toString

      detailConf should be
      (
        """"Config(SimpleConfigObject({
          |"executionMode":"local",
          |"rememberPartitioner":true,
          |"topGracefully":false
          |}))"""".stripMargin)

    }
    "getDetailConfig (Case: None should return the config" in {
      SparktaConfig.mainConfig = None
      SparktaConfig.swaggerConfig = None
      SparktaConfig.apiConfig = None

      val detailConf = SparktaConfig.getDetailConfig

      detailConf should be (None)
    }
    "getZookeeperConfig (Case: Some(config) should return zookeeper conf" in {
      SparktaConfig.mainConfig = None
      SparktaConfig.swaggerConfig = None
      SparktaConfig.apiConfig = None

      val configZk = ConfigFactory.parseString(
        """
          |sparkta {
          |  zk {
          |    "connectionString" : "localhost:6666"
          |    "connectionTimeout": 15000
          |    "sessionTimeout": 60000
          |    "retryAttempts": 5
          |    "retryInterval": 10000
          |  }
          |  }
        """.stripMargin
      )

      SparktaConfig.initMainConfig(Some(configZk), new MockConfigFactory(configZk))

      val zkConf = SparktaConfig.getZookeeperConfig.get.toString

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
      SparktaConfig.mainConfig = None
      SparktaConfig.swaggerConfig = None
      SparktaConfig.apiConfig = None

      val zkConf = SparktaConfig.getZookeeperConfig

      zkConf should be (None)
    }

    "initOptionalConfig should return a config" in {

      val config = ConfigFactory.parseString(
        """
          |sparkta {
          | testKey : "testValue"
          |}
        """.stripMargin)

      val sparktaConfig = SparktaConfig.initOptionalConfig(
        node = "sparkta",
        configFactory = new MockConfigFactory(config))
      sparktaConfig.get.getString("testKey") should be ("testValue")
    }

    "getOptionStringConfig should return None" in {

      val config = ConfigFactory.parseString(
        """
          |sparkta {
          | testKey : "testValue"
          |}
        """.stripMargin)
      val res = SparktaConfig.getOptionStringConfig(
        node = "sparkta",
        currentConfig = config)

      res should be (None)
    }
  }
}
