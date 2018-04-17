/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.select

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
class SelectTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A SelectTransformStepBatch" should "select fields of events from input RDD" in {
    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val schemaResult = StructType(Seq(StructField("color", StringType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row]
    )
    val dataDistinct = Seq(
      new GenericRowWithSchema(Array("blue"), schemaResult),
      new GenericRowWithSchema(Array("red"), schemaResult),
      new GenericRowWithSchema(Array("red"), schemaResult)
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputData = Map("step1" -> inputRdd1)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new SelectTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("selectExp" -> "color")
    ).transformWithSchema(inputData)._1
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    if (batchRegisters.nonEmpty)
      batchRegisters.foreach(row => assert(dataDistinct.contains(row)))

    assert(batchEvents === 3)
  }

  "A SelectTransformStepBatch" should "select fields from columns" in {
    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val schemaResult = StructType(Seq(StructField("color", StringType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row]
    )
    val dataSelect = Seq(
      new GenericRowWithSchema(Array(12.1), schemaResult),
      new GenericRowWithSchema(Array(12.2), schemaResult),
      new GenericRowWithSchema(Array(12.2), schemaResult)
    )
    val inputRdd1 = sc.parallelize(data1)
    val inputData = Map("step1" -> inputRdd1)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val columns =
      s"""[
         |{
         |   "name":"price"
         |}]
         | """.stripMargin
    val result = new SelectTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "columns" -> columns,
        "selectType" -> "COLUMNS"
      )
    ).transformWithSchema(inputData)._1
    val batchEvents = result.ds.count()
    val batchRegisters = result.ds.collect()

    if (batchRegisters.nonEmpty)
      batchRegisters.foreach(row => assert(dataSelect.contains(row)))

    assert(batchEvents === 3)
  }
}