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

package com.stratio.sparta.plugin.workflow.transformation.json

import java.io.{Serializable => JSerializable}

import com.jayway.jsonpath.PathNotFoundException
import com.stratio.sparta.sdk.workflow.enumerators.{SaveModeEnum, WhenError}
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class JsonPathTransformStepStreamTest extends WordSpecLike with Matchers {

  val inputField = "json"
  val schema = StructType(Seq(StructField(inputField, StringType)))
  val JSON =
    """{ "store": {
      |    "book": [
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
  "A JsonPathTransformStep" should {

    "parse json string" in {
      val schema = StructType(Seq(StructField("json", StringType)))
      val input = new GenericRowWithSchema(Array(JSON), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
      val queries =
        """[
          |{
          |   "field":"color",
          |   "query":"$.store.bicycle.color",
          |   "type":"string"
          |},
          |{
          |   "field":"price",
          |   "query":"$.store.bicycle.price",
          |   "type":"double"
          |}]
          | """.stripMargin

      val result = new JsonPathTransformStepStream(
        "json",
        outputOptions,
        null,
        null,
        Map("queries" -> queries.asInstanceOf[JSerializable], "inputField" -> "json",
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      ).parse(input)
      val expected = Seq(Row("red", 19.95))

      assertResult(expected)(result)
    }

    "parse json with raw" in {
      val schema = StructType(Seq(StructField("json", StringType)))
      val input = new GenericRowWithSchema(Array(JSON), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
      val queries =
        """[
          |{
          |   "field":"color",
          |   "query":"$.store.bicycle.color",
          |   "type":"string"
          |},
          |{
          |   "field":"price",
          |   "query":"$.store.bicycle.price",
          |   "type":"double"
          |}]
          | """.stripMargin

      val result = new JsonPathTransformStepStream(
        "json",
        outputOptions,
        null,
        null,
        Map("queries" -> queries.asInstanceOf[JSerializable],
          "fieldsPreservationPolicy" -> "APPEND",
          "inputField" -> "json")
      ).parse(input)
      val expected = Seq(Row(JSON, "red", 19.95))

      assertResult(expected)(result)
    }

    "not parse anything if the field does not match" in {
      val schema = StructType(Seq(StructField("wrong", StringType)))
      val input = new GenericRowWithSchema(Array(JSON), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
      val queries =
        """[
          |{
          |   "field":"color",
          |   "query":"$.store.bicycle.color",
          |   "type":"string"
          |},
          |{
          |   "field":"price",
          |   "query":"$.store.bicycle.price",
          |   "type":"double"
          |}]
          | """.stripMargin

      an[AssertionError] should be thrownBy new JsonPathTransformStepStream(
        "json",
        outputOptions,
        null,
        null,
        Map("queries" -> queries.asInstanceOf[JSerializable], "inputField" -> "json")
      ).parse(input)
    }

    "not parse when input is wrong" in {
      val schema = StructType(Seq(StructField("json", StringType)))
      val input = new GenericRowWithSchema(Array("{}"), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
      val queries =
        """[
          |{
          |   "field":"color",
          |   "query":"$.store.bicycle.color",
          |   "type":"string"
          |},
          |{
          |   "field":"price",
          |   "query":"$.store.bicycle.price",
          |   "type":"double"
          |}]
          | """.stripMargin

      an[Exception] should be thrownBy new JsonPathTransformStepStream(
        "json",
        outputOptions,
        null,
        null,
        Map("queries" -> queries.asInstanceOf[JSerializable], "inputField" -> "json")
      ).parse(input)
    }

    "parse when input is null" in {
      val JSON =
        """{ "store": {
          |    "bicycle": {
          |      "color": "red",
          |      "price": null
          |    }
          |  }
          |}""".stripMargin

      val schema = StructType(Seq(StructField("json", StringType)))
      val input = new GenericRowWithSchema(Array(JSON), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
      val queries =
        """[
          |{
          |   "field":"color",
          |   "query":"$.store.bicycle.color",
          |   "type":"string"
          |},
          |{
          |   "field":"price",
          |   "query":"$.store.bicycle.price",
          |   "type":"double"
          |}]
          | """.stripMargin

      val result = new JsonPathTransformStepStream(
        "json",
        outputOptions,
        null,
        null,
        Map("queries" -> queries.asInstanceOf[JSerializable],
          "whenError" -> WhenError.Null,
          "fieldsPreservationPolicy" -> "APPEND",
          "inputField" -> "json")
      ).parse(input)
      val expected = Seq(Row(JSON, "red", null))

      assertResult(expected)(result)
    }

    "parse when input is not found " in {
      val JSON =
        """{ "store": {
          |    "bicycle": {
          |      "color": "red"
          |    }
          |  }
          |}""".stripMargin

      val schema = StructType(Seq(StructField("json", StringType)))
      val input = new GenericRowWithSchema(Array(JSON), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
      val queries =
        """[
          |{
          |   "field":"color",
          |   "query":"$.store.bicycle.color",
          |   "type":"string"
          |},
          |{
          |   "field":"price",
          |   "query":"$.store.bicycle.price",
          |   "type":"double"
          |}]
          | """.stripMargin

      val result = new JsonPathTransformStepStream(
        "json",
        outputOptions,
        null,
        null,
        Map("queries" -> queries.asInstanceOf[JSerializable],
          "fieldsPreservationPolicy" -> "APPEND",
          "whenError" -> WhenError.Null,
          "inputField" -> "json")
      ).parse(input)
      val expected = Seq(Row(JSON, "red", null))

      assertResult(expected)(result)
    }

    "parse when input is not found and return is error or discard" in {
      val JSON =
        """{ "store": {
          |    "bicycle": {
          |      "color": "red"
          |    }
          |  }
          |}""".stripMargin

      val schema = StructType(Seq(StructField("json", StringType)))
      val input = new GenericRowWithSchema(Array(JSON), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
      val queries =
        """[
          |{
          |   "field":"color",
          |   "query":"$.store.bicycle.color",
          |   "type":"string"
          |},
          |{
          |   "field":"price",
          |   "query":"$.store.bicycle.price",
          |   "type":"double"
          |}]
          | """.stripMargin

      val transform = new JsonPathTransformStepStream(
        "json",
        outputOptions,
        null,
        null,
        Map("queries" -> queries.asInstanceOf[JSerializable],
          "whenError" -> WhenError.Error,
          "inputField" -> "json",
          "supportNullValues" -> false,
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      )

      an[PathNotFoundException] should be thrownBy transform.parse(input)
    }
  }
}