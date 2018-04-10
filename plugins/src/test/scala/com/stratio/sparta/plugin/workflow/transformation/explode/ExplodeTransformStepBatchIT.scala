/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.explode

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, DoubleType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}

@RunWith(classOf[JUnitRunner])
class ExplodeTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A ExplodeTransformStepIT" should "transform explode events from the input Batch" in {
    val inputField = "explode"
    val red = "red"
    val blue = "blue"
    val redPrice = 19.95d
    val bluePrice = 10d
    val explodeFieldMoreFields = Seq(
      Map("color" -> red, "price" -> redPrice),
      Map("color" -> blue, "price" -> bluePrice)
    )
    val inputSchema = StructType(Seq(
      StructField("foo", StringType),
      StructField(inputField, StringType)
    ))
    val outputSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataIn = Seq(
      new GenericRowWithSchema(Array("var", explodeFieldMoreFields), inputSchema).asInstanceOf[Row]
    )
    val dataOut = Seq(
      new GenericRowWithSchema(Array("red", redPrice), outputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("blue", bluePrice), outputSchema).asInstanceOf[Row]
    )
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
        "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
    ).transformWithSchema(inputData)._1

    val totalEvents = result.ds.count()

    assert(totalEvents === 2)
  }
}