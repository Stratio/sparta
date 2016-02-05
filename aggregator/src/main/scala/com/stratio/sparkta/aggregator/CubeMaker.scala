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

package com.stratio.sparkta.aggregator

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.sdk._
import org.apache.spark.sql.Row
import org.apache.spark.streaming.dstream.DStream
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}

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

/**
 * This class is necessary because we need test extractDimensionsAggregations with Spark testSuite for Dstreams.
 *
 * @param cube that will be contain the current cube.
 */

protected case class CubeOperations(cube: Cube) extends SLF4JLogging {

  /**
   * Extract a modified stream that will be needed to calculate aggregations.
   *
   * @param inputStream with the original stream of data.
   * @return a modified stream after join dimensions, cubes and operations.
   */
  def extractDimensionsAggregations(inputStream: DStream[Row]): DStream[(DimensionValuesTime, Row)] = {
    inputStream.flatMap(row => Try {
      val dimensionValues = for {
        dimension <- cube.dimensions
        value = row.get(cube.initSchema.fieldIndex(dimension.field))
        (precision, dimValue) = dimension.dimensionType.precisionValue(dimension.precisionKey, value)
      } yield DimensionValue(dimension, TypeOp.transformValueByTypeOp(precision.typeOp, dimValue))

      cube.expiringDataConfig match {
        case None =>
          (DimensionValuesTime(cube.name, dimensionValues), row)
        case Some(expiringDataConfig) =>
          val eventTime = extractEventTime(dimensionValues)
          val timeDimension = expiringDataConfig.timeDimension
          (DimensionValuesTime(cube.name, dimensionValues, Option(TimeConfig(eventTime, timeDimension))), row)
      }
    } match {
      case Success(dimensionValuesTime) => Some(dimensionValuesTime)
      case Failure(exception) =>
        val error = s"Failure[Aggregations]: ${row.toString} | ${exception.getLocalizedMessage}"
        log.error(error, exception)
        None
    })
  }

  private def extractEventTime(dimensionValues: Seq[DimensionValue]) = {

    val timeDimension = cube.expiringDataConfig.get.timeDimension
    val dimensionsDates =
      dimensionValues.filter(dimensionValue => dimensionValue.dimension.name == timeDimension)

    if (dimensionsDates.isEmpty)
      getDate
    else
      DateOperations.getMillisFromSerializable(dimensionsDates.head.value)
  }

  private def getDate: Long = {
    val checkpointGranularity = cube.expiringDataConfig.get.granularity
    DateOperations.dateFromGranularity(DateTime.now(), checkpointGranularity)
  }
}
