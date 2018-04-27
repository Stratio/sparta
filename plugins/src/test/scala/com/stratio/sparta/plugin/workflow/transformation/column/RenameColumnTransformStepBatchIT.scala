/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.column

import java.io.{Serializable => JSerializable}

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}

@RunWith(classOf[JUnitRunner])
class RenameColumnTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A RenameColumnTransformStepStreamingIT" should "rename columns in the schema" in {

    val fields =
      """[
        |{
        |   "name":"text",
        |   "alias":"textnew"
        |}]
        | """.stripMargin

    val inputSchema = StructType(Seq(StructField("text", StringType), StructField("color", StringType)))
    val outputSchema = StructType(Seq(StructField("textnew", StringType), StructField("color", StringType)))
    val dataIn =
      Seq(
        new GenericRowWithSchema(Array("this is text A ", "blue"), inputSchema),
        new GenericRowWithSchema(Array("this is text B ", "red"), inputSchema)
      )
    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    val dataOut = Seq(
      new GenericRowWithSchema(Array("this is text A ", "blue"), outputSchema),
      new GenericRowWithSchema(Array("this is text B ", "red"), outputSchema)
    )
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new RenameColumnTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("columns" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
      assert(row.schema.fieldNames.contains("textnew"))
      assert(!row.schema.fieldNames.contains("text"))
    }

    assert(arrayValues.length === 2)
  }
}