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

package com.stratio.sparta.serving.core.models.workflow

import com.stratio.sparta.sdk.utils.AggregationTime._
import com.stratio.sparta.serving.core.exception.ServingCoreException
import com.stratio.sparta.serving.core.models.ErrorModel
import com.stratio.sparta.serving.core.models.workflow.cube.CubeModel
import com.stratio.sparta.serving.core.models.workflow.fragment.{FragmentElementModel, FragmentType}
import com.stratio.sparta.serving.core.models.workflow.rawData.RawDataModel
import com.stratio.sparta.serving.core.models.workflow.transformations.TransformationsModel
import com.stratio.sparta.serving.core.models.workflow.trigger.TriggerModel

case class WorkflowModel(id: Option[String] = None,
                         name: String,
                         description: String = "Default description",
                         settings: SettingsModel,
                         rawData: Option[RawDataModel] = None,
                         transformations: Option[TransformationsModel] = None,
                         streamTriggers: Seq[TriggerModel] = Seq.empty[TriggerModel],
                         cubes: Seq[CubeModel] = Seq.empty[CubeModel],
                         input: Option[WorkflowElementModel] = None,
                         outputs: Seq[WorkflowElementModel] = Seq.empty[WorkflowElementModel],
                         fragments: Seq[FragmentElementModel] = Seq.empty[FragmentElementModel])

object WorkflowValidator {

  def validateDto(policy: WorkflowModel): Unit = {
    val subErrorModels = (validateCubes(policy) ::: validateTriggers(policy) ::: validateRawData(policy) :::
      validateTransformations(policy)).filter(element => !element._1)

    if (subErrorModels.nonEmpty)
      throw new ServingCoreException(ErrorModel.toString(
        new ErrorModel(ErrorModel.ValidationError, "Policy validation error",
          Option(subErrorModels.map(element => element._2)))))
  }

  def validateRawData(policy: WorkflowModel): List[(Boolean, ErrorModel)] = {
    val outputsNames = outputNamesFromPolicy(policy)

    List(
      (policy.rawData.forall(rawData => rawData.writer.outputs.forall(output => outputsNames.contains(output))),
        new ErrorModel(ErrorModel.ValidationError_Raw_data_with_a_bad_output, "Raw data with bad outputs")),
      (policy.rawData.forall(rawData => rawData.writer.tableName.isDefined),
        new ErrorModel(ErrorModel.ValidationError_Raw_data_with_bad_table_name, "Raw data with empty table name")),
      (policy.rawData.forall(rawData => rawData.dataField.nonEmpty),
        new ErrorModel(ErrorModel.ValidationError_Raw_data_with_bad_data_field, "Raw data with empty data field")),
      (policy.rawData.forall(rawData => rawData.timeField.nonEmpty),
        new ErrorModel(ErrorModel.ValidationError_Raw_data_with_bad_time_field, "Raw data with empty time field"))
    )
  }

  def validateTransformations(policy: WorkflowModel): List[(Boolean, ErrorModel)] = {
    val outputsNames = outputNamesFromPolicy(policy)

    List(
      (policy.transformations.forall(transformations =>
        transformations.writer.forall(writer => writer.outputs.forall(output => outputsNames.contains(output)))),
        new ErrorModel(
          ErrorModel.ValidationError_Transformations_with_a_bad_output, "Transformations with bad outputs"))
    )
  }

  //scalastyle:off
  private def validateCubes(policy: WorkflowModel): List[(Boolean, ErrorModel)] = {
    val outputsNames = outputNamesFromPolicy(policy)

    List(
      (policy.cubes.forall(cube => cube.name.nonEmpty),
        new ErrorModel(
          ErrorModel.ValidationError_There_is_at_least_one_cube_without_name,
          "There is at least one cube without name")),
      (policy.cubes.forall(cube => cube.dimensions.nonEmpty),
        new ErrorModel(
          ErrorModel.ValidationError_There_is_at_least_one_cube_without_dimensions,
          "There is at least one cube without dimensions")),
      (policy.cubes.nonEmpty || policy.streamTriggers.nonEmpty || policy.rawData.isDefined ||
        (policy.transformations.isDefined && policy.transformations.get.writer.isDefined),
        new ErrorModel(
          ErrorModel.ValidationError_The_policy_needs_at_least_one_cube_or_one_trigger_or_raw_data_or_transformations_with_save,
          "The policy needs at least one cube, one trigger, save raw data or save transformations")),
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
          parseValueToMilliSeconds(overLast) % parseValueToMilliSeconds(policy.settings.streamingSettings.window) == 0
        ) &&
          cube.triggers.flatMap(trigger => trigger.computeEvery).forall(computeEvery =>
            parseValueToMilliSeconds(computeEvery) % parseValueToMilliSeconds(
              policy.settings.streamingSettings.window) == 0
          )
      ),
        new ErrorModel(
          ErrorModel.ValidationError_There_is_at_least_one_trigger_with_a_bad_window_attribute,
          "There is at least one trigger with a bad overlast")),
      (policy.streamTriggers.flatMap(trigger => trigger.overLast).forall(overLast =>
        parseValueToMilliSeconds(overLast) % parseValueToMilliSeconds(policy.settings.streamingSettings.window) == 0
      ) &&
        policy.streamTriggers.flatMap(trigger => trigger.computeEvery).forall(computeEvery =>
          parseValueToMilliSeconds(computeEvery) % parseValueToMilliSeconds(policy.settings.streamingSettings.window) == 0
        ),
        new ErrorModel(
          ErrorModel.ValidationError_There_is_at_least_one_trigger_with_a_bad_window_attribute,
          "There is at least one trigger with a bad overlast"))
    )
  }

  //scalastyle:on

  private def validateTriggers(policy: WorkflowModel): List[(Boolean, ErrorModel)] = {
    val outputsNames = outputNamesFromPolicy(policy)
    val errorModels = List(
      (policy.streamTriggers.forall(trigger =>
        trigger.writer.outputs.forall(outputName =>
          outputsNames.contains(outputName))),
        new ErrorModel(
          ErrorModel.ValidationError_There_is_at_least_one_stream_trigger_with_a_bad_output,
          "There is at least one stream trigger that contains a bad output"))
    )
    errorModels
  }

  private def outputNamesFromPolicy(policy: WorkflowModel): Seq[String] = {
    val outputsNames = policy.outputs.map(_.name)
    val outputsFragmentsNames = policy.fragments.flatMap(fragment =>
      if (fragment.fragmentType == FragmentType.OutputValue) Some(fragment.name) else None)

    outputsNames ++ outputsFragmentsNames
  }
}
