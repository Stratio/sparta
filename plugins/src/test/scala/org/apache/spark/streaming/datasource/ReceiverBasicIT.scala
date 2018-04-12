/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.streaming.datasource

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.datasource.models.{InputSentences, OffsetConditions, OffsetField}
import org.apache.spark.streaming.{Milliseconds, Seconds, StreamingContext}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class ReceiverBasicIT extends TemporalDataSuite {

  test ("DataSource Receiver should read all the records in one streaming batch") {
    sc = new SparkContext(conf)
    sparkSession = SparkSession.builder().config(sc.getConf).getOrCreate()
    SparkSession.clearActiveSession()
    val rdd = sc.parallelize(registers)
    sparkSession.createDataFrame(rdd, schema).createOrReplaceTempView(tableName)
    ssc = new StreamingContext(sc, Milliseconds(batchWindow))
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")

    val inputSentences = InputSentences(
      s"select * from $tableName",
      OffsetConditions(Seq(OffsetField("idInt"))),
      initialStatements = Seq.empty[String]
    )
    val distributedStream = DatasourceUtils.createStream(ssc, inputSentences, datasourceParams)

    distributedStream.start()
    distributedStream.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      val streamingRegisters = rdd.collect()
      if (!rdd.isEmpty())
        assert(streamingRegisters === registers.reverse)
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(timeoutStreaming)
    ssc.stop()

    assert(totalEvents.value >= totalRegisters.toLong)
  }
}

