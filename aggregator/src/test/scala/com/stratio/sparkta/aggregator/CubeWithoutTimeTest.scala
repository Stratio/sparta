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
import com.stratio.sparkta.plugin.operator.count.CountOperator
import com.stratio.sparkta.plugin.operator.sum.SumOperator
import com.stratio.sparkta.sdk._
import org.apache.spark.streaming.TestSuiteBase
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CubeWithoutTimeTest extends TestSuiteBase {

  test("aggregate without time") {

    val PreserverOrder = true
    val defaultDimension = new DefaultField
    val checkpointInterval = 10000
    val name = "cubeName"

    val cube = CubeWithoutTime(
      name,
      Seq(Dimension("dim1", "foo", "identity", defaultDimension)),
      Seq(new CountOperator("count", Map()), new SumOperator("sum", Map("inputField" -> "n"))),
      checkpointInterval)

    testOperation(getInput, cube.aggregate, getOutput, PreserverOrder)

    def getInput: Seq[Seq[(DimensionValuesWithoutTime, InputFieldsValues)]] = Seq(Seq(
      (DimensionValuesWithoutTime(
        "testCube",
        Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar"))),
        InputFieldsValues(Map[String, JSerializable]("n" -> 4))),

      (DimensionValuesWithoutTime(
        "testCube",
        Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar"))),
        InputFieldsValues(Map[String, JSerializable]("n" -> 3))),

      (DimensionValuesWithoutTime(
        "testCube",
        Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "foo"))),
        InputFieldsValues(Map[String, JSerializable]("n" -> 3)))),

      Seq(
        (DimensionValuesWithoutTime(
          "testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar"))),
          InputFieldsValues(Map[String, JSerializable]("n" -> 4))),

        (DimensionValuesWithoutTime(
          "testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar"))),
          InputFieldsValues(Map[String, JSerializable]("n" -> 3))),

        (DimensionValuesWithoutTime(
          "testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "foo"))),
          InputFieldsValues(Map[String, JSerializable]("n" -> 3)))))

    def getOutput: Seq[Seq[(DimensionValuesWithoutTime, MeasuresValues)]] = Seq(
      Seq(
        (DimensionValuesWithoutTime(
          "testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar"))),
          MeasuresValues(Map("count" -> Some(2L), "sum" -> Some(7L)))),

        (DimensionValuesWithoutTime("testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "foo"))),
          MeasuresValues(Map("count" -> Some(1L), "sum" -> Some(3L))))),

      Seq(
        (DimensionValuesWithoutTime(
          "testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "bar"))),
          MeasuresValues(Map("count" -> Some(4L), "sum" -> Some(14L)))),

        (DimensionValuesWithoutTime(
          "testCube",
          Seq(DimensionValue(Dimension("dim1", "foo", "identity", defaultDimension), "foo"))),
          MeasuresValues(Map("count" -> Some(2L), "sum" -> Some(6L))))))
  }
}
