/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.csv

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class CsvTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A CSVTransformStepBatchIT" should "transform csv events the input RDD" in {

    val fields =
      """[
        |{
        |   "name":"color",
        |   "type":"string"
        |},
        |{
        |   "name":"price",
        |   "type":"double"
        |}]
        |""".stripMargin
    val inputField = "csv"
    val inputSchema = StructType(Seq(StructField(inputField, StringType)))
    val outputSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataIn = Seq(
      new GenericRowWithSchema(Array("blue,12.1"), inputSchema),
      new GenericRowWithSchema(Array("red,12.2"), inputSchema)
    )
    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    val dataOut = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), outputSchema),
      new GenericRowWithSchema(Array("red", 12.2), outputSchema)
    )
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new CsvTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("schema.fields" -> fields.asInstanceOf[JSerializable],
        "inputField" -> inputField,
        "schema.inputMode" -> "FIELDS",
        "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
    ).transformWithDiscards(inputData)._1
    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)
  }

  "A CSVTransformStepBatchIT" should "transform csv discarding the header" in {
    val inputField = "csv"
    val header = "color,price"
    val inputSchema = StructType(Seq(StructField(inputField, StringType)))
    val outputSchema = StructType(Seq(StructField("color", StringType), StructField("price", StringType)))
    val dataIn = Seq(
      new GenericRowWithSchema(Array(header), inputSchema),
      new GenericRowWithSchema(Array("blue,12.1"), inputSchema),
      new GenericRowWithSchema(Array("red,12.2"), inputSchema)
    )
    val dataOut = Seq(
      new GenericRowWithSchema(Array("blue", "12.1"), outputSchema),
      new GenericRowWithSchema(Array("red", "12.2"), outputSchema)
    )
    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new CsvTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("schema.header" -> header,
        "headerRemoval" -> true,
        "inputField" -> inputField,
        "schema.inputMode" -> "HEADER",
        "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
    ).transformWithDiscards(inputData)._1
    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }
    assert(arrayValues.length === 2)
  }

  "A CSVTransformStepBatchIT" should "discard rows in the input RDD" in {

    val fields =
      """[
        |{
        |   "name":"color",
        |   "type":"string"
        |},
        |{
        |   "name":"price",
        |   "type":"double"
        |}]
        |""".stripMargin
    val inputField = "csv"
    val inputSchema = StructType(Seq(StructField(inputField, StringType)))
    val outputSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataIn = Seq(
      new GenericRowWithSchema(Array("blue,12.1"), inputSchema),
      new GenericRowWithSchema(Array("red,12.2"), inputSchema),
      new GenericRowWithSchema(Array("wrong data"), inputSchema)
    )
    val dataInRow = dataIn.map(_.asInstanceOf[Row])
    val dataOut = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), outputSchema),
      new GenericRowWithSchema(Array("red", 12.2), outputSchema)
    )
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new CsvTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("schema.fields" -> fields.asInstanceOf[JSerializable],
        "inputField" -> inputField,
        "schema.inputMode" -> "FIELDS",
        "whenRowError" -> "RowDiscard",
        "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
    ).transformWithDiscards(inputData)

    val validData = result._1.ds.collect()
    val discardedData = result._3.get.ds.collect()

    validData.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    discardedData.foreach { row =>
      assert(dataIn.contains(row))
      assert(inputSchema == row.schema)
    }

    assert(validData.length === 2)
    assert(discardedData.length === 1)
  }
}