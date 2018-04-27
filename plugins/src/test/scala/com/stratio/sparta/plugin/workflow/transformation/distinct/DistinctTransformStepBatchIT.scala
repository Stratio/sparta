/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.distinct

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DistinctTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A DistinctTransformStepBatch" should "distinct events from input RDD" in {

    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row]
    )
    val dataDistinct = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema).asInstanceOf[Row]
    )
    val inputRDD = sc.parallelize(data1)
    val inputData = Map("step1" -> inputRDD)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new DistinctTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map()
    ).transformWithDiscards(inputData)._1
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (!result.ds.isEmpty())
      streamingRegisters.foreach(row => assert(dataDistinct.contains(row)))

    assert(streamingEvents === 2)

  }
}