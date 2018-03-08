/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.streaming.test

import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.scheduler.StreamInputInfo
import org.apache.spark.streaming.{StreamingContext, Time}

import scala.reflect.ClassTag

class TestDStream[T: ClassTag](@transient _ssc: StreamingContext,
                               rdd: RDD[T],
                               numEvents: Option[Long] = None,
                               stopAfterGeneration : Option[Long] = None)
  extends InputDStream[T](_ssc) {

  var elements = 0L
  def continueProcessing: Boolean =
    stopAfterGeneration.forall(stopAfter => elements < stopAfter)

  val sizeRDD = rdd.count()

  def itemsForThisIteration: Long =
    stopAfterGeneration.fold(sizeRDD) {
      stopAfter =>
        stopAfter - elements match {
          case x if x >= sizeRDD => sizeRDD
          case x => x
        }
    }

  require(rdd != null,
    "parameter rdd null is illegal, which will lead to NPE in the following transformation")

  override def start(): Unit = {
    log.info("Starting test DStream")
  }

  override def stop(): Unit = {
    log.info("Stopping test Receiver")
  }

  override def compute(validTime: Time): Option[RDD[T]] = {
    if (continueProcessing) {
      val countEvents = itemsForThisIteration
      _ssc.scheduler.inputInfoTracker.reportInfo(validTime, StreamInputInfo(id, countEvents))
      if(stopAfterGeneration.isDefined)
        elements += countEvents
      if(countEvents != sizeRDD)
        Some(_ssc.sc.makeRDD(rdd.take(countEvents.toInt)))
      else
        Some(rdd)
    }
    else {
      _ssc.scheduler.inputInfoTracker.reportInfo(validTime, StreamInputInfo(id, 0))
      Some(_ssc.sc.emptyRDD)
    }
  }
}
