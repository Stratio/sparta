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

package com.stratio.sparta.plugin.workflow.input.kafka

import java.util.UUID

import com.stratio.sparta.plugin.common.kafka.KafkaSuiteBase
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class KafkaInputStepIT extends KafkaSuiteBase {

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
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)

      log.info("Creating kafka input step")

      val input = new KafkaInputStep("kafka", outputOptions, ssc, sparkSession.get, props)
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
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)

      log.info("Creating kafka input step")

      val input = new KafkaInputStep("kafka", outputOptions, ssc, sparkSession.get, props)
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
