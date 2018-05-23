/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.explode

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ExplodeTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  val inputField = "explode"
  val explodedField = "explodedFieldName"

  "A ExplodeTransformStepIT" should "transform explode events from the input Batch and return" +
    "only the extracted data" in {
    val red = "red"
    val blue = "blue"
    val redPrice = 19.95d
    val bluePrice = 10d

    val rowSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val inputSchema = StructType(Seq(
      StructField("foo", StringType),
      StructField(inputField, ArrayType(rowSchema))
    ))
    val redRow = new GenericRowWithSchema(Array(red, redPrice), rowSchema)
    val blueRow = new GenericRowWithSchema(Array(blue, bluePrice), rowSchema)

    val dataIn = Seq(
      new GenericRowWithSchema(Array("var", Array(redRow, blueRow)), inputSchema).asInstanceOf[Row])

    val dataQueue = sc.parallelize(dataIn)

    val inputData = Map("step1" -> dataQueue)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new ExplodeTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("schema.fromRow" -> true,
        "inputField" -> inputField,
        "explodedField" -> explodedField,
        "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
    ).transformWithDiscards(inputData)._1

    val totalEvents = result.ds.count()
    assert(totalEvents === 2)
  }

  val pera = "pera"
  val manzana = "manzana"
  val fresa = "fresa"
  val fruits = Seq(pera, manzana, fresa)
  val inputSchemaFruits = StructType(Seq(
    StructField("foo", StringType),
    StructField(inputField, ArrayType(StringType))
  ))

  "A ExplodeTransformStepIT" should "transform explode events from the input Batch and append them to the old data" in {

    val outputSchema = StructType(Seq(
      StructField("foo", StringType),
      StructField(inputField, ArrayType(StringType)),
      StructField(explodedField, StringType)
    ))

    val dataIn = Seq(
      new GenericRowWithSchema(Array("var", fruits), inputSchemaFruits).asInstanceOf[Row])

    val dataQueue = sc.parallelize(dataIn)

    val inputData = Map("step1" -> dataQueue)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new ExplodeTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("schema.fromRow" -> true,
        "inputField" -> inputField,
        "explodedField" -> explodedField,
        "fieldsPreservationPolicy" -> "APPEND")
    ).transformWithDiscards(inputData)._1

    val registers = result.ds.collect()
    if (registers.nonEmpty) {
      assert(registers.head.schema == outputSchema)
    }

    val totalEvents = result.ds.count()
    assert(totalEvents === 3)
  }

  "A ExplodeTransformStepIT" should "transform explode events from the input Batch and replace the inputField column" +
    "with the new data" in {

    val outputSchema = StructType(Seq(
      StructField("foo", StringType),
      StructField(explodedField, StringType)
    ))

    val dataIn = Seq(
      new GenericRowWithSchema(Array("var", fruits), inputSchemaFruits).asInstanceOf[Row])

    val dataQueue = sc.parallelize(dataIn)

    val inputData = Map("step1" -> dataQueue)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new ExplodeTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("schema.fromRow" -> true,
        "inputField" -> inputField,
        "explodedField" -> explodedField,
        "fieldsPreservationPolicy" -> "REPLACE")
    ).transformWithDiscards(inputData)._1

    val registers = result.ds.collect()
    if (registers.nonEmpty) {
      assert(registers.head.schema == outputSchema)
    }

    val totalEvents = result.ds.count()
    assert(totalEvents === 3)
  }
}