/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

package com.stratio.sparkta.serving.core.helpers

import com.stratio.sparkta.serving.core.models._
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PolicyHelperTest extends FeatureSpec with GivenWhenThen with Matchers {

  val SparkStreamingWindow = 2000
  val storageLevel = Some("MEMORY_AND_DISK_SER_2")

  feature("A policy that contains fragments must parse these fragments and join them to input/outputs depending of " +
    "its type") {
    scenario("A policy that contains fragments must parse these fragments and join them to input/outputs " +
      "depending of its type") {
      Given("a policy with an input, an output and a fragment with an input")
      val checkpointDir = "checkpoint"

      val ap = new CommonPoliciesModel(
        None,
        None,
        storageLevel,
        "policy-test",
        "policy description",
        sparkStreamingWindow = SparkStreamingWindow,
        checkpointDir,
        new RawDataModel(),
        transformations = Seq(),
        cubes = Seq(),
        input = None,
        outputs = Seq(
          PolicyElementModel("output1", "output", Map())),
        fragments = Seq(
          FragmentElementModel(
            name = "fragment1",
            fragmentType = "input",
            description = "description",
            shortDescription = "short description",
            element = PolicyElementModel("inputF", "input", Map())),
          FragmentElementModel(
            name = "fragment1",
            fragmentType = "output",
            description = "description",
            shortDescription = "short description",
            element = PolicyElementModel("outputF", "output", Map())))
      )

      When("the helper parse these fragments")
      val result = PolicyHelper.parseFragments(ap)

      Then("outputs must have the existing outputs and the parsed input fragment and the first input")

      result.input should equal(Some(PolicyElementModel("inputF", "input", Map())))

      result.outputs.toSet should equal(Seq(
        PolicyElementModel("output1", "output", Map()),
        PolicyElementModel("outputF", "output", Map())).toSet
      )
    }
  }

  scenario("A policy that contains one input an a fragments with one input too must throw an exception because only " +
    "one input is allowed") {
    Given("a policy with an input, an output and a fragment with an input")
    val checkpointDir = "checkpoint"

    val ap = new CommonPoliciesModel(
      None,
      None,
      storageLevel,
      "policy-test",
      "policy description",
      sparkStreamingWindow = SparkStreamingWindow,
      checkpointDir,
      new RawDataModel(),
      transformations = Seq(),
      cubes = Seq(),
      input = Some(PolicyElementModel("input1", "input", Map())),
      outputs = Seq(
        PolicyElementModel("output1", "output", Map())),
      fragments = Seq(
        FragmentElementModel(
          name = "fragment1",
          fragmentType = "input",
          description = "description",
          shortDescription = "short description",
          element = PolicyElementModel("inputF", "input", Map())),
        FragmentElementModel(
          name = "fragment1",
          fragmentType = "output",
          description = "description",
          shortDescription = "short description",
          element = PolicyElementModel("outputF", "output", Map())))
    )

    When("the helper tries to parse the policy it throws an exception")
    val thrown = intercept[IllegalStateException] {
      PolicyHelper.parseFragments(ap)
    }

    Then("the exception must have the message")
    assert(thrown.getMessage === "Only one input is allowed in the policy.")
  }

  scenario("A policy that no contains outputs must throw an exception.") {
    Given("a policy with an input and no outputs")
    val checkpointDir = "checkpoint"

    val ap = new CommonPoliciesModel(
      None,
      None,
      storageLevel,
      "policy-test",
      "policy description",
      sparkStreamingWindow = SparkStreamingWindow,
      checkpointDir,
      new RawDataModel(),
      transformations = Seq(),
      cubes = Seq(),
      input = Some(PolicyElementModel("input1", "input", Map())),
      outputs = Seq(),
      fragments = Seq()
    )

    When("the helper tries to parse the policy it throws an exception")
    val thrown = intercept[IllegalStateException] {
      PolicyHelper.parseFragments(ap)
    }

    Then("the exception must have the message")
    assert(thrown.getMessage === "It is mandatory to define at least one output in the policy.")
  }
}
