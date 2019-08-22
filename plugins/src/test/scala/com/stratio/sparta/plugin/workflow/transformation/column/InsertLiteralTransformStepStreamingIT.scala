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
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions, TransformationStepManagement}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.workflow.transformation.column.RenameColumn.RenameColumnTransformStepStreaming

@RunWith(classOf[JUnitRunner])
class InsertLiteralTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A InsertLiteralTransformStepStreamingIT" should "Insert a string literal into a column at the beginning or the end of the string" in {

    val fields =
      """[
        |{
        |   "name":"id",
        |   "value":"id:",
        |   "offset": 0,
        |   "offsetFrom": "INSERT_LEFT"
        |},
        |{
        |   "name":"region",
        |   "value":"-MD",
        |   "offset": 0,
        |   "offsetFrom": "INSERT_RIGHT"
        |}
        |]
        | """.stripMargin

    val inputSchema = StructType(Seq(StructField("id", StringType), StructField("region", StringType)))
    val outputSchema = StructType(Seq(StructField("id", StringType), StructField("region", StringType)))
    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataIn =
      Seq(
        new GenericRowWithSchema(Array("0125671", "madrid"), inputSchema),
        new GenericRowWithSchema(Array("0124841", "ciudad real"), inputSchema)
      )
    val dataOut = Seq(
      new GenericRowWithSchema(Array("id:0125671", "madrid-MD"), outputSchema),
      new GenericRowWithSchema(Array("id:0124841", "ciudad real-MD"), outputSchema)
    )
    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))

    val result = new RenameColumnTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("columnsToInsertLiteral" -> fields.asInstanceOf[JSerializable])
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
          assert(row.schema.fieldNames.contains("textnew"))
        }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
  }
}