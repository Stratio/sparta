/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.column

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
import com.stratio.sparta.plugin.workflow.transformation.column.PartialInfo.PartialInfoTransformStepStreaming
import org.apache.spark.streaming.dstream.InputDStream

@RunWith(classOf[JUnitRunner])
class PartialInforTransformationStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits{

  "A DuplicateColumnsTransformStepBatchIT" should "Create a new column with a substring of another column." in {

    val fields =
      """[{"columnToExtractPartialInfo": "type",
        |"newColumnName": "subtype",
        |"start": "6",
        |"length": "1"
        |}
        |]""".stripMargin


    val inputSchema = StructType(Seq(StructField("type", StringType)))
    val outputSchema = StructType(Seq(StructField("type", StringType), StructField("subtype", StringType)))

    val dataQueue = new mutable.Queue[RDD[Row]]()

    val dataIn: Seq[Row] =
      Seq(
        new GenericRowWithSchema(Array("type-A"), inputSchema),
        new GenericRowWithSchema(Array("type-B"), inputSchema)
      ).map(_.asInstanceOf[Row])


    val dataOut = Seq(
      new GenericRowWithSchema(Array("type-A", "A"), outputSchema),
      new GenericRowWithSchema(Array("type-B", "B"), outputSchema)
    )

    dataQueue += sc.parallelize(dataIn)
    val stream: InputDStream[Row] = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new PartialInfoTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("columnsToPartialInfo" -> fields)
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






