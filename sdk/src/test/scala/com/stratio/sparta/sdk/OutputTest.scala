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
package com.stratio.sparta.sdk

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.test.{DimensionTypeMock, OutputMock}
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class OutputTest extends WordSpec with Matchers {

  trait CommonValues {

    val timeDimension = "minute"
    val tableName = "table"
    val timestamp = 1L
    val defaultDimension = new DimensionTypeMock(Map())
    val dimensionValuesT = DimensionValuesTime("testCube", Seq(DimensionValue(
      Dimension("dim1", "eventKey", "identity", defaultDimension), "value1"),
      DimensionValue(
        Dimension("dim2", "eventKey", "identity", defaultDimension), "value2"),
      DimensionValue(
        Dimension("minute", "eventKey", "identity", defaultDimension), 1L)))

    val dimensionValuesTFixed = DimensionValuesTime("testCube", Seq(DimensionValue(
      Dimension("dim1", "eventKey", "identity", defaultDimension), "value1"),
      DimensionValue(
        Dimension("minute", "eventKey", "identity", defaultDimension), 1L)))

    val tableSchema = TableSchema(Seq("outputName"), "myCube", StructType(Array(
      StructField("dim1", StringType, false),
      StructField("dim2", StringType, false),
      StructField("minute", DateType, false),
      StructField("op1", LongType, true))), Option("minute"))
    val outputName = "outputName"

    val output = new OutputMock(outputName,
      None,
      Map(),
      Seq(tableSchema))

    val outputOperation = new OutputMock(outputName,
      None,
      Map(),
      Seq(tableSchema))

    val outputProps = new OutputMock(outputName,
      None,
      Map(),
      Seq(tableSchema))

    val outputVersioned = new OutputMock(outputName,
      Option(1),
      Map(),
      Seq(tableSchema))
  }

  "Output" should {

    "Name must be " in new CommonValues {
      val expected = outputName
      val result = output.name
      result should be(expected)
    }

    "the spark date field returned must be " in new CommonValues {
      val expected = Output.defaultDateField("field", false)
      val result = Output.getTimeFieldType(TypeOp.Date, "field", false)
      result should be(expected)
    }

    "the spark timestamp field returned must be " in new CommonValues {
      val expected = Output.defaultTimeStampField("field", false)
      val result = Output.getTimeFieldType(TypeOp.Timestamp, "field", false)
      result should be(expected)
    }

    "the spark string field returned must be " in new CommonValues {
      val expected = Output.defaultStringField("field", false)
      val result = Output.getTimeFieldType(TypeOp.String, "field", false)
      result should be(expected)
    }

    "the spark other field returned must be " in new CommonValues {
      val expected = Output.defaultStringField("field", false)
      val result = Output.getTimeFieldType(TypeOp.ArrayDouble, "field", false)
      result should be(expected)
    }

    "the spark geo field returned must be " in new CommonValues {
      val expected = StructField("field", ArrayType(DoubleType), false)
      val result = Output.defaultGeoField("field", false)
      result should be(expected)
    }

    "classSuffix must be " in {
      val expected = "Output"
      val result = Output.ClassSuffix
      result should be(expected)
    }

    "the table name versioned must be " in new CommonValues {
      val expected = "table_v1"
      val result = outputVersioned.versionedTableName(tableName)
      result should be(expected)
    }
  }
}
