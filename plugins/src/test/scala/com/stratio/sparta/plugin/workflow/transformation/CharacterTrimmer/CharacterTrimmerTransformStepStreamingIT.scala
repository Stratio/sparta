/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.CharacterTrimmer

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions, TransformationStepManagement}
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
        |},
        |{"name": "Column2",
        |"characterToTrim": " ",
        |"trimType": "TRIM_LEFT"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType),StructField("Column2", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))

    val dataQueue = new mutable.Queue[RDD[Row]]()

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("baae", "  whitespace "), inputSchema),
        new GenericRowWithSchema(Array("booe", " whitespace"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("aae", " whitespace "), outputSchema),
      new GenericRowWithSchema(Array("ooe", "whitespace"), outputSchema)
    )

    dataQueue += sc.parallelize(dataIn)
    val stream: InputDStream[Row] = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new CharacterTrimmerTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToTrim" -> fields.asInstanceOf[JSerializable])
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
    streamingRegisters.foreach{ row =>
      print(row.toSeq)
      assert(dataOut.contains(row))
    }
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
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new CharacterTrimmerTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToTrim" -> fields.asInstanceOf[JSerializable])
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
    streamingRegisters.foreach{ row =>
      print(row.toSeq)
      assert(dataOut.contains(row))
    }
  }

  "A CharacterTrimmerTransformStepBatchIT" should "Remove selected characters into the left and right including whitespaces" in {

    val fields =
      """[{"name": "Column1",
        |"characterToTrim": "b",
        |"trimType": "TRIM_BOTH"
        |},
        |{"name": "Column2",
        |"characterToTrim": " ",
        |"trimType": "TRIM_BOTH"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))
    val outputSchema = StructType(Seq(StructField("Column1", StringType), StructField("Column2", StringType)))

    val dataQueue = new mutable.Queue[RDD[Row]]()

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("baab", " whitespace  "), inputSchema),
        new GenericRowWithSchema(Array("boob", "   whitespace "), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("aa", "whitespace "), outputSchema),
      new GenericRowWithSchema(Array("oo", "  whitespace"), outputSchema)
    )

    dataQueue += sc.parallelize(dataIn)
    val stream: InputDStream[Row] = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new CharacterTrimmerTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      None,
      sparkSession,
      Map("columnsToTrim" -> fields.asInstanceOf[JSerializable])
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
    streamingRegisters.foreach{ row =>
      print(row.toSeq)
      assert(dataOut.contains(row))
    }
  }
}
