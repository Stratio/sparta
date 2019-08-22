/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.casting

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions, TransformationStepManagement}
import com.stratio.sparta.core.enumerators.SaveModeEnum
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
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
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
    ).transformWithDiscards(inputData)._1
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
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
  }

  "A CastingTransformStepStream" should "discard rows in the input DStream" in {

    val inputSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val outputSchema = StructType(Seq(StructField("color", StringType), StructField("price", StringType)))
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataIn = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), inputSchema),
      new GenericRowWithSchema(Array("red", 12.2), inputSchema),
      new GenericRowWithSchema(Array("wrong data"), inputSchema)
    )
    val dataOut = Seq(
      new GenericRowWithSchema(Array("blue", "12.1"), outputSchema),
      new GenericRowWithSchema(Array("red", "12.2"), outputSchema)
    )
    dataQueue1 += sc.parallelize(dataIn)
    val stream1 = ssc.queueStream(dataQueue1)
    val inputData = Map("step1" -> stream1)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
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
      Map(
        "fields" -> fields.asInstanceOf[JSerializable],
        "whenRowError" -> "RowDiscard"
      )
    ).transformWithDiscards(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val totalDiscardsEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result._1.ds.foreachRDD(rdd => {
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

    result._3.get.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      totalDiscardsEvents += streamingEvents
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty())
        streamingRegisters.foreach { row =>
          assert(dataIn.contains(row))
          assert(inputSchema == row.schema)
        }
    })

    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
    assert(totalDiscardsEvents.value === 1)
  }
}