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

package com.stratio.sparta.plugin.workflow.transformation.casting

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Encoder, Row}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class CastingTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A CastingTransformStepStream" should "casting the input DStream" in {

    val rowSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val schemaCasting = StructType(Seq(StructField("color", StringType), StructField("price", StringType)))
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), rowSchema),
      new GenericRowWithSchema(Array("red", 12.2), rowSchema)
    )
    val dataCasting = Seq(
      new GenericRowWithSchema(Array("blue", "12.1"), schemaCasting),
      new GenericRowWithSchema(Array("red", "12.2"), schemaCasting)
    )
    dataQueue1 += sc.parallelize(data1)
    val stream1 = ssc.queueStream(dataQueue1)
    val inputData = Map("step1" -> stream1)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val fields =
      """[
        |{
        |   "name":"color",
        |   "type":"string"
        |},
        |{
        |   "name":"price",
        |   "type":"string"
        |}]
        | """.stripMargin
    val result = new CastingTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
 Option(ssc),
      sparkSession,
      Map("fields" -> fields.asInstanceOf[JSerializable])
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty())
        streamingRegisters.foreach(row => assert(dataCasting.contains(row)))
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(3000L)
    ssc.stop()

    assert(totalEvents.value === 2)
  }
}