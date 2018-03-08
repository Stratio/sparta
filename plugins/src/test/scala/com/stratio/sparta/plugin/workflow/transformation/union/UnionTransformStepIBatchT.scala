/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.union

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UnionTransformStepIBatchT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A UnionTransformStepBatch" should "unify RDDs" in {
    val data1 = Seq(Row.fromSeq(Seq("blue", 12.1)), Row.fromSeq(Seq("red", 12.2)))
    val data2 = Seq(Row.fromSeq(Seq("blue", 12.1)), Row.fromSeq(Seq("red", 12.2)))
    val inputRdd1 = sc.parallelize(data1)
    val inputRdd2 = sc.parallelize(data2)
    val inputData = Map("step1" -> inputRdd1, "step2" -> inputRdd2)
    val dataCasting = data1 ++ data2
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val result = new UnionTransformStepBatch(
      "union",
      outputOptions,
      TransformationStepManagement(),
 Option(ssc),
      sparkSession,
      Map()
    ).transform(inputData)
    val streamingEvents = result.ds.count()
    val streamingRegisters = result.ds.collect()

    if (streamingRegisters.nonEmpty)
      streamingRegisters.foreach(row => assert(dataCasting.contains(row)))

    assert(streamingEvents === 4)

  }
}