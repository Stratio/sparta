/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cube

import com.stratio.sparta.plugin.workflow.transformation.cube.model.{CubeModel, DimensionModel, OperatorModel}
import com.stratio.sparta.plugin.workflow.transformation.cube.operators.CountOperator
import com.stratio.sparta.plugin.workflow.transformation.cube.sdk._
import com.stratio.sparta.plugin.{TemporalSparkContext, TestReceiver}
import com.stratio.sparta.sdk.workflow.enumerators.{SaveModeEnum, WhenError, WhenFieldError, WhenRowError}
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.DStream
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class CubeTransformStepStreamingIT extends TemporalSparkContext with Matchers {

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
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
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
    val step = new CubeTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
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
    val operatorsExpected = Seq(new CountOperator("count1", WhenRowError.RowError, WhenFieldError.FieldError))
    val cubeExpected = new Cube(dimensionsExpected, operatorsExpected)

    step.cube.dimensions should be(cubeExpected.dimensions)
    step.cube.operators.head.isInstanceOf[CountOperator] should be(true)
    step.cube.waterMarkPolicy should be(cubeExpected.waterMarkPolicy)
  }
}
