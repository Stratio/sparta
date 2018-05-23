/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.orderBy

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class OrderByTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A OrderByTransformStepBatch" should "order fields of events from RDD[Row]" in {

    val schema = StructType(Seq(StructField("color", StringType)))
    val schemaResult = StructType(Seq(StructField("color", StringType)))
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue"), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red"), schema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red"), schema).asInstanceOf[Row]
    )
    val unorderedData = Seq(
      new GenericRowWithSchema(Array("red"), schemaResult).asInstanceOf[Row],
      new GenericRowWithSchema(Array("blue"), schemaResult).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red"), schemaResult).asInstanceOf[Row]

    )

    val inputRDD = sc.parallelize(unorderedData)
    val expectedRDD = data1
    val inputData = Map("step1" -> inputRDD)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new OrderByTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("orderExp" -> "color")
    ).transformWithDiscards(inputData)._1

    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (!result.ds.isEmpty())
      streamingRegisters.shouldEqual(expectedRDD)

    assert(streamingEvents === 3)

  }
}
