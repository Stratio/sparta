/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.dummydebug

import java.io.File
import java.net.URL

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.workflow.step.DebugOptions
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class DummyDebugInputStepBatchIT extends TemporalSparkContext with Matchers with DummyDebugTestUtils {

  "A dummyDebugInputStepBatch" should "read and correctly parse an uploaded CSV file" in {
      val resourcePath: URL = getClass().getResource("/test.csv")
      val properties = Map("path" -> s"file://${resourcePath.getFile}", "dummyInputSource" -> "FILE",
        "debugOptions" -> TestJsonUtil.toJson(DebugOptions(Some(s"file://${resourcePath.getFile}"), None, None)))
      val debugInput = new DummyDebugInputStepBatch("testDebug",outputOptions, Option(ssc), sparkSession, properties)
      val outputSchema = StructType(Seq(StructField("name", StringType)))
      val dataOut = Seq(new GenericRowWithSchema(Array("name"), outputSchema),
        new GenericRowWithSchema(Array("sparta"), outputSchema))
      val rdd = debugInput.initWithSchema()._1
      val count = rdd.ds.count()

      count shouldBe 2

      rdd.ds.collect().toSeq should be(dataOut)
    }

  "A dummyDebugInputStepBatch" should "read and correctly parse an uploaded Avro file" in {
    val resourcePath: URL = getClass().getResource("/test.avro")
    val properties = Map("path" -> s"file://${resourcePath.getFile}", "dummyInputSource" -> "FILE",
      "debugOptions" -> TestJsonUtil.toJson(DebugOptions(Some(s"file://${resourcePath.getFile}"), None, None)))
    val input = new DummyDebugInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    val outputSchema = StructType(Seq(StructField("name", StringType)))
    val dataOut = Seq(new GenericRowWithSchema(Array("jc"), outputSchema))
    val rdd = input.initWithSchema()._1
    val count = rdd.ds.count()

    count shouldBe 1

    rdd.ds.collect().toSeq should be(dataOut)
  }

  "A dummyDebugInputStepBatch" should "read and correctly parse an uploaded PARQUET file" in {
    val resourcePath: URL = getClass().getResource("/test.parquet")
    val properties = Map("path" -> s"file://${resourcePath.getFile}", "dummyInputSource" -> "FILE",
      "debugOptions" -> TestJsonUtil.toJson(DebugOptions(Some(s"file://${resourcePath.getFile}"), None, None)))
    val input = new DummyDebugInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    val outputSchema = StructType(Seq(StructField("name", StringType)))
    val dataOut = Seq(new GenericRowWithSchema(Array("jc"), outputSchema))
    val rdd = input.initWithSchema()._1
    val count = rdd.ds.count()

    count shouldBe 1

    rdd.ds.collect().toSeq should be(dataOut)
  }

  "A dummyDebugInputStepBatch" should "read and correctly parse an uploaded JSON file" in {
    val resourcePath: URL = getClass().getResource("/test.json")
    val properties = Map("path" -> s"file://${resourcePath.getFile}" , "dummyInputSource" -> "FILE",
    "debugOptions" -> TestJsonUtil.toJson(DebugOptions(Some(s"file://${resourcePath.getFile}"), None, None)))
    val input = new DummyDebugInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    val outputSchema = StructType(Seq(StructField("name", StringType)))
    val dataOut = Seq(new GenericRowWithSchema(Array("sparta"), outputSchema))
    val rdd = input.initWithSchema()._1
    val count = rdd.ds.count()

    count shouldBe 1

    rdd.ds.collect().toSeq should be(dataOut)
  }

  "A dummyDebugInputStepBatch" should
    "read and correctly parse an uploaded file with an unrecognized extension as text" in {
    val resourcePath: URL = getClass().getResource("/origin.txt")
    val properties = Map("path" -> s"file://${resourcePath.getFile}" , "dummyInputSource" -> "FILE",
      "debugOptions" -> TestJsonUtil.toJson(DebugOptions(Some(s"file://${resourcePath.getFile}"), None, None)))
    val input = new DummyDebugInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    val rdd = input.initWithSchema()._1
    val count = rdd.ds.count()
    count shouldBe 9
    rdd.ds.collect().toList.map(_.mkString("")) should be(lines)
  }

  "A dummyDebugInputStepBatch" should "read and correctly parse a query given as fake input" in {
    SparkSession.clearActiveSession()

    val schema = new StructType(Array(
      StructField("id", IntegerType, nullable = true),
      StructField("id2", IntegerType, nullable = true)
    ))
    val tableName = "tableName"
    val totalRegisters = 1000
    val registers = for (a <- 1 to totalRegisters) yield Row(a,a)
    val rdd = sc.parallelize(registers)

    sparkSession.createDataFrame(rdd, schema).createOrReplaceTempView(tableName)

    val datasourceParams = Map("dummyInputSource" -> "SQL",
      "debugOptions" -> TestJsonUtil.toJson(DebugOptions(None, Some(s"select * from $tableName"), None)))
    val debugInput = new DummyDebugInputStepBatch(
      "crossdata", outputOptions, Option(ssc), sparkSession, datasourceParams)

    val inputRdd = debugInput.initWithSchema()._1
    val batchEvents = inputRdd.ds.count()
    val batchRegisters = inputRdd.ds.collect()

    assert(batchRegisters === registers)
    assert(batchEvents === totalRegisters.toLong)
  }

  "A dummyDebugInputStepBatch" should "read a plain text as fake input" in {
    val properties = Map("path" -> s"file://$parentDir/existing.txt", "dummyInputSource" -> "EXAMPLE",
      "debugOptions" -> TestJsonUtil.toJson(DebugOptions(None, None, Some(exampleUserDefined))))
    val input = new DummyDebugInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    val rdd = input.initWithSchema()._1
    val count = rdd.ds.count()

    count shouldBe 22
    rdd.ds.collect().map(_.get(0)).map(_.toString()) should be(exampleUserDefined.split("\n"))
  }

  "A dummyDebugInputStepBatch" should "fail if no simulated input has been defined" in {
    val properties = Map("path" -> s"file://$parentDir/existing.txt", "dummyInputSource" -> "EXAMPLE",
      "debugOptions" -> TestJsonUtil.toJson(DebugOptions(Some("      "), Some("   "), Some(""))))
    val input = new DummyDebugInputStepBatch("name", outputOptions, Option(ssc), sparkSession, properties)
    an[IllegalArgumentException] should be thrownBy input.initWithSchema()
  }

}