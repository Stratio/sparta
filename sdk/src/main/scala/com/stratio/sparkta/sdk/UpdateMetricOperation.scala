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

case class UpdateMetricOperation(
                                  rollupKey: Seq[DimensionValue],
                                  var aggregations: Map[String, Long]) {

  if (rollupKey == null) {
    throw new NullPointerException("rollupKey")
  }
  if (aggregations == null) {
    throw new NullPointerException("aggregations")
  }

  private def SEPARATOR = "__"

  // TODO: This should be refactored out of here
  def keyString: String = {
    rollupKey.map(dimVal => {
      dimVal.bucketType match {
        case x if x == Bucketer.identity => dimVal.dimension.name
        case _ => dimVal.dimension.name + SEPARATOR + dimVal.bucketType.id
      }
    }) mkString SEPARATOR
  }

  override def toString: String = {
    this.keyString + " DATA: " + rollupKey.mkString("|") + " AGGREGATIONS: " + aggregations
  }

}
