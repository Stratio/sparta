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

import com.stratio.sparkta.sdk._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.dstream.DStream
import java.io.{Serializable => JSerializable}

/**
 * Use this class to describe a rollup that you want the datacube to keep.
 *
 * For example, if you're counting events with the dimensions (color, size, flavor) and you
 * want to keep a total count for all (color, size) combinations, you'd specify that using a Rollup.
 */
//TODO add operators
case class Rollup(components: Seq[(Dimension, BucketType)], operators: Seq[Operator]) {

  private lazy val operatorsMap = operators.map(_.key).zip(operators).toMap

  def this(dimension: Dimension, bucketType: BucketType, operators: Seq[Operator]) {
    this(Seq((dimension, bucketType)), operators)
  }

  def this(dimension: Dimension, operators: Seq[Operator]) {
    this(Seq((dimension, Bucketer.identity)), operators)
  }

  def aggregate(extractedDimensionsDstream : DStream[(Seq[DimensionValue], Map[String,JSerializable])]) : DStream[UpdateMetricOperation] = {
    //TODO catch errors and null elements control

    val filteredDimensionsDstream : DStream[(Seq[DimensionValue], Map[String,JSerializable])] =
      extractedDimensionsDstream
        .map(x => {
          val dimVals : Seq[DimensionValue] = x._1.filter(dimVal => components.contains(dimVal.dimension -> dimVal.bucketType))
          (dimVals, x._2)
        })
        .filter(_._1.nonEmpty)

    filteredDimensionsDstream
      .mapValues(inputFields =>
        operators.flatMap(op => op.processMap(inputFields).map(op.key -> _)).toMap
      )
      .groupByKey()
      .map(x => {
        val dimVals = x._1
        val reducedMetricMap = x._2.flatten.groupBy(_._1).map(x => {
          val name = x._1
          val op = operatorsMap(name)
          val values = x._2.map(_._2)
          val reducedValue = op.processReduce(values)
          (name, reducedValue)
        })
        UpdateMetricOperation(dimVals, reducedMetricMap)
      })
  }

  override def toString: String = {
    "[Rollup over " + components + "]"
  }
}

