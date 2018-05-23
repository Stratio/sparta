/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.orderBy

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable
import com.stratio.sparta.sdk.DistributedMonad.Implicits._
import com.stratio.sparta.sdk.models.{OutputOptions, TransformationStepManagement}

@RunWith(classOf[JUnitRunner])
class OrderByTransformStepStreamingIT extends TemporalSparkContext with Matchers {

  "A OrderByTransformStepStream" should "order fields of events from input DStream" in {

    val schema = StructType(Seq(StructField("color", StringType)))
    val schemaResult = StructType(Seq(StructField("color", StringType)))
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue"), schema),
      new GenericRowWithSchema(Array("red"), schema),
      new GenericRowWithSchema(Array("red"), schema)
    )
    val unorderedData = Seq(
      new GenericRowWithSchema(Array("red"), schemaResult),
      new GenericRowWithSchema(Array("blue"), schemaResult),
      new GenericRowWithSchema(Array("red"), schemaResult)

    )
    dataQueue1 += sc.parallelize(data1)
    val stream1 = ssc.queueStream(dataQueue1)
    val inputData = Map("step1" -> stream1)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val result = new OrderByTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
 Option(ssc),
      sparkSession,
      Map("orderExp" -> "color")
    ).transformWithDiscards(inputData)._1
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        assert(streamingRegisters.head.getString(0) == "blue")
        streamingRegisters.foreach(row => assert(unorderedData.contains(row)))
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 3)

  }
}