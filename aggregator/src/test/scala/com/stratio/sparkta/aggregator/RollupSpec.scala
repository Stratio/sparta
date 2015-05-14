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
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.plugin.bucketer.passthrough.PassthroughBucketer
import com.stratio.sparkta.plugin.operator.count.CountOperator
import com.stratio.sparkta.plugin.operator.sum.SumOperator
import com.stratio.sparkta.sdk._

@RunWith(classOf[JUnitRunner])
class RollupSpec extends TestSuiteBase {

  test("aggregate") {

    val PreserverOrder = true
    val bucketer = new PassthroughBucketer
    val checkpointInterval = 10000
    val checkpointTimeAvailability = 60000
    val checkpointGranularity = "minute"
    val eventGranularity = Output.dateFromGranularity(DateTime.now(), "minute").getTime
    val rollup = new Rollup(
      Seq(DimensionBucket(Dimension("foo", bucketer), new BucketType("identity"))),
      Seq(new CountOperator(Map()), new SumOperator(Map("inputField" -> "n"))),
      checkpointInterval,
      checkpointGranularity,
      checkpointTimeAvailability)

    testOperation(getInput, rollup.aggregate, getOutput, PreserverOrder)

    def getInput: Seq[Seq[(DimensionValuesTime, Map[String, JSerializable])]] = Seq(Seq(
      (DimensionValuesTime(Seq(DimensionValue(DimensionBucket(Dimension("foo", bucketer), new BucketType("identity")),
        "bar")), eventGranularity), Map[String,JSerializable]("n" -> 4)),
      (DimensionValuesTime(Seq(DimensionValue(DimensionBucket(Dimension("foo", bucketer), new BucketType("identity"))
        , "bar")), eventGranularity), Map[String,JSerializable]("n" -> 3)),
      (DimensionValuesTime(Seq(DimensionValue(DimensionBucket(Dimension("foo", bucketer), new BucketType("identity")),
        "foo")), eventGranularity), Map[String,JSerializable]("n" -> 3))),
      Seq(
        (DimensionValuesTime(Seq(DimensionValue(DimensionBucket(Dimension("foo", bucketer), new BucketType("identity")),
          "bar")), eventGranularity), Map[String,JSerializable]("n" -> 4)),
        (DimensionValuesTime(Seq(DimensionValue(DimensionBucket(Dimension("foo", bucketer), new BucketType("identity")),
          "bar")), eventGranularity), Map[String,JSerializable]("n" -> 3)),
        (DimensionValuesTime(Seq(DimensionValue(DimensionBucket(Dimension("foo", bucketer), new BucketType("identity")),
          "foo")), eventGranularity), Map[String,JSerializable]("n" -> 3))))

    def getOutput: Seq[Seq[(DimensionValuesTime, Map[String, Option[Any]])]] = Seq(Seq(
      (DimensionValuesTime(Seq(DimensionValue(DimensionBucket(Dimension("foo", new PassthroughBucketer),
        new BucketType("identity")), "bar")), eventGranularity), Map("count" -> Some(2L), "sum_n" -> Some(7L))),
      (DimensionValuesTime(Seq(DimensionValue(DimensionBucket(Dimension("foo", new PassthroughBucketer),
        new BucketType("identity")), "foo")), eventGranularity), Map("count" -> Some(1L), "sum_n" -> Some(3L)))),
      Seq((DimensionValuesTime(Seq(DimensionValue(DimensionBucket(Dimension("foo", new PassthroughBucketer),
        new BucketType("identity")), "bar")), eventGranularity), Map("count" -> Some(4L), "sum_n" -> Some(14L))),
        (DimensionValuesTime(Seq(DimensionValue(DimensionBucket(Dimension("foo", new PassthroughBucketer),
          new BucketType("identity")), "foo")), eventGranularity), Map("count" -> Some(2L), "sum_n" -> Some(6L)))))
  }

}
