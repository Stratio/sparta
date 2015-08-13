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

package com.stratio.sparkta.serving.api.test

import java.io.File

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import com.stratio.sparkta.serving.api.helpers.SparktaHelper
import com.stratio.sparkta.serving.core.models.AggregationPoliciesModel
import com.stratio.sparkta.serving.core.{AppConstant, MockSystem, SparktaConfig}
import org.json4s.{DefaultFormats, native}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import spray.testkit.ScalatestRouteTest

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class StreamingContextServiceIT extends WordSpecLike
with ScalatestRouteTest
with Matchers
with SLF4JLogging {

  val PathToPolicy = getClass.getClassLoader.getResource("policies/IKafka-OPrint.json").getPath

  /**
   * This is a workaround to find the jars either in the IDE or in a maven execution.
   * This test should be moved to acceptance tests when available
   * TODO: this is a unicorn shit and must be changed.
   */
  def getSparktaHome: String = {
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
      val sparktaConfig = SparktaConfig.initConfig("sparkta")
      val sparktaHome = SparktaHelper.initSparktaHome(new MockSystem(Map("SPARKTA_HOME" -> getSparktaHome), Map()))
      val jars = SparktaHelper.initJars(AppConstant.JarPaths, sparktaHome)

      val streamingContextService = new StreamingContextService(sparktaConfig, jars)
      val json = Source.fromFile(new File(PathToPolicy)).mkString
      implicit val formats = DefaultFormats + new JsoneyStringSerializer()
      val apConfig = native.Serialization.read[AggregationPoliciesModel](json)

      val ssc = streamingContextService.standAloneStreamingContext(apConfig)

      ssc should not be None
    }
  }
}
