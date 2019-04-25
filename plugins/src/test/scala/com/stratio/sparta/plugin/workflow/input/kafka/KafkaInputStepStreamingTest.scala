/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.input.kafka


import com.stratio.sparta.core.models.OutputOptions
import com.stratio.sparta.core.properties.JsoneyString
import com.stratio.sparta.core.enumerators.SaveModeEnum
import org.apache.kafka.clients.consumer.{RangeAssignor, RoundRobinAssignor}
import org.apache.kafka.common.TopicPartition
import org.apache.spark.sql.crossdata.XDSession
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.kafka010.LocationStrategies
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class KafkaInputStepStreamingTest extends WordSpec with Matchers with MockitoSugar {

  val ssc = mock[StreamingContext]
  val xdSession = mock[XDSession]
  val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)

  "KafkaInputStep" should {
    "return a tuples (topic,partition)" in {
      val topics =
        """[
          |{
          |   "topic":"test"
          |}
          |]
          |""".stripMargin

      val properties = Map("topics" -> JsoneyString(topics))
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      input.extractTopics should be(Set("test"))
    }

    "return a sequence of tuples (topic,partition)" in {
      val topics =
        """[
          |{"topic":"test"},{"topic":"test2"},{"topic":"test3"}
          |]
          |""".stripMargin
      val properties = Map("topics" -> JsoneyString(topics))
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      input.extractTopics should be(Set("test", "test2", "test3"))
    }

    "return a group id" in {
      val properties = Map("group.id" -> "test", "foo" -> "var")
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getGroupId
      result should be(Map("group.id" -> "test"))
    }

    "return a sequence of offsets" in {
      val offsets =
        """[
          |{
          | "topic":"test",
          | "partition":1,
          | "offsetValue":23
          | }
          |]
          |""".stripMargin
      val properties = Map("offsets" -> JsoneyString(offsets))
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getOffsets
      result should be(Map(new TopicPartition("test", 1) -> 23))
    }

    "return a sequence of offsets empty when is wrong" in {
      val offsets =
        """[
          |{
          | "topic":"test",
          | "offsetValue":23,
          | "foo": {"var": "a"}
          | }
          |]
          |""".stripMargin
      val properties = Map("offsets" -> JsoneyString(offsets))
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getOffsets
      result should be(Map.empty[TopicPartition, Long])
    }

    "return row serializer properties" in {
      val properties = Map("value.deserializer.inputFormat" -> "JSON",
        "value.deserializer.json.schema.fromRow" -> "true",
        "value.deserializer.json.schema.inputMode" -> "SPARKFORMAT",
        "value.deserializer.json.schema.provided" -> "",
        "value.deserializer.avro.schema" -> "",
        "outputField" -> "rawTest",
        "key.deserializer.json.foo" -> "var",
        "test" -> "notinclude")
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getRowSerializerProperties
      result should be(Map("outputField" -> "rawTest",
        "value.deserializer.outputField" -> "rawTest",
        "value.deserializer.json.schema.inputMode" -> "SPARKFORMAT",
        "value.deserializer.avro.schema" -> "",
        "value.deserializer.inputFormat" -> "JSON",
        "json.foo" -> "var",
        "json.schema.fromRow" -> "true",
        "key.deserializer.json.foo" -> "var",
        "inputFormat" -> "JSON", "avro.schema" -> "",
        "json.schema.inputMode" -> "SPARKFORMAT",
        "value.deserializer.json.schema.fromRow" -> "true",
        "value.deserializer.json.schema.provided" -> "",
        "json.schema.provided" -> ""
      ))
    }

    "return AutoOffset" in {
      val properties = Map("auto.offset.reset" -> "smalest")
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getAutoOffset
      result should be(Map("auto.offset.reset" -> "smalest"))
    }

    "return AutoOffset default" in {
      val properties = Map("foo" -> "var")
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getAutoOffset
      result should be(Map("auto.offset.reset" -> "latest"))
    }

    "return AutoCommit" in {
      val properties = Map("enable.auto.commit" -> "true")
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getAutoCommit
      result should be(Map("enable.auto.commit" -> true))
    }

    "return AutoCommit default" in {
      val properties = Map("foo" -> "var")
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getAutoCommit
      result should be(Map("enable.auto.commit" -> false))
    }

    "return AutoCommitInKafka" in {
      val properties = Map("storeOffsetInKafka" -> "false")
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getAutoCommitInKafka
      result should be(false)
    }

    "return AutoCommitInKafka default" in {
      val properties = Map("foo" -> "var")
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getAutoCommitInKafka
      result should be(true)
    }

    "return LocationStrategy brokers" in {
      val properties = Map("locationStrategy" -> "preferbrokers")
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getLocationStrategy
      result should be(LocationStrategies.PreferBrokers)
    }

    "return LocationStrategy consistent" in {
      val properties = Map("locationStrategy" -> "preferconsistent")
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getLocationStrategy
      result should be(LocationStrategies.PreferConsistent)
    }

    "return LocationStrategy default" in {
      val properties = Map("foo" -> "var")
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getLocationStrategy
      result should be(LocationStrategies.PreferConsistent)
    }

    "return PartitionStrategy range" in {
      val properties = Map("partition.assignment.strategy" -> "range")
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getPartitionStrategy
      result should be(Map("partition.assignment.strategy" -> classOf[RangeAssignor].getCanonicalName))
    }

    "return PartitionStrategy roundrobin" in {
      val properties = Map("partition.assignment.strategy" -> "roundrobin")
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getPartitionStrategy
      result should be(Map("partition.assignment.strategy" -> classOf[RoundRobinAssignor].getCanonicalName))
    }

    "return PartitionStrategy default" in {
      val properties = Map("foo" -> "var")
      val input = new KafkaInputStepStreaming("name", outputOptions, Option(ssc), xdSession, properties)
      val result = input.getPartitionStrategy
      result should be(Map("partition.assignment.strategy" -> classOf[RangeAssignor].getCanonicalName))
    }
  }
}
