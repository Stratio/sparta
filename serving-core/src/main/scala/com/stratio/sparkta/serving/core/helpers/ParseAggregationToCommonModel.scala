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

object ParseAggregationToCommonModel {

  private def parseCheckpointToCommon(checkpointModel: CheckpointModel): CommonCheckpointModel =
    CommonCheckpointModel.apply(
      timeDimension = checkpointModel.timeDimension,
      precision = checkpointModel.granularity,
      checkpointEvery = Option(checkpointModel.interval.getOrElse(60000).toString),
      computeLast = Option(checkpointModel.timeAvailability.toString))


  private def parseCubeToCommonCube(cube: CubeModel): CommonCubeModel =
    CommonCubeModel(
      name = cube.name,
      checkpointConfig = parseCheckpointToCommon(cube.checkpointConfig),
      dimensions = createCommonDimensionsModel(cube.dimensions),
      operators = cube.operators,
      writer = cube.writer)

  def createCommonDimensionsModel(dimensionModel: Seq[DimensionModel]): Seq[CommonDimensionModel] =
    dimensionModel.map(createCommonDimensionModel(_))

  def createCommonDimensionModel(dimensionModel: DimensionModel): CommonDimensionModel =
    CommonDimensionModel(
      name = dimensionModel.name,
      field = dimensionModel.field,
      precision = dimensionModel.precision,
      `type` = dimensionModel.`type`,
      configuration = dimensionModel.configuration
    )

  private def parseCubesToCommonCubes(cubes: Seq[CubeModel]): Seq[CommonCubeModel] =
    cubes.map(cube => parseCubeToCommonCube(cube))

  def parsePolicyToCommonPolicy(aggregationPoliciesModel: AggregationOldPoliciesModel): CommonPoliciesModel =
    CommonPoliciesModel(
      id = aggregationPoliciesModel.id,
      version = aggregationPoliciesModel.version,
      storageLevel = aggregationPoliciesModel.storageLevel,
      name = aggregationPoliciesModel.name,
      description = aggregationPoliciesModel.description,
      sparkStreamingWindow = aggregationPoliciesModel.sparkStreamingWindow,
      checkpointPath = aggregationPoliciesModel.checkpointPath,
      rawData = aggregationPoliciesModel.rawData,
      transformations = aggregationPoliciesModel.transformations,
      cubes = parseCubesToCommonCubes(aggregationPoliciesModel.cubes),
      input = aggregationPoliciesModel.input,
      outputs = aggregationPoliciesModel.outputs,
      fragments = aggregationPoliciesModel.fragments
    )
}
