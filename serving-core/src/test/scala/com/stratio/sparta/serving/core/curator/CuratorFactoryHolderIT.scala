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
package com.stratio.sparta.serving.core.curator

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.factory.CuratorFactoryHolder
import com.typesafe.config._
import org.apache.curator.framework.api.ExistsBuilder
import org.apache.curator.test.TestingCluster
import org.apache.curator.utils.CloseableUtils
import org.apache.zookeeper.CreateMode
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, _}

/**
 * This test specifies the behaviour of CuratorFactoryHolder that encapsulates the real curator's factory.
 * @author anistal
 */
@RunWith(classOf[JUnitRunner])
class CuratorFactoryHolderIT extends FlatSpec with Matchers with BeforeAndAfter with GivenWhenThen with SLF4JLogging {

  var zkTestServer: TestingCluster = _
  var clusterConfig: Option[Config] = None

  before {
    //zkTestServer = new TestingServer(CuratorFactoryHolderIT.TestServerZKPort)
    zkTestServer = new TestingCluster(1)
    zkTestServer.start()

    clusterConfig = Some(CuratorFactoryHolderIT.basicConfig.get.withValue("sparta.zookeeper.connectionString",
      ConfigValueFactory.fromAnyRef(zkTestServer.getConnectString)))

    SpartaConfig.initMainConfig(clusterConfig)
    //val instance = CuratorFactoryHolder.getInstance()
    Option(CuratorFactoryHolder.getInstance().checkExists().forPath("/test")) match {
      case eb: ExistsBuilder =>
        CuratorFactoryHolder.getInstance().delete().deletingChildrenIfNeeded().forPath(CuratorFactoryHolderIT.PathTestNode)
      case None =>
        log.debug("Test node not created. It is not necessary to delete it.")
    }
    CuratorFactoryHolder.resetInstance()
  }

  after {
    CuratorFactoryHolder.resetInstance()
    CloseableUtils.closeQuietly(zkTestServer)
  }

  "CuratorFactory holder" must "create correctly and to check if exists" in {
    Given(s"ZK configuration: $CuratorFactoryHolderIT.configString")
    SpartaConfig.initMainConfig(clusterConfig)
    val instance = CuratorFactoryHolder.getInstance()
    When("creates a ephemeral node in ZK server")
    instance.create().withMode(CreateMode.EPHEMERAL).forPath(CuratorFactoryHolderIT.PathTestNode)
    Then("the created node must be exists when it is searched")
    assert(Option(instance.checkExists().forPath(CuratorFactoryHolderIT.PathTestNode)).isDefined)
  }

  "CuratorFactory holder" must "reuse  the same connection" in {
    Given(s"ZK configuration: $CuratorFactoryHolderIT.configString")
    When("an instance is created with the CuratorFactoryHolder")
    val instance = CuratorFactoryHolder.getInstance()
    And("other instance is created with the CuratorFactoryHolder")
    val secondInstance = CuratorFactoryHolder.getInstance()
    Then("the factory return the same instance for both cases")
    instance should be theSameInstanceAs secondInstance
  }

  it must "not reuse  the same connection when resetInstance is invoked" in {
    Given(s"ZK configuration: $CuratorFactoryHolderIT.configString")
    When("an instance is created with the CuratorFactoryHolder")
    val instance = CuratorFactoryHolder.getInstance()
    When("reset is called in the factory")
    CuratorFactoryHolder.resetInstance()
    And("other instance is created with the CuratorFactoryHolder")
    val secondInstance = CuratorFactoryHolder.getInstance(clusterConfig)
    Then("the factory return other different instance and them are not equals")
    instance should not be equals(secondInstance)
  }
}

object CuratorFactoryHolderIT {

  val TestServerZKPort = 6666
  val PathTestNode = "/test"
  val configString = s"""
                        "sparta": {
                          "zookeeper": {
                            "connectionString": "localhost:$TestServerZKPort",
                            "connectionTimeout": 15000,
                            "sessionTimeout": 60000
                            "retryAttempts": 5
                            "retryInterval": 2000
                          }
                        }
                      """
  lazy val basicConfig = Some(ConfigFactory.parseString(configString))
}
