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

package com.stratio.sparkta.driver.test.helpers

import com.stratio.sparkta.driver.dto._
import com.stratio.sparkta.driver.helpers.PolicyHelper
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

/**
 * Tests over policy operations.
 * @author anistal
 */

@RunWith(classOf[JUnitRunner])
class PolicyHelperSpec extends FeatureSpec with GivenWhenThen with Matchers {

  feature("A policy that contains fragments must parse these fragments and join them to input/outputs depending of " +
    "its type") {
    Given("a policy with an input, an output and a fragment with an input")
    val checkpointInterval = 10000
    val checkpointAvailable = 60000
    val checkpointGranularity = "minute"
    val checkpointDir = "checkpoint"
    val checkpointDto =
      new CheckpointDto(checkpointDir, "", checkpointGranularity, checkpointInterval, checkpointAvailable)

    val ap = new AggregationPoliciesDto(
      "policy-test",
      sparkStreamingWindow = 2000,
      new RawDataDto(),
      transformations = Seq(),
      cubes = Seq(),
      inputs = Seq(
        PolicyElementDto("input1", "input", Map())),
      outputs = Seq(
        PolicyElementDto("output1", "output", Map())),
      fragments = Seq(
        FragmentElementDto(
          name = "fragment1",
          fragmentType = "input",
          element = PolicyElementDto("inputF", "input", Map())),
        FragmentElementDto(
          name = "fragment1",
          fragmentType = "output",
          element = PolicyElementDto("outputF", "output", Map()))),
      checkpointing = checkpointDto
    )

    When("the helper parse these fragments")
    val result = PolicyHelper.parseFragments(ap)

    Then("inputs/outputs must have the existing input/outputs and the parsed input fragment")
    result.inputs.toSet should equal(Seq(
      PolicyElementDto("input1", "input", Map()),
      PolicyElementDto("inputF", "input", Map())).toSet
    )

    result.outputs.toSet should equal(Seq(
      PolicyElementDto("output1", "output", Map()),
      PolicyElementDto("outputF", "output", Map())).toSet
    )
  }
}