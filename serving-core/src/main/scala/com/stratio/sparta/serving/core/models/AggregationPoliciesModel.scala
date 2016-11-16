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

package com.stratio.sparta.serving.core.models

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.helpers.DateOperationsHelper._
import com.stratio.sparta.serving.core.policy.status.PolicyStatusEnum

case class AggregationPoliciesModel(
                                     id: Option[String] = None,
                                     version: Option[Int] = None,
                                     storageLevel: Option[String] = AggregationPoliciesModel.storageDefaultValue,
                                     name: String,
                                     description: String = "default description",
                                     sparkStreamingWindow: String = AggregationPoliciesModel.sparkStreamingWindow,
                                     checkpointPath: Option[String] = None,
                                     rawData: RawDataModel,
                                     transformations: Seq[TransformationsModel],
                                     streamTriggers: Seq[TriggerModel],
                                     cubes: Seq[CubeModel],
                                     input: Option[PolicyElementModel] = None,
                                     outputs: Seq[PolicyElementModel],
                                     fragments: Seq[FragmentElementModel],
                                     userPluginsJars: Seq[UserJar] = Seq.empty[UserJar],
                                     remember: Option[String] = None,
                                     sparkConf: Seq[SparkProperty] = Seq.empty[SparkProperty],
                                     initSqlSentences: Seq[SqlSentence] = Seq.empty[SqlSentence],
                                     autoDeleteCheckpoint: Option[Boolean] = None
                                   )


case object AggregationPoliciesModel extends SLF4JLogging {

  val sparkStreamingWindow = "2s"
  val storageDefaultValue = Some("MEMORY_AND_DISK_SER_2")
}

case class PolicyWithStatus(status: PolicyStatusEnum.Value, policy: AggregationPoliciesModel)

case class PolicyResult(policyId: String, policyName: String)

object AggregationPoliciesValidator extends SpartaSerializer {

  def validateDto(policy: AggregationPoliciesModel): Unit = {
    val subErrorModels = (validateCubes(policy) ::: validateTriggers(policy))
      .filter(element => !element._1)

    if (subErrorModels.nonEmpty)
      throw new ServingCoreException(ErrorModel.toString(
        new ErrorModel(ErrorModel.ValidationError, "Policy validation error",
          Option(subErrorModels.map(element => element._2)))))
  }

  //scalastyle:off
  private def validateCubes(policy: AggregationPoliciesModel): List[(Boolean, ErrorModel)] = {
    val outputsNames = policy.outputs.map(_.name)
    val errorModels = List(
      (policy.cubes.forall(cube => cube.name.nonEmpty),
        new ErrorModel(
          ErrorModel.ValidationError_There_is_at_least_one_cube_without_name,
          "There is at least one cube without name")),
      (policy.cubes.forall(cube => cube.dimensions.nonEmpty),
        new ErrorModel(
          ErrorModel.ValidationError_There_is_at_least_one_cube_without_dimensions,
          "There is at least one cube without dimensions")),
      (policy.outputs.nonEmpty
        || (policy.streamTriggers.nonEmpty
        && policy.streamTriggers.forall(trigger => trigger.writer.outputs.nonEmpty)),
        new ErrorModel(
          ErrorModel.ValidationError_The_policy_needs_at_least_one_output,
          "The policy needs at least one output or one trigger in the cube with output")),
      (policy.cubes.nonEmpty || policy.streamTriggers.nonEmpty,
        new ErrorModel(
          ErrorModel.ValidationError_The_policy_needs_at_least_one_cube_or_one_trigger,
          "The policy needs at least one cube or one trigger")),
      (policy.cubes.forall(cube =>
        cube.writer.outputs.forall(output =>
          outputsNames.contains(output))),
        new ErrorModel(
          ErrorModel.ValidationError_There_is_at_least_one_cube_with_a_bad_output,
          "There is at least one cube with a bad output")),
      (policy.cubes.forall(cube =>
        cube.triggers.forall(trigger =>
          trigger.writer.outputs.forall(outputName => outputsNames.contains(outputName)))),
        new ErrorModel(
          ErrorModel.ValidationError_There_is_at_least_one_cube_with_triggers_with_a_bad_output,
          "There is at least one cube with triggers that contains a bad output")),
      (policy.cubes.forall(cube =>
        cube.triggers.flatMap(trigger => trigger.overLast).forall(overLast =>
          parseValueToMilliSeconds(overLast) % parseValueToMilliSeconds(policy.sparkStreamingWindow) == 0
        ) &&
          cube.triggers.flatMap(trigger => trigger.computeEvery).forall(computeEvery =>
            parseValueToMilliSeconds(computeEvery) % parseValueToMilliSeconds(policy.sparkStreamingWindow) == 0
          )
      ),
        new ErrorModel(
          ErrorModel.ValidationError_There_is_at_least_one_trigger_with_a_bad_window_attribute,
          "There is at least one trigger with a bad overlast")),
      (policy.streamTriggers.flatMap(trigger => trigger.overLast).forall(overLast =>
        parseValueToMilliSeconds(overLast) % parseValueToMilliSeconds(policy.sparkStreamingWindow) == 0
      ) &&
        policy.streamTriggers.flatMap(trigger => trigger.computeEvery).forall(computeEvery =>
          parseValueToMilliSeconds(computeEvery) % parseValueToMilliSeconds(policy.sparkStreamingWindow) == 0
        ),
        new ErrorModel(
          ErrorModel.ValidationError_There_is_at_least_one_trigger_with_a_bad_window_attribute,
          "There is at least one trigger with a bad overlast"))
    )
    errorModels
  }

  //scalastyle:on

  private def validateTriggers(policy: AggregationPoliciesModel): List[(Boolean, ErrorModel)] = {
    val outputsNames = policy.outputs.map(_.name)
    val errorModels = List(
      (policy.streamTriggers.forall(trigger =>
        trigger.writer.outputs.forall(outputName =>
          outputsNames.contains(outputName))),
        new ErrorModel(
          ErrorModel.ValidationError_There_is_at_least_one_stream_trigger_with_a_bad_output,
          "There is at least one stream trigger that contains a bad output")),
      (policy.streamTriggers.forall(trigger =>
        trigger.writer.outputs.nonEmpty),
        new ErrorModel(
          ErrorModel.ValidationError_There_is_at_least_one_stream_trigger_with_a_bad_output,
          "There is at least one stream trigger that contains a bad output"))
    )
    errorModels
  }
}
