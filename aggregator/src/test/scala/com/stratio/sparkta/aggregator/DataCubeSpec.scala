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

import com.stratio.sparkta.plugin.bucketer.passthrough.PassthroughBucketer
import com.stratio.sparkta.plugin.operator.count.CountOperator
import com.stratio.sparkta.sdk._
import org.apache.spark.streaming.TestSuiteBase
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FeatureSpec, FlatSpec}

/**
 * Tests over DataCube operations.
 * @author anistal
 */
@RunWith(classOf[JUnitRunner])
class DataCubeSpec extends TestSuiteBase {

  val PreserverOrder = false

  test("""Given a rollup defined with:
    - D = A dimension with name eventKey and a string value.
    - B = A PassthroughBucketer applied to the dimension
    - O = No operator for the rollup
    - R = Rollup with D+B+O

    This test should produce Seq[(Seq[DimensionValue], Map[String, JSerializable])] with values:

    List(
     (List(DimensionValue(
       Dimension(eventKey,PassthroughBucketer()),BucketType(identity,Map()),value1)),Map(eventKey -> value1)),
     (List(DimensionValue(
       Dimension(eventKey,PassthroughBucketer()),BucketType(identity,Map()),value2)),Map(eventKey -> value2)),
     (List(DimensionValue(
       Dimension(eventKey,PassthroughBucketer()),BucketType(identity,Map()),value3)),Map(eventKey -> value3)))
         """) {
    val bucketer = new PassthroughBucketer
    val dimension = Dimension("eventKey", bucketer)
    val operator = new CountOperator(Map())
    val bucketType = new BucketType("identity")
    val rollup = new Rollup(Seq(dimension -> bucketType), Seq(operator))
    val dataCube = new DataCube(Seq(dimension), Seq(rollup))

    testOperation(
      getEventInput,
      dataCube.extractDimensionsStream,
      getEventOutput,
      PreserverOrder
    )
  }

  /**
   * It gets a stream of data to test.
   * @return a stream of data.
   */
  def getEventInput(): Seq[Seq[Event]] =
    Seq(Seq(
      Event(Map("eventKey" -> "value1")),
      Event(Map("eventKey" -> "value2")),
      Event(Map("eventKey" -> "value3"))
    ))

  /**
   * The expected result to test the DataCube output.
   * @return the expected result to test
   */
  def getEventOutput(): Seq[Seq[(Seq[DimensionValue], Map[String, JSerializable])]] =
    Seq(Seq(
      (Seq(DimensionValue(
        Dimension("eventKey", new PassthroughBucketer), BucketType("identity", Map()), "value1")),
        Map("eventKey" -> "value1")
      ),
      (Seq(DimensionValue(
        Dimension("eventKey", new PassthroughBucketer), BucketType("identity", Map()), "value2")),
        Map("eventKey" -> "value2")
      ),
      (Seq(DimensionValue(
        Dimension("eventKey", new PassthroughBucketer), BucketType("identity", Map()), "value3")),
        Map("eventKey" -> "value3")
      )
    ))
}
