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

package com.stratio.sparta.driver.test.service

import java.io.File

import akka.actor.{ActorSystem, Props}
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.serving.core.actor.StatusActor
import com.stratio.sparta.serving.core.config.SpartaConfig
import com.stratio.sparta.serving.core.models.policy.PolicyModel
import com.stratio.sparta.serving.core.utils.PolicyUtils
import com.typesafe.config.ConfigFactory
import org.apache.curator.framework.CuratorFramework
import org.apache.spark.streaming.StreamingContextState
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.junit.runner.RunWith
import org.scalamock.scalatest.MockFactory
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class StreamingContextServiceIT extends WordSpecLike with Matchers with MockFactory with PolicyUtils {

  val PathToPolicy = getClass.getClassLoader.getResource("policies/ISocket-OPrint.json").getPath
  val curatorFramework = mock[CuratorFramework]
  val system = ActorSystem("test", SpartaConfig.mainConfig)
  val statusActorRef = system.actorOf(Props(new StatusActor(curatorFramework)))
  /**
   * This is a workaround to find the jars either in the IDE or in a maven execution.
   * This test should be moved to acceptance tests when available
   * TODO: this is a unicorn shit and must be changed.
   */
  def getSpartaHome: String = {
    val fileForIde = new File(".", "plugins")

    if (fileForIde.exists()) {
      new File(".").getCanonicalPath
    } else if (new File("../.", "plugins").exists()) {
      new File("../.").getCanonicalPath
    } else {
      new File("../../.").getCanonicalPath
    }
  }

  "A StreamingContextService should" should {
    "create spark streaming context from a policy" in {
      val json = Source.fromFile(new File(PathToPolicy)).mkString
      val apConfig = parse(json).extract[PolicyModel]
      val spartaConfig = SpartaConfig.initConfig("sparta")

      SpartaConfig.spartaHome = getSpartaHome
      SpartaConfig.initMainConfig(Option(StreamingContextServiceIT.config))

      val jars = jarsFromPolicy(apConfig)
      val streamingContextService = StreamingContextService(statusActorRef, spartaConfig)
      val ssc = streamingContextService.localStreamingContext(apConfig.copy(id = Some("1")), jars)

      ssc.getState() should be(StreamingContextState.INITIALIZED)
    }
  }
}

object StreamingContextServiceIT {

  val config = ConfigFactory.parseString(
    s"""
      "sparta": {
        "config": {
          "executionMode": "local"
        }
      }
    """.stripMargin)
}