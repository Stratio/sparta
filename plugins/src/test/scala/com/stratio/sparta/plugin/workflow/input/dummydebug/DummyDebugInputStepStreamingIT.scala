/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.dummydebug

import java.io.File
import java.net.URL

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.models.OutputWriterOptions
import com.stratio.sparta.core.workflow.step.DebugOptions
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.junit.JUnitRunner

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class DummyDebugInputStepStreamingIT extends TemporalSparkContext with TimeLimitedTests with Matchers
  with DummyDebugTestUtils {

  val SparkTimeOut = 1000L

  "A DummyDebugInputStepStreaming" should "read and correctly parse an uploaded CSV file" in {
    val resourcePath: URL = getClass().getResource("/test.csv")
    val properties = Map("path" -> s"file://${resourcePath.getFile}", "dummyInputSource" -> "FILE",
      "debugOptions" -> TestJsonUtil.toJson(DebugOptions(Some(s"file://${resourcePath.getFile}"), None, None)))
    val debugInput = new DummyDebugInputStepStreaming("testDebug",
      outputOptions, Option(ssc), sparkSession, properties)
    val outputSchema = StructType(Seq(StructField("name", StringType)))
    val dataOut = Seq(new GenericRowWithSchema(Array("name"), outputSchema),
      new GenericRowWithSchema(Array("sparta"), outputSchema))
    val distributedStream = debugInput.init()
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val eventsRegistered = new scala.collection.mutable.ListBuffer[Row]()
    log.debug("Evaluate the distributedStream")

    distributedStream.ds.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        val count = rdd.count()
        eventsRegistered ++= rdd.collect()
        log.debug(s"EVENTS COUNT : $count")
        totalEvents.add(count)
      } else log.debug("RDD is empty")
      log.debug(s"TOTAL EVENTS : $totalEvents")
    })

    ssc.start() // Start the computation

    log.debug("Started Streaming")

    ssc.awaitTerminationOrTimeout(SparkTimeOut)

    log.debug("Finished Streaming")

    totalEvents.value should ===(2L)
    eventsRegistered should be(dataOut)

  }

  "A DummyDebugInputStepStreaming" should "read and correctly parse an uploaded Avro file" in {
    val resourcePath: URL = getClass().getResource("/test.avro")
    val properties = Map("path" -> s"file://${resourcePath.getFile}", "dummyInputSource" -> "FILE",
      "debugOptions" -> TestJsonUtil.toJson(DebugOptions(Some(s"file://${resourcePath.getFile}"), None, None)))
    val input = new DummyDebugInputStepStreaming("name", outputOptions, Option(ssc), sparkSession, properties)
    val outputSchema = StructType(Seq(StructField("name", StringType)))
    val dataOut = Seq(new GenericRowWithSchema(Array("jc"), outputSchema))
    val distributedStream = input.init()
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val eventsRegistered = new scala.collection.mutable.ListBuffer[Row]()
    log.debug("Evaluate the distributedStream")

    distributedStream.ds.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        val count = rdd.count()
        eventsRegistered ++= rdd.collect()
        log.debug(s"EVENTS COUNT : $count")
        totalEvents.add(count)
      } else log.debug("RDD is empty")
      log.debug(s"TOTAL EVENTS : $totalEvents")
    })

    ssc.start() // Start the computation

    log.debug("Started Streaming")

    ssc.awaitTerminationOrTimeout(SparkTimeOut)

    log.debug("Finished Streaming")

    totalEvents.value should ===(1L)
    eventsRegistered should be(dataOut)

  }

  "A DummyDebugInputStepStreaming" should "read and correctly parse an uploaded PARQUET file" in {
    val resourcePath: URL = getClass().getResource("/test.parquet")
    val properties = Map("path" -> s"file://${resourcePath.getFile}", "dummyInputSource" -> "FILE",
      "debugOptions" -> TestJsonUtil.toJson(DebugOptions(Some(s"file://${resourcePath.getFile}"), None, None)))
    val input = new DummyDebugInputStepStreaming("name", outputOptions, Option(ssc), sparkSession, properties)
    val outputSchema = StructType(Seq(StructField("name", StringType)))
    val dataOut = Seq(new GenericRowWithSchema(Array("jc"), outputSchema))
    val distributedStream = input.init()
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val eventsRegistered = new scala.collection.mutable.ListBuffer[Row]()
    log.debug("Evaluate the distributedStream")

    distributedStream.ds.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        val count = rdd.count()
        eventsRegistered ++= rdd.collect()
        log.debug(s"EVENTS COUNT : $count")
        totalEvents.add(count)
      } else log.debug("RDD is empty")
      log.debug(s"TOTAL EVENTS : $totalEvents")
    })

    ssc.start() // Start the computation

    log.debug("Started Streaming")

    ssc.awaitTerminationOrTimeout(SparkTimeOut)

    log.debug("Finished Streaming")

    totalEvents.value should ===(1L)
    eventsRegistered should be(dataOut)

  }

  "A DummyDebugInputStepStreaming" should "read and correctly parse an uploaded JSON file" in {
    val resourcePath: URL = getClass().getResource("/test.json")
    val properties = Map("path" -> s"file://${resourcePath.getFile}" , "dummyInputSource" -> "FILE",
      "debugOptions" -> TestJsonUtil.toJson(DebugOptions(Some(s"file://${resourcePath.getFile}"), None, None)))
    val input = new DummyDebugInputStepStreaming("name", outputOptions, Option(ssc), sparkSession, properties)
    val outputSchema = StructType(Seq(StructField("name", StringType)))
    val dataOut = Seq(new GenericRowWithSchema(Array("sparta"), outputSchema))
    val distributedStream = input.init()
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val eventsRegistered = new scala.collection.mutable.ListBuffer[Row]()

    log.debug("Evaluate the distributedStream")

    distributedStream.ds.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        val count = rdd.count()
        eventsRegistered ++= rdd.collect()
        log.debug(s"EVENTS COUNT : $count")
        totalEvents.add(count)
      } else log.debug("RDD is empty")
      log.debug(s"TOTAL EVENTS : $totalEvents")
    })

    ssc.start() // Start the computation

    log.debug("Started Streaming")

    ssc.awaitTerminationOrTimeout(SparkTimeOut)

    log.debug("Finished Streaming")

    totalEvents.value should ===(1L)
    eventsRegistered should be(dataOut)
  }

  "A DummyDebugInputStepStreaming" should
    "read and correctly parse an uploaded file with an unrecognized extension as text" in {
    val resourcePath: URL = getClass().getResource("/origin.txt")
    val properties = Map("path" -> s"file://${resourcePath.getFile}" , "dummyInputSource" -> "FILE",
      "debugOptions" -> TestJsonUtil.toJson(DebugOptions(Some( s"file://${resourcePath.getFile}"), None, None)))
    val input = new DummyDebugInputStepStreaming("name", outputOptions, Option(ssc), sparkSession, properties)
    val distributedStream = input.init()
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val eventsRegistered = new scala.collection.mutable.ListBuffer[String]()
    log.debug("Evaluate the distributedStream")

    distributedStream.ds.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        val count = rdd.count()
        eventsRegistered ++= rdd.collect().map(_.get(0).toString())
        log.debug(s"EVENTS COUNT : $count")
        totalEvents.add(count)
      } else log.debug("RDD is empty")
      log.debug(s"TOTAL EVENTS : $totalEvents")
    })

    ssc.start() // Start the computation

    log.debug("Started Streaming")

    ssc.awaitTerminationOrTimeout(SparkTimeOut)

    log.debug("Finished Streaming")

    totalEvents.value should ===(9L)
    eventsRegistered should be(lines)

  }

  "A DummyDebugInputStepStreaming" should "read and correctly parse a query given as fake input" in {

    val schema = new StructType(Array(
      StructField("id", IntegerType, nullable = true),
      StructField("id2", IntegerType, nullable = true)
    ))
    val tableName = "tableName"
    val totalRegisters = 1000
    val registers = for (a <- 1 to totalRegisters) yield new GenericRowWithSchema(Array(a,a), schema)
    val registersRow = registers.map(_.asInstanceOf[Row])
    val distributedStream = sc.parallelize(registersRow)

    sparkSession.createDataFrame(distributedStream, schema).createOrReplaceTempView(tableName)
    sparkSession.sql("CREATE TABLE tableName3 USING org.apache.spark.sql.parquet AS select * FROM tableName")

    val datasourceParams = Map("dummyInputSource" -> "SQL",
      "debugOptions" -> TestJsonUtil.toJson(DebugOptions(None, Some(s"select * from tableName3 limit 1000"), None)))
    val debugInput = new DummyDebugInputStepStreaming(
      "crossdata", outputOptions, Option(ssc), sparkSession, datasourceParams)

    val inputdstream = debugInput.init()
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val eventsRegistered = new scala.collection.mutable.ListBuffer[Row]()
    log.debug("Evaluate the distributedStream")

    inputdstream.ds.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        val count = rdd.count()
        eventsRegistered ++= rdd.collect()
        log.debug(s"EVENTS COUNT : $count")
        totalEvents.add(count)
      } else log.debug("RDD is empty")
      log.debug(s"TOTAL EVENTS : $totalEvents")
    })

    ssc.start() // Start the computation

    log.debug("Started Streaming")

    ssc.awaitTerminationOrTimeout(SparkTimeOut)

    log.debug("Finished Streaming")

    sparkSession.sql("DROP TABLE tableName3")

    totalEvents.value should ===(registers.size)
    eventsRegistered.foreach(row => assert(registers.contains(row)))
  }

  "A DummyDebugInputStepStreaming" should "read a plain text as fake input" in {
    val properties = Map("path" -> s"file://$parentDir/origin.txt", "dummyInputSource" -> "EXAMPLE",
      "debugOptions" -> TestJsonUtil.toJson(DebugOptions(None, None, Some(exampleUserDefined))))
    val input = new DummyDebugInputStepStreaming("name", outputOptions, Option(ssc), sparkSession, properties)
    val distributedStream = input.init()
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    log.debug("Evaluate the distributedStream")
    val eventsRegistered = new scala.collection.mutable.ListBuffer[String]()

    distributedStream.ds.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        val count = rdd.count()
        eventsRegistered ++= rdd.collect().map(_.get(0).toString())
        log.debug(s"EVENTS COUNT : $count")
        totalEvents.add(count)
      } else log.debug("RDD is empty")
      log.debug(s"TOTAL EVENTS : $totalEvents")
    })

    ssc.start() // Start the computation

    log.debug("Started Streaming")

    ssc.awaitTerminationOrTimeout(SparkTimeOut)

    log.debug("Finished Streaming")

    totalEvents.value should ===(22L)
    eventsRegistered.toList should be(exampleUserDefined.split("\n"))
  }

  "A DummyDebugInputStepStreaming" should "fail if no simulated input has been defined" in {
    val properties = Map("path" -> s"file://$parentDir/existing.txt", "dummyInputSource" -> "EXAMPLE",
      "debugOptions" -> TestJsonUtil.toJson(DebugOptions(Some("      "), Some("   "), Some(""))))
    val input = new DummyDebugInputStepStreaming("name", outputOptions, Option(ssc), sparkSession, properties)
    an[IllegalArgumentException] should be thrownBy input.init()
  }
}
