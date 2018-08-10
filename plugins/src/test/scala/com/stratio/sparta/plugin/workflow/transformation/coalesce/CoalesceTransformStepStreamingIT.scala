/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.coalesce

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.plugin.TemporalSparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.storage.StorageLevel
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable


@RunWith(classOf[JUnitRunner])
class CoalesceTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {
  "A CoalesceTransformStepBatchIT" should "coalesce RDD" in {
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val inputSchema = StructType(Seq(StructField("color", StringType)))
    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataIn = Seq(
      new GenericRowWithSchema(Array("blue"), inputSchema),
      new GenericRowWithSchema(Array("red"), inputSchema)
    )
    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)

    val result = new CoalesceTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("partitions" -> "4", "shuffle" -> false)
    ).transformWithDiscards(inputData)._1

    result.ds.foreachRDD(rdd =>
      assert(rdd.partitions.length == 4)
    )

    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()
  }
}
