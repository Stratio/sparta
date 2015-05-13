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

import org.apache.spark.HashPartitioner
import org.apache.spark.streaming.Duration
import org.apache.spark.streaming.dstream.DStream
import org.joda.time.DateTime

import com.stratio.sparkta.sdk._

/**
 * Use this class to describe a rollup that you want the datacube to keep.
 *
 * For example, if you're counting events with the dimensions (color, size, flavor) and you
 * want to keep a total count for all (color, size) combinations, you'd specify that using a Rollup or
 * multipelexer the output
 */

case class Rollup(components: Seq[(Dimension, BucketType)],
                  operators: Seq[Operator],
                  checkpointInterval: Int,
                  checkpointGranularity: String,
                  checkpointTimeAvailability: Int) {

  private lazy val operatorsMap = operators.map(op => op.key -> op).toMap

  def this(dimension: Dimension,
           bucketType: BucketType,
           operators: Seq[Operator],
           checkpointInterval: Int,
           checkpointGranularity: String,
           checkpointAvailable : Int) {
    this(Seq((dimension, bucketType)), operators, checkpointInterval, checkpointGranularity, checkpointAvailable)
  }

  def this(dimension: Dimension,
           operators: Seq[Operator],
           checkpointInterval: Int,
           checkpointGranularity: String,
           checkpointAvailable : Int) {
    this(Seq((dimension, Bucketer.identity)), operators, checkpointInterval, checkpointGranularity, checkpointAvailable)
  }

  def aggregate(dimensionsValues: DStream[((Seq[DimensionValue], Long),
    Map[String, JSerializable])]): DStream[UpdateMetricOperation] = {
    val valuesFiltered = filterDimensionValues(dimensionsValues)
    valuesFiltered.checkpoint(new Duration(checkpointInterval))
    aggregateValues(updateState(valuesFiltered))
  }

  protected def filterDimensionValues(dimensionValues: DStream[((Seq[DimensionValue], Long),
    Map[String, JSerializable])]): DStream[((Seq[DimensionValue], Long), Map[String, JSerializable])] = {
    dimensionValues.map { case ((dimensions, time), aggregationValues) => {
      val dimensionsFiltered = dimensions.filter(dimVal =>
        components.find(comp => comp._1 == dimVal.dimension && comp._2.id == dimVal.bucketType.id).nonEmpty)
      ((dimensionsFiltered, time), aggregationValues)
    }
    }.filter(_._1._1.nonEmpty)
  }

  protected def updateState(dimensionsValues : DStream[((Seq[DimensionValue], Long), Map[String, JSerializable])]):
  DStream[((Seq[DimensionValue], Long), Seq[(String, Option[Any])])] = {
    val newUpdateFunc = (iterator: Iterator[((Seq[DimensionValue], Long),
      Seq[Map[String, JSerializable]],
      Option[Seq[(String, Option[Any])]])]) => {
      val eventTime = Output.dateFromGranularity(DateTime.now(), checkpointGranularity).getTime -
        checkpointTimeAvailability
      iterator.filter(dimensionsData => dimensionsData._1._2 >= eventTime)
        .flatMap { case (dimensionsKey, values, state) =>
        updateFunction(values, state).map(result => (dimensionsKey, result))
      }
    }
    dimensionsValues.updateStateByKey(
      newUpdateFunc, new HashPartitioner(dimensionsValues.context.sparkContext.defaultParallelism), true)
  }

  protected def updateFunction(values: Seq[Map[String, JSerializable]],
                     state: Option[Seq[(String, Option[Any])]]): Option[Seq[(String, Option[Any])]] = {
    val procMap = values.flatMap(inputFields =>
      operators.flatMap(op => op.processMap(inputFields).map(op.key -> Some(_))))
    Some(state.getOrElse(Seq()) ++ procMap)
  }

  protected def aggregateValues(dimensionsValues: DStream[((Seq[DimensionValue], Long), Seq[(String, Option[Any])])]):
  DStream[UpdateMetricOperation] = {
    dimensionsValues.map { case (rollupKey, aggregationValues) => {
      val aggregations = aggregationValues.groupBy { case (name, value) => name }
        .map { case (name, value) => (name, operatorsMap(name).processReduce(value.map(_._2))) }
      UpdateMetricOperation(rollupKey._1, aggregations)
    }
    }
  }

  override def toString: String = "[Rollup over " + components + "]"

  def sortComponents: Seq[(Dimension, BucketType)] = {
    components.sortWith((rollup1, rollup2) =>
      (rollup1._1.name + rollup1._2.id) < (rollup2._1.name + rollup2._2.id))
  }

  def componentNames(dimValues: Seq[(Dimension, BucketType)]): Seq[String] = {
    dimValues.map { case (dimension, bucketType) => {
      bucketType match {
        case Bucketer.identity => dimension.name
        case _ => bucketType.id
      }
    }
    }
  }

  def sortedComponentsNames: Seq[String] = componentNames(sortComponents)

  def sortOperators: Seq[Operator] = operators.sortWith((operator1, operator2) => (operator1.key) < (operator2.key))

  def operatorsNames(operators: Seq[Operator]): Seq[String] = operators.map(operator => operator.key)

  def sortedOperatorsNames: Seq[String] = operatorsNames(sortOperators)
}

