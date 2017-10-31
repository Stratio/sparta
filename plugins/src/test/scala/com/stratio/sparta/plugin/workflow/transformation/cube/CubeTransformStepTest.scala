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

package com.stratio.sparta.plugin.workflow.transformation.cube

import java.util.Date

import com.stratio.sparta.plugin.workflow.transformation.cube.operators._
import com.stratio.sparta.plugin.workflow.transformation.cube.sdk._
import com.stratio.sparta.sdk.workflow.enumerators.WhenError
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.apache.spark.streaming.TestSuiteBase
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class CubeTransformStepTest extends TestSuiteBase with Matchers {


  test("Cube associative") {
    val initSchema = StructType(Seq(
      StructField("dim1", StringType, nullable = false),
      StructField("dim2", IntegerType, nullable = false),
      StructField("op1", IntegerType, nullable = false)
    ))
    val dimensions = Seq(Dimension("dim1"))
    val operators = Seq(
      new CountOperator("count1", WhenError.Error),
      new SumOperator("sum1", WhenError.Error, inputField = Some("op1"))
    )
    val cube = new Cube(dimensions, operators)
    val fields = new GenericRowWithSchema(Array("foo", 1, 2), initSchema)

    testOperation(getInput, cube.execute, getOutput, true)

    def getInput: Seq[Seq[(DimensionValues, InputFields)]] = Seq(Seq(
      (DimensionValues(
        Seq(DimensionValue(Dimension("dim1"), "foo", StructField("dim1", StringType, nullable = true))),
        None
      ), InputFields(fields, 1))
    ))

    def getOutput: Seq[Seq[(DimensionValues, MeasuresValues)]] = Seq(
      Seq(
        (DimensionValues(
          Seq(DimensionValue(Dimension("dim1"), "foo", StructField("dim1", StringType, nullable = true))),
          None
        ), MeasuresValues(Map("sum1" -> Some(2d), "count1" -> Some(1L))))
      ))
  }

  test("Cube no associative") {
    val initSchema = StructType(Seq(
      StructField("dim1", StringType, nullable = false),
      StructField("dim2", IntegerType, nullable = false),
      StructField("op1", IntegerType, nullable = false)
    ))
    val dimensions = Seq(Dimension("dim1"))
    val operators = Seq(
      new MedianOperator("avg", WhenError.Error, inputField = Some("op1"))
    )
    val cube = new Cube(dimensions, operators)
    val fields = new GenericRowWithSchema(Array("foo", 1, 2), initSchema)

    testOperation(getInput, cube.execute, getOutput, true)

    def getInput: Seq[Seq[(DimensionValues, InputFields)]] = Seq(Seq(
      (DimensionValues(
        Seq(DimensionValue(Dimension("dim1"), "foo", StructField("dim1", StringType, nullable = true))),
        None
      ), InputFields(fields, 1))
    ))

    def getOutput: Seq[Seq[(DimensionValues, MeasuresValues)]] = Seq(
      Seq(
        (DimensionValues(
          Seq(DimensionValue(Dimension("dim1"), "foo", StructField("dim1", StringType, nullable = true))),
          None
        ), MeasuresValues(Map("avg" -> Some(2d))))
      ))
  }

  test("Cube no associative and associative") {
    val initSchema = StructType(Seq(
      StructField("dim1", StringType, nullable = false),
      StructField("dim2", IntegerType, nullable = false),
      StructField("op1", IntegerType, nullable = false)
    ))
    val dimensions = Seq(Dimension("dim1"))
    val operators = Seq(
      new CountOperator("count1", WhenError.Error),
      new SumOperator("sum1", WhenError.Error, inputField = Some("op1")),
      new MedianOperator("avg", WhenError.Error, inputField = Some("op1"))
    )
    val cube = new Cube(dimensions, operators)
    val fields = new GenericRowWithSchema(Array("foo", 1, 2), initSchema)

    testOperation(getInput, cube.execute, getOutput, true)

    def getInput: Seq[Seq[(DimensionValues, InputFields)]] = Seq(Seq(
      (DimensionValues(
        Seq(DimensionValue(Dimension("dim1"), "foo", StructField("dim1", StringType, nullable = true))),
        None
      ), InputFields(fields, 1))
    ))

    def getOutput: Seq[Seq[(DimensionValues, MeasuresValues)]] = Seq(
      Seq(
        (DimensionValues(
          Seq(DimensionValue(Dimension("dim1"), "foo", StructField("dim1", StringType, nullable = true))),
          None
        ), MeasuresValues(Map("sum1" -> Some(2d), "count1" -> Some(1L), "avg" -> Some(2d))))
      ))
  }

  test("Cube without time") {
    val initSchema = StructType(Seq(
      StructField("dim1", StringType, nullable = false),
      StructField("dim2", IntegerType, nullable = false),
      StructField("op1", IntegerType, nullable = false)
    ))
    val dimensions = Seq(Dimension("dim1"))
    val operators = Seq(
      new CountOperator("count1", WhenError.Error),
      new SumOperator("sum1", WhenError.Error, inputField = Some("op1"))
    )
    val cube = new Cube(dimensions, operators)
    val fields = new GenericRowWithSchema(Array("foo", 1, 2), initSchema)

    testOperation(getInput, cube.execute, getOutput, true)

    def getInput: Seq[Seq[(DimensionValues, InputFields)]] = Seq(Seq(
      (DimensionValues(
        Seq(DimensionValue(Dimension("dim1"), "foo", StructField("dim1", StringType, nullable = true))),
        None
      ), InputFields(fields, 1))
    ))

    def getOutput: Seq[Seq[(DimensionValues, MeasuresValues)]] = Seq(
      Seq(
        (DimensionValues(
          Seq(DimensionValue(Dimension("dim1"), "foo", StructField("dim1", StringType, nullable = true))),
          None
        ), MeasuresValues(Map("sum1" -> Some(2d), "count1" -> Some(1L))))
      ))
  }

  test("Cube with time") {
    val initSchema = StructType(Seq(
      StructField("dim1", StringType, nullable = false),
      StructField("time", LongType, nullable = false),
      StructField("op1", IntegerType, nullable = false)
    ))
    val dimensions = Seq(Dimension("dim1"))
    val operators = Seq(
      new CountOperator("count1", WhenError.Error),
      new SumOperator("sum1", WhenError.Error, inputField = Some("op1"))
    )
    val timeValue = new Date().getTime
    val waterMarkPolicy = WaterMarkPolicy("time", "1m")
    val cube = new Cube(dimensions, operators, WhenError.Error, None, None, Option(waterMarkPolicy))
    val fields = new GenericRowWithSchema(Array("foo", timeValue, 2), initSchema)

    testOperation(getInput, cube.execute, getOutput, true)

    def getInput: Seq[Seq[(DimensionValues, InputFields)]] = Seq(Seq(
      (DimensionValues(
        Seq(DimensionValue(Dimension("dim1"), "foo", StructField("dim1", StringType, nullable = true))),
        Some(WaterMark(timeValue, "time"))
      ), InputFields(fields, 1))
    ))

    def getOutput: Seq[Seq[(DimensionValues, MeasuresValues)]] = Seq(
      Seq(
        (DimensionValues(
          Seq(DimensionValue(Dimension("dim1"), "foo", StructField("dim1", StringType, nullable = true))),
          Some(WaterMark(timeValue, "time"))
        ), MeasuresValues(Map("sum1" -> Some(2d), "count1" -> Some(1L))))
      ))
  }
}
