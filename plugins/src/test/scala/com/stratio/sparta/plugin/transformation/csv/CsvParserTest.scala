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

package com.stratio.sparta.plugin.transformation.csv

import java.io.{Serializable => JSerializable}

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class CsvParserTest extends WordSpecLike with Matchers {

  val inputField = Some("csv")
  val schema = StructType(Seq(
    StructField(inputField.get, StringType),
    StructField("color", StringType),
    StructField("price", DoubleType))
  )
  val CSV = "red,19.95"

  //scalastyle:off
  "A CsvParser" should {

    "parse CSV string" in {
      val input = Row(CSV)
      val outputsFields = Seq("color", "price")
      val fields =
        """[
          |{
          |   "name":"color"
          |},
          |{
          |   "name":"price"
          |}]
          |""".stripMargin
      val result = new CsvParser(
        1,
        inputField,
        outputsFields,
        schema,
        Map("fields" -> fields.asInstanceOf[JSerializable])
      ).parse(input, false)
      val expected = Option(Row(CSV, "red", 19.95))

      assertResult(result)(expected)
    }

    "parse CSV string removing raw" in {
      val input = Row(CSV)
      val outputsFields = Seq("color", "price")
      val fields =
        """[
          |{
          |   "name":"color"
          |},
          |{
          |   "name":"price"
          |}]
          |""".stripMargin
      val result = new CsvParser(
        1,
        inputField,
        outputsFields,
        schema,
        Map("fields" -> fields.asInstanceOf[JSerializable])
      ).parse(input, true)
      val expected = Option(Row("red", 19.95))

      assertResult(result)(expected)
    }

    "not parse anything if the field does not match" in {
      val input = Row(CSV)
      val schema = StructType(Seq(StructField("wrongfield", StringType)))
      val outputsFields = Seq("color", "price")
      val fields =
        """[
          |{
          |   "name":"color"
          |},
          |{
          |   "name":"price"
          |}]
          |""".stripMargin

      an[IllegalStateException] should be thrownBy new CsvParser(
        1,
        inputField,
        outputsFields,
        schema,
        Map("fields" -> fields.asInstanceOf[JSerializable])
      ).parse(input, false)
    }

    "not parse when input is wrong" in {
      val input = Row("{}")
      val outputsFields = Seq("color", "price")
      val fields =
        """[
          |{
          |   "name":"color"
          |},
          |{
          |   "name":"price"
          |}]
          |""".stripMargin

      an[Exception] should be thrownBy new CsvParser(
        1,
        inputField,
        outputsFields,
        schema,
        Map("fields" -> fields.asInstanceOf[JSerializable])
      ).parse(input, false)
    }
  }
}