/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.catalog.duplicateColumns

import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.plugin.workflow.transformation.catalog.CatalogTransformStepStreaming
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.streaming.dstream.InputDStream
import org.scalatest.Matchers

import scala.collection.mutable

class CatalogTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits{

  "A CatalogTransformStepStreaming" should "change a value of a column from a dictionary" in {

    val inputSchema = StructType(Seq(StructField("column1", StringType)))
    val outputSchema = StructType(Seq(StructField("column1", StringType)))

    val dataQueue = new mutable.Queue[RDD[Row]]()

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("hello"), inputSchema)
      ).map(_.asInstanceOf[Row])

    val dataOut = Seq(
      new GenericRowWithSchema(Array("bye"), outputSchema)
    )

    dataQueue += sc.parallelize(dataIn)

    val stream: InputDStream[Row] = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new CatalogTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "columnName" -> "column1",
        "catalogStrategy" -> "CATALOG_FROM_DICTIONARY",
        "catalogFromDictionary" -> """[{"dictionaryKey":"hello","dictionaryValue":"bye"}]"""
      )
    ).transformWithDiscards(inputData)._1

    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val errorEvents = ssc.sparkContext.accumulator(0L, "Number of events with errors")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if(!rdd.isEmpty()){
        streamingRegisters.foreach { row =>
          if(!dataOut.contains(row)) errorEvents += 1
          if(!(outputSchema == row.schema)) errorEvents += 1
        }
      }
    })

    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 1)
    assert(errorEvents.value === 0)
  }
}
