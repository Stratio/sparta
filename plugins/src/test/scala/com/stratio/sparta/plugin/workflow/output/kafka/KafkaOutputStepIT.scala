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

package com.stratio.sparta.plugin.workflow.output.kafka

import java.util.UUID

import com.stratio.sparta.plugin.common.kafka.KafkaSuiteBase
import com.stratio.sparta.plugin.workflow.input.kafka.KafkaInputStep
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class KafkaOutputStepIT extends KafkaSuiteBase {

  val topics = Seq(
    s"topicTest-${this.getClass.getName}-${UUID.randomUUID().toString}",
    s"topicTest2-${this.getClass.getName}-${UUID.randomUUID().toString}"
  )

  override def beforeAll(): Unit = {
    createTopics(topics)
  }

  override def afterAll(): Unit = {
    resetTopics(topics)
  }

  "KafkaOutputStep " should {

    "Send all the records in Json format" in {
      val hostPort =
        s"""[
           |{
           |    "port": "9092",
           |    "host": "$hosts"
           |}]
           | """.stripMargin

      val props = Map(
        "bootstrap.servers" -> hostPort.asInstanceOf[java.io.Serializable],
        "value.serializer.outputFormat" -> "JSON"
      )
      val output = new KafkaOutputStep("kafka1", sparkSession.get, props)
      val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
      val data1 = Seq(
        new GenericRowWithSchema(Array("blue", 12.1), schema),
        new GenericRowWithSchema(Array("red", 12.2), schema)
      )
      val rdd = sc.get.parallelize(data1).asInstanceOf[RDD[Row]]
      val data = sparkSession.get.createDataFrame(rdd, schema)
      val saveOpts = Map("tableName" -> topics.head)

      log.info("Send dataframe to kafka")

      output.save(data, SaveModeEnum.Append, saveOpts)

      output.cleanUp()

      val topicsProp =
        s"""[
           |{
           |   "topic":"${topics.head}"
           |}]
           | """.stripMargin
      val propsConsumer = Map(
        "storeOffsetInKafka" -> "false",
        "value.deserializer" -> "row",
        "value.deserializer.inputFormat" -> "JSON",
        "bootstrap.servers" -> hostPort.asInstanceOf[java.io.Serializable],
        "topics" -> topicsProp.asInstanceOf[java.io.Serializable],
        "auto.offset.reset" -> "earliest"
      )
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
      val input = new KafkaInputStep("kafka", outputOptions, ssc, sparkSession.get, propsConsumer)
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
        val streamingRegisters = rdd.collect()
        if (!rdd.isEmpty())
          streamingRegisters.foreach(row => assert(data1.contains(row)))
      })

      ssc.get.start()

      log.info("Started Streaming")

      ssc.get.awaitTerminationOrTimeout(SparkTimeOut)

      log.info("Finished Streaming")

      totalEvents.value should ===(data1.size)
    }

    "Send all the records in Row format" in {
      val hostPort =
        s"""[
           |{
           |    "port": "9092",
           |    "host": "$hosts"
           |}]
           | """.stripMargin

      val props = Map(
        "bootstrap.servers" -> hostPort.asInstanceOf[java.io.Serializable],
        "value.serializer.outputFormat" -> "ROW"
      )
      val output = new KafkaOutputStep("kafka2", sparkSession.get, props)
      val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
      val data1 = Seq(
        new GenericRowWithSchema(Array("blue", 12.1), schema),
        new GenericRowWithSchema(Array("red", 12.2), schema)
      )
      val dataRaw = Seq("blue,12.1", "red,12.2")
      val rdd = sc.get.parallelize(data1).asInstanceOf[RDD[Row]]
      val data = sparkSession.get.createDataFrame(rdd, schema)
      val saveOpts = Map("tableName" -> topics.last)

      log.info("Send dataframe to kafka")

      output.save(data, SaveModeEnum.Append, saveOpts)

      output.cleanUp()

      val topicsProp =
        s"""[
           |{
           |   "topic":"${topics.last}"
           |}]
           | """.stripMargin
      val propsConsumer = Map(
        "storeOffsetInKafka" -> "false",
        "value.deserializer" -> "row",
        "value.deserializer.inputFormat" -> "STRING",
        "bootstrap.servers" -> hostPort.asInstanceOf[java.io.Serializable],
        "topics" -> topicsProp.asInstanceOf[java.io.Serializable],
        "auto.offset.reset" -> "earliest"
      )
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
      val input = new KafkaInputStep("kafka", outputOptions, ssc, sparkSession.get, propsConsumer)
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
        val streamingRegisters = rdd.collect()
        if (!rdd.isEmpty())
          streamingRegisters.foreach(row => assert(dataRaw.contains(row.get(0))))
      })

      ssc.get.start()

      log.info("Started Streaming")

      ssc.get.awaitTerminationOrTimeout(SparkTimeOut)

      log.info("Finished Streaming")

      totalEvents.value should ===(data1.size)
    }
  }
}