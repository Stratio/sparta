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
package com.stratio.sparkta.sdk

import org.apache.spark.streaming.dstream.DStream

/**
 * Created by jcgarcia on 24/03/15.
 */
trait Multiplexer {

  def getStreamsFromOptions(stream: DStream[UpdateMetricOperation], multiplexer: Boolean,
                            fixedBucket: String): DStream[UpdateMetricOperation]
}

object Multiplexer {

  def combine[T](in: Seq[T]): Seq[Seq[T]] = {
    for {
      len <- 1 to in.length
      combinations <- in combinations len
    } yield combinations
  }

  def multiplexStream(stream: DStream[UpdateMetricOperation]) : DStream[UpdateMetricOperation] = {
     for {
        upMetricOp: UpdateMetricOperation <- stream
        comb: Seq[DimensionValue] <- combine(upMetricOp.rollupKey)
          .filter(dimVals => dimVals.size >= 1)
          // necesary for other options?
          //|| ((dimVals.size == 1) && (dimVals.last.bucketType != Bucketer.fulltext)))
      } yield UpdateMetricOperation(UpdateMetricOperation.sortDimensionValues(comb), upMetricOp.aggregations)
  }

  def multiplexStream[T](stream: DStream[UpdateMetricOperation],
                               fixedBucket : T) : DStream[UpdateMetricOperation] = {
    for {
      upMetricOp: UpdateMetricOperation <- stream
      fixedDim = fixedBucket match {
        case Some(value: DimensionValue) => fixedBucket.asInstanceOf[Option[DimensionValue]]
        case value : String => upMetricOp.rollupKey.find(
          dimValue => dimValue.bucketType.id == fixedBucket.asInstanceOf[String])
      }
      comb: Seq[DimensionValue] <- combine(
        upMetricOp.rollupKey.filter(_.bucketType.id != (fixedDim match {
          case None => ""
          case _ => fixedDim.get.bucketType.id
        })))
        .filter(dimVals => dimVals.size >= 1)
        .map(seqDimVal => {
        fixedDim match {
          case None => seqDimVal
          case _ => seqDimVal ++ Seq(fixedDim.get)
        }
      })
    } yield UpdateMetricOperation(UpdateMetricOperation.sortDimensionValues(comb), upMetricOp.aggregations)
  }
}