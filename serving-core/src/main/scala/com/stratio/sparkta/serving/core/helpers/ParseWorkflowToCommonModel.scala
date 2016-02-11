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

object ParseWorkflowToCommonModel {

  private def parseWorkflowCubeToCommonCube(cube: WorkflowCubeModel): CommonCubeModel =
    CommonCubeModel(
      name = cube.name,
      checkpointConfig = createCheckpoint(cube),
      dimensions = createCommonDimensionsModel(cube.dimensions),
      operators = cube.operators
    )

  private def parseWorkflowCubesToCommonCubes(cube: Seq[WorkflowCubeModel]): Seq[CommonCubeModel] =
    cube.map(parseWorkflowCubeToCommonCube(_))

  def createCommonDimensionsModel(workflowDimensionModel: Seq[WorkflowDimensionModel]): Seq[CommonDimensionModel] =
    workflowDimensionModel.map(createCommonDimensionModel(_))

  def createCommonDimensionModel(workflowDimensionModel: WorkflowDimensionModel): CommonDimensionModel =
    CommonDimensionModel(
      name = workflowDimensionModel.name,
      field = workflowDimensionModel.field,
      precision = workflowDimensionModel.precision,
      `type` = workflowDimensionModel.`type`,
      configuration = workflowDimensionModel.configuration
    )

  def findWorkflowTimeDimension(cubeModel: WorkflowCubeModel): Option[Seq[String]] = {
    val dimensionComputeLast = cubeModel
      .dimensions
      .filter(dimensionModel => dimensionModel.computeLast.isDefined)
      .headOption
    dimensionComputeLast match {
      case Some(value) => Option(Seq(value.name, value.precision, value.computeLast.get))
      case None => None
    }
  }

  def createCheckpoint(cube: WorkflowCubeModel): CommonCheckpointModel = {
    findWorkflowTimeDimension(cube) match {
      case Some(Seq(name, precision, computeLast)) => CommonCheckpointModel(
        timeDimension = name,
        precision = precision,
        computeLast = Option(computeLast),
        checkpointEvery = Option(cube.checkpointEvery)
      )
      case None => CommonCheckpointModel(
        timeDimension = "none",
        precision = "default",
        computeLast = None,
        checkpointEvery = None
      )
    }
  }

  def parsePolicyToCommonPolicy(workflowPoliciesModel: WorkflowPoliciesModel): CommonPoliciesModel =
    CommonPoliciesModel(
      id = workflowPoliciesModel.id,
      version = workflowPoliciesModel.version,
      storageLevel = workflowPoliciesModel.storageLevel,
      name = workflowPoliciesModel.name,
      description = workflowPoliciesModel.description,
      sparkStreamingWindow = workflowPoliciesModel.sparkStreamingWindow,
      checkpointPath = workflowPoliciesModel.checkpointPath,
      rawData = workflowPoliciesModel.rawData,
      transformations = workflowPoliciesModel.transformations,
      cubes = parseWorkflowCubesToCommonCubes(workflowPoliciesModel.cubes),
      input = workflowPoliciesModel.input,
      outputs = workflowPoliciesModel.outputs,
      fragments = workflowPoliciesModel.fragments
    )
}
