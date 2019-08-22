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
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions, TransformationStepManagement}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.workflow.transformation.column.InsertLiteral.InsertLiteralTransformStepBatch

@RunWith(classOf[JUnitRunner])
class InsertLiteralTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A InsertLiteralTransformStepBatchIT" should "Insert a string literal into a column at the beginning or the end of the string" in {

    val fields =
      """[
        |{
        |   "name":"id",
        |   "value":"id:",
        |   "offset": 0,
        |   "offsetFrom": "INSERT_LEFT"
        |},
        |{
        |   "name":"region",
        |   "value":"-MD",
        |   "offset": 0,
        |   "offsetFrom": "INSERT_RIGHT"
        |}
        |]
        | """.stripMargin

    val inputSchema = StructType(Seq(StructField("id", StringType), StructField("region", StringType)))
    val outputSchema = StructType(Seq(StructField("id", StringType), StructField("region", StringType)))
    val dataIn =
      Seq(
        new GenericRowWithSchema(Array("0125671", "madrid"), inputSchema),
        new GenericRowWithSchema(Array("0124841", "ciudad real"), inputSchema)
      )
    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    val dataOut = Seq(
      new GenericRowWithSchema(Array("id:0125671", "madrid-MD"), outputSchema),
      new GenericRowWithSchema(Array("id:0124841", "ciudad real-MD"), outputSchema)
    )
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new InsertLiteralTransformStepBatch(
      "columnsToInsertLiteral",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToInsertLiteral" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)
  }

  "A InsertLiteralTransformStepBatchIT" should "Insert a string literal into a column at the middle of the string" in {

    val fields =
      """[
        |{
        |   "name":"id",
        |   "value":"-A-",
        |   "offset": 2,
        |   "offsetFrom": "INSERT_LEFT"
        |},
        |{
        |   "name":"region",
        |   "value":" ",
        |   "offset": 4,
        |   "offsetFrom": "INSERT_RIGHT"
        |}
        |]
        | """.stripMargin

    val inputSchema = StructType(Seq(StructField("id", StringType), StructField("region", StringType)))
    val outputSchema = StructType(Seq(StructField("id", StringType), StructField("region", StringType)))
    val dataIn =
      Seq(
        new GenericRowWithSchema(Array("0125671", "ciudadreal"), inputSchema),
        new GenericRowWithSchema(Array("0124841", "ciudadreal"), inputSchema)
      )
    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    val dataOut = Seq(
      new GenericRowWithSchema(Array("01-A-25671", "ciudad real"), outputSchema),
      new GenericRowWithSchema(Array("01-A-24841", "ciudad real"), outputSchema)
    )
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new InsertLiteralTransformStepBatch(
      "columnsToInsertLiteral",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToInsertLiteral" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)
  }

}