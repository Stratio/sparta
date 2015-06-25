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

import com.stratio.sparkta.plugin.field.default.DefaultField
import org.joda.time.DateTime
import com.stratio.sparkta.plugin.operator.count.CountOperator
import com.stratio.sparkta.sdk._

import org.apache.spark.streaming.TestSuiteBase
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MultiCubeSpec extends TestSuiteBase {

  val PreserverOrder = false

  /**
   * Given a cube defined with:
    - D = A dimension with name eventKey and a string value.
    - B = A DefaultDimension applied to the dimension
    - O = No operator for the cube
    - R = Cube with D+B+O

    This test should produce Seq[(Seq[DimensionValue], Map[String, JSerializable])] with values:

    List(
     ((DimensionValuesTime(Seq(DimensionValue(
        Dimension("dim1", "eventKey", "identity", defaultDimension), "value1")), timestamp), Map("eventKey" -> "value1")
        ),
      (DimensionValuesTime(Seq(DimensionValue(
        Dimension("dim1", "eventKey", "identity", defaultDimension), "value2")), timestamp), Map("eventKey" -> "value2")
        ),
      (DimensionValuesTime(Seq(DimensionValue(
        Dimension("dim1", "eventKey", "identity", defaultDimension), "value3")), timestamp), Map("eventKey" -> "value3")
        ))
   */
  test("DataCube extracts dimensions from events") {

    val checkpointInterval = 10000
    val checkpointTimeAvailability = 60000
    val checkpointGranularity = "minute"
    val timePrecision = None
    val timestamp = DateOperations.dateFromGranularity(DateTime.now(), checkpointGranularity)
    val name = "cubeName"
    val operator = new CountOperator(Map())
    val multiplexer = false
    val defaultDimension = new DefaultField
    val dimension = Dimension("dim1", "eventKey", "identity", defaultDimension)
    val cube = new Cube(name,
      Seq(dimension),
      Seq(operator),
      multiplexer,
      checkpointInterval,
      checkpointGranularity,
      checkpointTimeAvailability)
    val dataCube = new MultiCube(Seq(cube), timePrecision, checkpointGranularity)
    dataCube.currentCube = cube

    testOperation(getEventInput, dataCube.extractDimensionsStream, getEventOutput(timestamp), PreserverOrder)
  }

  /**
   * It gets a stream of data to test.
   * @return a stream of data.
   */
  def getEventInput: Seq[Seq[Event]] =
    Seq(Seq(
      Event(Map("eventKey" -> "value1")),
      Event(Map("eventKey" -> "value2")),
      Event(Map("eventKey" -> "value3"))
    ))

  /**
   * The expected result to test the DataCube output.
   * @return the expected result to test
   */
  def getEventOutput(timestamp : Long): Seq[Seq[(DimensionValuesTime, Map[String, JSerializable])]] = {
    val defaultDimension = new DefaultField
    Seq(Seq(
      (DimensionValuesTime(Seq(DimensionValue(
        Dimension("dim1", "eventKey", "identity", defaultDimension), "value1")), timestamp), Map("eventKey" -> "value1")
        ),
      (DimensionValuesTime(Seq(DimensionValue(
        Dimension("dim1", "eventKey", "identity", defaultDimension), "value2")), timestamp), Map("eventKey" -> "value2")
        ),
      (DimensionValuesTime(Seq(DimensionValue(
        Dimension("dim1", "eventKey", "identity", defaultDimension), "value3")), timestamp), Map("eventKey" -> "value3")
        )
    ))
  }
}
