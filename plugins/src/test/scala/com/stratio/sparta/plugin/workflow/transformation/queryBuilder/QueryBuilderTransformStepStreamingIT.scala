/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.queryBuilder

import com.stratio.sparta.core.DistributedMonad.DistributedMonadImplicits
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions, TransformationStepManagement}
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.plugin.TemporalSparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.collection.mutable

@RunWith(classOf[JUnitRunner])
class QueryBuilderTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  "A QueryBuilderTransformStep" should "make sql over one DStream" in {

    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1),
      new GenericRowWithSchema(Array("red", 12.2), schema1),
      new GenericRowWithSchema(Array("red", 1.2), schema1).asInstanceOf[Row]
    )
    dataQueue1 += sc.parallelize(data1)
    val stream1 = ssc.queueStream(dataQueue1)
    val inputData = Map("step1" -> stream1)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
    val visualClause =
      """{
        |   "selectClauses": [
        |     {"expression": "color", "alias": "color2"},
        |     {"expression": "price", "alias": "price2"}
        |   ],
        |   "fromClause": {"tableName": "step1", "alias": "st1"},
        |   "whereClause": "price > 12",
        |   "orderByClauses": [
        |     {"field": "price", "order": "ASC"}
        |   ]
        |}
        | """.stripMargin
    val result = new QueryBuilderTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("visualQuery" -> visualClause)
    ).transformWithDiscards(inputData)

    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result._1.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        streamingRegisters.foreach(row => assert(data1.contains(row)))
      }
    })

    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)

  }

  "A QueryBuilderTransformStep" should "make trigger over two DStreams" in {
    val schema1 = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
    val schema2 = StructType(Seq(StructField("color", StringType),
      StructField("company", StringType), StructField("name", StringType)))
    val schemaResult = StructType(Seq(StructField("color", StringType),
      StructField("company", StringType), StructField("name", StringType), StructField("price", DoubleType)))
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataQueue2 = new mutable.Queue[RDD[Row]]()
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1),
      new GenericRowWithSchema(Array("red", 12.2), schema1)
    )
    val data2 = Seq(
      new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee"), schema2),
      new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee"), schema2)
    )
    dataQueue1 += sc.parallelize(data1)
    dataQueue2 += sc.parallelize(data2)
    val stream1 = ssc.queueStream(dataQueue1)
    val stream2 = ssc.queueStream(dataQueue2)
    val inputData = Map("step1" -> stream1, "step2" -> stream2)
    val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
    val visualClause =
      """{
        |   "selectClauses": [
        |     {"expression": "st1.color", "alias": "color"},
        |     {"expression": "st2.company", "alias": "company"},
        |     {"expression": "st2.name", "alias": "name"},
        |     {"expression": "st1.price", "alias": "price"}
        |   ],
        |   "joinClause": {
        |                   "leftTable": {"tableName": "step1", "alias": "st1"},
        |                   "rightTable": {"tableName": "step2", "alias": "st2"},
        |                   "joinTypes": "LEFT",
        |                   "joinConditions": [
        |                       {"leftField": "color", "rightField": "color"}
        |                   ]
        |                 },
        |   "whereClause": "st1.price > 12",
        |   "orderByClauses": [
        |     {"field": "st2.name", "order": "ASC", "position":3},
        |     {"field": "st1.price", "order": "ASC", "position":2}
        |   ]
        |}
        | """.stripMargin
    val result = new QueryBuilderTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map("visualQuery" -> visualClause)
    ).transformWithDiscards(inputData)._1
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        val queryData = Seq(
          new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee", 12.1), schemaResult),
          new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee", 12.2), schemaResult))
        streamingRegisters.foreach(row => assert(queryData.contains(row)))
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)

  }
}