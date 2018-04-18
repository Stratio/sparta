/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.explode

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.sdk.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.{OutputOptions, TransformationStepManagement}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class ExplodeTransformStepStreamingIT extends TemporalSparkContext
  with Matchers with DistributedMonadImplicits {
  val inputField = "explode"
  val explodedField = "explodedFieldName"

  val pera = "pera"
  val manzana = "manzana"
  val fresa = "fresa"
  val fruits = Seq(pera, manzana, fresa)
  val inputSchemaFruits = StructType(Seq(
    StructField("foo", StringType),
    StructField(inputField, ArrayType(StringType))
  ))

  "A ExplodeTransformStepIT" should
    "transform explode events with nested fields from the input DStream" in {
      val red = "red"
      val blue = "blue"
      val redPrice = 19.95d
      val bluePrice = 10d

      val rowSchema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
      val inputSchema = StructType(Seq(
        StructField("foo", StringType),
        StructField(inputField, ArrayType(rowSchema))
      ))
      val dataQueue = new mutable.Queue[RDD[Row]]()
      val redRow = new GenericRowWithSchema(Array(red, redPrice), rowSchema)
      val blueRow = new GenericRowWithSchema(Array(blue, bluePrice), rowSchema)

      val dataIn = Seq(
        new GenericRowWithSchema(Array("var", Array(redRow, blueRow)), inputSchema))

      val dataOut = Seq(redRow, blueRow)
      val outputSchema = StructType(Seq(StructField(explodedField,
        StructType(Seq(StructField("color", StringType), StructField("price", DoubleType))))))

      dataQueue += sc.parallelize(dataIn)

      val stream = ssc.queueStream(dataQueue)
      val inputData = Map("step1" -> stream)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

      val result = new ExplodeTransformStepStreaming(
        "dummy",
        outputOptions,
        TransformationStepManagement(),
        Option(ssc),
        sparkSession,
        Map("schema.fromRow" -> true,
          "inputField" -> inputField,
          "explodedField" -> explodedField,
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
          assert(streamingRegisters.forall(row => dataOut.contains(redRow) || dataOut.contains(blueRow) ))
        }
      })
      ssc.start()
      ssc.awaitTerminationOrTimeout(timeoutStreaming)
      ssc.stop()

      assert(totalEvents.value === 2)
    }

  "A ExplodeTransformStepIT" should "transform explode events from the input Batch and append them to the old data" in {
    val outputSchema = StructType(Seq(
      StructField("foo", StringType),
      StructField(inputField, ArrayType(StringType)),
      StructField(explodedField, StringType)
    ))

    val dataIn = Seq(
      new GenericRowWithSchema(Array("var", fruits), inputSchemaFruits))

    val dataQueue = new mutable.Queue[RDD[Row]]()
    dataQueue += sc.parallelize(dataIn)

    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new ExplodeTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("schema.fromRow" -> true,
        "inputField" -> inputField,
        "explodedField" -> explodedField,
        "fieldsPreservationPolicy" -> "APPEND")
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")


    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty())
        assert(streamingRegisters.head.schema == outputSchema)
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 3)
  }

  "A ExplodeTransformStepIT" should "transform explode events from the input Batch and replace the inputField column" +
    "with the new data" in {
      val outputSchema = StructType(Seq(
        StructField("foo", StringType),
        StructField(explodedField, StringType)
      ))

      val dataIn = Seq(
        new GenericRowWithSchema(Array("var", fruits), inputSchemaFruits))

      val dataQueue = new mutable.Queue[RDD[Row]]()
      dataQueue += sc.parallelize(dataIn)

      val stream = ssc.queueStream(dataQueue)
      val inputData = Map("step1" -> stream)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

      val result = new ExplodeTransformStepStreaming(
        "dummy",
        outputOptions,
        TransformationStepManagement(),
        Option(ssc),
        sparkSession,
        Map("schema.fromRow" -> true,
          "inputField" -> inputField,
          "explodedField" -> explodedField,
          "fieldsPreservationPolicy" -> "REPLACE")
      ).transform(inputData)
      val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")


      result.ds.foreachRDD(rdd => {
        val streamingEvents = rdd.count()
        log.info(s" EVENTS COUNT : \t $streamingEvents")
        totalEvents += streamingEvents
        log.info(s" TOTAL EVENTS : \t $totalEvents")
        val streamingRegisters = rdd.collect()
        if (!rdd.isEmpty())
          assert(streamingRegisters.head.schema == outputSchema)
      })
      ssc.start()
      ssc.awaitTerminationOrTimeout(timeoutStreaming)
      ssc.stop()

      assert(totalEvents.value === 3)

  }
}