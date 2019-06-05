/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.column

import java.io.{Serializable => JSerializable}
import java.sql.Date

import com.stratio.sparta.core.DistributedMonad
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StructField, _}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.workflow.transformation.column.Mask.MaskTransformStepBatch
import org.apache.spark.rdd.RDD


@RunWith(classOf[JUnitRunner])
class MaskTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {
  //scalastyle:off
  "A InsertLiteralTransformStepBatchIT" should "Mask multiple columns depending of the type of data" in {

    val fields =
      """[
        |{
        |   "name":"string"
        |},
        |{
        |   "name":"integer"
        |},
        |{
        |   "name":"float"
        |}
        |]
        | """.stripMargin

    val inputSchema = StructType(Seq(StructField("string", StringType, false), StructField("integer", IntegerType, false), StructField("float", FloatType, false)))
    val outputSchema = StructType(Seq(StructField("string", StringType, false), StructField("integer", IntegerType, false),StructField("float", FloatType, false)))
    val dataIn =
      Seq(
        new GenericRowWithSchema(Array("barcelona", 6, 20.5F), inputSchema),
        new GenericRowWithSchema(Array("madrid", 13, 12.4F), inputSchema)
      )
    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    val dataOut = Seq(
      new GenericRowWithSchema(Array("X", 0, 0F), outputSchema),
      new GenericRowWithSchema(Array("X", 0, 0F), outputSchema)
    )
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new MaskTransformStepBatch(
      "columnsToMask",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToMask" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)
  }


  "A InsertLiteralTransformStepBatchIT" should "Mask multiple columns column depending of the type of data (Double and Long)" in {

    val fields =
      """[
        |{
        |   "name":"double"
        |},
        |{
        |   "name":"long"
        |}
        |]
        | """.stripMargin

    val inputSchema = StructType(Seq(StructField("double", DoubleType, false), StructField("long", LongType, false)))
    val outputSchema = StructType(Seq(StructField("double", DoubleType, false), StructField("long", LongType, false)))
    val dataIn =
      Seq(
        new GenericRowWithSchema(Array(15150D, 68784L, new Date(0)), inputSchema),
        new GenericRowWithSchema(Array(87845424D, 58454L, new Date(0)), inputSchema)
      )
    // new java.sql.Timestamp(4)
    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    val dataOut = Seq(
      new GenericRowWithSchema(Array(0D, 0L), outputSchema),
      new GenericRowWithSchema(Array(0D, 0L), outputSchema)
    )
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new MaskTransformStepBatch(
      "columnsToMask",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToMask" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1

    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)
  }


  "A InsertLiteralTransformStepBatchIT" should "Mask a DateType column" in {

    val fields =
      """[
        |{
        |   "name":"date"
        |}
        |]
        | """.stripMargin

    val inputSchema = StructType(Seq(StructField("date", DateType, false)))
    val outputSchema = StructType(Seq(StructField("date", DateType, false)))
    val dataIn =
      Seq(
        new GenericRowWithSchema(Array(new Date(5L)), inputSchema)
      )

    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    val dataOut = Seq(
      new GenericRowWithSchema(Array(new Date(0L)), outputSchema)
    )
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result: DistributedMonad[RDD] = new MaskTransformStepBatch(
      "columnsToMask",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToMask" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1

    val expectedDate = dataOut.head.getDate(0)
    val resultHeadOption= result.ds.collect().toSeq.headOption
    assert(resultHeadOption.exists(_.getDate(0).toLocalDate == expectedDate.toLocalDate))

  }
}
