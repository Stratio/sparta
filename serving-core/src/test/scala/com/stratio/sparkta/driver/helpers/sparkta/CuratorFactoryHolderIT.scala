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

package com.stratio.sparkta.driver.helpers.sparkta

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.serving.core.CuratorFactoryHolder
import com.typesafe.config.{ConfigException, ConfigFactory}
import org.apache.curator.framework.api.ExistsBuilder
import org.apache.curator.test.TestingServer
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

  var zkTestServer: TestingServer = _

  before {
    zkTestServer = new TestingServer(CuratorFactoryHolderIT.TestServerZKPort)
    zkTestServer.start()

    val instance = CuratorFactoryHolder.getInstance(CuratorFactoryHolderIT.basicConfig)
    Option(instance.get.checkExists().forPath("/test")) match {
      case eb: ExistsBuilder =>
        instance.get.delete().deletingChildrenIfNeeded().forPath(CuratorFactoryHolderIT.PathTestNode)
      case None =>
        log.debug("Test node not created. It is not necessary to delete it.")
    }
    CuratorFactoryHolder.resetInstance()
  }

  after {
    CuratorFactoryHolder.resetInstance()
    zkTestServer.stop()
  }

  "CuratorFactory holder" must "throws the correct error" in {
    Given("an empty configuration")
    Then("throws a missing config exception")
    an[ConfigException] should be thrownBy
      (CuratorFactoryHolder.getInstance(CuratorFactoryHolderIT.emptyConfig))
  }

  it must "create correctly and to check if exists" in {
    Given(s"ZK configuration: $CuratorFactoryHolderIT.configString")
    val instance = CuratorFactoryHolder.getInstance(CuratorFactoryHolderIT.basicConfig)
    When("creates a ephimeral node in ZK server")
    instance.get.create().withMode(CreateMode.EPHEMERAL).forPath(CuratorFactoryHolderIT.PathTestNode)
    Then("the created node must be exists when it is searched")
    assert(Option(instance.get.checkExists().forPath(CuratorFactoryHolderIT.PathTestNode)) != None)
  }

  it must "reuse  the same connection" in {
    Given(s"ZK configuration: $CuratorFactoryHolderIT.configString")
    When("an instance is created with the CuratorFactoryHolder")
    val instance = CuratorFactoryHolder.getInstance(CuratorFactoryHolderIT.basicConfig)
    And("other instance is created with the CuratorFactoryHolder")
    val secondInstance = CuratorFactoryHolder.getInstance(CuratorFactoryHolderIT.basicConfig)
    Then("the factory return the same instance for both cases")
    instance should be theSameInstanceAs secondInstance
  }

  it must "not reuse  the same connection when resetInstance is invoked" in {
    Given(s"ZK configuration: $CuratorFactoryHolderIT.configString")
    When("an instance is created with the CuratorFactoryHolder")
    val instance = CuratorFactoryHolder.getInstance(CuratorFactoryHolderIT.basicConfig)
    When("reset is called in the factory")
    CuratorFactoryHolder.resetInstance()
    And("other instance is created with the CuratorFactoryHolder")
    val secondInstance = CuratorFactoryHolder.getInstance(CuratorFactoryHolderIT.basicConfig)
    Then("the factory return other different instance and them are not equals")
    instance should not be secondInstance
  }
}

object CuratorFactoryHolderIT {

  val TestServerZKPort = 6666
  val PathTestNode = "/test"
  val configString = s"""
                        "zk": {
                          "connectionString": "localhost:$TestServerZKPort",
                          "connectionTimeout": 15000,
                          "sessionTimeout": 60000
                          "retryAttempts": 5
                          "retryInterval": 2000
                        }
                      """
  lazy val basicConfig = ConfigFactory.parseString(configString)
  lazy val emptyConfig = ConfigFactory.empty
}
