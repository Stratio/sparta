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
import org.apache.spark.streaming.dstream.DStream
import java.io.{Serializable => JSerializable}

case class DataCube(dimensions: Seq[Dimension], rollups: Seq[Rollup]) {

  def setUp(inputStream: DStream[Event]): Seq[DStream[UpdateMetricOperation]] = {
    //TODO: add event values
    val extractedDimensionsStream: DStream[(Seq[DimensionValue], Map[String, JSerializable])] = inputStream
      .map((e: Event) => {
        val dimVals = for {
          dimension: Dimension <- dimensions
          value <- e.keyMap.get(dimension.name).toSeq
          (bucketType, bucketedValue) <- dimension.bucketer.bucket(value)
        } yield DimensionValue(dimension, bucketType, bucketedValue)
        (dimVals, e.keyMap)
      })
      .cache()

    // Create rollups
    rollups.map(_.aggregate(extractedDimensionsStream))
  }

}
