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

import com.stratio.sparkta.plugin.bucketer.passthrough.PassthroughBucketer
import com.stratio.sparkta.plugin.operator.count.CountOperator
import com.stratio.sparkta.sdk.{BucketType, UpdateMetricOperation, Dimension, DimensionValue}
import org.apache.spark.streaming.TestSuiteBase
import java.io.{Serializable => JSerializable}

class RollupSpec extends TestSuiteBase {

  test("aggregate") {
    val bucketer = new PassthroughBucketer
    val input : Seq[Seq[(Seq[DimensionValue], Map[String,JSerializable])]] = Seq(Seq(
      (Seq(DimensionValue(Dimension("foo", bucketer), new BucketType("identity"), "bar")), Map[String,JSerializable]()),
      (Seq(DimensionValue(Dimension("foo", bucketer), new BucketType("identity"), "bar")), Map[String,JSerializable]()),
      (Seq(DimensionValue(Dimension("foo", bucketer), new BucketType("identity"), "foo")), Map[String,JSerializable]())
    ))
    val rollup = new Rollup(Seq(Dimension("foo", bucketer) -> new BucketType("identity")), Seq(new CountOperator(Map())))
    testOperation(
      input,
      rollup.aggregate,
      Seq(Seq(
        UpdateMetricOperation(Seq(DimensionValue(Dimension("foo", new PassthroughBucketer), new BucketType("identity"), "bar")), Map("COUNT" -> 2L)),
        UpdateMetricOperation(Seq(DimensionValue(Dimension("foo", new PassthroughBucketer), new BucketType("identity"), "foo")), Map("COUNT" -> 1L))
      )),
      false
    )
  }

}
