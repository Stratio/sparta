/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.split

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.enumerators.SaveModeEnum
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class SplitTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A SplitTransformStepStreamIT" should "split events the input DStream" in {
    val subFamily = 333210
    val fields =
      """[
        |{
        |   "name":"sector"
        |},
        |{
        |   "name":"section"
        |},
        |{
        |   "name":"familyGroup"
        |},
        |{
        |   "name":"family"
        |},
        |{
        |   "name":"subFamily"
        |}]
        |""".stripMargin
    val inputField = "split"
    val inputSchema = StructType(Seq(StructField(inputField, StringType)))
    val outputSchema = StructType(Seq(
      StructField("sector", StringType),
      StructField("section", StringType),
      StructField("familyGroup", StringType),
      StructField("family", StringType),
      StructField("subFamily", StringType)
    ))
    val dataIn = Seq(new GenericRowWithSchema(Array(subFamily), inputSchema))
    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataOut = Seq(new GenericRowWithSchema(Array("3", "33", "2", "1", "0"), outputSchema))
    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val listIndexes = "1,3,4,5"
    val result = new SplitTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("splitMethod" -> "BYINDEX",
        "byIndexPattern" -> listIndexes,
        "excludeIndexes" -> JsoneyString.apply("false"),
        "inputField" -> inputField,
        "schema.fields" -> fields.asInstanceOf[JSerializable],
        "schema.inputMode" -> "FIELDS"
      )
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

    assert(totalEvents.value === 1)
  }

  "A SplitTransformStepStreamIT" should "discard events" in {
    val subFamily = 333210
    val fields =
      """[
        |{
        |   "name":"sector"
        |},
        |{
        |   "name":"section"
        |},
        |{
        |   "name":"familyGroup"
        |},
        |{
        |   "name":"family"
        |},
        |{
        |   "name":"subFamily"
        |}]
        |""".stripMargin
    val inputField = "split"
    val inputSchema = StructType(Seq(StructField(inputField, StringType)))
    val outputSchema = StructType(Seq(
      StructField("sector", StringType),
      StructField("section", StringType),
      StructField("familyGroup", StringType),
      StructField("family", StringType),
      StructField("subFamily", StringType)
    ))
    val dataIn = Seq(
      new GenericRowWithSchema(Array(subFamily), inputSchema),
      new GenericRowWithSchema(Array(1), inputSchema)
    )
    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataOut = Seq(new GenericRowWithSchema(Array("3", "33", "2", "1", "0"), outputSchema))
    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
    val listIndexes = "1,3,4,5"
    val result = new SplitTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("splitMethod" -> "BYINDEX",
        "byIndexPattern" -> listIndexes,
        "excludeIndexes" -> JsoneyString.apply("false"),
        "inputField" -> inputField,
        "whenRowError" -> "RowDiscard",
        "schema.fields" -> fields.asInstanceOf[JSerializable],
        "schema.inputMode" -> "FIELDS"
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

    assert(totalEvents.value === 1)
    assert(totalDiscardsEvents.value === 1)
  }
}