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

import java.io

import com.stratio.sparkta.sdk._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.dstream.DStream

/**
 * Use this class to describe a rollup that you want the datacube to keep.
 *
 * For example, if you're counting events with the dimensions (color, size, flavor) and you
 * want to keep a total count for all (color, size) combinations, you'd specify that using a Rollup.
 */
//TODO add operators
case class Rollup(components: Seq[(Dimension, BucketType)], private val operators: Seq[Operator]) {

  def this(dimension: Dimension, bucketType: BucketType, operators: Seq[Operator]) {
    this(Seq((dimension, bucketType)), operators)
  }

  def this(dimension: Dimension, operators: Seq[Operator]) {
    this(Seq((dimension, Bucketer.identity)), operators)
  }

  def aggregate(extractedDimensionsDstream:
                DStream[Map[Dimension, Map[BucketType, Seq[io.Serializable]]]]): DStream[UpdateMetricOperation] = {

    //TODO catch errors and null elements control
    val filteredDimensionsDstream: DStream[Seq[(Dimension, BucketType, Seq[io.Serializable])]] =
      extractedDimensionsDstream.map(m =>
        components.map(c => (c._1, c._2, m.get(c._1).get.get(c._2).get))
      )
    val dstreamProcessedList =
      filteredDimensionsDstream
        .flatMap((l: Seq[(Dimension, BucketType, Seq[io.Serializable])]) => operators.map(o => o.process(l)))
        .groupByKey()
        .map(x => {
        // TODO only support counts. Implement max and min
        val values: Map[String, Long] = x._2.groupBy(_._1).map(kv => (kv._1, kv._2.map(_._2).sum)).toMap
        (x._1, values)
      })
    dstreamProcessedList.map(m => new UpdateMetricOperation(m._1, m._2))
  }

  override def toString: String = {
    "[Rollup over " + components + "]"
  }
}

