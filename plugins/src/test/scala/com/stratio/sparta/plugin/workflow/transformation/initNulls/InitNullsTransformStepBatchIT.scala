/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.initNulls

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class InitNullsTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A InitNullsTransformStepBatch" should "not initialize nulls values" in {
    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 1.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row]
    )
    val rddInput = sc.parallelize(data1)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new InitNullsTransformStepBatch(
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

  "A InitNullsTransformStepBatch" should "initialize nulls values" in {
    val columnsValues ="""[
                   |{
                   |   "columnName":"color",
                   |   "value":"sparta"
                   |}]
                   | """.stripMargin
    val columnsTypes ="""[
                         |{
                         |   "type":"double",
                         |   "value": "1.1"
                         |}]
                         | """.stripMargin
    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", null), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array(null, null), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", null), schema).asInstanceOf[Row]
    )
    val dataOutput = Seq(
      new GenericRowWithSchema(Array("blue", 1.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("sparta", 1.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 1.1), schema).asInstanceOf[Row]
    )
    val rddInput = sc.parallelize(data1)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new InitNullsTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "defaultValueToColumn" -> columnsValues,
        "defaultValueToType" -> columnsTypes
      )
    ).transformWithDiscards(inputData)._1
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(dataOutput.contains(row)))

    assert(streamingEvents === 3)
  }

  "A InitNullsTransformStepBatch" should "discard rows" in {
    val inputSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataIn = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 1.1), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("wrong data"), inputSchema)
    )
    val rddInput = sc.parallelize(dataIn)
    val inputData = Map("step1" -> rddInput)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new InitNullsTransformStepBatch(
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

    assert(validData.length === 3)
    assert(discardedData.length === 1)
  }
}