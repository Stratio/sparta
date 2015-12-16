/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.plugin.parser.ingestion

import com.stratio.sparkta.sdk.{Event, Input}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class IngestionParserTest extends WordSpecLike with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  val ParserName = "IngestionParser"
  val ParserOrder = 1
  val InputField = Input.RawDataKey
  val OutputsFields = Seq("ColumnA", "ColumnB", "ColumnC", "ColumnD", "ColumnE", "ColumnF")
  val ParserConfig = Map(
    "ColumnA" -> "string",
    "ColumnB" -> "long",
    "ColumnC" -> "integer",
    "ColumnD" -> "float",
    "ColumnE" -> "double",
    "ColumnF" -> "datetime"
  )

  "A IngestionParser" should {
    "parse an event with an input that has the same columns that the schema specified in the config" in {
      val expectedEvent = Event(Map(
        "ColumnA" -> "columnAValue",
        "ColumnB" -> 1L,
        "ColumnC" -> 1,
        "ColumnD" -> 1F,
        "ColumnE" -> 1D
      ))

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
          |""".stripMargin

      val inputEvent = Event(Map(InputField -> json),None)
      val ingestionParser = new IngestionParser(ParserName, ParserOrder, InputField, OutputsFields, ParserConfig)
      val event = ingestionParser.parse(inputEvent)

      event should be eq(expectedEvent)
    }

    "parse an event with an input that has different number of columns that the schema specified in the config" in {
      val expectedEvent = Event(Map(
        "ColumnA" -> "columnAValue",
        "ColumnB" -> 1L
      ))

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
          |""".stripMargin

      val inputEvent = Event(Map(InputField -> json),None)
      val ingestionParser = new IngestionParser(ParserName, ParserOrder, InputField, OutputsFields, ParserConfig)
      val event = ingestionParser.parse(inputEvent)

      event should be eq(expectedEvent)
    }

    "parse an event with an input that has different number of columns that the schema specified in the config and " +
      "one of the column is not defined in the schema" in {
      val expectedEvent = Event(Map(
        "ColumnA" -> "columnAValue",
        "ColumnB" -> 1L
      ))
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
          |""".stripMargin

      val inputEvent = Event(Map(InputField -> json),None)
      val ingestionParser = new IngestionParser(ParserName, ParserOrder, InputField, OutputsFields, ParserConfig)
      val event = ingestionParser.parse(inputEvent)

      event should be eq(expectedEvent)
    }

    "throws an exception when a element is defined in the schema, but the type do not exists" in {
      val WrongParserConfig = Map(
        "ColumnA" -> "intugur")

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
          |""".stripMargin

      val inputEvent = Event(Map(InputField -> json),None)
      val ingestionParser = new IngestionParser(ParserName, ParserOrder, InputField, OutputsFields, WrongParserConfig)

      an[NoSuchElementException] should  be thrownBy(ingestionParser.parse(inputEvent))
    }
  }
}
