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

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.driver.cube.Cube
import com.stratio.sparta.driver.trigger.Trigger
import com.stratio.sparta.plugin.default.DefaultField
import com.stratio.sparta.plugin.operator.count.CountOperator
import com.stratio.sparta.plugin.operator.sum.SumOperator
import com.stratio.sparta.sdk._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{LongType, StringType, StructField, StructType}
import org.apache.spark.streaming.TestSuiteBase
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CubeTest extends TestSuiteBase {

  test("aggregate with time") {

    val PreserverOrder = true
    val defaultDimension = new DefaultField
    val checkpointTimeAvailability = 60000
    val checkpointGranularity = "minute"
    val eventGranularity = DateOperations.dateFromGranularity(DateTime.now(), "minute")
    val name = "cubeName"
    val timeConfig = Option(TimeConfig(eventGranularity, checkpointGranularity))

    val expiringDataConfig = ExpiringDataConfig(
      checkpointGranularity, checkpointGranularity, checkpointTimeAvailability
    )
    val initSchema = StructType(Seq(
      StructField("n", LongType, false)
    ))
    val operatorCount = new CountOperator("count", StructType(Seq(StructField("count", LongType, true))), Map())
    val operatorSum =
      new SumOperator("sum", StructType(Seq(StructField("n", LongType, true))), Map("inputField" -> "n"))


    val cube = Cube(
      name,
      Seq(Dimension("dim1", "foo", "identity", defaultDimension)),
      Seq(operatorCount, operatorSum),
      initSchema,
      Option(expiringDataConfig),
      Seq.empty[Trigger]
    )

    testOperation(getInput, cube.aggregate, getOutput, PreserverOrder)

    def getInput: Seq[Seq[(DimensionValuesTime, InputFields)]] = Seq(Seq(
      (DimensionValuesTime("testCube",
        Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar")),
        timeConfig), InputFields(Row(4), 1)),

      (DimensionValuesTime("testCube",
        Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar")), timeConfig),
        InputFields(Row(3), 1)),

      (DimensionValuesTime("testCube",
        Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "foo")), timeConfig),
        InputFields(Row(3), 1))),

      Seq(
        (DimensionValuesTime("testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar")), timeConfig),
          InputFields(Row(4), 1)),

        (DimensionValuesTime("testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar")), timeConfig),
          InputFields(Row(3), 1)),

        (DimensionValuesTime("testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "foo")), timeConfig),
          InputFields(Row(3), 1))))

    def getOutput: Seq[Seq[(DimensionValuesTime, MeasuresValues)]] = Seq(
      Seq(
        (DimensionValuesTime("testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar")),
          timeConfig),
          MeasuresValues(Map("count" -> Some(2L), "sum" -> Some(7L)))),

        (DimensionValuesTime("testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "foo")),
          timeConfig),
          MeasuresValues(Map("count" -> Some(1L), "sum" -> Some(3L))))),

      Seq(
        (DimensionValuesTime("testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar")),
          timeConfig),
          MeasuresValues(Map("count" -> Some(4L), "sum" -> Some(14L)))),

        (DimensionValuesTime("testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "foo")),
          timeConfig),
          MeasuresValues(Map("count" -> Some(2L), "sum" -> Some(6L))))))
  }

  test("aggregate without time") {

    val PreserverOrder = true
    val defaultDimension = new DefaultField
    val name = "cubeName"

    val initSchema = StructType(Seq(
      StructField("n", StringType, false)
    ))
    val operatorCount = new CountOperator("count", StructType(Seq(StructField("count", LongType, true))), Map())
    val operatorSum =
      new SumOperator("sum", StructType(Seq(StructField("n", LongType, true))), Map("inputField" -> "n"))

    val cube = Cube(
      name,
      Seq(Dimension("dim1", "foo", "identity", defaultDimension)),
      Seq(operatorCount, operatorSum),
    initSchema,
      expiringDataConfig = None,
      Seq.empty[Trigger]
    )

    testOperation(getInput, cube.aggregate, getOutput, PreserverOrder)

    def getInput: Seq[Seq[(DimensionValuesTime, InputFields)]] = Seq(Seq(
      (DimensionValuesTime("testCube",
        Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar"))), InputFields(Row(4), 1)),

      (DimensionValuesTime("testCube",
        Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar"))), InputFields(Row(3), 1)),

      (DimensionValuesTime("testCube",
        Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "foo"))), InputFields(Row(3), 1))),

      Seq(
        (DimensionValuesTime("testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar"))), InputFields(Row(4), 1)),

        (DimensionValuesTime("testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar"))), InputFields(Row(3), 1)),

        (DimensionValuesTime("testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "foo"))), InputFields(Row(3), 1))))

    def getOutput: Seq[Seq[(DimensionValuesTime, MeasuresValues)]] = Seq(
      Seq(
        (DimensionValuesTime("testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar"))),
          MeasuresValues(Map("count" -> Some(2L), "sum" -> Some(7L)))),

        (DimensionValuesTime("testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "foo"))),
          MeasuresValues(Map("count" -> Some(1L), "sum" -> Some(3L))))),

      Seq(
        (DimensionValuesTime("testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar"))),
          MeasuresValues(Map("count" -> Some(4L), "sum" -> Some(14L)))),

        (DimensionValuesTime("testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "foo"))),
          MeasuresValues(Map("count" -> Some(2L), "sum" -> Some(6L))))))
  }
}
