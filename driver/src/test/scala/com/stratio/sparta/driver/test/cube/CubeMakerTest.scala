/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
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
package com.stratio.sparta.driver.test.cube

import java.sql.Timestamp

import com.github.nscala_time.time.Imports._
import com.stratio.sparta.driver.step.{Cube, CubeOperations}
import com.stratio.sparta.driver.writer.WriterOptions
import com.stratio.sparta.plugin.default.DefaultField
import com.stratio.sparta.plugin.cube.field.datetime.DateTimeField
import com.stratio.sparta.plugin.cube.operator.count.CountOperator
import com.stratio.sparta.sdk.pipeline.aggregation.cube.{Dimension, DimensionValue, DimensionValuesTime, InputFields}
import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import com.stratio.sparta.sdk.utils.AggregationTime
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{LongType, StringType, StructField, StructType, TimestampType}
import org.apache.spark.streaming.TestSuiteBase
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CubeMakerTest extends TestSuiteBase {

  val PreserverOrder = false

  /**
   * Given a cube defined with:
    - D = A dimension with name eventKey and a string value.
    - B = A DefaultDimension applied to the dimension
    - O = No operator for the cube
    - R = Cube with D+B+O

    This test should produce Seq[(Seq[DimensionValue], Map[String, JSerializable])]
   */
  test("DataCube extracts dimensions from events") {

    val checkpointGranularity = "minute"
    val millis = AggregationTime.truncateDate(DateTime.now, checkpointGranularity)
    val sqlTimestamp = new Timestamp(millis)
    val name = "cubeName"
    val operator = new CountOperator("count", StructType(Seq(StructField("count", LongType, true))), Map())
    val defaultDimension = new DefaultField
    val timeField = new DateTimeField
    val dimension = Dimension("dim1", "eventKey", "identity", defaultDimension)
    val timeDimension = Dimension("minute", "minute", "minute", timeField)
    val initSchema = StructType(Seq(
      StructField("eventKey", StringType, false),
      StructField("minute", TimestampType, false)
    ))
    val cube = Cube(name,
      Seq(dimension, timeDimension),
      Seq(operator),
      initSchema,
      initSchema,
      TypeOp.Timestamp,
      expiringDataConfig = None,
      WriterOptions(),
      true
    )
    val dataCube = new CubeOperations(cube)

    testOperation(getEventInput(sqlTimestamp), dataCube.extractDimensionsAggregations,
      getEventOutput(sqlTimestamp, millis), PreserverOrder)
  }

  /**
   * It gets a stream of data to test.
   * @return a stream of data.
   */
  def getEventInput(ts: Timestamp): Seq[Seq[Row]] =
    Seq(Seq(
      Row("value1", ts),
      Row("value2", ts),
      Row("value3", ts)
    ))

  /**
   * The expected result to test the DataCube output.
   * @return the expected result to test
   */
  def getEventOutput(timestamp: Timestamp, millis: Long):
  Seq[Seq[(DimensionValuesTime, InputFields)]] = {
    val dimensionString = Dimension("dim1", "eventKey", "identity", new DefaultField)
    val dimensionTime = Dimension("minute", "minute", "minute", new DateTimeField)
    val dimensionValueString1 = DimensionValue(dimensionString, "value1")
    val dimensionValueString2 = dimensionValueString1.copy(value = "value2")
    val dimensionValueString3 = dimensionValueString1.copy(value = "value3")
    val dimensionValueTs = DimensionValue(dimensionTime, timestamp)
    val tsMap = Row(timestamp)
    val valuesMap1 = InputFields(Row("value1", timestamp), 1)
    val valuesMap2 = InputFields(Row("value2", timestamp), 1)
    val valuesMap3 = InputFields(Row("value3", timestamp), 1)

    Seq(Seq(
      (DimensionValuesTime("cubeName", Seq(dimensionValueString1, dimensionValueTs)), valuesMap1),
      (DimensionValuesTime("cubeName", Seq(dimensionValueString2, dimensionValueTs)), valuesMap2),
      (DimensionValuesTime("cubeName", Seq(dimensionValueString3, dimensionValueTs)), valuesMap3)
    ))
  }
}
