/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.hash

import scala.collection.mutable
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.workflow.transformation.column.Hash.HashTransformStepStreaming
import org.apache.spark.streaming.dstream.InputDStream

@RunWith(classOf[JUnitRunner])
class HashTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {


  "A HashTransformStepStreamingIT" should "create an MD5 hash for the selected column" in {

    val fields =
      """[
        |{
        |   "columnToHash":"id",
        |   "hashType":"MD5",
        |   "hashlength":""
        |}]
        | """.stripMargin

    val inputSchema = StructType(Seq(StructField("id", StringType), StructField("name", StringType)))
    val outputSchema = StructType(Seq(StructField("id", StringType), StructField("name", StringType)))

    val dataQueue = new mutable.Queue[RDD[Row]]()

    val dataIn =
      Seq(
        new GenericRowWithSchema(Array("555", "Pepe"), inputSchema),
        new GenericRowWithSchema(Array("333", "Pablo"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("15de21c670ae7c3f6f3f1f37029303c9", "Pepe"), outputSchema),
      new GenericRowWithSchema(Array("310dcbbf4cce62f762a2aaa148d556bd", "Pablo"), outputSchema)
    )

    dataQueue += sc.parallelize(dataIn)
    val stream: InputDStream[Row] = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val streamingRegisters: scala.collection.mutable.ArrayBuffer[Row] = scala.collection.mutable.ArrayBuffer.empty[Row]
    var actualSchema: Option[StructType] = None

    val result = new HashTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("columnsToHash" -> fields)
    ).transformWithDiscards(inputData)._1

    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
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
      assert(dataOut.contains(row))
    }
  }


  "A HashTransformStepStreamingIT" should "create an SHA2 hash with a length of 0 for the selected column" in {

    val fields =
      """[
        |{
        |   "columnToHash":"id",
        |   "hashType":"SHA2",
        |   "hashlength":0
        |}]
        | """.stripMargin

    val inputSchema = StructType(Seq(StructField("id", StringType), StructField("name", StringType)))
    val outputSchema = StructType(Seq(StructField("id", StringType), StructField("name", StringType)))

    val dataQueue = new mutable.Queue[RDD[Row]]()

    val dataIn =
      Seq(
        new GenericRowWithSchema(Array("555", "Pepe"), inputSchema),
        new GenericRowWithSchema(Array("333", "Pablo"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("91a73fd806ab2c005c13b4dc19130a884e909dea3f72d46e30266fe1a1f588d8", "Pepe"), outputSchema),
      new GenericRowWithSchema(Array("556d7dc3a115356350f1f9910b1af1ab0e312d4b3e4fc788d2da63668f36d017", "Pablo"), outputSchema)
    )

    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val streamingRegisters: scala.collection.mutable.ArrayBuffer[Row] = scala.collection.mutable.ArrayBuffer.empty[Row]
    var actualSchema: Option[StructType] = None

    val result = new HashTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("columnsToHash" -> fields)
    ).transformWithDiscards(inputData)._1

    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
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
      assert(dataOut.contains(row))
    }
  }


  "A HashTransformStepStreamingIT" should "create an SHA2 hash with a length of 256 for the selected column" in {

    val fields =
      """[
        |{
        |   "columnToHash":"id",
        |   "hashType":"SHA2",
        |   "hashlength":256
        |}]
        | """.stripMargin

    val inputSchema = StructType(Seq(StructField("id", StringType), StructField("name", StringType)))
    val outputSchema = StructType(Seq(StructField("id", StringType), StructField("name", StringType)))

    val dataQueue = new mutable.Queue[RDD[Row]]()

    val dataIn =
      Seq(
        new GenericRowWithSchema(Array("555", "Pepe"), inputSchema),
        new GenericRowWithSchema(Array("333", "Pablo"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("91a73fd806ab2c005c13b4dc19130a884e909dea3f72d46e30266fe1a1f588d8", "Pepe"), outputSchema),
      new GenericRowWithSchema(Array("556d7dc3a115356350f1f9910b1af1ab0e312d4b3e4fc788d2da63668f36d017", "Pablo"), outputSchema)
    )

    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val streamingRegisters: scala.collection.mutable.ArrayBuffer[Row] = scala.collection.mutable.ArrayBuffer.empty[Row]
    var actualSchema: Option[StructType] = None

    val result = new HashTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("columnsToHash" -> fields)
    ).transformWithDiscards(inputData)._1

    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
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
      assert(dataOut.contains(row))
    }
  }

  "A HashTransformStepStreamingIT" should "create an SHA2 hash with a length of 384 for the selected column" in {

    val fields =
      """[
        |{
        |   "columnToHash":"id",
        |   "hashType":"SHA2",
        |   "hashlength":384
        |}]
        | """.stripMargin

    val inputSchema = StructType(Seq(StructField("id", StringType), StructField("name", StringType)))
    val outputSchema = StructType(Seq(StructField("id", StringType), StructField("name", StringType)))

    val dataQueue = new mutable.Queue[RDD[Row]]()

    val dataIn =
      Seq(
        new GenericRowWithSchema(Array("555", "Pepe"), inputSchema),
        new GenericRowWithSchema(Array("333", "Pablo"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("544c0e92feb84985dc6827d0c2d8ccdeadcc067556eeec1912bf48924b54d4d3dfee3c3827b118f42c488af3e019978d", "Pepe"), outputSchema),
      new GenericRowWithSchema(Array("cae9394ccbec70c18cb15e800104d401ae067da602ee51eb4bca25bff025a7f1b1051209e61b8af43effc605a025fed2", "Pablo"), outputSchema)
    )

    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val streamingRegisters: scala.collection.mutable.ArrayBuffer[Row] = scala.collection.mutable.ArrayBuffer.empty[Row]
    var actualSchema: Option[StructType] = None

    val result = new HashTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("columnsToHash" -> fields)
    ).transformWithDiscards(inputData)._1

    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
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
      assert(dataOut.contains(row))
    }
  }

  "A HashTransformStepStreamingIT" should "create an SHA2 hash with a length of 512 for the selected column" in {

    val fields =
      """[
        |{
        |   "columnToHash":"id",
        |   "hashType":"SHA2",
        |   "hashlength":512
        |}]
        | """.stripMargin

    val inputSchema = StructType(Seq(StructField("id", StringType), StructField("name", StringType)))
    val outputSchema = StructType(Seq(StructField("id", StringType), StructField("name", StringType)))

    val dataQueue = new mutable.Queue[RDD[Row]]()

    val dataIn =
      Seq(
        new GenericRowWithSchema(Array("555", "Pepe"), inputSchema),
        new GenericRowWithSchema(Array("333", "Pablo"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("4e2589ee5a155a86ac912a5d34755f0e3a7d1f595914373da638c20fecd7256ea1647069a2bb48ac421111a875d7f4294c7236292590302497f84f19e7227d80", "Pepe"), outputSchema),
      new GenericRowWithSchema(Array("5e3155774d39d97c5f9e17c108c2b3e0485a43ae34ebd196f61a6f8bf732ef71a49e5710594cfc7391db114edf99f5da3ed96ef1d6ca5e598e85f91bd41e7eeb", "Pablo"), outputSchema)
    )

    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val streamingRegisters: scala.collection.mutable.ArrayBuffer[Row] = scala.collection.mutable.ArrayBuffer.empty[Row]
    var actualSchema: Option[StructType] = None

    val result = new HashTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("columnsToHash" -> fields)
    ).transformWithDiscards(inputData)._1

    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
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
      assert(dataOut.contains(row))
    }
  }

}

