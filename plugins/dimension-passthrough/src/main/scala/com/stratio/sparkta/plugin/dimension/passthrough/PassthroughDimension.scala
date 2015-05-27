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
package com.stratio.sparkta.plugin.dimension.passthrough

import java.io

import com.stratio.sparkta.sdk._


case class PassthroughDimension() extends Bucketer {

  override def bucket(value: io.Serializable): Map[BucketType, io.Serializable] = {
    Map(Bucketer.getIdentity(getTypeOperation, TypeOp.String) -> value)
  }

  override lazy val bucketTypes: Seq[BucketType] = Seq(Bucketer.getIdentity(getTypeOperation, TypeOp.String))
}
