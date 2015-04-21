/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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

import com.stratio.sparkta.driver.dto._
import com.stratio.sparkta.sdk.JsoneyString
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpecLike}

/**
 * Created by dcarroza on 4/13/15.
 */
class AggregationPoliciesDtoSpec extends WordSpecLike
with MockitoSugar
with Matchers {

  "A AggregationPoliciesValidator should" should {
    "validate dimensions is required and has at least 1 element" in {

      val configuration: Map[String, JsoneyString]
      = Map(("topics", new JsoneyString("zion2:1")), ("kafkaParams.group.id", new JsoneyString("kafka-pruebas")))
      val input = new PolicyElementDto("kafka-input", "KafkaInput", configuration)

      val apd = new AggregationPoliciesDto(
        "policy-name",
        "true",
        "example",
        0,
        Seq(),
        Seq(mock[RollupDto]),
        Seq(mock[PolicyElementDto]),
        Seq(input),
        Seq(mock[PolicyElementDto]),
        Seq(mock[PolicyElementDto]))


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
