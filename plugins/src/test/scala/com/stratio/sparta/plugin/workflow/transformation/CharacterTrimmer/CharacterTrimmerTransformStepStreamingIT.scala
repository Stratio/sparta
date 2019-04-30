/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.CharacterTrimmer

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.workflow.transformation.column.CharacterTrimmer.CharacterTrimmerTransformStepStreaming
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.streaming.dstream.InputDStream
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

//scalastyle:off
@RunWith(classOf[JUnitRunner])
class CharacterTrimmerTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits{


  "A CharacterTrimmerTransformStepBatchIT" should "Remove selected characters into the left" in {

    val fields =
      """[{"name": "Column1",
        |"characterToTrim": "b",
        |"trimType": "TRIM_LEFT"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType)))

    val dataQueue = new mutable.Queue[RDD[Row]]()

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("baae"), inputSchema),
        new GenericRowWithSchema(Array("booe"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("aae"), outputSchema),
      new GenericRowWithSchema(Array("ooe"), outputSchema)
    )

    dataQueue += sc.parallelize(dataIn)
    val stream: InputDStream[Row] = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new CharacterTrimmerTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToTrim" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1


    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        streamingRegisters.foreach { row =>
          assert(dataOut.contains(row))
          assert(outputSchema == row.schema)
        }
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
  }


  "A CharacterTrimmerTransformStepBatchIT" should "Remove selected characters into the right" in {

    val fields =
      """[{"name": "Column1",
        |"characterToTrim": "e",
        |"trimType": "TRIM_RIGHT"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType)))

    val dataQueue = new mutable.Queue[RDD[Row]]()

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("baae"), inputSchema),
        new GenericRowWithSchema(Array("booe"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("baa"), outputSchema),
      new GenericRowWithSchema(Array("boo"), outputSchema)
    )

    dataQueue += sc.parallelize(dataIn)
    val stream: InputDStream[Row] = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new CharacterTrimmerTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToTrim" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1


    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        streamingRegisters.foreach { row =>
          assert(dataOut.contains(row))
          assert(outputSchema == row.schema)
        }
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
  }

  "A CharacterTrimmerTransformStepBatchIT" should "Remove selected characters into the left and right" in {

    val fields =
      """[{"name": "Column1",
        |"characterToTrim": "b",
        |"trimType": "TRIM_BOTH"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType)))

    val dataQueue = new mutable.Queue[RDD[Row]]()

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("baab"), inputSchema),
        new GenericRowWithSchema(Array("boob"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("aa"), outputSchema),
      new GenericRowWithSchema(Array("oo"), outputSchema)
    )

    dataQueue += sc.parallelize(dataIn)
    val stream: InputDStream[Row] = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new CharacterTrimmerTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToTrim" -> fields.asInstanceOf[JSerializable])
    ).transformWithDiscards(inputData)._1


    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        streamingRegisters.foreach { row =>
          assert(dataOut.contains(row))
          assert(outputSchema == row.schema)
        }
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
  }
}
