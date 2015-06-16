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

package com.stratio.sparkta.driver.test.dto

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}

import com.stratio.sparkta.driver.dto._
import com.stratio.sparkta.sdk.JsoneyString

@RunWith(classOf[JUnitRunner])
class AggregationPoliciesDtoSpec extends WordSpecLike
with MockitoSugar
with Matchers {

  "A AggregationPoliciesValidator should" should {
    "validate dimensions is required and has at least 1 element" in {

      val checkpointInterval = 10000
      val checkpointAvailable = 60000
      val checkpointGranularity = "minute"
      val checkpointDir = "checkpoint"

      val configuration: Map[String, JsoneyString]
      = Map(("topics", new JsoneyString("zion2:1")), ("kafkaParams.group.id", new JsoneyString("kafka-pruebas")))
      val input = new PolicyElementDto("kafka-input", "KafkaInput", configuration)

      val cubeName = "cubeTest"
      val DimensionToCube = "dimension2"
      val dimensionDto = new DimensionDto("dimensionType", "dimension1", None)
      val cubeDto = new CubeDto(cubeName, Seq(new PrecisionDto(DimensionToCube, "dimensionType", None)), Seq())

      val apd = new AggregationPoliciesDto(
        "policy-name",
        "true",
        "example",
        "day",
        checkpointDir,
        "",
        checkpointGranularity,
        checkpointInterval,
        checkpointAvailable,
        0,
        Seq(),
        Seq(cubeDto),
        Seq(mock[PolicyElementDto]),
        Seq(input),
        Seq(mock[PolicyElementDto]),
        Seq(mock[PolicyElementDto]),
        Seq(mock[FragmentElementDto]))

      val test = AggregationPoliciesValidator.validateDto(apd)

      test._1 should equal(false)
      test._2 should include("com.github.fge.jsonschema.core.report.ListProcessingReport: failure\n" +
        "--- BEGIN MESSAGES ---\n" +
        "error: array is too short: must have at least 1 elements but instance has 0 elements\n" +
        "    level: \"error\"\n" +
        "    schema: {\"loadingURI\":\"#\",\"pointer\":\"/properties/dimensions\"}\n" +
        "    instance: {\"pointer\":\"/dimensions\"}\n" +
        "    domain: \"validation\"\n" +
        "    keyword: \"minItems\"\n" +
        "    minItems: 1\n" +
        "    found: 0\n" +
        "---  END MESSAGES  ---")
    }
  }
}
