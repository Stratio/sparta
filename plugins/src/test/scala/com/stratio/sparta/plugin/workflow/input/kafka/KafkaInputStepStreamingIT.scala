/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.kafka

import java.util.UUID

import com.stratio.sparta.plugin.common.kafka.KafkaSuiteBase
import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.core.enumerators.SaveModeEnum
import com.stratio.sparta.plugin.workflow.output.kafka.KafkaOutputStep
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class KafkaInputStepStreamingIT extends KafkaSuiteBase {

  val topic1 = s"topicTest1-${UUID.randomUUID().toString}"
  val topic2 = s"topicTest2-${UUID.randomUUID().toString}"
  val topic3 = s"topicTest3-${UUID.randomUUID().toString}"

  val topicsCreate = Seq(topic1, topic2)
  val topicsDelete = Seq(topic1, topic2, topic3)

  override def beforeAll(): Unit = {
    createTopics(topicsCreate)
    produceEvents(topicsCreate)
  }

  override def afterAll(): Unit = {
    resetTopics(topicsDelete)
  }

  "KafkaInputStep " should {
    "Read all the records" in {
      val topicsProp =
        s"""[
           |{
           |   "topic":"$topic1"
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
        "storeOfSerializationException: Unknown magic byte!fsetInKafka" -> "false",
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
          log.info(s"EVENTS COUNT in read all the records: $count")
          totalEvents.add(count)
        } else log.info("RDD is empty in read all the records")
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
           |   "topic":"$topic2"
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
        "value.deserializer.inputFormat" -> "STRING",
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
          log.info(s"EVENTS COUNT in read all the records storing offset in kafka: $count")
          totalEvents.add(count)
        } else log.info("RDD is empty in read all the records storing offset in kafka")
        log.info(s"TOTAL EVENTS : $totalEvents")
      })

      ssc.get.start() // Start the computation

      log.info("Started Streaming")

      ssc.get.awaitTerminationOrTimeout(SparkTimeOut)

      log.info("Finished Streaming")

      totalEvents.value should ===(totalRegisters.toLong)
    }

    "Read and deserialize the correct schema with Schema Registry with kafka (Local)" in {

      val hostPort =
        s"""[
           |{
           |    "port": "9092",
           |    "host": "$hosts"
           |}]
           | """.stripMargin

      val topicsProp =
        s"""[
           |{
           |   "topic":"$topic3"
           |}]
           | """.stripMargin

      val propsProducer = Map(
        "bootstrap.servers" -> hostPort.asInstanceOf[java.io.Serializable],
        "value.serializer.outputFormat" -> "SCHEMAREGISTRY",
        "value.serializer.schema.registry.url" -> schemaRegistry
      )

      log.info("Creating kafka Output step")

      log.info(s"Sending to Schema Registry url: $schemaRegistry")
      val output = new KafkaOutputStep("kafka1", sparkSession.get, propsProducer)

      val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
      val rows = Seq(
        new GenericRowWithSchema(Array("blue", 12.1), schema),
        new GenericRowWithSchema(Array("red", 12.2), schema)
      )
      val rdd = sc.get.parallelize(rows).asInstanceOf[RDD[Row]]
      val data = sparkSession.get.createDataFrame(rdd, schema)
      val saveOpts = Map("tableName" -> topic3)

      log.info("Send dataframe to kafka")

      output.save(data, SaveModeEnum.Append, saveOpts)

      output.cleanUp()

      val propsConsumer = Map(
        "value.deserializer.inputFormat" -> "SCHEMAREGISTRY",
        "bootstrap.servers" -> hostPort.asInstanceOf[java.io.Serializable],
        "topics" -> topicsProp.asInstanceOf[java.io.Serializable],
        "value.deserializer.schema.registry.url" -> schemaRegistry,
        "auto.offset.reset" -> "earliest"
      )

      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

      log.info("Creating kafka input step")

      val input = new KafkaInputStepStreaming("kafka2", outputOptions, ssc, sparkSession.get, propsConsumer)

      val distributedStream = input.init
      val totalEvents = ssc.get.sparkContext.accumulator(0L, "Number of events received")

      log.info("Evaluate the DStream")
      distributedStream.ds.foreachRDD(rdd => {
        if (!rdd.isEmpty()) {
          val count = rdd.count()
          log.info(s"EVENTS COUNT in read all the records storing offset in kafka: $count")
          totalEvents.add(count)
          val objects = rdd.collect()
          objects.foreach { row =>
            assert(rows.contains(row))
          }
        } else log.info("RDD is empty in read all the records storing offset in kafka")
        log.info(s"TOTAL EVENTS : $totalEvents")
      })

      ssc.get.start() // Start the computation
      log.info("Started Streaming")

      ssc.get.awaitTerminationOrTimeout(SparkTimeOut)
      log.info("Finished Streaming")

      totalEvents.value should ===(rows.size)
      input.cleanUp()

    }
  }
}
