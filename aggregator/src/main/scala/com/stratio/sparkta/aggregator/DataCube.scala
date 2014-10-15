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

import com.stratio.sparkta.aggregator.bucket.BucketType
import com.stratio.sparkta.aggregator.domain.Event
import org.apache.spark.streaming.dstream.DStream

/**
 * Created by ajnavarro on 9/10/14.
 */
case class DataCube(dimensions: Seq[Dimension], rollups: Seq[Rollup]) {

  def setUp(inputStream: DStream[Event]): Seq[DStream[UpdateMetricOperation]] = {
    //TODO: add event values
    val extractedDimensionsStream: DStream[Map[Dimension, Map[BucketType, Seq[io.Serializable]]]] =
      inputStream.map((e: Event) =>
        (
          for {
            dimension: Dimension <- dimensions
            value: io.Serializable <- e.keyMap.get(dimension.name)
          } yield (dimension, dimension.bucketer.bucketForWrite(value))
          ).toMap

      )
    val cachedExtractedDimensionsStream = extractedDimensionsStream.cache()

    // Create rollups
    rollups.map(rollup => {
      rollup.aggregate(cachedExtractedDimensionsStream)
    })
  }

}
