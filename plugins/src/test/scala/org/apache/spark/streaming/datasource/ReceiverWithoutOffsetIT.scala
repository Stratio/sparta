/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

