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

package com.stratio.sparkta.serving.core.models

import com.stratio.sparkta.serving.core.policy.status.PolicyStatusEnum

case class AggregationPoliciesModel(id: Option[String] = None,
                                    version: Option[Int] = None,
                                    storageLevel: Option[String] = AggregationPoliciesModel.storageDefaultValue,
                                    name: String,
                                    description: String = "default description",
                                    sparkStreamingWindow: String = AggregationPoliciesModel.sparkStreamingWindow,
                                    checkpointPath: String,
                                    rawData: RawDataModel,
                                    transformations: Seq[TransformationsModel],
                                    streamTriggers: Seq[TriggerModel],
                                    cubes: Seq[CubeModel],
                                    input: Option[PolicyElementModel] = None,
                                    outputs: Seq[PolicyElementModel],
                                    fragments: Seq[FragmentElementModel],
                                    userPluginsJars: Seq[String])

case object AggregationPoliciesModel {

  val sparkStreamingWindow = "2s"
  val storageDefaultValue = Some("MEMORY_AND_DISK_SER_2")

  def checkpointPath(policy: AggregationPoliciesModel): String =
    s"${policy.checkpointPath}/${policy.name}"
}

case class PolicyWithStatus(status: PolicyStatusEnum.Value,
                            policy: AggregationPoliciesModel)

case class PolicyResult(policyId: String, policyName: String)

object AggregationPoliciesValidator extends SparktaSerializer {

  //scalastyle:off
  def validateDto(aggregationPoliciesDto: AggregationPoliciesModel): (Boolean, String) = {
    //TODO Validate policy according to the old schema validation rules
    // https://stratio.atlassian.net/browse/SPARKTA-458
    val validCubeName = aggregationPoliciesDto.cubes.forall(cube => cube.name.nonEmpty)
    val cubesHaveAtLeastOneDimension = aggregationPoliciesDto.cubes.forall(cube => cube.dimensions.nonEmpty)
    val policyHaveAtLeastOneOutput = aggregationPoliciesDto.outputs.nonEmpty
    val policyHaveAtLeastOneAction =
      aggregationPoliciesDto.cubes.nonEmpty || aggregationPoliciesDto.streamTriggers.nonEmpty
    val outputsNames = aggregationPoliciesDto.outputs.map(_.name)
    val cubeOutputsLinks = aggregationPoliciesDto.cubes.forall(cube =>
      cube.writer.outputs.forall(outputName => outputsNames.contains(outputName)))
    val cubeTriggersOutputsLinks = aggregationPoliciesDto.cubes.forall(cube =>
      cube.triggers.forall(trigger =>
        trigger.outputs.forall(outputName => outputsNames.contains(outputName))))
    val streamTriggersOutputsLinks = aggregationPoliciesDto.streamTriggers.forall(trigger =>
      trigger.outputs.forall(outputName => outputsNames.contains(outputName)))
    val msgCubeName =
      if (validCubeName)
        ""
      else
        """No usable value for cubes names.
          |Must be non empty.""".stripMargin
    val msgOneAction =
      if (policyHaveAtLeastOneAction)
        ""
      else
        """No usable value for cubes and streamTriggers.
          |Must have at least 1 elements cubes or streamTriggers arrays.""".stripMargin
    val msgOneDimension =
      if (cubesHaveAtLeastOneDimension)
        ""
      else
        """No usable value for Cubes-dimensions.
          |Array is too short: must have at least 1 elements
          |but instance has 0 elements.""".stripMargin
    val msgOneOutput =
      if (policyHaveAtLeastOneOutput)
        ""
      else
        """No usable value for outputs.
          |Array is too short: must have at least 1 elements
          |but instance has 0 elements.""".stripMargin
    val msgCubeOtputsLinks =
      if (cubeOutputsLinks)
        ""
      else
        """No usable value for outputs names in cube writers.
          |Must be included in the output array.""".stripMargin
    val msgStreamTriggersOtputsLinks =
      if (streamTriggersOutputsLinks)
        ""
      else
        """No usable value for outputs names in stream triggers.
          |Must be included in the output array.""".stripMargin
    val msgCubeTriggersOtputsLinks =
      if (cubeTriggersOutputsLinks)
        ""
      else
        """No usable value for outputs names in stream triggers.
          |Must be included in the output array.""".stripMargin

    (validCubeName && cubesHaveAtLeastOneDimension && policyHaveAtLeastOneOutput && policyHaveAtLeastOneAction &&
      cubeOutputsLinks && streamTriggersOutputsLinks && cubeTriggersOutputsLinks,
      msgCubeName ++ msgOneAction ++ msgOneDimension ++ msgOneOutput ++ msgCubeOtputsLinks ++
        msgStreamTriggersOtputsLinks ++ msgCubeTriggersOtputsLinks)
  }
  //scalastyle:on
}
