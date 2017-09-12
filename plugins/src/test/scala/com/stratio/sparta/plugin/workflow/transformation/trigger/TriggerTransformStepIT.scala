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

package com.stratio.sparta.plugin.workflow.transformation.trigger

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputFields, OutputOptions}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class TriggerTransformStepIT extends TemporalSparkContext with Matchers {

  "A TriggerTransformStep" should "make trigger over one DStream" in {

    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1),
      new GenericRowWithSchema(Array("red", 12.2), schema1)
    )
    dataQueue1 += sc.parallelize(data1)
    val stream1 = ssc.queueStream(dataQueue1)
    val inputData = Map("step1" -> stream1)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val query = s"SELECT * FROM step1 ORDER BY step1.color"
    val result = new TriggerTransformStep(
      "dummy",
      outputOptions,
      ssc,
      sparkSession,
      Map("sql" -> query)
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        streamingRegisters.foreach(row => assert(data1.contains(row)))
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(10000L)

    assert(totalEvents.value === 2)

  }

  "A TriggerTransformStep" should "make trigger over two DStreams" in {

    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val schema2 = StructType(Seq(StructField("color", StringType),
      StructField("company", StringType), StructField("name", StringType)))
    val schemaResult = StructType(Seq(StructField("color", StringType),
      StructField("company", StringType), StructField("name", StringType), StructField("price", DoubleType)))
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataQueue2 = new mutable.Queue[RDD[Row]]()
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1),
      new GenericRowWithSchema(Array("red", 12.2), schema1)
    )
    val data2 = Seq(
      new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee"), schema2),
      new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee"), schema2)
    )
    dataQueue1 += sc.parallelize(data1)
    dataQueue2 += sc.parallelize(data2)
    val stream1 = ssc.queueStream(dataQueue1)
    val stream2 = ssc.queueStream(dataQueue2)
    val inputData = Map("step1" -> stream1, "step2" -> stream2)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val query = s"SELECT step1.color, step2.company, step2.name, step1.price " +
      s"FROM step2 JOIN step1 ON step2.color = step1.color ORDER BY step1.color"
    val result = new TriggerTransformStep(
      "dummy",
      outputOptions,
      ssc,
      sparkSession,
      Map("sql" -> query)
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        val queryData = Seq(
          new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee", 12.1), schemaResult),
          new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee", 12.2), schemaResult))
        streamingRegisters.foreach(row => assert(queryData.contains(row)))
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(10000L)

    assert(totalEvents.value === 2)

  }
}