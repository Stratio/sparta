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

package com.stratio.sparta.plugin.workflow.transformation.union

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputFields, OutputOptions}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class UnionTransformStepIT extends TemporalSparkContext with Matchers {

  "A UnionTransformStep" should "unify DStreams" in {

    val inputStep1 = "step1"
    val inputStep2 = "step2"
    val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataQueue2 = new mutable.Queue[RDD[Row]]()
    val data1 = Seq(Row.fromSeq(Seq("blue", 12.1)),Row.fromSeq(Seq("red", 12.2)))
    val data2 = Seq(Row.fromSeq(Seq("blue", 12.1)),Row.fromSeq(Seq("red", 12.2)))
    dataQueue1 += sc.parallelize(data1)
    dataQueue2 += sc.parallelize(data2)
    val stream1 = ssc.queueStream(dataQueue1)
    val stream2 = ssc.queueStream(dataQueue2)
    val inputData = Map("step1" -> stream1, "step2" -> stream2)
    val dataCasting = data1 ++ data2
    val outputsFields = Seq(OutputFields("color", "string"), OutputFields("price", "double"))
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val result = new UnionTransformStep(
      "union",
      Map(inputStep1 -> schema, inputStep2 -> schema),
      outputsFields,
      outputOptions,
      ssc,
      sparkSession,
      Map()
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty())
        streamingRegisters.foreach(row => assert(dataCasting.contains(row)))
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(10000L)

    assert(totalEvents.value === 4)

  }
}