/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

package com.stratio.sparta.plugin.workflow.transformation.column

import java.io.{Serializable => JSerializable}
import java.nio.file.Paths

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
import com.stratio.sparta.plugin.workflow.transformation.column.Integrity.IntegrityTransformStepStreaming

@RunWith(classOf[JUnitRunner])
class IntegrityTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A IntegrityTransformStepStreamingIT" should "Convert all fields of a column that no match with the file of the path given by the user" in {

    val fields =
      """[{"name": "city",
        |"defaultValue": "null"
        |}
        |]""".stripMargin

    val inputSchema = StructType(Seq(StructField("name", StringType), StructField("city", StringType)))
    val outputSchema= StructType(Seq(StructField("name", StringType), StructField("city", StringType)))

    val dataQueue = new mutable.Queue[RDD[Row]]()
    val dataIn =
      Seq(
        new GenericRowWithSchema(Array("Paco", "bilbao "), inputSchema),
        new GenericRowWithSchema(Array("Antonio", "madrid"), inputSchema),
        new GenericRowWithSchema(Array("Miguel", " "), inputSchema)
      )
    val dataOut: Seq[GenericRowWithSchema] = Seq(
      new GenericRowWithSchema(Array("Paco", "null"), inputSchema),
      new GenericRowWithSchema(Array("Antonio", "madrid"), inputSchema),
      new GenericRowWithSchema(Array("Miguel", "null"), inputSchema)
    )
    dataQueue += sc.parallelize(dataIn)
    val stream = ssc.queueStream(dataQueue)
    val inputData = Map("step1" -> stream)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val result = new IntegrityTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "path" -> s"${Paths.get(getClass.getResource("/citiesTest.csv").toURI()).toString}",
        "columnsIntegrity" -> fields.asInstanceOf[JSerializable]
      )
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

    assert(totalEvents.value === 3)
    assert(actualSchema.fold(false)(schema => schema equals outputSchema))
    streamingRegisters.foreach{ row =>
      print(row.toSeq)
      assert(dataOut.contains(row))
    }

  }
}