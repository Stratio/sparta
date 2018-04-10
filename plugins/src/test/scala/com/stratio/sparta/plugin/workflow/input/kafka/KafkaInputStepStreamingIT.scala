/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.kafka

import java.util.UUID

import com.stratio.sparta.plugin.common.kafka.KafkaSuiteBase
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class KafkaInputStepStreamingIT extends KafkaSuiteBase {

  val topics = Seq(
    s"topicTest-${this.getClass.getName}-${UUID.randomUUID().toString}",
    s"topicTest2-${this.getClass.getName}-${UUID.randomUUID().toString}"
  )

  override def beforeAll(): Unit = {
    createTopics(topics)
    produceEvents(topics)
  }

  override def afterAll(): Unit = {
    resetTopics(topics)
  }

  "KafkaInputStep " should {
    "Read all the records" in {
      val topicsProp =
        s"""[
           |{
           |   "topic":"${topics.head}"
           |}]
           | """.stripMargin

      val hostPort =
        s"""[
           |{
           |    "port": "9092",
           |    "host": "$hosts"
           |}]
           | """.stripMargin

      val props = Map(
        "storeOffsetInKafka" -> "false",
        "bootstrap.servers" -> hostPort.asInstanceOf[java.io.Serializable],
        "topics" -> topicsProp.asInstanceOf[java.io.Serializable],
        "auto.offset.reset" -> "earliest"
      )
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

      log.info("Creating kafka input step")

      val input = new KafkaInputStepStreaming("kafka", outputOptions, ssc, sparkSession.get, props)
      val distributedStream = input.init
      val totalEvents = ssc.get.sparkContext.accumulator(0L, "Number of events received")

      log.info("Evaluate the DStream")

      distributedStream.ds.foreachRDD(rdd => {
        if (!rdd.isEmpty()) {
          val count = rdd.count()
          log.info(s"EVENTS COUNT : $count")
          totalEvents.add(count)
        } else log.info("RDD is empty")
        log.info(s"TOTAL EVENTS : $totalEvents")
      })

      ssc.get.start() // Start the computation

      log.info("Started Streaming")

      ssc.get.awaitTerminationOrTimeout(SparkTimeOut)

      log.info("Finished Streaming")

      totalEvents.value should ===(totalRegisters.toLong)
    }

    "Read all the records storing offset in kafka" in {
      val topicsProp =
        s"""[
           |{
           |   "topic":"${topics.last}"
           |}]
           | """.stripMargin

      val hostPort =
        s"""[
           |{
           |    "port": "9092",
           |    "host": "$hosts"
           |}]
           | """.stripMargin

      val props = Map(
        "storeOffsetInKafka" -> "true",
        "bootstrap.servers" -> hostPort.asInstanceOf[java.io.Serializable],
        "topics" -> topicsProp.asInstanceOf[java.io.Serializable],
        "auto.offset.reset" -> "earliest"
      )
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

      log.info("Creating kafka input step")

      val input = new KafkaInputStepStreaming("kafka", outputOptions, ssc, sparkSession.get, props)
      val distributedStream = input.init
      val totalEvents = ssc.get.sparkContext.accumulator(0L, "Number of events received")

      log.info("Evaluate the DStream")

      distributedStream.ds.foreachRDD(rdd => {
        if (!rdd.isEmpty()) {
          val count = rdd.count()
          log.info(s"EVENTS COUNT : $count")
          totalEvents.add(count)
        } else log.info("RDD is empty")
        log.info(s"TOTAL EVENTS : $totalEvents")
      })

      ssc.get.start() // Start the computation

      log.info("Started Streaming")

      ssc.get.awaitTerminationOrTimeout(SparkTimeOut)

      log.info("Finished Streaming")

      totalEvents.value should ===(totalRegisters.toLong)
    }
  }
}
