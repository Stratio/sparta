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
package com.stratio.sparta.plugin.transformation.ingestion

import java.io.{ByteArrayOutputStream, Serializable => JSerializable}
import java.util
import java.util.Date

import scala.collection.JavaConverters._
import org.apache.avro.Schema
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import com.stratio.decision.commons.avro._
import com.stratio.sparta.plugin.input.kafka.{KafkaDirectInput, KafkaInput}
import com.stratio.sparta.plugin.input.websocket.WebSocketInput
import com.stratio.sparta.sdk.pipeline.input.Input

@RunWith(classOf[JUnitRunner])
class IngestionParserTest extends WordSpecLike with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  val ParserName = "IngestionParser"
  val ParserOrder = 1
  val InputField = Input.RawDataKey
  val OutputsFields = Seq("ColumnA", "ColumnB", "ColumnC", "ColumnD", "ColumnE", "ColumnF")
  val validSchema = StructType(Seq(StructField(Input.RawDataKey, StringType),
    StructField("ColumnA", StringType),
    StructField("ColumnB", LongType),
    StructField("ColumnC", IntegerType),
    StructField("ColumnD", FloatType),
    StructField("ColumnE", DoubleType),
    StructField("ColumnF", DateType)))
  val ParserConfig = Map.empty[String, JSerializable]


  "A IngestionParser" should {


    "parse an event with an input that has the same columns that the schema specified in the config" in {
      val json =
        """
          |{
          |   "columns":[
          |      {
          |         "column":"ColumnA",
          |         "value":"columnAValue"
          |      },
          |      {
          |         "column":"ColumnB",
          |         "value":"1"
          |      },
          |      {
          |         "column":"ColumnC",
          |         "value":"1"
          |      },
          |      {
          |         "column":"ColumnD",
          |         "value":"1"
          |      },
          |      {
          |         "column":"ColumnE",
          |         "value":"1"
          |      }
          |   ]
          |}
          | """.stripMargin
      val columns = List(
        new ColumnType("ColumnA", "columnAValue", "STRING"),
        new ColumnType("ColumnB", "1", "LONG"),
        new ColumnType("ColumnC", "1", "INTEGER"),
        new ColumnType("ColumnD", "1", "FLOAT"),
        new ColumnType("ColumnE", "1", "DOUBLE")
      )
      val insertMessage: InsertMessage = new InsertMessage(
        "insert",
        "c_orders",
        "1454571270574",
        1454575187944L,
        columns.asJava,
        new util.ArrayList[Action]())
      val inputEvent = Row(serializeInsertMessageToAvro(insertMessage))
      val OutputsFields = Seq("ColumnA", "ColumnB", "ColumnC", "ColumnD", "ColumnE")
      val ingestionParser = new IngestionParser(ParserOrder, InputField, OutputsFields, validSchema, ParserConfig)
      val values = Seq("columnAValue", 1L, 1, 1F, 1D)

      val event = ingestionParser.parse(row = inputEvent, removeRaw = false)

      event.get should be eq Row.fromSeq(Seq(json) ++ event.toSeq)
      event.get.size should be (6)
      event.get should be eq Row.fromSeq(Seq(json) ++ values)
    }

    "parse an event with an input that has different number of columns that the schema specified in the config" in {
      val json =
        """
          |{
          |   "columns":[
          |      {
          |         "column":"ColumnA",
          |         "value":"columnAValue"
          |      },
          |      {
          |         "column":"ColumnB",
          |         "value":"1"
          |      }
          |   ]
          |}
          | """.stripMargin
      val columns = List(
        new ColumnType("ColumnA", "columnAValue", ""),
        new ColumnType("ColumnB", "1", "LONG")
      )
      val insertMessage: InsertMessage = new InsertMessage(
        "insert",
        "c_orders",
        "1454571270574",
        1454575187944L,
        columns.asJava,
        new util.ArrayList[Action]())
      val inputEvent = Row(serializeInsertMessageToAvro(insertMessage))
      val OutputsFields = Seq("ColumnA", "ColumnB")
      val ingestionParser = new IngestionParser(ParserOrder, InputField, OutputsFields, validSchema, ParserConfig)
      val event = ingestionParser.parse(inputEvent, false)
      val values = Seq("columnAValue", 1L)

      event.get should be eq Row.fromSeq(Seq(json) ++ event.toSeq)

      event.get.size should be (3)

      event.get should be eq Row.fromSeq(Seq(json) ++ values)
    }

    "parse an event with an input that has different number of columns that the schema specified in the config and " +
      "one of the column is not defined in the schema" in {
      val json =
        """
          |{
          |   "columns":[
          |      {
          |         "column":"ColumnA",
          |         "value":"columnAValue"
          |      },
          |      {
          |         "column":"ColumnNotDefined",
          |         "value":"1"
          |      }
          |   ]
          |}
          | """.stripMargin

      val columns = List(
        new ColumnType("ColumnA", "columnAValue", ""),
        new ColumnType("ColumnNotDefined", "1", "")
      )
      val insertMessage: InsertMessage = new InsertMessage(
        "insert",
        "c_orders",
        "1454571270574",
        1454575187944L,
        columns.asJava,
        new util.ArrayList[Action]())
      val inputEvent = Row(serializeInsertMessageToAvro(insertMessage))
      val OutputsFields = Seq("ColumnA")
      val ingestionParser = new IngestionParser(ParserOrder, InputField, OutputsFields, validSchema, ParserConfig)
      val event = ingestionParser.parse(inputEvent, false)
      val values = Seq("columnAValue")

      event.get should be eq Row.fromSeq(Seq(json) ++ event.toSeq)

      event.get.size should be (2)

      event.get should be eq Row.fromSeq(Seq(json) ++ values)
    }

    "parse an event with an input that has different number of columns that the schema specified in the config and " +
      "one of the column is not defined in the schema removing the raw data" in {
      val json =
        """
          |{
          |   "columns":[
          |      {
          |         "column":"ColumnA",
          |         "value":"columnAValue"
          |      },
          |      {
          |         "column":"ColumnNotDefined",
          |         "value":"1"
          |      }
          |   ]
          |}
          | """.stripMargin

      val columns = List(
        new ColumnType("ColumnA", "columnAValue", ""),
        new ColumnType("ColumnNotDefined", "1", "")
      )
      val insertMessage: InsertMessage = new InsertMessage(
        "insert",
        "c_orders",
        "1454571270574",
        1454575187944L,
        columns.asJava,
        new util.ArrayList[Action]())
      val inputEvent = Row(serializeInsertMessageToAvro(insertMessage))
      val OutputsFields = Seq("ColumnA")
      val ingestionParser = new IngestionParser(ParserOrder, InputField, OutputsFields, validSchema, ParserConfig)
      val event = ingestionParser.parse(inputEvent, true)
      val values = Seq("columnAValue")

      event.get should be eq Row.fromSeq(Seq(json) ++ event.toSeq)

      event.get.size should be (1)

      event.get should be eq Row.fromSeq(values)
    }

    "one exception was returned when element is not defined in the schema" in {
      val json =
        """
          |{
          |   "columns":[
          |      {
          |         "column":"ColumnA",
          |         "value":"columnAValue"
          |      }
          |   ]
          |}
          | """.stripMargin

      val columns = List(
        new ColumnType("ColumnA", "columnAValue", "")
      )
      val insertMessage: InsertMessage = new InsertMessage(
        "insert",
        "c_orders",
        "1454571270574",
        1454575187944L,
        columns.asJava,
        new util.ArrayList[Action]())
      val inputEvent = Row(serializeInsertMessageToAvro(insertMessage))
      val ingestionParser = new IngestionParser(ParserOrder, InputField, OutputsFields, validSchema, ParserConfig)

      an[RuntimeException] should be thrownBy ingestionParser.parse(inputEvent, true)
    }

    def serializeInsertMessageToAvro(insertMessage: InsertMessage): Array[Byte] = {
      val out = new ByteArrayOutputStream()
      // scalastyle:off
      val encoder = EncoderFactory.get.binaryEncoder(out, null)
      // scalastyle:on
      val Schema : Schema = new org.apache.avro.Schema.Parser()
        .parse("{\"type\":\"record\",\"name\":\"InsertMessage\",\"namespace\":\"com.stratio.decision.commons.avro\"," +
          "\"fields\":[{\"name\":\"operation\",\"type\":[\"null\",\"string\"],\"default\":\"null\"}," +
          "{\"name\":\"streamName\",\"type\":\"string\"},{\"name\":\"sessionId\",\"type\":[\"null\",\"string\"]," +
          "\"default\":\"null\"},{\"name\":\"timestamp\",\"type\":[\"null\",\"long\"],\"default\":\"null\"}," +
          "{\"name\":\"data\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"ColumnType\"," +
          "\"fields\":[{\"name\":\"column\",\"type\":\"string\"},{\"name\":\"value\",\"type\":[\"null\",\"string\"]," +
          "\"default\":\"null\"},{\"name\":\"type\",\"type\":[\"null\",\"string\"],\"default\":\"null\"}]}}}," +
          "{\"name\":\"actions\",\"type\":[\"null\",{\"type\":\"array\",\"items\":{\"type\":\"enum\"," +
          "\"name\":\"Action\",\"symbols\":[\"LISTEN\",\"SAVE_TO_CASSANDRA\",\"SAVE_TO_MONGO\",\"SAVE_TO_SOLR\"," +
          "\"SAVE_TO_ELASTICSEARCH\"]}}],\"default\":\"null\"}]}")
      val writer = new SpecificDatumWriter[InsertMessage](Schema)

      writer.write(insertMessage, encoder)
      encoder.flush
      out.close
      out.toByteArray
    }

  }
}
