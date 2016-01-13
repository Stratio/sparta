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
import java.sql.Timestamp

import com.github.nscala_time.time.Imports._
import org.apache.spark.streaming.TestSuiteBase
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.plugin.field.datetime.DateTimeField
import com.stratio.sparkta.plugin.field.default.DefaultField
import com.stratio.sparkta.plugin.operator.count.CountOperator
import com.stratio.sparkta.sdk._

@RunWith(classOf[JUnitRunner])
class CubeWithTimeMakerTest extends TestSuiteBase {

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
    val checkpointTimeAvailability = 600000
    val checkpointGranularity = "minute"
    val timeDimensionName = "minute"
    val millis = DateOperations.dateFromGranularity(DateTime.now, checkpointGranularity)
    val sqlTimestamp = new Timestamp(millis)
    val name = "cubeName"
    val operator = new CountOperator("count", Map())
    val defaultDimension = new DefaultField
    val timeField = new DateTimeField
    val dimension = Dimension("dim1", "eventKey", "identity", defaultDimension)
    val timeDimension = Dimension("minute", "minute", "minute", timeField)
    val cube = CubeWithTime(name,
      Seq(dimension, timeDimension),
      Seq(operator),
      checkpointGranularity,
      checkpointInterval,
      checkpointGranularity,
      checkpointTimeAvailability)
    val dataCube = new CubeOperationsWithTime(cube, timeDimensionName, checkpointGranularity)

    testOperation(getEventInput(sqlTimestamp), dataCube.extractDimensionsAggregations,
      getEventOutput(sqlTimestamp, millis), PreserverOrder)
  }

  /**
   * It gets a stream of data to test.
   * @return a stream of data.
   */
  def getEventInput(ts: Timestamp): Seq[Seq[Event]] =
    Seq(Seq(
      Event(Map("eventKey" -> "value1", "minute" -> ts)),
      Event(Map("eventKey" -> "value2", "minute" -> ts)),
      Event(Map("eventKey" -> "value3", "minute" -> ts))
    ))

  /**
   * The expected result to test the DataCube output.
   * @return the expected result to test
   */
  def getEventOutput(timestamp: Timestamp, millis: Long):
  Seq[Seq[(DimensionValuesTime, InputFieldsValues)]] = {
    val dimensionString = Dimension("dim1", "eventKey", "identity", new DefaultField)
    val dimensionTime = Dimension("minute", "minute", "minute", new DateTimeField)
    val dimensionValueString1 = DimensionValue(dimensionString, "value1")
    val dimensionValueString2 = dimensionValueString1.copy(value = "value2")
    val dimensionValueString3 = dimensionValueString1.copy(value = "value3")
    val dimensionValueTs = DimensionValue(dimensionTime, timestamp)
    val tsMap = Map("minute" -> timestamp)
    val valuesMap1 = InputFieldsValues(Map("eventKey" -> "value1") ++ tsMap)
    val valuesMap2 = InputFieldsValues(Map("eventKey" -> "value2") ++ tsMap)
    val valuesMap3 = InputFieldsValues(Map("eventKey" -> "value3") ++ tsMap)

    Seq(Seq(
      (DimensionValuesTime("cubeName",Seq(dimensionValueString1, dimensionValueTs), millis, "minute"), valuesMap1),
      (DimensionValuesTime("cubeName",Seq(dimensionValueString2, dimensionValueTs), millis, "minute"), valuesMap2),
      (DimensionValuesTime("cubeName",Seq(dimensionValueString3, dimensionValueTs), millis, "minute"), valuesMap3)
    ))
  }
}
