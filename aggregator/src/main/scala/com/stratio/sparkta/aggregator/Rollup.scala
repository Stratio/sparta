/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.aggregator

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.dstream.DStream

/**
 * Use this class to describe a rollup that you want the datacube to keep.
 *
 * For example, if you're counting events with the dimensions (color, size, flavor) and you
 * want to keep a total count for all (color, size) combinations, you'd specify that using a Rollup.
 */

case class Rollup(components: Seq[(Dimension, BucketType)], operators: Seq[Operator]) {

  private lazy val operatorsMap: Map[String, Operator] = operators.map(op => op.key -> op).toMap

  def this(dimension: Dimension, bucketType: BucketType, operators: Seq[Operator]) {
    this(Seq((dimension, bucketType)), operators)
  }

  def this(dimension: Dimension, operators: Seq[Operator]) {
    this(Seq((dimension, Bucketer.identity)), operators)
  }

  private def mergeLongMaps[K](m1: Map[K, Long], m2: Map[K, Long]): Map[K, Long] =
    m1 ++ m2.map { case (k, v) => k -> (v + m1.getOrElse(k, 0L))}


  def aggregate(dimensionValuesStream: DStream[(Seq[DimensionValue], Map[String, JSerializable])])
  : DStream[UpdateMetricOperation] = {
    //TODO catch errors and null elements control

    val filteredDimensionsDstream: DStream[(Seq[DimensionValue], Map[String, JSerializable])] =
      dimensionValuesStream
        .map(dimensions => {
        val dimVals: Seq[DimensionValue] = dimensions._1
          .filter(dimVal => components.find(comp =>
            comp._1 == dimVal.dimension && comp._2.id == dimVal.bucketType.id).isEmpty)
        (dimVals, dimensions._2)
      })
        .filter(_._1.nonEmpty)

    //Remove fullText bucketer?? Map and then filter key
    filteredDimensionsDstream
      /*.map(inputFields => {
      (inputFields._1.filter(...), operators.flatMap(op => op.processMap(inputFields._2).map(op.key -> _)).toMap)
      })*/
      .mapValues(inputFields => operators.flatMap(op => op.processMap(inputFields).map(op.key -> Some(_)))
        .toMap
      )
      .groupByKey()
      .map(dimGrouped => {
      val dimVals: Seq[DimensionValue] = dimGrouped._1
      val metrics = dimGrouped._2.flatMap(_.toSeq)
      val reducedMetricMap = metrics.groupBy(_._1).map(operation => {
        val name: String = operation._1
        val op = operatorsMap(name)
        val values = operation._2.map(_._2)
        val reducedValue = op.processReduce(values)
        (name, reducedValue)
      })
      //multiplexer output, is better in the outputs plugins
      UpdateMetricOperation(dimVals, reducedMetricMap)
    })
  }

  override def toString: String = {
    "[Rollup over " + components + "]"
  }
}

