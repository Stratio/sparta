/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.common.rest

import akka.actor.ActorSystem
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class SparkExecutorRestUtilsTest extends FlatSpec with Matchers {

  "SparkExecutorActorSystem" should "create an ActorySystem with custom properties overriding default properties" in {

    val aSystem = ActorSystem(
      "test-as",
      SparkExecutorRestUtils.configFromProperties(Map("akka.logger-startup-timeout" -> "17 s"))
    )

    aSystem.settings.LoggerStartTimeout.duration.toSeconds shouldBe 17
    aSystem.settings.UnstartedPushTimeout.duration.toSeconds shouldBe 10
  }

}
