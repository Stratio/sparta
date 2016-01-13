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

package com.stratio.sparkta.driver.test.factory

import java.io.{Serializable => JSerializable}

import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar

import com.stratio.sparkta.aggregator.CubeWithTime
import com.stratio.sparkta.driver.factory.SchemaFactory
import com.stratio.sparkta.sdk._

@RunWith(classOf[JUnitRunner])
class SchemaFactoryTest extends FlatSpec with ShouldMatchers
with MockitoSugar {

  "SchemaFactorySpec" should "return a list of schemas" in new CommonValues {
    val cube = CubeWithTime(cubeName, Seq(dim1, dim2), Seq(op1),
      "minute", checkpointInterval, checkpointGranularity, checkpointAvailable)
    val cubes = Seq(cube)
    val tableSchema = TableSchema("outputName", "cubeTest", StructType(Array(
      StructField("dim1", StringType, false),
      StructField("dim2", StringType, false),
      StructField(checkpointGranularity, DateType, false),
      StructField("op1", LongType, true))), "minute")

    val res = SchemaFactory.cubesOperatorsSchemas(cubes, configOptions)

    res should be(Seq(tableSchema))
  }

  "SchemaFactorySpec" should "return the operator and the type" in new CommonValues {
    val expected = Map("op1" ->(WriteOp.Inc, TypeOp.Long))

    val res = SchemaFactory.operatorsKeyOperation(Seq(op1))

    res should be(expected)
  }

  class OperatorTest(name: String, properties: Map[String, JSerializable]) extends Operator(name, properties) {

    override val defaultTypeOperation = TypeOp.Long

    override val writeOperation = WriteOp.Inc

    override val defaultCastingFilterType = TypeOp.Number

    override def processMap(inputFields: InputFieldsValues): Option[Any] = {
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

    override def precisionValue(keyName: String, value: JSerializable): (Precision, JSerializable) = {
      val precision = DimensionType.getIdentity(getTypeOperation, defaultTypeOperation)
      (precision, TypeOp.transformValueByTypeOp(precision.typeOp, value))
    }

    override def precision(keyName: String): Precision =
      DimensionType.getIdentity(getTypeOperation, defaultTypeOperation)
  }

  trait CommonValues {

    val dim1: Dimension = Dimension("dim1", "field1", "", new DimensionTypeTest)
    val dim2: Dimension = Dimension("dim2", "field2", "", new DimensionTypeTest)

    val op1: Operator = new OperatorTest("op1", Map())

    val configOptions: Seq[(String, Map[String, String])] = Seq(("outputName", Map("" -> "")))
    val checkpointInterval = 10000
    val checkpointAvailable = 60000
    val checkpointGranularity = "minute"
    val cubeName = "cubeTest"
    val timestamp = 1L
    val defaultDimension = new DimensionTypeTest
    val dimensionValuesT = DimensionValuesTime("testCube",Seq(DimensionValue(
      Dimension("dim1", "eventKey", "identity", defaultDimension), "value1"),
      DimensionValue(
        Dimension("dim2", "eventKey", "identity", defaultDimension), "value2"),
      DimensionValue(
        Dimension("minute", "eventKey", "identity", defaultDimension), 1L)),
      timestamp, checkpointGranularity)
    val measures = Map("field" -> Some("value"))
    val fixedDimensionsName = Seq("dim2")
    val fixedDimensions = Some(Seq(("dim3", "value3")))
    val fixedMeasure = Map("agg2" -> Some("2"))
  }

}
