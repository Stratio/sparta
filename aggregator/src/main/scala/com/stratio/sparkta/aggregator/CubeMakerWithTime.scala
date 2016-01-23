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

import scala.util.{Failure, Success, Try}

import akka.event.slf4j.SLF4JLogging
import org.apache.spark.streaming.dstream.DStream
import org.joda.time.DateTime

import com.stratio.sparkta.sdk._

/**
  * It builds a pre-calculated DataCube with dimension/s, cube/s and operation/s defined by the user in the policy.
  * Steps:
  * From a event stream it builds a Seq[(Seq[DimensionValue],Map[String, JSerializable])] with all needed data.
  * For each cube it calculates aggregations taking the stream calculated in the previous step.
  * Finally, it returns a modified stream with pre-calculated data encapsulated in a UpdateMetricOperation.
  * This final stream will be used mainly by outputs.
  * @param cube that will be contain how the data will be aggregate.
  */
case class CubeMakerWithTime(cube: CubeWithTime) {

  /**
    * It builds the DataCube calculating aggregations.
    * @param inputStream with the original stream of data.
    * @return the built Cube.
    */
  def setUp(inputStream: DStream[Event]): Seq[DStream[(DimensionValuesTime, MeasuresValues)]] = {
      val currentCube = new CubeOperationsWithTime(cube, cube.checkpointTimeDimension, cube.checkpointGranularity)
      val extractedDimensionsStream = currentCube.extractDimensionsAggregations(inputStream)
      Seq(cube.aggregate(extractedDimensionsStream))
  }
}

/**
  * This class is necessary because we need test extractDimensionsAggregations with Spark testSuite for Dstreams.
  * @param cube that will be contain the current cube.
  * @param timeDimension that will be contain the dimensionType id that contain the date.
  * @param checkpointGranularity that will be contain the granularity to calculate the time, only if this
  *                              dimensionType is not present.
  */

private case class CubeOperationsWithTime(cube: CubeWithTime,
                                    timeDimension: String,
                                    checkpointGranularity: String) extends SLF4JLogging {

  /**
    * Extract a modified stream that will be needed to calculate aggregations.
    * @param inputStream with the original stream of data.
    * @return a modified stream after join dimensions, cubes and operations.
    */
  def extractDimensionsAggregations(inputStream: DStream[Event]): DStream[(DimensionValuesTime, InputFieldsValues)] = {
    inputStream.map(event => Try({
      val dimensionValues = for {
        dimension <- cube.dimensions
        value <- event.keyMap.get(dimension.field).toSeq
        (precision, dimValue) = dimension.dimensionType.precisionValue(dimension.precisionKey, value)
      } yield DimensionValue(dimension, TypeOp.transformValueByTypeOp(precision.typeOp, dimValue))
      val eventTime = extractEventTime(dimensionValues)
      (DimensionValuesTime(cube.name, dimensionValues, eventTime, timeDimension), InputFieldsValues(event.keyMap))
    }) match {
      case Success(dimensionValuesTime) => Some(dimensionValuesTime)
      case Failure(exception) => {
        val error = s"Failure[Aggregations]: ${event.toString} | ${exception.getLocalizedMessage}"
        log.error(error, exception)
        None
      }
    }
    ).flatMap(event => event match {
      case Some(value) => Seq(value)
      case None => Seq()
    })
  }

  private def extractEventTime(dimensionValues: Seq[DimensionValue]) = {
    val dimensionsDates =
      dimensionValues.filter(dimensionValue => dimensionValue.dimension.name == timeDimension)

    if (dimensionsDates.isEmpty)
      getDate
    else
      DateOperations.getMillisFromSerializable(dimensionsDates.head.value)
  }

  private def getDate: Long = DateOperations.dateFromGranularity(DateTime.now(), checkpointGranularity)
}
