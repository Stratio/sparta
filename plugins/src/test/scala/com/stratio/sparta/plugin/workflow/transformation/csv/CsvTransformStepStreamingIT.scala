/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.csv

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Encoder, Row}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable
import scala.reflect.ClassTag

@RunWith(classOf[JUnitRunner])
class CsvTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A CSVTransformStepStreamIT" should "transform csv events the input DStream" in {

    val fields =
      """[
        |{
        |   "name":"color",
        |   "type":"string"
        |},
        |{
        |   "name":"price",
        |   "type":"double"
        |}]
        |""".stripMargin
    val inputField = "csv"
    val inputSchema = StructType(Seq(StructField(inputField, StringType)))
    val outputSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataIn = Seq(
      new GenericRowWithSchema(Array("blue,12.1"), inputSchema),
      new GenericRowWithSchema(Array("red,12.2"), inputSchema)
    )
    val dataOut = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), outputSchema),
      new GenericRowWithSchema(Array("red", 12.2), outputSchema)
    )
    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new CsvTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("schema.fields" -> fields.asInstanceOf[JSerializable],
        "inputField" -> inputField,
        "schema.inputMode" -> "FIELDS",
        "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
    ).transformWithDiscards(inputData)._1
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
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
  }

  "A CSVTransformStepStreamIT" should "transform csv discarding the header" in {
    val inputField = "csv"
    val header = "color,price"
    val inputSchema = StructType(Seq(StructField(inputField, StringType)))
    val outputSchema = StructType(Seq(StructField("color", StringType), StructField("price", StringType)))
    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataIn = Seq(
      new GenericRowWithSchema(Array(header), inputSchema),
      new GenericRowWithSchema(Array("blue,12.1"), inputSchema),
      new GenericRowWithSchema(Array("red,12.2"), inputSchema)
    )
    val dataOut = Seq(
      new GenericRowWithSchema(Array("blue", "12.1"), outputSchema),
      new GenericRowWithSchema(Array("red", "12.2"), outputSchema)
    )
    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new CsvTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("schema.header" -> header,
        "headerRemoval" -> true,
        "inputField" -> inputField,
        "schema.inputMode" -> "HEADER",
        "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
    ).transformWithDiscards(inputData)._1
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
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
  }

  "A CSVTransformStepStreamIT" should "discard rows in the input DStream" in {

    val fields =
      """[
        |{
        |   "name":"color",
        |   "type":"string"
        |},
        |{
        |   "name":"price",
        |   "type":"double"
        |}]
        |""".stripMargin
    val inputField = "csv"
    val inputSchema = StructType(Seq(StructField(inputField, StringType)))
    val outputSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataIn = Seq(
      new GenericRowWithSchema(Array("blue,12.1"), inputSchema),
      new GenericRowWithSchema(Array("red,12.2"), inputSchema),
      new GenericRowWithSchema(Array("wrong data"), inputSchema)
    )
    val dataOut = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), outputSchema),
      new GenericRowWithSchema(Array("red", 12.2), outputSchema)
    )
    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new CsvTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("schema.fields" -> fields.asInstanceOf[JSerializable],
        "inputField" -> inputField,
        "schema.inputMode" -> "FIELDS",
        "whenRowError" -> "RowDiscard",
        "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
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