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
package org.apache.spark.rdd

import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{StreamingContext, Time}

import scala.io.Source

class OneFileDStream(
                      @transient ssc_ : StreamingContext,
                      path: String,
                      batchSize: Int = 1000
                      ) extends InputDStream[String](ssc_) {

  @transient private var lines: Iterator[String] = null

  override def start(): Unit =
    lines = Source.fromURL(getClass.getResource(path)).getLines()

  override def stop(): Unit =
    lines = null

  override def compute(validTime: Time): Option[RDD[String]] =
    if (lines == null || !lines.hasNext) {
      log.debug("Reached the end of stream")
      lines = null
      Some(new EmptyRDD[String](context.sparkContext))
    } else {
      val batch = lines.take(batchSize).toSeq
      log.info(s"Got ${batch.size} lines")
      Some(context.sparkContext.makeRDD[String](batch))
    }

}
