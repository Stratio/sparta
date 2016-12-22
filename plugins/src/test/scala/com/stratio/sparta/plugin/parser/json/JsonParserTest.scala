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

package com.stratio.sparta.plugin.parser.json

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import java.io.{Serializable => JSerializable}

@RunWith(classOf[JUnitRunner])
class JsonParserTest extends WordSpecLike with Matchers {

  val inputField = "json"
  val schema = StructType(Seq(
    StructField(inputField, StringType),
    StructField("color", StringType),
    StructField("price", DoubleType))
  )
  val JSON = """{ "store": {
               |    "book": [
               |      { "category": "reference",
               |        "author": "Nigel Rees",
               |        "title": "Sayings of the Century",
               |        "price": 8.95
               |      },
               |      { "category": "fiction",
               |        "author": "Evelyn Waugh",
               |        "title": "Sword of Honour",
               |        "price": 12.99
               |      },
               |      { "category": "fiction",
               |        "author": "Herman Melville",
               |        "title": "Moby Dick",
               |        "isbn": "0-553-21311-3",
               |        "price": 8.99
               |      },
               |      { "category": "fiction",
               |        "author": "J. R. R. Tolkien",
               |        "title": "The Lord of the Rings",
               |        "isbn": "0-395-19395-8",
               |        "price": 22.99
               |      }
               |    ],
               |    "bicycle": {
               |      "color": "red",
               |      "price": 19.95
               |    }
               |  }
               |}""".stripMargin

  //scalastyle:off
  "A JsonParser" should {

    "parse json string" in {
      val input = Row(JSON)
      val outputsFields = Seq("color", "price")
      val queries =
        """[
          |{
          |   "field":"color",
          |   "query":"$.store.bicycle.color"
          |},
          |{
          |   "field":"price",
          |   "query":"$.store.bicycle.price"
          |}]
          |""".stripMargin
      val result = new JsonParser(
        1,
        inputField,
        outputsFields,
        schema,
        Map("queries" -> queries.asInstanceOf[JSerializable])
      ).parse(input, false)
      val expected = Row(JSON, "red", 19.95)

      assertResult(result)(expected)
    }

    "parse json string removing raw" in {
      val input = Row(JSON)
      val outputsFields = Seq("color", "price")
      val queries =
        """[
          |{
          |   "field":"color",
          |   "query":"$.store.bicycle.color"
          |},
          |{
          |   "field":"price",
          |   "query":"$.store.bicycle.price"
          |}]
          |""".stripMargin
      val result = new JsonParser(
        1,
        inputField,
        outputsFields,
        schema,
        Map("queries" -> queries.asInstanceOf[JSerializable])
      ).parse(input, true)
      val expected = Row("red", 19.95)

      assertResult(result)(expected)
    }

    "not parse anything if the field does not match" in {
      val input = Row(JSON)
      val schema = StructType(Seq(StructField("wrongfield", StringType)))
      val outputsFields = Seq("color", "price")
      val queries =
        """[
          |{
          |   "field":"color",
          |   "query":"$.store.bicycle.color"
          |},
          |{
          |   "field":"price",
          |   "query":"$.store.bicycle.price"
          |}]
          |""".stripMargin

      an[IllegalStateException] should be thrownBy new JsonParser(
        1,
        inputField,
        outputsFields,
        schema,
        Map("queries" -> queries.asInstanceOf[JSerializable])
      ).parse(input, false)
    }

    "not parse when input is wrong" in {
      val input = Row("{}")
      val outputsFields = Seq("color", "price")
      val queries =
        """[
          |{
          |   "field":"color",
          |   "query":"$.store.bicycle.color"
          |},
          |{
          |   "field":"price",
          |   "query":"$.store.bicycle.price"
          |}]
          |""".stripMargin

      an[Exception] should be thrownBy new JsonParser(
        1,
        inputField,
        outputsFields,
        schema,
        Map("queries" -> queries.asInstanceOf[JSerializable])
      ).parse(input, false)
    }
  }
}