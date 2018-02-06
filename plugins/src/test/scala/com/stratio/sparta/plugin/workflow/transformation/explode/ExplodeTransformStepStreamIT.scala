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

package com.stratio.sparta.plugin.workflow.transformation.explode

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, DoubleType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class ExplodeTransformStepStreamIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A ExplodeTransformStepIT" should "transform explode events from the input DStream" in {
    val inputField = "explode"
    val red = "red"
    val blue = "blue"
    val redPrice = 19.95d
    val bluePrice = 10d
    val explodeFieldMoreFields = Seq(
      Map("color" -> red, "price" -> redPrice),
      Map("color" -> blue, "price" -> bluePrice)
    )
    val inputSchema = StructType(Seq(
      StructField("foo", StringType),
      StructField(inputField, StringType)
    ))
    val outputSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataIn = Seq(
      new GenericRowWithSchema(Array("var", explodeFieldMoreFields), inputSchema)
    )
    val dataOut = Seq(
      new GenericRowWithSchema(Array("red", redPrice), outputSchema),
      new GenericRowWithSchema(Array("blue", bluePrice), outputSchema)
    )
    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)

    val result = new ExplodeTransformStepStream(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
 Option(ssc),
      sparkSession,
      Map("schema.fromRow" -> true,
        "inputField" -> inputField,
        "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        assert(streamingRegisters.head.schema == outputSchema)
        assert(streamingRegisters.forall(row => dataOut.contains(row)))
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(3000L)
    ssc.stop()

    assert(totalEvents.value === 2)
  }
}