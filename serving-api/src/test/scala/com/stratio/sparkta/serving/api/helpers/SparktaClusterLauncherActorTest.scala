/**
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
package com.stratio.sparkta.serving.api.helpers

import com.stratio.sparkta.serving.api.helpers.SparktaHelper
import com.stratio.sparkta.serving.core.{SparktaConfig, MockConfigFactory, MockSystem}
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalamock.scalatest._
import org.scalatest._
import org.scalatest.junit.JUnitRunner

/**
 * Tests over sparkta helper operations used to wake up a Sparkta's context.
 * @author anistal
 */
@RunWith(classOf[JUnitRunner])
class SparktaClusterLauncherActorTest extends FlatSpec with MockFactory with ShouldMatchers with Matchers {

  it should "init SparktaConfig from a file with a configuration" in {
    val config = ConfigFactory.parseString(
      """
        |sparkta {
        | testKey : "testValue"
        |}
      """.stripMargin)

    val sparktaConfig = SparktaConfig.initConfig(node = "sparkta", configFactory = new MockConfigFactory(config))
    sparktaConfig.get.getString("testKey") should be ("testValue")
  }

  it should "init a config from a given config" in {
    val config = ConfigFactory.parseString(
      """
        |sparkta {
        |  testNode {
        |    testKey : "testValue"
        |  }
        |}
      """.stripMargin)

    val sparktaConfig = SparktaConfig.initConfig(node = "sparkta", configFactory = new MockConfigFactory(config))
    val testNodeConfig = SparktaConfig.initConfig("testNode", sparktaConfig, new MockConfigFactory(config))
    testNodeConfig.get.getString("testKey") should be ("testValue")
  }
}
