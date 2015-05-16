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

package com.stratio.sparkta.sdk

import org.apache.spark.streaming.dstream.DStream

trait Multiplexer {

  def getStreamsFromOptions(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])], multiplexer: Boolean,
                            fixedBuckets: Seq[String]): DStream[(DimensionValuesTime, Map[String, Option[Any]])] = {
    if (multiplexer) {
      if(fixedBuckets.isEmpty) Multiplexer.multiplexStream(stream)
      else Multiplexer.multiplexStream[String](stream, fixedBuckets)
    } else stream
  }
}

object Multiplexer {

  def combine[T](in: (Seq[T], Long)): Seq[(Seq[T], Long)] = {
    for {
      len <- 1 to in._1.length
      combinations <- in._1 combinations len
    } yield (combinations, in._2)
  }

  def combine[T](in: Seq[T]): Seq[Seq[T]] = {
    for {
      len <- 1 to in.length
      combinations <- in combinations len
    } yield combinations
  }

  def multiplexStream(stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])])
  : DStream[(DimensionValuesTime, Map[String, Option[Any]])] = {
    for {
      (dimensionValuesT, aggregations) <- stream
      comb <- combine(dimensionValuesT.dimensionValues).filter(dimVals => dimVals.size >= 1)
    } yield (DimensionValuesTime(comb.sorted, dimensionValuesT.time), aggregations)
  }

  def multiplexStream[T](stream: DStream[(DimensionValuesTime, Map[String, Option[Any]])], fixedBuckets: Seq[T])
  : DStream[(DimensionValuesTime, Map[String, Option[Any]])] = {
    for {
      (dimensionValuesT, aggregations) <- stream
      fixedDims = fixedBuckets.flatMap(bucket => {
        bucket match {
          case value: DimensionValue => bucket.asInstanceOf[Option[DimensionValue]]
          case _ => dimensionValuesT.dimensionValues.find(
            dimValue => dimValue.dimensionBucket.bucketType.id == bucket.asInstanceOf[String])
        }
      })
      comb <- combine(
        dimensionValuesT.dimensionValues.filter(dimVal =>
          !fixedDims.map(dim => dim.getNameDimension).contains(dimVal.getNameDimension)
        )).filter(_.nonEmpty).map(dimensionsValues => dimensionsValues ++ fixedDims)
    } yield (DimensionValuesTime(comb.sorted, dimensionValuesT.time), aggregations)
  }
}