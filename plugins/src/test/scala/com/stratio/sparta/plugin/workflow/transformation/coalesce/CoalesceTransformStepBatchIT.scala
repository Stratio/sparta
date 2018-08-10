/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.coalesce

import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.plugin.TemporalSparkContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class CoalesceTransformStepBatchIT  extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A CoalesceTransformStepBatchIT" should "coalesce RDD" in {
    val schemaResult = StructType(Seq(StructField("color", StringType)))
    val unorderedData = Seq(
      new GenericRowWithSchema(Array("red"), schemaResult).asInstanceOf[Row],
      new GenericRowWithSchema(Array("blue"), schemaResult).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red"), schemaResult).asInstanceOf[Row]
    )
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val inputRDD = sc.parallelize(unorderedData)
    val inputData = Map("step1" -> inputRDD)

    val result = new CoalesceTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("partitions" -> "4", "shuffle" -> false)
    ).transformWithDiscards(inputData)._1

    assert(result.ds.partitions.length == 4)
  }
}
