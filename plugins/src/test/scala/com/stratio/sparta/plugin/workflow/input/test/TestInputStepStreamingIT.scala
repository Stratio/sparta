/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.test

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestInputStepStreamingIT extends TemporalSparkContext with Matchers {

  "TestInputStepStream " should "generate event specified on each streaming batch" in {
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val eventsPerBatch = 100
    val maxNumber = "500"
    val event = "testEvent"
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val properties = Map(
      "eventType" -> "STRING",
      "event" -> event,
      "numEvents" -> eventsPerBatch.asInstanceOf[java.io.Serializable]
    )
    val testInput = new TestInputStepStreaming("test", outputOptions, Option(ssc), sparkSession, properties)
    val inputStream = testInput.init
    val generatedRow = new GenericRowWithSchema(Array(event), testInput.outputSchema)

    inputStream.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      assert(streamingEvents === 100)
      assert(rdd.first() === generatedRow)
    })
    ssc.start()
    ssc.stop()
    ssc.awaitTerminationOrTimeout(3000L)
  }

  "TestInputStepStream " should "generate number records on each streaming batch" in {
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val eventsPerBatch = 100
    val maxNumber = "500"
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val properties = Map(
      "eventType" -> "RANDOM_NUMBER",
      "maxNumber" -> maxNumber,
      "numEvents" -> eventsPerBatch.asInstanceOf[java.io.Serializable]
    )
    val testInput = new TestInputStepStreaming("test", outputOptions, Option(ssc), sparkSession, properties)
    val inputStream = testInput.init

    inputStream.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      assert(streamingEvents === 100)
      rdd.collect().foreach { row =>
        val number = row.get(0).toString.toInt
        assert(number >= 0 && number < 500)
      }

    })
    ssc.start()
    ssc.stop()
    ssc.awaitTerminationOrTimeout(3000L)
  }
}

