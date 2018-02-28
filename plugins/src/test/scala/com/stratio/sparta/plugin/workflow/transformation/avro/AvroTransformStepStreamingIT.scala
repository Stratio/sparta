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

package com.stratio.sparta.plugin.workflow.transformation.avro

import java.nio.charset.StandardCharsets

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import com.twitter.bijection.avro.GenericAvroCodecs
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericRecord}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class AvroTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A AvroTransformStepStreamIT" should "transform csv events the input DStream" in {

    val record = s"""{"type":"record","name":"myrecord","fields":[
      | { "name":"color", "type":["string","null"] },
      | { "name":"price", "type":["double","null"] }
      | ]}""".stripMargin
    val parser = new Schema.Parser()
    val schema = parser.parse(record)
    val recordInjection = GenericAvroCodecs.toBinary[GenericRecord](schema)
    val avroRecordBlue = new GenericData.Record(schema)
    avroRecordBlue.put("color", "blue")
    avroRecordBlue.put("price", 12.1)
    val bytesBlue = recordInjection.apply(avroRecordBlue)
    val avroRecordRed = new GenericData.Record(schema)
    avroRecordRed.put("color", "red")
    avroRecordRed.put("price", 12.2)
    val bytesRed = recordInjection.apply(avroRecordRed)
    val inputField = "avro"
    val inputSchema = StructType(Seq(StructField(inputField, StringType)))
    val outputSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataIn = Seq(
      new GenericRowWithSchema(Array(new String(bytesBlue, StandardCharsets.UTF_8)), inputSchema),
      new GenericRowWithSchema(Array(new String(bytesRed, StandardCharsets.UTF_8)), inputSchema)
    )
    val dataOut = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), outputSchema),
      new GenericRowWithSchema(Array("red", 12.2), outputSchema)
    )
    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
    val transformationsStepManagement = TransformationStepManagement()
    val result = new AvroTransformStepStreaming(
      "dummy",
      outputOptions,
      transformationsStepManagement,
      Some(ssc),
      sparkSession,
      Map(
        "inputField" -> inputField,
        "schema.provided" -> record,
        "fieldsPreservationPolicy" -> "JUST_EXTRACTED"
      )
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty())
        streamingRegisters.foreach { row =>
          assert(dataOut.contains(row))
          assert(outputSchema == row.schema)
        }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(3000L)
    ssc.stop()

    assert(totalEvents.value === 2)
  }
}