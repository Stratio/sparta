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

package com.stratio.sparkta.aggregator

import org.apache.spark.streaming.dstream.DStream
import org.joda.time.DateTime

import com.stratio.sparkta.sdk._

/**
 * It builds a pre-calculated DataCube with dimension/s, rollup/s and operation/s defined by the user in the policy.
 * Steps:
 *   From a event stream it builds a Seq[(Seq[DimensionValue],Map[String, JSerializable])] with all needed data.
 *   For each rollup it calculates aggregations taking the stream calculated in the previous step.
 *   Finally, it returns a modified stream with pre-calculated data encapsulated in a UpdateMetricOperation.
 *   This final stream will be used mainly by outputs.
 * @param dimensions that will be contain the fields of the datacube.
 * @param rollups that will be contain how the data will be aggregate.
 */
case class DataCube(dimensions: Seq[Dimension], rollups: Seq[Rollup]) {

case class DataCube(dimensions: Seq[Dimension], rollups: Seq[Rollup], checkpointGranularity: String) {

  def setUp(inputStream: DStream[Event]): Seq[DStream[UpdateMetricOperation]] = {
    val eventGranularity = Output.dateFromGranularity(DateTime.now(), checkpointGranularity).getTime
    val extractedDimensionsStream = inputStream.map((e: Event) => {
      val dimVals = for {
        dimension: Dimension <- dimensions
        value <- e.keyMap.get(dimension.name).toSeq
        (bucketType, bucketedValue) <- dimension.bucketer.bucket(value)
      } yield DimensionValue(dimension, bucketType, bucketedValue)
      ((dimVals, eventGranularity),e.keyMap)
    }).cache()
  /**
   * It builds the DataCube calculating aggregations.
   * @param inputStream with the original stream of data.
   * @return the built DataCube.
   */
  def setUp(inputStream: DStream[Event]): Seq[DStream[UpdateMetricOperation]] = {
    val extractedDimensionsStream = extractDimensionsStream(inputStream)
    rollups.map(_.aggregate(extractedDimensionsStream))
  }
}

  /**
   * Extract a modified stream that will be needed to calculate aggregations.
   * @param inputStream with the original stream of data.
   * @return a modified stream after join dimensions, rollups and operations.
   */
  def extractDimensionsStream(inputStream: DStream[Event]):
    DStream[(Seq[DimensionValue], Map[String, JSerializable])] = {
    inputStream.map((e: Event) => {
      val dimVals = for {
        dimension: Dimension <- dimensions
        value <- e.keyMap.get(dimension.name).toSeq
        (bucketType, bucketedValue) <- dimension.bucketer.bucket(value)
      } yield DimensionValue(dimension, bucketType, bucketedValue)
      (dimVals, e.keyMap)
    }).cache()
  }
}