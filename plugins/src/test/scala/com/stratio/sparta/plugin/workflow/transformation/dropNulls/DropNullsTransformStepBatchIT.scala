/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.dropNulls

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DropNullsTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A DropNullsTransformStepBatch" should "filter events that contains nulls in the column values" in {
    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", null), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row]
    )
    val rddInput = sc.parallelize(data1)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new DropNullsTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map()
    ).transformWithDiscards(inputData)._1
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 2)
  }

  "A DropNullsTransformStepBatch" should "no filter events when nulls are not present in values" in {
    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 1.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row]
    )
    val rddInput = sc.parallelize(data1)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new DropNullsTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map()
    ).transformWithDiscards(inputData)._1
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 3)
  }

  "A DropNullsTransformStepBatch" should "filter events that contains nulls in all columns values" in {
    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", null), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array(null, null), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", null), schema).asInstanceOf[Row]
    )
    val rddInput = sc.parallelize(data1)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new DropNullsTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("cleanMode" -> "all")
    ).transformWithDiscards(inputData)._1
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 2)
  }

  "A DropNullsTransformStepBatch" should "filter events that contains nulls in one column" in {
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
    val result = new DropNullsTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("cleanMode" -> "any", "columns" -> columns)
    ).transformWithDiscards(inputData)._1
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 1)
  }

  "A DropNullsTransformStepBatch" should "filter events that contains nulls in two columns" in {
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
    val result = new DropNullsTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("cleanMode" -> "all", "columns" -> columns)
    ).transformWithDiscards(inputData)._1
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 2)
  }

  "A DropNullsTransformStepBatch" should "discard rows" in {
    val inputSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataIn = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", null), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), inputSchema).asInstanceOf[Row]
    )
    val rddInput = sc.parallelize(dataIn)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new DropNullsTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("whenRowError" -> "RowDiscard")
    ).transformWithDiscards(inputData)

    val validData = result._1.ds.collect()
    val discardedData = result._3.get.ds.collect()

    validData.foreach { row =>
      assert(dataIn.contains(row))
      assert(inputSchema == row.schema)
    }

    discardedData.foreach { row =>
      assert(dataIn.contains(row))
      assert(inputSchema == row.schema)
    }

    assert(validData.length === 2)
    assert(discardedData.length === 1)
  }
}