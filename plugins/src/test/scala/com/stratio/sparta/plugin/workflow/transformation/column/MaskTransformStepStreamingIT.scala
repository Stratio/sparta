/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.column

import java.io.{Serializable => JSerializable}
import java.sql.Date

import com.stratio.sparta.core.DistributedMonad

import scala.collection.mutable
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.workflow.transformation.column.Mask.MaskTransformStepStreaming
import org.apache.spark.streaming.dstream.DStream

@RunWith(classOf[JUnitRunner])
class MaskTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {
//scalastyle:off
  "A InsertLiteralTransformStepStreamingIT" should "Mask multiple columns depending of the type of data" in {

    val fields =
      """[
        |{
        |   "name":"string"
        |},
        |{
        |   "name":"integer"
        |},
        |{
        |   "name":"float"
        |}
        |]
        | """.stripMargin


    val inputSchema = StructType(Seq(StructField("string", StringType, false), StructField("integer", IntegerType, false), StructField("float", FloatType, false)))
    val outputSchema = StructType(Seq(StructField("string", StringType, false), StructField("integer", IntegerType, false), StructField("float", FloatType, false)))
    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataIn =
      Seq(
        new GenericRowWithSchema(Array("barcelona", 6, 20.5F), inputSchema),
        new GenericRowWithSchema(Array("madrid", 13, 12.4F), inputSchema)
      )
    val dataOut = Seq(
      new GenericRowWithSchema(Array("X", 0, 0F), outputSchema),
      new GenericRowWithSchema(Array("X", 0, 0F), outputSchema)
    )

    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new MaskTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("columnsToMask" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val streamingRegisters: scala.collection.mutable.ArrayBuffer[Row] = scala.collection.mutable.ArrayBuffer.empty[Row]
    var actualSchema: Option[StructType] = None

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      streamingRegisters.++=(rdd.collect())
      if (!rdd.isEmpty()) actualSchema = Some(rdd.first().schema)
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
    assert(actualSchema.fold(false)(schema => schema equals outputSchema))
    streamingRegisters.foreach { row =>
      print(row.toSeq)
      assert(dataOut.contains(row))
    }
  }

  "A InsertLiteralTransformStepStreamingIT" should "Mask multiple columns column depending of the type of data (Double and Long)" in {

  val fields =
    """[
      |{
      |   "name":"double"
      |},
      |{
      |   "name":"long"
      |}
      |]
      | """.stripMargin


  val inputSchema = StructType(Seq(StructField("double", DoubleType, false), StructField("long", LongType, false)))
  val outputSchema = StructType(Seq(StructField("double", DoubleType, false), StructField("long", LongType, false)))

    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataIn =
      Seq(
        new GenericRowWithSchema(Array(15150D, 68784L, new Date(0)), inputSchema),
        new GenericRowWithSchema(Array(87845424D, 58454L, new Date(0)), inputSchema)
      )
    val dataOut = Seq(
      new GenericRowWithSchema(Array(0D, 0L), outputSchema),
      new GenericRowWithSchema(Array(0D, 0L), outputSchema)
    )

    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new MaskTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("columnsToMask" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val streamingRegisters: scala.collection.mutable.ArrayBuffer[Row] = scala.collection.mutable.ArrayBuffer.empty[Row]
    var actualSchema: Option[StructType] = None

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      streamingRegisters.++=(rdd.collect())
      if (!rdd.isEmpty()) actualSchema = Some(rdd.first().schema)
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
    assert(actualSchema.fold(false)(schema => schema equals outputSchema))
    streamingRegisters.foreach { row =>
      print(row.toSeq)
      assert(dataOut.contains(row))
    }
  }

  "A InsertLiteralTransformStepStreamingIT" should "Mask a DateType column" in {

    val fields =
      """[
        |{
        |   "name":"date"
        |}
        |]
        | """.stripMargin


    val inputSchema = StructType(Seq(StructField("date", DateType, false)))
    val outputSchema = StructType(Seq(StructField("date", DateType, false)))

    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataIn =
      Seq(
        new GenericRowWithSchema(Array(new Date(5L)), inputSchema)
      )
    val dataOut = Seq(
      new GenericRowWithSchema(Array(new Date(0L)), outputSchema)
    )

    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result: DistributedMonad[DStream] = new MaskTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("columnsToMask" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1

    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val streamingRegisters: scala.collection.mutable.ArrayBuffer[Row] = scala.collection.mutable.ArrayBuffer.empty[Row]
    var actualSchema: Option[StructType] = None

    val expectedDate = dataOut.head.getDate(0)
    var resultHeadOption = None

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      streamingRegisters.++=(rdd.collect())
      if (!rdd.isEmpty()) actualSchema = Some(rdd.first().schema)
      var resultHeadOption = rdd.collect().toSeq.headOption
      assert(resultHeadOption.exists(_.getDate(0).toLocalDate == expectedDate.toLocalDate))

    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 1)

  }
}