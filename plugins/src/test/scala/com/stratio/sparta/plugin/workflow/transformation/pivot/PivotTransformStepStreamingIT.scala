/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.pivot

import com.stratio.sparta.plugin.TemporalSparkContext
import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.core.enumerators.SaveModeEnum
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import java.io.{Serializable => JSerializable}

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class PivotTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A PivotTransformStepStream for input mode column" should "select fields of events from input DStream" in {

    val inputSchema = StructType(Seq(StructField("A", StringType), StructField("B", StringType), StructField("C", StringType),
      StructField("D", DoubleType)))

    val dataIn = Seq(
      new GenericRowWithSchema(Array("foo", "one", "small", 1.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "one", "large", 2.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "one", "large", 2.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "two", "small", 3.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "two", "small", 3.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "one", "large", 4.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "one", "small", 5.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "two", "small", 6.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "two", "large", 7.0), inputSchema).asInstanceOf[Row]
    )

    val dataOut = Seq(
      new GenericRowWithSchema(Array("foo", "one", 4.0, 1.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "two", null, 6.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "two", 7.0, 6.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "one", 4.0, 5.0), inputSchema).asInstanceOf[Row]
    )

    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    dataQueue1 += sc.parallelize(dataIn)
    val stream1 = ssc.queueStream(dataQueue1)
    val inputData = Map("step1" -> stream1)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val propertiesWithColumn = Map(
      "selectType" -> "COLUMNS",
      "columns" ->
        """[
          |{
          |   "name": "A"
          |   },
          |{
          |   "name": "B"
          |   },
          |{
          |   "name": "C"
          |   },
          |{
          |   "name": "D"
          |   }
          |   ]""".stripMargin.asInstanceOf[JSerializable],
      "groupByColumns" ->
        """[
          |{
          |   "name": "A"
          |   },
          |{
          |   "name": "B"
          |   }
          |   ]""".stripMargin.asInstanceOf[JSerializable],
      "pivotColumn" -> "C",
      "aggOperations" ->
        """[
          |{
          |   "operationType": "sum",
          |   "name": "D"
          |   }
          |]""".stripMargin.asInstanceOf[JSerializable]
    )

    val result = new PivotTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      propertiesWithColumn
    ).transformWithDiscards(inputData)._1
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT with Column Selection: \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS with Column Selection: \t $totalEvents")
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === dataOut.size)
  }

  "A PivotTransformStepStream for input mode expression" should "select fields of events from input DStream" in {

    val inputSchema = StructType(Seq(StructField("A", StringType), StructField("B", StringType), StructField("C", StringType),
      StructField("D", DoubleType)))

    val dataIn = Seq(
      new GenericRowWithSchema(Array("foo", "one", "small", 1.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "one", "large", 2.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "one", "large", 2.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "two", "small", 3.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "two", "small", 3.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "one", "large", 4.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "one", "small", 5.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "two", "small", 6.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "two", "large", 7.0), inputSchema).asInstanceOf[Row]
    )

    val dataOut = Seq(
      new GenericRowWithSchema(Array("foo", "one", 4.0, 1.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("foo", "two", null, 6.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "two", 7.0, 6.0), inputSchema).asInstanceOf[Row],
      new GenericRowWithSchema(Array("bar", "one", 4.0, 5.0), inputSchema).asInstanceOf[Row]
    )

    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    dataQueue1 += sc.parallelize(dataIn)
    val stream1 = ssc.queueStream(dataQueue1)
    val inputData = Map("step1" -> stream1)
    val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

    val propertiesWithExp = Map (
      "selectType" -> "EXPRESSION",
      "pivotColumn" -> " C",
      "selectExp" -> "  A, B, C, D",
      "groupByExp" -> "A,    B",
      "aggregateExp" -> "   sum( D)"
    )

    val result = new PivotTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      propertiesWithExp
    ).transformWithDiscards(inputData)._1
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT with Expression Selection: \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS with Expression Selection: \t $totalEvents")
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === dataOut.size)
  }
}