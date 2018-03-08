/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube

import java.util.Date

import com.stratio.sparta.plugin.workflow.transformation.cube.operators._
import com.stratio.sparta.plugin.workflow.transformation.cube.sdk._
import com.stratio.sparta.sdk.workflow.enumerators.{WhenError, WhenFieldError, WhenRowError}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.apache.spark.streaming.TestSuiteBase
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class CubeTransformStepStreamingTest extends TestSuiteBase with Matchers {


  test("Cube associative") {
    val initSchema = StructType(Seq(
      StructField("dim1", StringType, nullable = false),
      StructField("dim2", IntegerType, nullable = false),
      StructField("op1", IntegerType, nullable = false)
    ))
    val dimensions = Seq(Dimension("dim1"))
    val operators = Seq(
      new CountOperator("count1", WhenRowError.RowError, WhenFieldError.FieldError),
      new SumOperator("sum1", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("op1"))
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
      new MedianOperator("avg", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("op1"))
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
      new CountOperator("count1", WhenRowError.RowError, WhenFieldError.FieldError),
      new SumOperator("sum1", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("op1")),
      new MedianOperator("avg", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("op1"))
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
      new CountOperator("count1", WhenRowError.RowError, WhenFieldError.FieldError),
      new SumOperator("sum1", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("op1"))
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
      new CountOperator("count1", WhenRowError.RowError, WhenFieldError.FieldError),
      new SumOperator("sum1", WhenRowError.RowError, WhenFieldError.FieldError, inputField = Some("op1"))
    )
    val timeValue = new Date().getTime
    val waterMarkPolicy = WaterMarkPolicy("time", "1m")
    val cube = new Cube(dimensions, operators, WhenRowError.RowError, WhenFieldError.FieldError, None, None, Option(waterMarkPolicy))
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
