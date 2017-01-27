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

package com.stratio.sparta.plugin.transformation.filter

import java.io.{Serializable => JSerializable}

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class FilterParserTest extends WordSpecLike with Matchers {

  val schema = StructType(Seq(
    StructField("color", StringType),
    StructField("price", DoubleType))
  )
  val whiteValues = Seq("white", 5.0)
  val whiteRow = Row.fromSeq(whiteValues)
  val blackRow = Row.fromSeq(Seq("black", 5.0))
  val tenRow = Row.fromSeq(Seq("white", 10.0))


  //scalastyle:off
  "A FilterParser" should {

    "parse filter string adding output fields" in {
      val input = whiteRow
      val outputsFields = Seq()
      val queries =
        """[
          |{
          |   "field":"color",
          |   "type":"=",
          |   "value":"white"
          |}
          |]
          |""".stripMargin

      val result = new FilterParser(
        1,
        None,
        outputsFields,
        schema,
        Map("filters" -> queries.asInstanceOf[JSerializable])
      ).parse(input)
      val expected = Seq(Row.fromSeq(whiteValues))

      assertResult(result)(expected)
    }

    "parse filter string removing output fields" in {
      val input = whiteRow
      val outputsFields = Seq()
      val queries =
        """[
          |{
          |   "field":"color",
          |   "type":"=",
          |   "value":"white"
          |}
          |]
          |""".stripMargin

      val result = new FilterParser(
        1,
        None,
        outputsFields,
        schema,
        Map("filters" -> queries.asInstanceOf[JSerializable])
      ).parse(input)
      val expected = Seq(Row.fromSeq(whiteValues))

      assertResult(result)(expected)
    }

    "parse filter double adding output fields" in {
      val input = whiteRow
      val outputsFields = Seq()
      val queries =
        """[
          |{
          |   "field":"price",
          |   "type":">",
          |   "value":4.0
          |}
          |]
          |""".stripMargin

      val result = new FilterParser(
        1,
        None,
        outputsFields,
        schema,
        Map("filters" -> queries.asInstanceOf[JSerializable])
      ).parse(input)
      val expected = Seq(Row.fromSeq(whiteValues))

      assertResult(result)(expected)
    }

    "parse filter double discarding row" in {
      val input = whiteRow
      val outputsFields = Seq("color", "price")
      val queries =
        """[
          |{
          |   "field":"price",
          |   "type":">",
          |   "value":6.0
          |}
          |]
          |""".stripMargin

      val result = new FilterParser(
        1,
        None,
        outputsFields,
        schema,
        Map("filters" -> queries.asInstanceOf[JSerializable])
      ).parse(input)
      val expected = Seq.empty

      assertResult(result)(expected)
    }

    "parse filter discarding row with two filters" in {
      val input = whiteRow
      val outputsFields = Seq("color", "price")
      val queries =
        """[
          |{
          |   "field":"price",
          |   "type":">",
          |   "value":6.0
          |},
          |{
          |   "field":"color",
          |   "type":"=",
          |   "value":"white"
          |}
          |]
          |""".stripMargin

      val result = new FilterParser(
        1,
        None,
        outputsFields,
        schema,
        Map("filters" -> queries.asInstanceOf[JSerializable])
      ).parse(input)
      val expected = Seq.empty

      assertResult(result)(expected)
    }

    "parse filter row with two filters" in {
      val input = whiteRow
      val outputsFields = Seq("color", "price")
      val queries =
        """[
          |{
          |   "field":"price",
          |   "type":"<",
          |   "value":6.0
          |},
          |{
          |   "field":"color",
          |   "type":"!=",
          |   "value":"black"
          |}
          |]
          |""".stripMargin

      val result = new FilterParser(
        1,
        None,
        outputsFields,
        schema,
        Map("filters" -> queries.asInstanceOf[JSerializable])
      ).parse(input)
      val expected = Seq(Row.fromSeq(whiteValues))

      assertResult(result)(expected)
    }
  }
}