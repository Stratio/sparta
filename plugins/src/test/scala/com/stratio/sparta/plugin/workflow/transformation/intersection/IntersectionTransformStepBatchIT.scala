/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.intersection

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class IntersectionTransformStepBatchIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A IntersectionTransformStepBatch" should "intersect RDD" in {

    val data1 = Seq(Row.fromSeq(Seq("blue", 12.1)), Row.fromSeq(Seq("red", 12.2)))
    val data2 = Seq(Row.fromSeq(Seq("blue", 12.1)), Row.fromSeq(Seq("red", 12.2)))
    val rdd1 = sc.parallelize(data1)
    val rdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> rdd1, "step2" -> rdd2)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val result = new IntersectionTransformStepBatch(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
 Option(ssc),
      sparkSession,
      Map()
    ).transform(inputData)
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(data1.contains(row)))

    assert(streamingEvents === 2)

  }
}