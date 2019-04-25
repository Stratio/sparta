/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.output.kafka

import java.util.UUID

import com.sksamuel.elastic4s.mappings.FieldType
import com.stratio.sparta.plugin.common.kafka.KafkaSuiteBase
import com.stratio.sparta.plugin.workflow.input.kafka.KafkaInputStepStreaming
import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.core.enumerators.SaveModeEnum
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class KafkaOutputStepIT extends KafkaSuiteBase {

  val topic1 = s"topicTest1-${UUID.randomUUID().toString}"
  val topic2 = s"topicTest2-${UUID.randomUUID().toString}"
  val topic3 = s"topicTest3-${UUID.randomUUID().toString}"
  val topic4 = s"topicTest4-${UUID.randomUUID().toString}"

  val topics = Seq(topic1, topic2, topic3, topic4)

  override def beforeAll(): Unit = {
    createTopics(topics)
  }

  override def afterAll(): Unit = {
    resetTopics(topics)
  }

  "KafkaOutputStep " should {

    "Send all the records in Binary format" in {
      val hostPort =
        s"""[
           |{
           |    "port": "9092",
           |    "host": "$hosts"
           |}]
           | """.stripMargin

      val props = Map(
        "bootstrap.servers" -> hostPort.asInstanceOf[java.io.Serializable],
        "value.serializer.outputFormat" -> "BINARY"
      )
      val output = new KafkaOutputStep("kafka1", sparkSession.get, props)
      val schema = StructType(Seq(StructField("price", DataTypes.ByteType)))
      val data1 = Seq(
        new GenericRowWithSchema(Array(12.toByte), schema),
        new GenericRowWithSchema(Array(15.toByte), schema)
      )
      val rdd = sc.get.parallelize(data1).asInstanceOf[RDD[Row]]
      val data = sparkSession.get.createDataFrame(rdd, schema)
      val saveOpts = Map("tableName" -> topic1)

      log.info("Send dataframe to kafka")

      output.save(data, SaveModeEnum.Append, saveOpts)

      output.cleanUp()

      val topicsProp =
        s"""[
           |{
           |   "topic":"${topic1}"
           |}]
           | """.stripMargin
      val propsConsumer = Map(
        "storeOffsetInKafka" -> "false",
        "value.deserializer.inputFormat" -> "BINARY",
        "bootstrap.servers" -> hostPort.asInstanceOf[java.io.Serializable],
        "topics" -> topicsProp.asInstanceOf[java.io.Serializable],
        "auto.offset.reset" -> "earliest"
      )
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
      val input = new KafkaInputStepStreaming("kafka", outputOptions, ssc, sparkSession.get, propsConsumer)
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
          streamingRegisters.foreach { row =>
            val elemToCheck = row.get(0).asInstanceOf[Array[Byte]].map(_.toChar).mkString
            assert(data1.map(_.toString()).contains(elemToCheck))
          }
      })

      ssc.get.start()

      log.info("Started Streaming")

      ssc.get.awaitTerminationOrTimeout(SparkTimeOut)

      log.info("Finished Streaming")

      totalEvents.value should ===(data1.size)
    }

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
      val saveOpts = Map("tableName" -> topic2)

      log.info("Send dataframe to kafka")

      output.save(data, SaveModeEnum.Append, saveOpts)

      output.cleanUp()

      val topicsProp =
        s"""[
           |{
           |   "topic":"$topic2"
           |}]
           | """.stripMargin
      val propsConsumer = Map(
        "storeOffsetInKafka" -> "false",
        "value.deserializer.inputFormat" -> "JSON",
        "bootstrap.servers" -> hostPort.asInstanceOf[java.io.Serializable],
        "topics" -> topicsProp.asInstanceOf[java.io.Serializable],
        "auto.offset.reset" -> "earliest"
      )
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
      val input = new KafkaInputStepStreaming("kafka", outputOptions, ssc, sparkSession.get, propsConsumer)
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
      val saveOpts = Map("tableName" -> topic3)

      log.info("Send dataframe to kafka")

      output.save(data, SaveModeEnum.Append, saveOpts)

      output.cleanUp()

      val topicsProp =
        s"""[
           |{
           |   "topic":"$topic3"
           |}]
           | """.stripMargin
      val propsConsumer = Map(
        "storeOffsetInKafka" -> "false",
        "value.deserializer.inputFormat" -> "STRING",
        "bootstrap.servers" -> hostPort.asInstanceOf[java.io.Serializable],
        "topics" -> topicsProp.asInstanceOf[java.io.Serializable],
        "auto.offset.reset" -> "earliest"
      )
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
      val input = new KafkaInputStepStreaming("kafka", outputOptions, ssc, sparkSession.get, propsConsumer)
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

    "Send all the records in Avro format using Schema Registry" in {
      val hostPort =
        s"""[
           |{
           |    "port": "9092",
           |    "host": "$hosts"
           |}]
           | """.stripMargin

      val props = Map(
        "bootstrap.servers" -> hostPort.asInstanceOf[java.io.Serializable],
        "value.serializer.outputFormat" -> "SCHEMAREGISTRY",
        "value.serializer.schema.registry.url" -> schemaRegistry
      )
      val output = new KafkaOutputStep("kafka10", sparkSession.get, props)


      val schema = StructType(Seq(StructField("color", StringType), StructField("price", DoubleType)))
      val rows = Seq(
        new GenericRowWithSchema(Array("blue", 12.1), schema),
        new GenericRowWithSchema(Array("red", 12.2), schema)
      )
      val rdd = sc.get.parallelize(rows).asInstanceOf[RDD[Row]]
      val data = sparkSession.get.createDataFrame(rdd, schema)
      val saveOpts = Map("tableName" -> topic4)

      log.info("Send dataframe to kafka")

      output.save(data, SaveModeEnum.Append, saveOpts)
      output.cleanUp()

      val topicsProp =
        s"""[
           |{
           |   "topic":"$topic4"
           |}]
           | """.stripMargin

      val propsConsumer = Map(
        "value.deserializer.inputFormat" -> "SCHEMAREGISTRY",
        "bootstrap.servers" -> hostPort.asInstanceOf[java.io.Serializable],
        "topics" -> topicsProp.asInstanceOf[java.io.Serializable],
        "value.deserializer.schema.registry.url" -> schemaRegistry,
        "auto.offset.reset" -> "earliest"
      )
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
      val input = new KafkaInputStepStreaming("kafka10", outputOptions, ssc, sparkSession.get, propsConsumer)
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

      ssc.get.start()
      log.info("Started Streaming")

      ssc.get.awaitTerminationOrTimeout(SparkTimeOut)
      log.info("Finished Streaming")

      totalEvents.value should ===(rows.size)

      input.cleanUp()

    }
  }
}