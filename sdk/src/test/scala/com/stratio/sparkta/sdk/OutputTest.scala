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

package com.stratio.sparkta.sdk

import java.io.{Serializable => JSerializable}

import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import com.stratio.sparkta.sdk.test.{DimensionTypeMock, OutputMock}

@RunWith(classOf[JUnitRunner])
class OutputTest extends WordSpec with Matchers {

  trait CommonValues {

    val timeDimension = "minute"
    val tableName = "table"
    val timestamp = 1L
    val defaultDimension = new DimensionTypeMock(Map())
    val dimensionValuesT = DimensionValuesTime("testCube",Seq(DimensionValue(
      Dimension("dim1", "eventKey", "identity", defaultDimension), "value1"),
      DimensionValue(
        Dimension("dim2", "eventKey", "identity", defaultDimension), "value2"),
      DimensionValue(
        Dimension("minute", "eventKey", "identity", defaultDimension), 1L)),
      timestamp, timeDimension)

    val dimensionValuesTFixed = DimensionValuesTime("testCube",Seq(DimensionValue(
      Dimension("dim1", "eventKey", "identity", defaultDimension), "value1"),
      DimensionValue(
        Dimension("minute", "eventKey", "identity", defaultDimension), 1L)),
      timestamp, timeDimension)

    val tableSchema = TableSchema("outputName", "myCube", StructType(Array(
      StructField("dim1", StringType, false),
      StructField("dim2", StringType, false),
      StructField("minute", DateType, false),
      StructField("op1", LongType, true))), "minute")
    val outputName = "outputName"

    val output = new OutputMock(outputName,
      None,
      Map(),
      Some(Map("op1" ->(WriteOp.Set, TypeOp.Long))),
      Some(Seq(tableSchema)))

    val outputOperation = new OutputMock(outputName,
      None,
      Map(),
      Some(Map("op1" ->(WriteOp.Inc, TypeOp.Long))),
      Some(Seq(tableSchema)))

    val outputProps = new OutputMock(outputName,
      None,
      Map(
        "fixedDimensions" -> "dim2",
        "fixedMeasure" -> "op2:1",
        "isAutoCalculateId" -> "true"
      ),
      Some(Map("op1" ->(WriteOp.Set, TypeOp.Long))),
      Some(Seq(tableSchema)))


    val outputVersioned = new OutputMock(outputName,
      Option(1),
      Map(),
      Some(Map("op1" ->(WriteOp.Set, TypeOp.Long))),
      Some(Seq(tableSchema)))

  }

  "Output" should {

    "Name must be " in new CommonValues {
      val expected = outputName
      val result = output.getName
      result should be(expected)
    }

    "Date type must be " in new CommonValues {
      val expected = TypeOp.Timestamp
      val result = output.dateType
      result should be(expected)
    }

    "Fixed dimensions type must be " in new CommonValues {
      val expected = TypeOp.String
      val result = output.fixedDimensionsType
      result should be(expected)
    }

    "Supported write operations must be " in new CommonValues {
      val expected = Seq(WriteOp.Set)
      val result = output.supportedWriteOps
      result should be(expected)
    }

    "without fixed Dimensions must be " in new CommonValues {
      val expected = Array()
      val result = output.fixedDimensions
      result should be(expected)
    }

    "with fixed dimensions must be " in new CommonValues {
      val expected = Array("dim2")
      val result = outputProps.fixedDimensions
      result should be(expected)
    }

    "without fixed measures must be " in new CommonValues {
      val expected = MeasuresValues(Map())
      val result = output.fixedMeasures
      result should be(expected)
    }

    "with fixed measures must be " in new CommonValues {
      val expected = MeasuresValues(Map("op2" -> Some("1")))
      val result = outputProps.fixedMeasures
      result should be(expected)
    }

    "without id autocalculated must be " in new CommonValues {
      val expected = false
      val result = output.isAutoCalculateId
      result should be(expected)
    }

    "with id autocalculated must be " in new CommonValues {
      val expected = true
      val result = outputProps.isAutoCalculateId
      result should be(expected)
    }

    "the correct table schema according to the empty properties must be " in new CommonValues {
      val expected = TableSchema("outputName", "myCube", StructType(Array(
        StructField("dim1", StringType, false),
        StructField("dim2", StringType, false),
        StructField("minute", TimestampType, false),
        StructField("op1", LongType, true))), "minute")
      val result = output.getTableSchemaFixedId(tableSchema)
      result should be(expected)
    }

    "the correct table schema according to the properties must be " in new CommonValues {
      val expected = TableSchema("outputName", "myCube", StructType(Array(
        StructField("id", StringType, false),
        StructField("dim1", StringType, false),
        StructField("dim2", StringType, false),
        StructField("minute", TimestampType, false),
        StructField("op1", LongType, true))), "minute")
      val result = outputProps.getTableSchemaFixedId(tableSchema)
      result should be(expected)
    }

    "the correct table schema according to the properties with fixed dimension must be " in new CommonValues {
      val expected = TableSchema("outputName", "myCube", StructType(Array(
        StructField("id", StringType, false),
        StructField("dim1", StringType, false),
        StructField("dim2", StringType, false),
        StructField("minute", TimestampType, false),
        StructField("op1", LongType, true))), "minute")
      val result = outputProps.getTableSchemaFixedId(tableSchema)
      result should be(expected)
    }

    "the fixed dimensions according to the empty properties must be " in new CommonValues {
      val expected = None
      val result = output.getFixedDimensionsWithTime(dimensionValuesT)
      result should be(expected)
    }

    "the fixed dimensions according to the properties must be " in new CommonValues {
      val expected = Some(Seq(("dim2", "value2")))
      val result = outputProps.getFixedDimensionsWithTime(dimensionValuesT)
      result should be(expected)
    }

    "the filtered dimensions by fixed dimensions according to the empty properties must be " in new CommonValues {
      val expected = dimensionValuesT
      val result = output.filterDimensionValueTimeByFixedDimensionsWithTime(dimensionValuesT)
      result should be(expected)
    }

    "the filtered dimensions by fixed dimensions according to the properties must be " in new CommonValues {
      val expected = dimensionValuesTFixed
      val result = outputProps.filterDimensionValueTimeByFixedDimensionsWithTime(dimensionValuesT)
      result should be(expected)
    }

    "the filtered tableSchema by fixed dimensions according to the empty properties must be " in new CommonValues {
      val expected = Seq(tableSchema)
      val result = output.filterSchemaByFixedAndTimeDimensions(Seq(tableSchema))
      result should be(expected)
    }

    "the filtered tableSchema by fixed dimensions according to the properties must be " in new CommonValues {
      val expected = Seq(tableSchema)
      val result = outputProps.filterSchemaByFixedAndTimeDimensions(Seq(tableSchema))
      result should be(expected)
    }

    "the operations type supported must be " in new CommonValues {
      val expected = true
      val result = outputProps.checkOperationTypes
      result should be(expected)
    }

    "the operations type not supported must be " in new CommonValues {
      val expected = false
      val result = outputOperation.checkOperationTypes
      result should be(expected)
    }

    "the spark date field returned must be " in new CommonValues {
      val expected = Output.defaultDateField("field", false)
      val result = Output.getFieldType(TypeOp.Date, "field", false)
      result should be(expected)
    }

    "the spark timestamp field returned must be " in new CommonValues {
      val expected = Output.defaultTimeStampField("field", false)
      val result = Output.getFieldType(TypeOp.Timestamp, "field", false)
      result should be(expected)
    }

    "the spark string field returned must be " in new CommonValues {
      val expected = Output.defaultStringField("field", false)
      val result = Output.getFieldType(TypeOp.String, "field", false)
      result should be(expected)
    }

    "the spark other field returned must be " in new CommonValues {
      val expected = Output.defaultStringField("field", false)
      val result = Output.getFieldType(TypeOp.ArrayDouble, "field", false)
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
