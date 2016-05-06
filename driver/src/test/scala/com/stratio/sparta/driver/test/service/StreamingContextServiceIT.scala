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

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.driver.service.StreamingContextService
import com.stratio.sparta.driver.util.PolicyUtils
import com.stratio.sparta.serving.core.SpartaConfig
import com.stratio.sparta.serving.core.constants.AppConstant
import com.stratio.sparta.serving.core.helpers.JarsHelper
import com.stratio.sparta.serving.core.models.{AggregationPoliciesModel, SpartaSerializer}
import org.json4s.native
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class StreamingContextServiceIT extends WordSpecLike
with Matchers
with SLF4JLogging
with SpartaSerializer {

  val PathToPolicy = getClass.getClassLoader.getResource("policies/IKafka-OPrint.json").getPath

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
      val apConfig = native.Serialization.read[AggregationPoliciesModel](json)
      val spartaConfig = SpartaConfig.initConfig("sparta")

      SpartaConfig.spartaHome = getSpartaHome

      val jars = PolicyUtils.jarsFromPolicy(apConfig)
      val streamingContextService = new StreamingContextService(None, spartaConfig)
      val ssc = streamingContextService.standAloneStreamingContext(apConfig.copy(id = Some("1")), jars)

      ssc should not be None
    }
  }
}
