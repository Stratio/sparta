/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
class TestInputStepIT extends TemporalSparkContext with Matchers {

  "TestInputStep " should "generate event specified on each streaming batch" in {
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val eventsPerBatch = 100
    val event = "testEvent"
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val properties = Map(
      "eventType" -> "STRING",
      "event" -> event,
      "numEvents" -> eventsPerBatch.asInstanceOf[java.io.Serializable]
    )
    val testInput = new TestInputStep("test", outputOptions, ssc, sparkSession, properties)
    val inputStream = testInput.initStream
    val generatedRow = new GenericRowWithSchema(Array(event), testInput.outputSchema)

    inputStream.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      assert(streamingEvents === 100)
      assert(rdd.first() === generatedRow)
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(4000L)
  }

  "TestInputStep " should "generate number records on each streaming batch" in {
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val eventsPerBatch = 100
    val maxNumber = "500"
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val properties = Map(
      "eventType" -> "RANDOM_NUMBER",
      "maxNumber" -> maxNumber,
      "numEvents" -> eventsPerBatch.asInstanceOf[java.io.Serializable]
    )
    val testInput = new TestInputStep("test", outputOptions, ssc, sparkSession, properties)
    val inputStream = testInput.initStream

    inputStream.foreachRDD(rdd => {
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
    ssc.awaitTerminationOrTimeout(4000L)
  }
}

