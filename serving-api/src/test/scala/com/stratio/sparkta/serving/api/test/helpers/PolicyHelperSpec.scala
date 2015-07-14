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

import com.stratio.sparkta.driver.models._
import com.stratio.sparkta.serving.api.helpers.PolicyHelper
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
      new CheckpointModel(checkpointDir, "", checkpointGranularity, checkpointInterval, checkpointAvailable)

    val ap = new AggregationPoliciesModel(
      "policy-test",
      sparkStreamingWindow = 2000,
      new RawDataModel(),
      transformations = Seq(),
      cubes = Seq(),
      inputs = Seq(
        PolicyElementModel("input1", "input", Map())),
      outputs = Seq(
        PolicyElementModel("output1", "output", Map())),
      fragments = Seq(
        FragmentElementModel(
          name = "fragment1",
          fragmentType = "input",
          element = PolicyElementModel("inputF", "input", Map())),
        FragmentElementModel(
          name = "fragment1",
          fragmentType = "output",
          element = PolicyElementModel("outputF", "output", Map()))),
      checkpointing = checkpointDto
    )

    When("the helper parse these fragments")
    val result = PolicyHelper.parseFragments(ap)

    Then("inputs/outputs must have the existing input/outputs and the parsed input fragment")
    result.inputs.toSet should equal(Seq(
      PolicyElementModel("input1", "input", Map()),
      PolicyElementModel("inputF", "input", Map())).toSet
    )

    result.outputs.toSet should equal(Seq(
      PolicyElementModel("output1", "output", Map()),
      PolicyElementModel("outputF", "output", Map())).toSet
    )
  }
}