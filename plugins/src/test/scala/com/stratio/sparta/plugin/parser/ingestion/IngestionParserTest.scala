/**
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
package com.stratio.sparta.plugin.parser.ingestion

import com.stratio.sparta.plugin.parser.ingestion.IngestionParser
import com.stratio.sparta.sdk.Input
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import java.io.{Serializable => JSerializable}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class IngestionParserTest extends WordSpecLike with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  val ParserName = "IngestionParser"
  val ParserOrder = 1
  val InputField = Input.RawDataKey
  val OutputsFields = Seq("ColumnA", "ColumnB", "ColumnC", "ColumnD", "ColumnE", "ColumnF")
  val wrongSchema = StructType(Seq(StructField(Input.RawDataKey, StringType)))
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

      val inputEvent = Row(json)
      val ingestionParser = new IngestionParser(ParserOrder, InputField, OutputsFields, validSchema, ParserConfig)
      val event = ingestionParser.parse(inputEvent, false)
      val values = Seq("columnAValue", 1L, 1, 1F, 1D)

      event should be eq Row.fromSeq(Seq(json) ++ event.toSeq)

      event.size should be (6)

      event should be eq Row.fromSeq(Seq(json) ++ values)
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

      val inputEvent = Row(json)
      val ingestionParser = new IngestionParser(ParserOrder, InputField, OutputsFields, validSchema, ParserConfig)
      val event = ingestionParser.parse(inputEvent, false)
      val values = Seq("columnAValue", 1L)

      event should be eq Row.fromSeq(Seq(json) ++ event.toSeq)

      event.size should be (3)

      event should be eq Row.fromSeq(Seq(json) ++ values)
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

      val inputEvent = Row(json)
      val ingestionParser = new IngestionParser(ParserOrder, InputField, OutputsFields, validSchema, ParserConfig)
      val event = ingestionParser.parse(inputEvent, false)
      val values = Seq("columnAValue")

      event should be eq Row.fromSeq(Seq(json) ++ event.toSeq)

      event.size should be (2)

      event should be eq Row.fromSeq(Seq(json) ++ values)
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

      val inputEvent = Row(json)
      val ingestionParser = new IngestionParser(ParserOrder, InputField, OutputsFields, validSchema, ParserConfig)
      val event = ingestionParser.parse(inputEvent, true)
      val values = Seq("columnAValue")

      event should be eq Row.fromSeq(Seq(json) ++ event.toSeq)

      event.size should be (1)

      event should be eq Row.fromSeq(values)
    }

    "empty row returned when element is not defined in the schema" in {
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

      val inputEvent = Row(json)
      val ingestionParser = new IngestionParser(ParserOrder, InputField, OutputsFields, wrongSchema, ParserConfig)
      val event = ingestionParser.parse(inputEvent, true)

      event should be eq Row.fromSeq(Seq())

      event.size should be (0)
    }
  }
}
