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
package com.stratio.sparta.driver.cube

import com.stratio.sparta.sdk.{DimensionValuesTime, MeasuresValues}
import org.apache.spark.sql.Row
import org.apache.spark.streaming.dstream.DStream

/**
 * It builds a pre-calculated DataCube with dimension/s, cube/s and operation/s defined by the user in the policy.
 * Steps:
 * From a event stream it builds a Seq[(Seq[DimensionValue],Map[String, JSerializable])] with all needed data.
 * For each cube it calculates aggregations taking the stream calculated in the previous step.
 * Finally, it returns a modified stream with pre-calculated data encapsulated in a UpdateMetricOperation.
 * This final stream will be used mainly by outputs.
 * @param cubes that will be contain how the data will be aggregate.
 */
case class CubeMaker(cubes: Seq[Cube]) {

  /**
   * It builds the DataCube calculating aggregations.
   * @param inputStream with the original stream of data.
   * @return the built Cube.
   */
  def setUp(inputStream: DStream[Row]): Seq[(String, DStream[(DimensionValuesTime, MeasuresValues)])] = {
    cubes.map(cube => {
      val currentCube = new CubeOperations(cube)
      val extractedDimensionsStream = currentCube.extractDimensionsAggregations(inputStream)
      (cube.name, cube.aggregate(extractedDimensionsStream))
    })
  }
}
