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

package com.stratio.sparta.driver.test.writer

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.driver.step.{Cube, Trigger}
import com.stratio.sparta.driver.writer.{CubeWriter, CubeWriterOptions}
import com.stratio.sparta.sdk.pipeline.aggregation.cube.{Dimension, DimensionType, DimensionValue, DimensionValuesTime, ExpiringData, MeasuresValues, Precision}
import com.stratio.sparta.sdk.pipeline.aggregation.operator.Operator
import com.stratio.sparta.sdk.pipeline.output.{Output, SaveModeEnum}
import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, Row}
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CubeWriterTest extends FlatSpec with ShouldMatchers {

  "CubeWriterTest" should "return a row with values and timeDimension" in
    new CommonValues {
      val schema = StructType(Array(
        StructField("dim1", StringType, false),
        StructField("dim2", StringType, false),
        StructField(checkpointGranularity, TimestampType, false),
        StructField("op1", LongType, true)))
      val cube = Cube(cubeName, Seq(dim1, dim2), Seq(op1), initSchema, schema,
        Option(ExpiringData("minute", checkpointGranularity, "100000ms")), Seq.empty[Trigger], CubeWriterOptions())
     
      val writerOptions = CubeWriterOptions(Seq("outputName"))
      val output = new OutputMock("outputName", Map())
      val cubeWriter = CubeWriter(cube, Seq(output))
      val res = cubeWriter.toRow(dimensionValuesT, measures)

      res should be(Row.fromSeq(Seq("value1", "value2", 1L, "value")))
    }

  "CubeWriterTest" should "return a row with values without timeDimension" in
    new CommonValues {
      val schema =  StructType(Array(
        StructField("dim1", StringType, false),
        StructField("dim2", StringType, false),
        StructField("op1", LongType, true)))
      val cube = Cube(cubeName, Seq(dim1, dim2), Seq(op1), initSchema, schema, None, Seq.empty[Trigger],
        CubeWriterOptions())
      val writerOptions = CubeWriterOptions(Seq("outputName"))
      val output = new OutputMock("outputName", Map())
      val cubeWriter = CubeWriter(cube, Seq(output))
      val res = cubeWriter.toRow(dimensionValuesNoTime, measures)

      res should be(Row.fromSeq(Seq("value1", "value2", "value")))
    }

  "CubeWriterTest" should "return a row with values with noTime" in
    new CommonValues {
      val schema =  StructType(Array(
        StructField("dim1", StringType, false),
        StructField("dim2", StringType, false),
        StructField("op1", LongType, true)))
      val cube = Cube(cubeName, Seq(dim1, dim2), Seq(op1), initSchema, schema, None, Seq.empty[Trigger],
        CubeWriterOptions())
      val writerOptions = CubeWriterOptions(Seq("outputName"), TypeOp.Timestamp)
      val output = new OutputMock("outputName", Map())
      val cubeWriter = CubeWriter(cube, Seq(output))
      val res = cubeWriter.toRow(dimensionValuesNoTime, measures)

      res should be(Row.fromSeq(Seq("value1", "value2", "value")))
    }

  "CubeWriterTest" should "return a row with values with time" in
    new CommonValues {
      val schema = StructType(Array(
        StructField("dim1", StringType, false),
        StructField("dim2", StringType, false),
        StructField("op1", LongType, true)))
      val cube = Cube(cubeName, Seq(dim1, dim2), Seq(op1), initSchema, schema, None, Seq.empty[Trigger],
        CubeWriterOptions())
      val writerOptions = CubeWriterOptions(Seq("outputName"), TypeOp.Timestamp)
      val output = new OutputMock("outputName", Map())
      val cubeWriter = CubeWriter(cube, Seq(output))
      val res = cubeWriter.toRow(dimensionValuesT, measures)

      res should be(Row.fromSeq(Seq("value1", "value2", 1L, "value")))
    }

  class OperatorTest(name: String, val schema: StructType, properties: Map[String, JSerializable])
    extends Operator(name, schema, properties) {

    override val defaultTypeOperation = TypeOp.Long

    override val defaultCastingFilterType = TypeOp.Number

    override def processMap(inputFields: Row): Option[Any] = {
      None
    }

    override def processReduce(values: Iterable[Option[Any]]): Option[Long] = {
      None
    }
  }

  class OutputMock(keyName: String, properties: Map[String, JSerializable])
    extends Output(keyName, properties) {

    override def save(dataFrame: DataFrame, saveMode: SaveModeEnum.Value, options: Map[String, String]): Unit = {}
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

    val measures = MeasuresValues(Map("field" -> Option("value")))
    val initSchema = StructType(Seq(StructField("n", StringType, false)))
  }
}
