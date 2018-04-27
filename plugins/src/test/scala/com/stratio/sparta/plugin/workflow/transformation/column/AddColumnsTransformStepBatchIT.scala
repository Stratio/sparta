/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.column

import java.io.{Serializable => JSerializable}

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}

//scalastyle:off
@RunWith(classOf[JUnitRunner])
class AddColumnsTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A DefaultColumnValuesTransformStepBatchIT" should "add new columns with default values" in {

    val fields =
      """[
        |{
        |   "field":"color",
        |   "query":"blue",
        |   "type": "string"
        |},
        |{
        |   "field":"price",
        |   "query":10,
        |   "type": "integer"
        |}]
        | """.stripMargin

    val inputSchema = StructType(Seq(StructField("text", StringType)))
    val outputSchema = StructType(Seq(StructField("text", StringType), StructField("color", StringType,false), StructField("price", IntegerType,false)))
    val dataIn =
      Seq(
        new GenericRowWithSchema(Array("A"), inputSchema),
        new GenericRowWithSchema(Array("B"), inputSchema)
      )
    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    val dataOut = Seq(
      new GenericRowWithSchema(Array("A", "blue", 10), outputSchema),
      new GenericRowWithSchema(Array("B", "blue", 10), outputSchema)
    )
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new AddColumnsTransformStepBatch(
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
    }

    assert(arrayValues.length === 2)
  }
}

//scalastyle:on