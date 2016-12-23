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

package com.stratio.sparta.plugin.transformation.xpath

import java.io.{Serializable => JSerializable}

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{BooleanType, StringType, StructField, StructType}
import org.scalatest._

class XPathParserTest extends WordSpecLike with Matchers {


  val inputField = "json"
  val schema = StructType(Seq(
    StructField(inputField, StringType),
    StructField("id", StringType),
    StructField("enabled", BooleanType))
  )
  val XML = """
               |<root>
               |    <element id="1" enabled="true"/>
               |    <element id="2" enabled="false"/>
               |    <element id="3" enabled="true"/>
               |    <element id="4" enabled="false"/>
               |</root>
             """.stripMargin

  //scalastyle:off
  
  
  "A XPathParser" should {

    "parse xml string" in {
      val input = Row(XML)
      val outputsFields = Seq("id", "enabled")
      val queries =
        """[
          |{
          |   "field":"id",
          |   "query":"//element/@id"
          |},
          |{
          |   "field":"enabled",
          |   "query":"//element/@enabled"
          |}]
          |""".stripMargin

      val result = new XPathParser(
        1,
        inputField,
        outputsFields,
        schema,
        Map("queries" -> queries.asInstanceOf[JSerializable])
      ).parse(input, false)
      val expected = Option(Row(XML, "1", true))

      assertResult(result)(expected)
    }

    "parse xml string removing raw" in {
      val input = Row(XML)
      val outputsFields = Seq("id", "enabled")
      val queries =
        """[
          |{
          |   "field":"id",
          |   "query":"//element/@id"
          |},
          |{
          |   "field":"enabled",
          |   "query":"//element/@enabled"
          |}]
          |""".stripMargin

      val result = new XPathParser(
        1,
        inputField,
        outputsFields,
        schema,
        Map("queries" -> queries.asInstanceOf[JSerializable])
      ).parse(input, true)

      val expected = Option(Row("1", true))

      assertResult(result)(expected)
    }

    "not parse anything if the field does not match" in {
      val input = Row(XML)
      val schema = StructType(Seq(StructField("wrongfield", StringType)))
      val outputsFields = Seq("id", "enabled")
      val queries =
        """[
          |{
          |   "field":"id",
          |   "query":"//element/@id"
          |},
          |{
          |   "field":"enabled",
          |   "query":"//element/@enabled"
          |}]
          |""".stripMargin

      an[IllegalStateException] should be thrownBy new XPathParser(
        1,
        inputField,
        outputsFields,
        schema,
        Map("queries" -> queries.asInstanceOf[JSerializable])
      ).parse(input, false)
    }

    "not parse when input is wrong" in {
      val input = Row("{}")
      val outputsFields = Seq("id", "enabled")
      val queries =
        """[
          |{
          |   "field":"id",
          |   "query":"//element/@id"
          |},
          |{
          |   "field":"enabled",
          |   "query":"//element/@enabled"
          |}]
          |""".stripMargin

      an[Exception] should be thrownBy new XPathParser(
        1,
        inputField,
        outputsFields,
        schema,
        Map("queries" -> queries.asInstanceOf[JSerializable])
      ).parse(input, false)
    }
  }

}
