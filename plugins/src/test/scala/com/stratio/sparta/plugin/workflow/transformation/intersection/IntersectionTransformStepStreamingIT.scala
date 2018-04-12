/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.intersection

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputFields, OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class IntersectionTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A IntersectionTransformStepStream" should "intersect DStreams" in {

    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataQueue2 = new mutable.Queue[RDD[Row]]()
    val data1 = Seq(Row.fromSeq(Seq("blue", 12.1)),Row.fromSeq(Seq("red", 12.2)))
    val data2 = Seq(Row.fromSeq(Seq("blue", 12.1)),Row.fromSeq(Seq("red", 12.2)))
    dataQueue1 += sc.parallelize(data1)
    dataQueue2 += sc.parallelize(data2)
    val stream1 = ssc.queueStream(dataQueue1)
    val stream2 = ssc.queueStream(dataQueue2)
    val inputData = Map("step1" -> stream1, "step2" -> stream2)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new IntersectionTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map()
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty())
        streamingRegisters.foreach(row => assert(data1.contains(row)))
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)

  }
}