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

import com.stratio.sparta.plugin.workflow.transformation.cube.model.{CubeModel, DimensionModel, OperatorModel}
import com.stratio.sparta.plugin.workflow.transformation.cube.operators.CountOperator
import com.stratio.sparta.plugin.workflow.transformation.cube.sdk._
import com.stratio.sparta.plugin.{TemporalSparkContext, TestReceiver}
import com.stratio.sparta.sdk.workflow.enumerators.{SaveModeEnum, WhenError}
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class CubeTransformStepIT extends TemporalSparkContext with Matchers {

  "Cube" should "create stream without time" in {

    val initSchema = StructType(Seq(
      StructField("dim1", StringType, nullable = false),
      StructField("dim2", IntegerType, nullable = false),
      StructField("op1", IntegerType, nullable = false)
    ))
    val fields = new GenericRowWithSchema(Array("foo", 1, 2), initSchema)
    val initDStream = ssc.receiverStream(new TestReceiver(fields, 2, StorageLevel.MEMORY_ONLY))
      .asInstanceOf[DStream[Row]]
    val inputData = Map("step1" -> initDStream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val dimensionsProp =
      s"""[
         | {"name":"dim1"}
         |]""".stripMargin
    val operatorsProp =
      s"""[
         | {
         | "name":"count1",
         | "classType":"Count"
         | }
         |]""".stripMargin
    val step = new CubeTransformStep(
      "dummy",
      outputOptions,
      Option(ssc),
      sparkSession,
      Map("dimensions" -> dimensionsProp, "operators" -> operatorsProp)
    )

    val dimensionModelExpected = Seq(DimensionModel("dim1"))
    val operatorsModelExpected = Seq(OperatorModel("count1", "Count"))
    val cubeModelExpected = CubeModel(dimensionModelExpected, operatorsModelExpected)

    step.cubeModel should be(cubeModelExpected)

    step.timeDataType should be(TimestampType)

    step.timeoutKey should be(None)

    step.partitions should be(None)

    step.waterMarkField should be(None)

    step.availability should be(None)

    val dimensionsExpected = Seq(Dimension("dim1"))
    val operatorsExpected = Seq(new CountOperator("count1", WhenError.Error))
    val cubeExpected = new Cube(dimensionsExpected, operatorsExpected)

    step.cube.dimensions should be(cubeExpected.dimensions)
    step.cube.operators.head.isInstanceOf[CountOperator] should be(true)
    step.cube.waterMarkPolicy should be(cubeExpected.waterMarkPolicy)
  }
}
