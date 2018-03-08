/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package org.apache.spark.streaming.datasource

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.datasource.models.InputSentences
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ReceiverWithoutOffsetIT extends TemporalDataSuite {

  test("DataSource Receiver should read all the records on each batch without offset conditions") {
    sc = new SparkContext(conf)
    sparkSession = SparkSession.builder().config(sc.getConf).getOrCreate()
    SparkSession.clearActiveSession()
    val rdd = sc.parallelize(registers)
    sparkSession.createDataFrame(rdd, schema).createOrReplaceTempView(tableName)
    ssc = new StreamingContext(sc, Seconds(1))
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val inputSentences = InputSentences(
      s"select * from $tableName",
      initialStatements = Seq.empty[String]
    )
    log.info(s" EVENTS COUNT : \t ${rdd.count()}")

    val distributedStream = DatasourceUtils.createStream(ssc, inputSentences, datasourceParams)

    distributedStream.start()
    distributedStream.foreachRDD(rdd => {
      val streamingEvents = rdd.count()
      log.info(s" EVENTS COUNT : \t $streamingEvents")
      totalEvents += streamingEvents
      log.info(s" TOTAL EVENTS : \t $totalEvents")
      if (!rdd.isEmpty())
        assert(streamingEvents === totalRegisters.toLong)
    })
    ssc.start()
    ssc.awaitTerminationOrTimeout(3000L)
    ssc.stop()

    assert(totalEvents.value >= totalRegisters.toLong * 3)
  }
}

