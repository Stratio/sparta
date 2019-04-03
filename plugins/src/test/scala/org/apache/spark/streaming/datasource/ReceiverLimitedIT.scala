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
class ReceiverLimitedIT extends TemporalDataSuite {

  override val timeoutStreaming = 2000L

  test("DataSource Receiver should read the records limited on each batch") {
    sc = new SparkContext(conf)
    sparkSession = SparkSession.builder().config(sc.getConf).getOrCreate()
    SparkSession.clearActiveSession()
    val rdd = sc.parallelize(registers)
    sparkSession.createDataFrame(rdd, schema).createOrReplaceTempView(tableName)

    ssc = new StreamingContext(sc, Milliseconds(batchWindow))
    val totalEvents = ssc.sparkContext.accumulator(0L, "Number of events received")
    val inputSentences = InputSentences(
      s"select * from $tableName",
      OffsetConditions(Seq(OffsetField("idInt")), limitRecords = 500),
      initialStatements = Seq.empty[String],
      continuousStatements = Seq.empty[String]
    )
    val distributedStream = DatasourceUtils.createStream(ssc, inputSentences, datasourceParams)

    // Start up the receiver.
    distributedStream.start()

    // Fires each time the configured window has passed.
    distributedStream.foreachRDD(rdd => {
      totalEvents += rdd.count()
    })

    ssc.start() // Start the computation
    ssc.awaitTerminationOrTimeout(timeoutStreaming) // Wait for the computation to terminate
    ssc.stop()

    assert(totalEvents.value >= totalRegisters.toLong)
  }
}

