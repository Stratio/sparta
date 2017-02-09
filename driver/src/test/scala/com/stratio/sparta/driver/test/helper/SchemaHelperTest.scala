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
package com.stratio.sparta.driver.test.helper

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.driver.cube.Cube
import com.stratio.sparta.driver.trigger.Trigger
import com.stratio.sparta.driver.helper.SchemaHelper
import com.stratio.sparta.sdk._
import com.stratio.sparta.sdk.pipeline.aggregation.cube.{Dimension, DimensionType, ExpiringData, Precision}
import com.stratio.sparta.sdk.pipeline.aggregation.operator.Operator
import com.stratio.sparta.sdk.pipeline.autoCalculations.AutoCalculatedField
import com.stratio.sparta.sdk.pipeline.input.Input
import com.stratio.sparta.sdk.pipeline.schema.{SpartaSchema, TypeOp}
import com.stratio.sparta.sdk.properties.JsoneyString
import com.stratio.sparta.serving.core.models._
import com.stratio.sparta.serving.core.models.policy.{CheckpointModel, OutputFieldsModel, PolicyElementModel, TransformationsModel}
import com.stratio.sparta.serving.core.models.policy.cube.{CubeModel, DimensionModel, OperatorModel}
import com.stratio.sparta.serving.core.models.policy.writer.WriterModel
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

@RunWith(classOf[JUnitRunner])
class SchemaHelperTest extends FlatSpec with ShouldMatchers
  with MockitoSugar {

  trait CommonValues {

    val initSchema = StructType(Array(
      StructField("field1", LongType, false),
      StructField("field2", IntegerType, false),
      StructField("field3", StringType, false),
      StructField("field4", StringType, false)))

    val dim1: Dimension = Dimension("dim1", "field1", "", new DimensionTypeTest)
    val dim2: Dimension = Dimension("dim2", "field2", "", new DimensionTypeTest)
    val dimensionTime: Dimension = Dimension("minute", "field3", "minute", new TimeDimensionTypeTest)
    val dimId: Dimension = Dimension("id", "field2", "", new DimensionTypeTest)
    val op1: Operator = new OperatorTest("op1", initSchema, Map())
    val dimension1Model = DimensionModel(
      "dim1", "field1", DimensionType.IdentityName, DimensionType.DefaultDimensionClass, configuration = Some(Map())
    )
    val dimension2Model =
      DimensionModel("dim2", "field2", DimensionType.IdentityName, DimensionType.DefaultDimensionClass)
    val dimensionTimeModel =
      DimensionModel("minute", "field3", DimensionType.TimestampName, DimensionType.TimestampName, Option("10m"))
    val dimensionId = DimensionModel("id", "field2", DimensionType.IdentityName, DimensionType.DefaultDimensionClass)
    val operator1Model = OperatorModel("Count", "op1", Map())
    val output1Model = PolicyElementModel("outputName", "MongoDb", Map())
    val checkpointModel = CheckpointModel("minute", checkpointGranularity, None, 10000)
    val noCheckpointModel = CheckpointModel("none", checkpointGranularity, None, 10000)
    val writerModelId = WriterModel(Seq("outputName"), None, Seq())
    val writerModelTimeDate = WriterModel(Seq("outputName"), Option("date"), Seq())
    val checkpointAvailable = 60000
    val checkpointGranularity = "minute"
    val cubeName = "cubeTest"

    val outputFieldModel1 = OutputFieldsModel("field1", Some("long"))
    val outputFieldModel2 = OutputFieldsModel("field2", Some("int"))
    val outputFieldModel3 = OutputFieldsModel("field3", Some("fake"))
    val outputFieldModel4 = OutputFieldsModel("field4", Some("string"))
    val transformationModel1 =
      TransformationsModel("Parser", 0, Some(Input.RawDataKey), Seq(outputFieldModel1, outputFieldModel2))

    val transformationModel2 = TransformationsModel("Parser", 1, Some("field1"), Seq(outputFieldModel3,
      outputFieldModel4))
    val writerModel = WriterModel(Seq("outputName"))
  }

  "SchemaHelperTest" should "return a list of schemas" in new CommonValues {
    val cube = Cube(cubeName, Seq(dim1, dim2, dimensionTime), Seq(op1), initSchema,
      Option(ExpiringData("minute", checkpointGranularity, "100000ms")), Seq.empty[Trigger])

    val cubeModel =
      CubeModel(cubeName, Seq(dimension1Model, dimension2Model, dimensionTimeModel), Seq(operator1Model), writerModel)
    val cubes = Seq(cube)
    val cubesModel = Seq(cubeModel)
    val tableSchema = SpartaSchema(
      Seq("outputName"),
      "cubeTest",
      StructType(Array(
        StructField("dim1", StringType, false, SchemaHelper.PkMetadata),
        StructField("dim2", StringType, false, SchemaHelper.PkMetadata),
        StructField(checkpointGranularity, TimestampType, false, SchemaHelper.PkTimeMetadata),
        StructField("op1", LongType, false, SchemaHelper.MeasureMetadata))),
      Option("minute"),
      TypeOp.Timestamp,
      Seq.empty[AutoCalculatedField]
    )

    val res = SchemaHelper.getSchemasFromCubes(cubes, cubesModel)

    res should be(Seq(tableSchema))
  }

  it should "return a list of schemas without time" in new CommonValues {
    val cube = Cube(cubeName, Seq(dim1, dim2), Seq(op1), initSchema, None, Seq.empty[Trigger])
    val cubeModel = CubeModel(cubeName, Seq(dimension1Model, dimension2Model), Seq(operator1Model), writerModel)
    val cubes = Seq(cube)
    val cubesModel = Seq(cubeModel)
    val tableSchema = SpartaSchema(
      Seq("outputName"),
      "cubeTest",
      StructType(Array(
        StructField("dim1", StringType, false, SchemaHelper.PkMetadata),
        StructField("dim2", StringType, false, SchemaHelper.PkMetadata),
        StructField("op1", LongType, false, SchemaHelper.MeasureMetadata))),
      None,
      TypeOp.Timestamp,
      Seq.empty[AutoCalculatedField]
    )

    val res = SchemaHelper.getSchemasFromCubes(cubes, cubesModel)

    res should be(Seq(tableSchema))
  }

  it should "return a list of schemas with field id but not in writer" in new CommonValues {
    val cube = Cube(cubeName, Seq(dim1, dimId), Seq(op1), initSchema, None, Seq.empty[Trigger])
    val cubeModel =
      CubeModel(cubeName, Seq(dimension1Model, dimension2Model), Seq(operator1Model), writerModel)
    val cubes = Seq(cube)
    val cubesModel = Seq(cubeModel)
    val tableSchema = SpartaSchema(
      Seq("outputName"),
      "cubeTest",
      StructType(Array(
        StructField("dim1", StringType, false, SchemaHelper.PkMetadata),
        StructField("id", StringType, false, SchemaHelper.PkMetadata),
        StructField("op1", LongType, false, SchemaHelper.MeasureMetadata))),
      None,
      TypeOp.Timestamp,
      Seq.empty[AutoCalculatedField]
    )

    val res = SchemaHelper.getSchemasFromCubes(cubes, cubesModel)

    res should be(Seq(tableSchema))
  }

  it should "return a list of schemas with timeDimension with DateFormat" in
    new CommonValues {
      val cube = Cube(cubeName, Seq(dim1, dim2, dimensionTime), Seq(op1), initSchema,
        Option(ExpiringData("minute", checkpointGranularity, "100000ms")), Seq.empty[Trigger])
      val cubeModel = CubeModel(
        cubeName, Seq(dimension1Model, dimension2Model, dimensionTimeModel), Seq(operator1Model), writerModelTimeDate
      )
      val cubes = Seq(cube)
      val cubesModel = Seq(cubeModel)
      val tableSchema = SpartaSchema(
        Seq("outputName"),
        "cubeTest",
        StructType(Array(
          StructField("dim1", StringType, false, SchemaHelper.PkMetadata),
          StructField("dim2", StringType, false, SchemaHelper.PkMetadata),
          StructField(checkpointGranularity, DateType, false, SchemaHelper.PkTimeMetadata),
          StructField("op1", LongType, false, SchemaHelper.MeasureMetadata))),
        Option("minute"),
        TypeOp.Date,
        Seq.empty[AutoCalculatedField]
      )

      val res = SchemaHelper.getSchemasFromCubes(cubes, cubesModel)

      res should be(Seq(tableSchema))
    }

  it should "return a map with the name of the transformation and the schema" in
    new CommonValues {
      val transformationsModel = Seq(transformationModel1, transformationModel2)

      val res = SchemaHelper.getSchemasFromParsers(transformationsModel, Map())

      val expected = Map(
        "0" -> StructType(Seq(StructField("field1", LongType), StructField("field2", IntegerType))),
        "1" -> StructType(Seq(StructField("field1", LongType), StructField("field2", IntegerType),
          StructField("field3", StringType), StructField("field4", StringType)))
      )

      res should be(expected)
    }

  it should "return a map with the name of the transformation and the schema with the raw" in
    new CommonValues {
      val transformationsModel = Seq(transformationModel1, transformationModel2)

      val res = SchemaHelper.getSchemasFromParsers(transformationsModel, Input.InitSchema)

      val expected = Map(
        Input.RawDataKey -> StructType(Seq(StructField(Input.RawDataKey, StringType))),
        "0" -> StructType(Seq(StructField(Input.RawDataKey, StringType),
          StructField("field1", LongType),
          StructField("field2", IntegerType))
        ),
        "1" -> StructType(Seq(StructField(Input.RawDataKey, StringType),
          StructField("field1", LongType), StructField("field2", IntegerType),
          StructField("field3", StringType), StructField("field4", StringType)))
      )

      res should be(expected)
    }

  it should "return a schema without the raw" in
    new CommonValues {

      val transformationNoRaw1 =
        TransformationsModel("Parser", 0, Some(Input.RawDataKey), Seq(outputFieldModel1, outputFieldModel2),
          Map("removeInputField" -> JsoneyString.apply("true")))
      val transformationNoRaw2 = TransformationsModel("Parser", 1, Some("field1"), Seq(outputFieldModel3,
        outputFieldModel4),Map("removeInputField" -> JsoneyString.apply("true")))

      val transformationsModel = Seq(transformationNoRaw1, transformationNoRaw2)

      val schemaWithoutRaw = SchemaHelper.getSchemasFromParsers(transformationsModel, Input.InitSchema)

      val expected = Map(
        Input.RawDataKey -> StructType(Seq(StructField(Input.RawDataKey, StringType))),
        "0" -> StructType(Seq(StructField("field1", LongType), StructField("field2", IntegerType))),
        "1" -> StructType(Seq(StructField("field2", IntegerType),StructField("field3", StringType),
          StructField("field4", StringType)))
        )


      schemaWithoutRaw should be(expected)
    }

  class OperatorTest(name: String,
                     val schema: StructType,
                     properties: Map[String, JSerializable]) extends Operator(name, schema, properties) {

    override val defaultTypeOperation = TypeOp.Long

    override val defaultCastingFilterType = TypeOp.Number

    override def processMap(inputFields: Row): Option[Any] = {
      None
    }

    override def processReduce(values: Iterable[Option[Any]]): Option[Long] = {
      None
    }
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

  class TimeDimensionTypeTest extends DimensionType {

    override val operationProps: Map[String, JSerializable] = Map()

    override val properties: Map[String, JSerializable] = Map()

    override val defaultTypeOperation = TypeOp.Timestamp

    override def precisionValue(keyName: String, value: Any): (Precision, Any) = {
      val precision = DimensionType.getIdentity(getTypeOperation, defaultTypeOperation)
      (precision, TypeOp.transformValueByTypeOp(precision.typeOp, value))
    }

    override def precision(keyName: String): Precision =
      DimensionType.getIdentity(getTypeOperation, defaultTypeOperation)
  }

}