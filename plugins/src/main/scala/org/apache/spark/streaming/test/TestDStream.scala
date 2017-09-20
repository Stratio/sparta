/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
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

package org.apache.spark.streaming.test

import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.scheduler.StreamInputInfo
import org.apache.spark.streaming.{StreamingContext, Time}

import scala.reflect.ClassTag

class TestDStream[T: ClassTag](@transient _ssc: StreamingContext, rdd: RDD[T], numEvents: Option[Long] = None)
  extends InputDStream[T](_ssc) {

  require(rdd != null,
    "parameter rdd null is illegal, which will lead to NPE in the following transformation")

  override def start(): Unit = {
    log.info("Starting test DStream")
  }

  override def stop(): Unit = {
    log.info("Stopping test Receiver")
  }

  override def compute(validTime: Time): Option[RDD[T]] = {
    val countEvents = numEvents.getOrElse(rdd.count())

    _ssc.scheduler.inputInfoTracker.reportInfo(validTime, StreamInputInfo(id, countEvents))
    Some(rdd)
  }
}
