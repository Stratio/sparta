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

import com.stratio.sparta.driver.cube.{CubeWriterOptions, CubeWriter, Cube}
import com.stratio.sparta.driver.trigger.Trigger
import com.stratio.sparta.sdk._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CubeWriterTest extends FlatSpec with ShouldMatchers {

  "CubeWriterTest" should "return a row with values and timeDimension" in
    new CommonValues {
      val cube = Cube(cubeName, Seq(dim1, dim2), Seq(op1), initSchema,
        Option(ExpiringDataConfig("minute", checkpointGranularity, 100000)), Seq.empty[Trigger])
      val tableSchema = TableSchema(
        Seq("outputName"),
        "cubeTest",
        StructType(Array(
          StructField("dim1", StringType, false),
          StructField("dim2", StringType, false),
          StructField(checkpointGranularity, TimestampType, false),
          StructField("op1", LongType, true))),
        Option("minute")
      )
      val writerOptions = CubeWriterOptions(Seq("outputName"))
      val output = new OutputMock("outputName",
        None,
        Map(),
        Seq(tableSchema)
      )
      val cubeWriter =
        CubeWriter(cube, tableSchema, writerOptions, Seq(output), Seq.empty[Output], Seq.empty[TableSchema])
      val res = cubeWriter.toRow(dimensionValuesT, measures)

      res should be(Row.fromSeq(Seq("value1", "value2", 1L, "value")))
    }

  "CubeWriterTest" should "return a row with values without timeDimension" in
    new CommonValues {
      val cube = Cube(cubeName, Seq(dim1, dim2), Seq(op1), initSchema,  None, Seq.empty[Trigger])
      val tableSchema = TableSchema(
        Seq("outputName"),
        "cubeTest",
        StructType(Array(
          StructField("dim1", StringType, false),
          StructField("dim2", StringType, false),
          StructField("op1", LongType, true))),
        None
      )
      val writerOptions = CubeWriterOptions(Seq("outputName"))
      val output = new OutputMock("outputName",
        None,
        Map(),
        Seq(tableSchema)
      )
      val cubeWriter =
        CubeWriter(cube, tableSchema, writerOptions, Seq(output), Seq.empty[Output], Seq.empty[TableSchema])
      val res = cubeWriter.toRow(dimensionValuesNoTime, measures)

      res should be(Row.fromSeq(Seq("value1", "value2", "value")))
    }

  "CubeWriterTest" should "return a row with values with noTime and idAutoCalculated" in
    new CommonValues {
      val cube = Cube(cubeName, Seq(dim1, dim2), Seq(op1), initSchema, None, Seq.empty[Trigger])
      val tableSchema = TableSchema(
        Seq("outputName"),
        "cubeTest",
        StructType(Array(
          StructField("dim1", StringType, false),
          StructField("dim2", StringType, false),
          StructField("op1", LongType, true))),
        None
      )
      val writerOptions = CubeWriterOptions(Seq("outputName"), TypeOp.Timestamp, MeasuresValues(Map.empty), true)
      val output = new OutputMock("outputName",
        None,
        Map(),
        Seq(tableSchema)
      )
      val cubeWriter =
        CubeWriter(cube, tableSchema, writerOptions, Seq(output), Seq.empty[Output], Seq.empty[TableSchema])
      val res = cubeWriter.toRow(dimensionValuesNoTime, measures)

      res should be(Row.fromSeq(Seq("value1_value2", "value1", "value2", "value")))
    }

  "CubeWriterTest" should "return a row with values with time and idAutoCalculated" in
    new CommonValues {
      val cube = Cube(cubeName, Seq(dim1, dim2), Seq(op1), initSchema, None, Seq.empty[Trigger])
      val tableSchema = TableSchema(
        Seq("outputName"),
        "cubeTest",
        StructType(Array(
          StructField("dim1", StringType, false),
          StructField("dim2", StringType, false),
          StructField("op1", LongType, true))),
        None
      )
      val writerOptions = CubeWriterOptions(Seq("outputName"), TypeOp.Timestamp, MeasuresValues(Map.empty), true)
      val output = new OutputMock("outputName",
        None,
        Map(),
        Seq(tableSchema)
      )
      val cubeWriter =
        CubeWriter(cube, tableSchema, writerOptions, Seq(output), Seq.empty[Output], Seq.empty[TableSchema])
      val res = cubeWriter.toRow(dimensionValuesT, measures)

      res should be(Row.fromSeq(Seq("value1_value2_1", "value1", "value2", 1L, "value")))
    }

  "CubeWriterTest" should "return a row with values with time, idAutoCalculated and fixedMeasure" in
    new CommonValues {
      val cube = Cube(cubeName, Seq(dim1, dim2), Seq(op1), initSchema, None, Seq.empty[Trigger])
      val tableSchema = TableSchema(
        Seq("outputName"),
        "cubeTest",
        StructType(Array(
          StructField("dim1", StringType, false),
          StructField("dim2", StringType, false),
          StructField("op1", LongType, true))),
        None
      )
      val writerOptions = CubeWriterOptions(Seq("outputName"), TypeOp.Timestamp, fixedMeasure, true)
      val output = new OutputMock("outputName",
        None,
        Map(),
        Seq(tableSchema)
      )
      val cubeWriter =
        CubeWriter(cube, tableSchema, writerOptions, Seq(output), Seq.empty[Output], Seq.empty[TableSchema])
      val res = cubeWriter.toRow(dimensionValuesT, measures)

      res should be(Row.fromSeq(Seq("value1_value2_1", "value1", "value2", 1L, "2", "value")))
    }

  class OperatorTest(name: String, schema: StructType, properties: Map[String, JSerializable])
    extends Operator(name, schema, properties) {

    override val defaultTypeOperation = TypeOp.Long

    override val writeOperation = WriteOp.Inc

    override val defaultCastingFilterType = TypeOp.Number

    override def processMap(inputFields: Row): Option[Any] = {
      None
    }

    override def processReduce(values: Iterable[Option[Any]]): Option[Long] = {
      None
    }
  }

  class OutputMock(keyName: String,
                   version: Option[Int],
                   properties: Map[String, JSerializable],
                   schemas: Seq[TableSchema])
    extends Output(keyName, version, properties, schemas) {

    override def upsert(dataFrame: DataFrame, options: Map[String, String]): Unit = {}
  }

  class DimensionTypeTest extends DimensionType {

    override val operationProps: Map[String, JSerializable] = Map()

    override val properties: Map[String, JSerializable] = Map()

    override val defaultTypeOperation = TypeOp.String

    override def precisionValue(keyName: String, value: Any): (Precision, Any) = {
      val precision = DimensionType.getIdentity(getTypeOperation, defaultTypeOperation)
      (precision, TypeOp.transformValueByTypeOp(precision.typeOp, value))
    }

    override def precision(keyName: String): Precision =
      DimensionType.getIdentity(getTypeOperation, defaultTypeOperation)
  }

  trait CommonValues {

    val dim1: Dimension = Dimension("dim1", "field1", "", new DimensionTypeTest)
    val dim2: Dimension = Dimension("dim2", "field2", "", new DimensionTypeTest)
    val dimId: Dimension = Dimension("id", "field2", "", new DimensionTypeTest)
    val op1: Operator = new OperatorTest("op1", StructType(Seq(StructField("n", LongType, false))), Map())
    val checkpointAvailable = 60000
    val checkpointGranularity = "minute"
    val cubeName = "cubeTest"
    val defaultDimension = new DimensionTypeTest
    val dimensionValuesT = DimensionValuesTime("testCube", Seq(DimensionValue(
      Dimension("dim1", "eventKey", "identity", defaultDimension), "value1"),
      DimensionValue(
        Dimension("dim2", "eventKey", "identity", defaultDimension), "value2"),
      DimensionValue(
        Dimension("minute", "eventKey", "identity", defaultDimension), 1L)))

    val dimensionValuesNoTime = DimensionValuesTime("testCube", Seq(DimensionValue(
      Dimension("dim1", "eventKey", "identity", defaultDimension), "value1"),
      DimensionValue(
        Dimension("dim2", "eventKey", "identity", defaultDimension), "value2")))

    val fixedMeasure = MeasuresValues(Map("agg2" -> Option("2")))
    val measures = MeasuresValues(Map("field" -> Option("value")))
    val initSchema = StructType(Seq(StructField("n", StringType, false)))

  }

}
