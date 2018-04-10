/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.casting

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class CastingTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A CastingTransformStepBatch" should "casting the input RDD" in {

    val rowSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val outputSchema = StructType(Seq(StructField("color", StringType), StructField("price", StringType)))
    val dataIn = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), rowSchema),
      new GenericRowWithSchema(Array("red", 12.2), rowSchema)
    )
    val dataInRow = dataIn.map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("blue", "12.1"), outputSchema),
      new GenericRowWithSchema(Array("red", "12.2"), outputSchema)
    )
    val dataSet = sc.parallelize(dataInRow)
    val inputData = Map("step1" -> dataSet)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val fields =
      """[
        |{
        |   "name":"color",
        |   "type":"string"
        |},
        |{
        |   "name":"price",
        |   "type":"string"
        |}]
        | """.stripMargin
    val transformationsStepManagement = TransformationStepManagement()
    val result = new CastingTransformStepBatch(
      "dummy",
      outputOptions,
      transformationsStepManagement,
      Option(ssc),
      sparkSession,
      Map("fields" -> fields.asInstanceOf[JSerializable])
    ).transformWithSchema(inputData)._1
    val arrayValues = result.ds.collect()

    arrayValues.foreach { row =>
      assert(dataOut.contains(row))
      assert(outputSchema == row.schema)
    }

    assert(arrayValues.length === 2)
  }
}