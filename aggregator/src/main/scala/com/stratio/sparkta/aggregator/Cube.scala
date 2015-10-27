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
import scala.util.Try

import org.apache.spark.HashPartitioner
import org.apache.spark.streaming.Duration
import org.apache.spark.streaming.dstream.DStream
import org.joda.time.DateTime

import com.stratio.sparkta.sdk._
import com.stratio.sparkta.serving.core.SparktaConfig
import com.stratio.sparkta.serving.core.constants.AppConstant

/**
 * Use this class to describe a cube that you want the multicube to keep.
 *
 * For example, if you're counting events with the dimensions (color, size, flavor) and you
 * want to keep a total count for all (color, size) combinations, you'd specify that using a Cube or
 * multipelexer the output
 */
case class Cube(name: String,
                dimensions: Seq[Dimension],
                operators: Seq[Operator],
                checkpointTimeDimension: String,
                checkpointInterval: Int,
                checkpointGranularity: String,
                checkpointTimeAvailability: Long) {

  private lazy val operatorsMap = operators.map(op => op.key -> op).toMap
  private lazy val rememberPartitioner =
    Try(SparktaConfig.getDetailConfig.get.getBoolean(AppConstant.ConfigRememberPartitioner)).getOrElse(true)

  def aggregate(dimensionsValues: DStream[(DimensionValuesTime,
    Map[String, JSerializable])]): DStream[(DimensionValuesTime, Map[String, Option[Any]])] = {
    val valuesFiltered = filterDimensionValues(dimensionsValues)
    valuesFiltered.checkpoint(new Duration(checkpointInterval))
    aggregateValues(updateState(valuesFiltered))
  }

  protected def filterDimensionValues(dimensionValues: DStream[(DimensionValuesTime,
    Map[String, JSerializable])]): DStream[(DimensionValuesTime, Map[String, JSerializable])] = {
    dimensionValues.map { case (dimensionsValuesTime, aggregationValues) => {
      val dimensionsFiltered = dimensionsValuesTime.dimensionValues.filter(dimVal =>
        dimensions.find(comp => comp.name == dimVal.dimension.name).nonEmpty)
      (DimensionValuesTime(dimensionsFiltered, dimensionsValuesTime.time, checkpointTimeDimension), aggregationValues)
    }
    }
  }

  protected def updateState(dimensionsValues: DStream[(DimensionValuesTime, Map[String, JSerializable])]):
  DStream[(DimensionValuesTime, Seq[(String, Option[Any])])] = {
    val newUpdateFunc = (iterator: Iterator[(DimensionValuesTime,
      Seq[Map[String, JSerializable]],
      Option[Seq[(String, Option[Any])]])]) => {
      val eventTime =
        DateOperations.dateFromGranularity(DateTime.now(), checkpointGranularity) - checkpointTimeAvailability
      iterator.filter(dimensionsData => {
        dimensionsData._1.time >= eventTime
      })
        .flatMap { case (dimensionsKey, values, state) =>
          updateFunction(values, state).map(result => (dimensionsKey, result))
        }
    }
    dimensionsValues.updateStateByKey(
      newUpdateFunc, new HashPartitioner(dimensionsValues.context.sparkContext.defaultParallelism), rememberPartitioner)
  }

  protected def updateFunction(values: Seq[Map[String, JSerializable]],
                               state: Option[Seq[(String, Option[Any])]]): Option[Seq[(String, Option[Any])]] = {
    val procMap = values.flatMap(inputFields =>
      operators.flatMap(op => op.processMap(inputFields).map(op.key -> Some(_))))
    Some(state.getOrElse(Seq()) ++ procMap)
  }

  protected def aggregateValues(dimensionsValues: DStream[(DimensionValuesTime, Seq[(String, Option[Any])])]):
  DStream[(DimensionValuesTime, Map[String, Option[Any]])] = {
    dimensionsValues.mapValues(aggregationValues => {
      aggregationValues.groupBy { case (name, value) => name }
        .map { case (name, value) => (name, operatorsMap(name).processReduce(value.map(_._2))) }
    })
  }
}
