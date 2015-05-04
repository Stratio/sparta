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

import org.apache.spark.streaming.dstream.DStream

import com.stratio.sparkta.sdk._

/**
 * Use this class to describe a rollup that you want the datacube to keep.
 *
 * For example, if you're counting events with the dimensions (color, size, flavor) and you
 * want to keep a total count for all (color, size) combinations, you'd specify that using a Rollup.
 */

case class Rollup(components: Seq[(Dimension, BucketType)], operators: Seq[Operator]) {

  private lazy val operatorsMap = operators.map(op => op.key -> op).toMap

  def this(dimension: Dimension, bucketType: BucketType, operators: Seq[Operator]) {
    this(Seq((dimension, bucketType)), operators)
  }

  def this(dimension: Dimension, operators: Seq[Operator]) {
    this(Seq((dimension, Bucketer.identity)), operators)
  }

  private def mergeLongMaps[K](m1: Map[K, Long], m2: Map[K, Long]): Map[K, Long] = m1 ++ m2.map {
    case (k, v) => k -> (v + m1.getOrElse(k, 0L))
  }

  def aggregate(dimensionValuesStream: DStream[(Seq[DimensionValue],
    Map[String, JSerializable])]): DStream[UpdateMetricOperation] = {

    val filteredDimensionsDstream = dimensionValuesStream.map { case (dimensionValues, aggregationValues) => {
      val dimensionsFiltered = dimensionValues.filter(dimVal => components.find(comp =>
        comp._1 == dimVal.dimension && comp._2.id == dimVal.bucketType.id).nonEmpty)
      (dimensionsFiltered, aggregationValues)
    }
    }.filter(_._1.nonEmpty)

    filteredDimensionsDstream
      .mapValues(inputFields => operators.flatMap(op => op.processMap(inputFields).map(op.key -> Some(_))).toMap)
      .groupByKey()
      .map { case (rollupKey, aggregationValues) => {
      val aggregations = aggregationValues.flatMap(_.toSeq)
        .groupBy { case (name, value) => name }
        .map { case (name, value) => (name, operatorsMap(name).processReduce(value.map(_._2))) }
      UpdateMetricOperation(rollupKey, aggregations)
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

