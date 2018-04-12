/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.cleanNulls

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
class CleanNullsTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A CleanNullsTransformStepBatch" should "filter events that contains nulls in the column values" in {

    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", null), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row]
    )
    val rddInput = sc.parallelize(data1)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new CleanNullsTransformStepBatch(
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

  "A CleanNullsTransformStepBatch" should "no filter events when nulls are not present in values" in {

    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 1.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row]
    )
    val rddInput = sc.parallelize(data1)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new CleanNullsTransformStepBatch(
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

  "A CleanNullsTransformStepBatch" should "filter events that contains nulls in all columns values" in {

    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", null), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array(null, null), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", null), schema).asInstanceOf[Row]
    )
    val rddInput = sc.parallelize(data1)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new CleanNullsTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("cleanMode" -> "all")
    ).transformWithSchema(inputData)._1
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 2)
  }

  "A CleanNullsTransformStepBatch" should "filter events that contains nulls in one column" in {

    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array(null, 1.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array(null, 1.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", null), schema).asInstanceOf[Row]
    )
    val columns ="""[
                |{
                |   "name":"color"
                |}]
                | """.stripMargin
    val rddInput = sc.parallelize(data1)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new CleanNullsTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("cleanMode" -> "any", "columns" -> columns)
    ).transformWithSchema(inputData)._1
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 1)
  }

  "A CleanNullsTransformStepBatch" should "filter events that contains nulls in two columns" in {

    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array(null, 1.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array(null, null), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", null), schema).asInstanceOf[Row]
    )
    val columns ="""[
                   |{
                   |   "name":"color"
                   |},
                   |{
                   |   "name":"price"
                   |}
                   |]
                   | """.stripMargin
    val rddInput = sc.parallelize(data1)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new CleanNullsTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("cleanMode" -> "all", "columns" -> columns)
    ).transformWithSchema(inputData)._1
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 2)
  }
}