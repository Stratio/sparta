/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.join

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
class JoinTransformStepStreamingIT extends TemporalSparkContext with Matchers with DistributedMonadImplicits {

  val schema1 = StructType(Seq(
    StructField("color", StringType),
    StructField("price", DoubleType))
  )
  val schema2 = StructType(Seq(
    StructField("color", StringType),
    StructField("company", StringType),
    StructField("name", StringType))
  )
  val schemaResult = StructType(Seq(
    StructField("color", StringType),
    StructField("company", StringType),
    StructField("name", StringType),
    StructField("price", DoubleType)
  ))
  val data1 = Seq(
    new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
    new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row]
  )
  val data2 = Seq(
    new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
    new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee"), schema2).asInstanceOf[Row]
  )
  val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
  val conditions =
    s"""[
       |{
       |   "leftField":"color",
       |   "rightField":"color"
       |}]
       | """.stripMargin

  "A TriggerTransformStep" should "make INNER join over two DStreams" in {
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataQueue2 = new mutable.Queue[RDD[Row]]()
    dataQueue1 += sc.parallelize(data1)
    dataQueue2 += sc.parallelize(data2)
    val stream1 = ssc.queueStream(dataQueue1)
    val stream2 = ssc.queueStream(dataQueue2)
    val inputData = Map("step1" -> stream1, "step2" -> stream2)
    val result = new JoinTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinReturn" -> "all",
        "joinType" -> "INNER",
        "joinConditions" -> conditions
      )
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        val queryData = Seq(
          new GenericRowWithSchema(Array("blue", 12.1, "blue", "Stratio", "Stratio employee"), schemaResult),
          new GenericRowWithSchema(Array("red", 12.2, "red", "Paradigma", "Paradigma employee"), schemaResult))
        streamingRegisters.foreach(row => assert(queryData.contains(row)))
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
  }

  "A TriggerTransformStep" should "make INNER join over two DStreams selecting left fields" in {
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataQueue2 = new mutable.Queue[RDD[Row]]()
    dataQueue1 += sc.parallelize(data1)
    dataQueue2 += sc.parallelize(data2)
    val stream1 = ssc.queueStream(dataQueue1)
    val stream2 = ssc.queueStream(dataQueue2)
    val inputData = Map("step1" -> stream1, "step2" -> stream2)
    val result = new JoinTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinReturn" -> "left",
        "joinType" -> "INNER",
        "joinConditions" -> conditions
      )
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        val queryData = Seq(
          new GenericRowWithSchema(Array("blue", 12.1), schemaResult),
          new GenericRowWithSchema(Array("red", 12.2), schemaResult))
        streamingRegisters.foreach(row => assert(queryData.contains(row)))
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
  }

  "A TriggerTransformStep" should "make INNER join over two Dstreams selecting right fields" in {
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataQueue2 = new mutable.Queue[RDD[Row]]()
    dataQueue1 += sc.parallelize(data1)
    dataQueue2 += sc.parallelize(data2)
    val stream1 = ssc.queueStream(dataQueue1)
    val stream2 = ssc.queueStream(dataQueue2)
    val inputData = Map("step1" -> stream1, "step2" -> stream2)
    val result = new JoinTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinReturn" -> "right",
        "joinType" -> "INNER",
        "joinConditions" -> conditions
      )
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        val queryData = Seq(
          new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee"), schemaResult),
          new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee"), schemaResult))
        streamingRegisters.foreach(row => assert(queryData.contains(row)))
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
  }

  "A TriggerTransformStep" should "make INNER join over two Dstreams selecting columns" in {
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataQueue2 = new mutable.Queue[RDD[Row]]()
    dataQueue1 += sc.parallelize(data1)
    dataQueue2 += sc.parallelize(data2)
    val stream1 = ssc.queueStream(dataQueue1)
    val stream2 = ssc.queueStream(dataQueue2)
    val inputData = Map("step1" -> stream1, "step2" -> stream2)
    val schemaResult = StructType(Seq(
      StructField("color2", StringType)
    ))
    val columns =
      s"""[
         |{
         |   "tableSide":"LEFT",
         |   "column":"color",
         |   "alias": "color2"
         |}]
         | """.stripMargin
    val result = new JoinTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinReturn" -> "COLUMNS",
        "joinType" -> "INNER",
        "joinReturnColumns" -> columns,
        "joinConditions" -> conditions
      )
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        val queryData = Seq(
          new GenericRowWithSchema(Array("blue"), schemaResult),
          new GenericRowWithSchema(Array("red"), schemaResult))
        streamingRegisters.foreach { row =>
          assert(queryData.contains(row))
          row.schema should be(schemaResult)
        }
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
  }

  "A TriggerTransformStep" should "make LEFT join over two Dstreams" in {
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataQueue2 = new mutable.Queue[RDD[Row]]()
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("yellow", 12.3), schema1).asInstanceOf[Row]
    )
    dataQueue1 += sc.parallelize(data1)
    dataQueue2 += sc.parallelize(data2)
    val stream1 = ssc.queueStream(dataQueue1)
    val stream2 = ssc.queueStream(dataQueue2)
    val inputData = Map("step1" -> stream1, "step2" -> stream2)
    val result = new JoinTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinReturn" -> "all",
        "joinType" -> "LEFT",
        "joinConditions" -> conditions
      )
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        val queryData = Seq(
          new GenericRowWithSchema(Array("blue", 12.1, "blue", "Stratio", "Stratio employee"), schemaResult),
          new GenericRowWithSchema(Array("yellow", 12.3, null, null, null), schemaResult),
          new GenericRowWithSchema(Array("red", 12.2, "red", "Paradigma", "Paradigma employee"), schemaResult))
        streamingRegisters.foreach(row => assert(queryData.contains(row)))
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 3)
  }

  "A TriggerTransformStep" should "make RIGHT join over two Dstreams" in {
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataQueue2 = new mutable.Queue[RDD[Row]]()
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("yellow", 12.3), schema1).asInstanceOf[Row]
    )
    dataQueue1 += sc.parallelize(data1)
    dataQueue2 += sc.parallelize(data2)
    val stream1 = ssc.queueStream(dataQueue1)
    val stream2 = ssc.queueStream(dataQueue2)
    val inputData = Map("step1" -> stream1, "step2" -> stream2)
    val result = new JoinTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinReturn" -> "all",
        "joinType" -> "RIGHT",
        "joinConditions" -> conditions
      )
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        val queryData = Seq(
          new GenericRowWithSchema(Array("blue", 12.1, "blue", "Stratio", "Stratio employee"), schemaResult),
          new GenericRowWithSchema(Array("red", 12.2, "red", "Paradigma", "Paradigma employee"), schemaResult))
        streamingRegisters.foreach(row => assert(queryData.contains(row)))
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
  }

  "A TriggerTransformStep" should "make RIGHT_ONLY join over two Dstreams" in {
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataQueue2 = new mutable.Queue[RDD[Row]]()
    val data2 = Seq(
      new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
      new GenericRowWithSchema(Array("yellow", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee"), schema2).asInstanceOf[Row]
    )
    dataQueue1 += sc.parallelize(data1)
    dataQueue2 += sc.parallelize(data2)
    val stream1 = ssc.queueStream(dataQueue1)
    val stream2 = ssc.queueStream(dataQueue2)
    val inputData = Map("step1" -> stream1, "step2" -> stream2)
    val result = new JoinTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinType" -> "RIGHT_ONLY",
        "joinConditions" -> conditions
      )
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        val queryData = Seq(new GenericRowWithSchema(Array("yellow", "Stratio", "Stratio employee"), schema2))
        streamingRegisters.foreach(row => assert(queryData.contains(row)))
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 1)
  }

  "A TriggerTransformStep" should "make LEFT_ONLY join over two Dstreams" in {
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataQueue2 = new mutable.Queue[RDD[Row]]()
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("yellow", 12.3), schema1).asInstanceOf[Row]
    )
    dataQueue1 += sc.parallelize(data1)
    dataQueue2 += sc.parallelize(data2)
    val stream1 = ssc.queueStream(dataQueue1)
    val stream2 = ssc.queueStream(dataQueue2)
    val inputData = Map("step1" -> stream1, "step2" -> stream2)
    val result = new JoinTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinType" -> "LEFT_ONLY",
        "joinConditions" -> conditions
      )
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        val queryData = Seq(new GenericRowWithSchema(Array("yellow", 12.3), schema1).asInstanceOf[Row])
        streamingRegisters.foreach(row => assert(queryData.contains(row)))
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 1)
  }

  "A TriggerTransformStep" should "make LEFT_RIGHT_ONLY join over two Dstreams" in {
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataQueue2 = new mutable.Queue[RDD[Row]]()
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("yellow", 12.3), schema1).asInstanceOf[Row]
    )
    val data2 = Seq(
      new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
      new GenericRowWithSchema(Array("brown", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee"), schema2).asInstanceOf[Row]
    )
    dataQueue1 += sc.parallelize(data1)
    dataQueue2 += sc.parallelize(data2)
    val stream1 = ssc.queueStream(dataQueue1)
    val stream2 = ssc.queueStream(dataQueue2)
    val inputData = Map("step1" -> stream1, "step2" -> stream2)
    val result = new JoinTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinType" -> "LEFT_RIGHT_ONLY",
        "joinConditions" -> conditions
      )
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        val queryData = Seq(
          new GenericRowWithSchema(Array("yellow", 12.3, null, null, null), schemaResult),
          new GenericRowWithSchema(Array(null, null, "brown", "Stratio", "Stratio employee"), schemaResult))
        streamingRegisters.foreach(row => assert(queryData.contains(row)))
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 2)
  }

  "A TriggerTransformStep" should "make FULL join over two Dstreams" in {
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataQueue2 = new mutable.Queue[RDD[Row]]()
    val data1 = Seq(
      new GenericRowWithSchema(Array("blue", 12.1), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", 12.2), schema1).asInstanceOf[Row],
      new GenericRowWithSchema(Array("yellow", 12.3), schema1).asInstanceOf[Row]
    )
    val data2 = Seq(
      new GenericRowWithSchema(Array("blue", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
      new GenericRowWithSchema(Array("brown", "Stratio", "Stratio employee"), schema2).asInstanceOf[Row],
      new GenericRowWithSchema(Array("red", "Paradigma", "Paradigma employee"), schema2).asInstanceOf[Row]
    )
    dataQueue1 += sc.parallelize(data1)
    dataQueue2 += sc.parallelize(data2)
    val stream1 = ssc.queueStream(dataQueue1)
    val stream2 = ssc.queueStream(dataQueue2)
    val inputData = Map("step1" -> stream1, "step2" -> stream2)
    val result = new JoinTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinType" -> "FULL",
        "joinConditions" -> conditions
      )
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        val queryData = Seq(
          new GenericRowWithSchema(Array("blue", 12.1, "blue", "Stratio", "Stratio employee"), schemaResult),
          new GenericRowWithSchema(Array("red", 12.2, "red", "Paradigma", "Paradigma employee"), schemaResult),
          new GenericRowWithSchema(Array("yellow", 12.3, null, null, null), schemaResult),
          new GenericRowWithSchema(Array(null, null, "brown", "Stratio", "Stratio employee"), schemaResult))
        streamingRegisters.foreach(row => assert(queryData.contains(row)))
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 4)
  }

  "A TriggerTransformStep" should "make CROSS join over two Dstreams" in {
    val dataQueue1 = new mutable.Queue[RDD[Row]]()
    val dataQueue2 = new mutable.Queue[RDD[Row]]()
    dataQueue1 += sc.parallelize(data1)
    dataQueue2 += sc.parallelize(data2)
    val stream1 = ssc.queueStream(dataQueue1)
    val stream2 = ssc.queueStream(dataQueue2)
    val inputData = Map("step1" -> stream1, "step2" -> stream2)
    val result = new JoinTransformStepStreaming(
      "dummy",
      outputOptions,
      TransformationStepManagement(),
      Option(ssc),
      sparkSession,
      Map(
        "leftTable" -> "step1",
        "rightTable" -> "step2",
        "joinType" -> "CROSS",
        "joinConditions" -> conditions
      )
    ).transform(inputData)
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    result.ds.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty()) {
        val queryData = Seq(
          new GenericRowWithSchema(Array("blue", 12.1, "blue", "Stratio", "Stratio employee"), schemaResult),
          new GenericRowWithSchema(Array("blue", 12.1, "red", "Paradigma", "Paradigma employee"), schemaResult),
          new GenericRowWithSchema(Array("red", 12.2, "blue", "Stratio", "Stratio employee"), schemaResult),
          new GenericRowWithSchema(Array("red", 12.2, "red", "Paradigma", "Paradigma employee"), schemaResult)
        )
        streamingRegisters.foreach(row => assert(queryData.contains(row)))
      }
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value === 4)
  }

}