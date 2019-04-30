/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.column


import java.io.{Serializable => JSerializable}

import scala.collection.mutable
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.workflow.transformation.column.Formatter.FormatterTransformStepStreaming

@RunWith(classOf[JUnitRunner])
class FormatterTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A FormatterTransformStepStreamingIT" should "Replace a pattern with a string entered by the user" in {

    val fields =
      """[{"columnToFormat": "text",
        |"pattern": "text",
        |"replacement": "option"
        |},
        |{"columnToFormat": "color",
        |"pattern": "blue",
        |"replacement": "red"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("text", StringType), StructField("color", StringType)))
    val outputSchema = StructType(Seq(StructField("textnew", StringType), StructField("color", StringType)))
    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataIn =
      Seq(
        new GenericRowWithSchema(Array("this is text A ", "blue is red"), inputSchema),
        new GenericRowWithSchema(Array("this is text B ", "red is blue"), inputSchema)
      )
    val dataOut = Seq(
      new GenericRowWithSchema(Array("this is option A ", "red is red"), outputSchema),
      new GenericRowWithSchema(Array("this is option B ", "red is red"), outputSchema)
    )
    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new FormatterTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("columnsToFormat" -> fields.asInstanceOf[JSerializable])
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
}