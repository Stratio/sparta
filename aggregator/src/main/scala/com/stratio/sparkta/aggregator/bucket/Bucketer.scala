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
package com.stratio.sparkta.aggregator.bucket

import java.io

/**
 * Created by ajnavarro on 9/10/14.
 */

case class BucketType(id: String)

trait Bucketer {
  /**
   * When writing to the cube at some address, the address will have one coordinate for each
   * dimension in the cube, for example (time: 348524388, location: portland). For each
   * dimension, for each bucket type within that dimension, the bucketer must transform the
   * input data into the bucket that should be used to store the data.
   */
  def bucketForWrite(value: io.Serializable): Map[BucketType, Seq[io.Serializable]]

  def bucketTypes(): Seq[BucketType]

}

object Bucketer {
  val identity = new BucketType("identity")
}
