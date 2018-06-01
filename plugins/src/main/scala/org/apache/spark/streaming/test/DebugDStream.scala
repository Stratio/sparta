/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.streaming.test

import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.scheduler.{StreamInputInfo, StreamingListener}
import org.apache.spark.streaming.{StreamingContext, Time}

import scala.reflect.ClassTag

class DebugDStream[T: ClassTag](@transient _ssc: StreamingContext, rdd: RDD[T])
  extends InputDStream[T](_ssc) {

  val numberElementsToProcess: Long = rdd.count()
  var numberElementsProcessed: Long = 0L

  override def start(): Unit = {
    log.debug("Starting debug DStream")
  }

  override def stop(): Unit = {
    log.debug("Stopping debug DStream")
  }

  override def compute(validTime: Time): Option[RDD[T]] = {
    if(numberElementsProcessed == numberElementsToProcess){
      Some(_ssc.sc.emptyRDD)
    }
    else {
      _ssc.scheduler.inputInfoTracker.reportInfo(validTime, StreamInputInfo(id, numberElementsToProcess))
      numberElementsProcessed += numberElementsToProcess
      Some(rdd)
    }
  }

}
