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


object WorkflowValidator {

  //TODO validate workflow
  def validateDto(policy: Workflow): Unit = {}
  /*
  def validateDto(policy: WorkflowModel): Unit = {
    val subErrorModels = (validateRawData(policy) :::
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
  */
}
