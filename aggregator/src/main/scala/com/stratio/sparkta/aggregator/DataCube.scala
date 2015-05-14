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

import java.io.{Serializable => JSerializable}
import java.sql.Timestamp

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
 * @param timeBucket that will be contain the bucketer id that contain the date.
 * @param checkpointGranularity that will be contain the granularity to calculate the time, only if this bucketer is
 *                              not present.
 */
case class DataCube(dimensions: Seq[Dimension],
                    rollups: Seq[Rollup],
                    timeBucket: Option[String],
                    checkpointGranularity: String) {
  /**
   * It builds the DataCube calculating aggregations.
   * @param inputStream with the original stream of data.
   * @return the built DataCube.
   */
  def setUp(inputStream: DStream[Event]): Seq[DStream[(DimensionValuesTime, Map[String, Option[Any]])]] = {
    val extractedDimensionsStream = extractDimensionsStream(inputStream)
    rollups.map(_.aggregate(extractedDimensionsStream))
  }

  /**
   * Extract a modified stream that will be needed to calculate aggregations.
   * @param inputStream with the original stream of data.
   * @return a modified stream after join dimensions, rollups and operations.
   */
  def extractDimensionsStream(inputStream: DStream[Event]):
  DStream[(DimensionValuesTime, Map[String, JSerializable])] = {
    inputStream.map(event => {
      val dimensionValues = for {
        dimension <- dimensions
        value <- event.keyMap.get(dimension.name).toSeq
        (bucketType, bucketedValue) <- dimension.bucketer.bucket(value)
      } yield DimensionValue(DimensionBucket(dimension, bucketType), bucketedValue)
      val eventTime = extractEventTime(dimensionValues)
      (DimensionValuesTime(dimensionValues, eventTime), event.keyMap)
    }).cache()
  }

  private def extractEventTime(dimensionValues : Seq[DimensionValue]) = {
    timeBucket match {
      case Some(bucket) => {
        val dimensionsDates =
          dimensionValues.filter(dimensionValue => dimensionValue.dimensionBucket.bucketType.id == bucket)
        if (dimensionsDates.isEmpty) getDate else dimensionsDates.head.value.asInstanceOf[Timestamp].getTime
      }
      case None => getDate
    }
  }

  private def getDate: Long = Output.dateFromGranularity(DateTime.now(), checkpointGranularity).getTime
}
