/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.test

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.core.enumerators.SaveModeEnum
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestInputStepBatchIT extends TemporalSparkContext with Matchers {

  "TestInputStepBatch" should "generate event specified on the batch" in {
    val totalEvents = sc.accumulator(0L, "Number of events received")
    val eventsPerBatch = 100
    val event = "testEvent"
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val properties = Map(
      "eventType" -> "STRING",
      "event" -> event,
      "numEvents" -> eventsPerBatch.asInstanceOf[java.io.Serializable]
    )
    val testInput = new TestInputStepBatch("test", outputOptions, None, sparkSession, properties)
    val input = testInput.initWithSchema()._1
    val generatedRow = new GenericRowWithSchema(Array(event), testInput.outputSchema)

    input.ds.foreachPartition { partition =>
      totalEvents += partition.size
    }

    assert(totalEvents.value == 100)
    assert(input.ds.first() == generatedRow)
  }

  "TestInputStepBatch" should "generate number records on the batch" in {
    val totalEvents = sc.accumulator(0L, "Number of events received")
    val eventsPerBatch = 100
    val maxNumber = "500"
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val properties = Map(
      "eventType" -> "RANDOM_NUMBER",
      "maxNumber" -> maxNumber,
      "numEvents" -> eventsPerBatch.asInstanceOf[java.io.Serializable]
    )
    val testInput = new TestInputStepBatch("test", outputOptions, None, sparkSession, properties)
    val input = testInput.initWithSchema()._1

    input.ds.foreachPartition { partition =>
      totalEvents += partition.size
    }

    assert(totalEvents.value == 100)

    input.ds.collect() foreach { row =>
      val number = row.get(0).toString.toInt
      assert(number >= 0 && number < 500)
    }
  }
}

