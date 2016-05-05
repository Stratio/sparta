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
package com.stratio.sparta.serving.api.helpers

import com.stratio.sparta.serving.api.helpers.SpartaHelper
import com.stratio.sparta.serving.core.{SpartaConfig, MockConfigFactory, MockSystem}
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalamock.scalatest._
import org.scalatest._
import org.scalatest.junit.JUnitRunner

/**
 * Tests over sparta helper operations used to wake up a Sparta's context.
 * @author anistal
 */
@RunWith(classOf[JUnitRunner])
class SpartaClusterLauncherActorTest extends FlatSpec with MockFactory with ShouldMatchers with Matchers {

  it should "init SpartaConfig from a file with a configuration" in {
    val config = ConfigFactory.parseString(
      """
        |sparta {
        | testKey : "testValue"
        |}
      """.stripMargin)

    val spartaConfig = SpartaConfig.initConfig(node = "sparta", configFactory = new MockConfigFactory(config))
    spartaConfig.get.getString("testKey") should be ("testValue")
  }

  it should "init a config from a given config" in {
    val config = ConfigFactory.parseString(
      """
        |sparta {
        |  testNode {
        |    testKey : "testValue"
        |  }
        |}
      """.stripMargin)

    val spartaConfig = SpartaConfig.initConfig(node = "sparta", configFactory = new MockConfigFactory(config))
    val testNodeConfig = SpartaConfig.initConfig("testNode", spartaConfig, new MockConfigFactory(config))
    testNodeConfig.get.getString("testKey") should be ("testValue")
  }
}
