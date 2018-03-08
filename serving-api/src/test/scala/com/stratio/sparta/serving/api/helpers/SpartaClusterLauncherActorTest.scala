/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.serving.api.helpers

import com.stratio.sparta.serving.core.config.{SpartaConfigFactory, SpartaConfig}
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalamock.scalatest._
import org.scalatest._
import org.scalatest.junit.JUnitRunner

/**
 * Tests over sparta helper operations used to wake up a Sparta's context.
 *
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

    val spartaConfig = SpartaConfig.initConfig(node = "sparta", configFactory = SpartaConfigFactory(config))
    spartaConfig.get.getString("testKey") should be("testValue")
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

    val spartaConfig = SpartaConfig.initConfig(node = "sparta", configFactory = SpartaConfigFactory(config))
    val testNodeConfig = SpartaConfig.initConfig("testNode", spartaConfig, SpartaConfigFactory(config))
    testNodeConfig.get.getString("testKey") should be("testValue")
  }
}
