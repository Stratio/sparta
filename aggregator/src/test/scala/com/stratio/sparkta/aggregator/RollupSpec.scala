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

package com.stratio.sparkta.aggregator

import java.io.{Serializable => JSerializable}

import org.apache.spark.streaming.TestSuiteBase
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.plugin.bucketer.passthrough.PassthroughBucketer
import com.stratio.sparkta.plugin.operator.count.CountOperator
import com.stratio.sparkta.plugin.operator.sum.SumOperator
import com.stratio.sparkta.sdk.{BucketType, Dimension, DimensionValue, UpdateMetricOperation}

@RunWith(classOf[JUnitRunner])
class RollupSpec extends TestSuiteBase {

  test("aggregate") {
    val bucketer = new PassthroughBucketer
    val input: Seq[Seq[(Seq[DimensionValue], Map[String, JSerializable])]] = Seq(Seq(
      (Seq(DimensionValue(Dimension("foo", bucketer), new BucketType("identity"), "bar")),
        Map[String, JSerializable]("n" -> 4)),
      (Seq(DimensionValue(Dimension("foo", bucketer), new BucketType("identity"), "bar")),
        Map[String, JSerializable]("n" -> 3)),
      (Seq(DimensionValue(Dimension("foo", bucketer), new BucketType("identity"), "foo")),
        Map[String, JSerializable]("n" -> 2))
    ))
    val rollup = new Rollup(
      Seq(Dimension("foo", bucketer) -> new BucketType("identity")),
      Seq(new CountOperator(Map()), new SumOperator(Map("inputField" -> "n")))
    )
    testOperation(
      input,
      rollup.aggregate,
      Seq(Seq(
        UpdateMetricOperation(Seq(DimensionValue(Dimension("foo", new PassthroughBucketer),
          new BucketType("identity"), "bar")), Map("count" -> Some(2L), "sum_n" -> Some(BigDecimal(7)))),
        UpdateMetricOperation(Seq(DimensionValue(Dimension("foo", new PassthroughBucketer),
          new BucketType("identity"), "foo")), Map("count" -> Some(1L), "sum_n" -> Some(BigDecimal(2))))
      )),
      true
    )
  }
}
