/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.dropDuplicates

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DropDuplicatesTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A DropDuplicatesTransformStepBatch" should "don't remove duplicate events from input Batch" in {
    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.3), schema).asInstanceOf[Row]
    )
    val rddInput = sc.parallelize(data1)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new DropDuplicatesTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map()
    ).transformWithSchema(inputData)._1
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 3)
  }

  "A DropDuplicatesTransformStepBatch" should "remove duplicate events from input Batch" in {
    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row]
    )
    val rddInput = sc.parallelize(data1)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new DropDuplicatesTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map()
    ).transformWithSchema(inputData)._1
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 2)
  }

  "A DropDuplicatesTransformStepBatch" should "remove duplicate events from input Batch based on columns" in {
    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.3), schema).asInstanceOf[Row]
    )
    val rddInput = sc.parallelize(data1)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val columns =
      s"""[
         |{
         |   "name":"color"
         |}]
         | """.stripMargin
    val result = new DropDuplicatesTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("columns" -> columns)
    ).transformWithSchema(inputData)._1
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 2)
  }
}