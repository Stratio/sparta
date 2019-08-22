/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.json

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.core.models.{OutputOptions, OutputWriterOptions, TransformationStepManagement}
import com.stratio.sparta.core.enumerators.{SaveModeEnum, WhenError, WhenFieldError}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class JsonPathTransformStepStreamingTest extends WordSpecLike with Matchers {

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
      val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
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

      val result = new JsonPathTransformStepStreaming(
        "json",
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("queries" -> queries.asInstanceOf[JSerializable], "inputField" -> "json",
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      ).generateNewRow(input)
      val expected = Row("red", 19.95)

      assertResult(expected)(result)
    }

    "parse json with raw" in {
      val schema = StructType(Seq(StructField("json", StringType)))
      val input = new GenericRowWithSchema(Array(JSON), schema)
      val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
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

      val result = new JsonPathTransformStepStreaming(
        "json",
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("queries" -> queries.asInstanceOf[JSerializable],
          "fieldsPreservationPolicy" -> "APPEND",
          "inputField" -> "json")
      ).generateNewRow(input)
      val expected = Row(JSON, "red", 19.95)

      assertResult(expected)(result)
    }

    "not parse anything if the field does not match" in {
      val schema = StructType(Seq(StructField("wrong", StringType)))
      val input = new GenericRowWithSchema(Array(JSON), schema)
      val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
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

      an[AssertionError] should be thrownBy new JsonPathTransformStepStreaming(
        "json",
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("queries" -> queries.asInstanceOf[JSerializable], "inputField" -> "json")
      ).generateNewRow(input)
    }

    "not parse when input is wrong" in {
      val schema = StructType(Seq(StructField("json", StringType)))
      val input = new GenericRowWithSchema(Array("{}"), schema)
      val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
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

      an[Exception] should be thrownBy new JsonPathTransformStepStreaming(
        "json",
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("queries" -> queries.asInstanceOf[JSerializable], "inputField" -> "json")
      ).generateNewRow(input)
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
      val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
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

      val result = new JsonPathTransformStepStreaming(
        "json",
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("queries" -> queries.asInstanceOf[JSerializable],
          "whenFieldError" -> WhenFieldError.Null,
          "fieldsPreservationPolicy" -> "APPEND",
          "inputField" -> "json")
      ).generateNewRow(input)
      val expected = Row(JSON, "red", null)

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
      val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
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

      val result = new JsonPathTransformStepStreaming(
        "json",
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("queries" -> queries.asInstanceOf[JSerializable],
          "fieldsPreservationPolicy" -> "APPEND",
          "whenFieldError" -> WhenFieldError.Null,
          "inputField" -> "json")
      ).generateNewRow(input)
      val expected = Row(JSON, "red", null)

      assertResult(expected)(result)
    }

    "parse when input is not found and return is error" in {
      val JSON =
        """{ "store": {
          |    "bicycle": {
          |      "color": "red"
          |    }
          |  }
          |}""".stripMargin

      val schema = StructType(Seq(StructField("json", StringType)))
      val input = new GenericRowWithSchema(Array(JSON), schema)
      val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
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

      val transform = new JsonPathTransformStepStreaming(
        "json",
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("queries" -> queries.asInstanceOf[JSerializable],
          "whenFieldError" -> WhenFieldError.FieldError,
          "inputField" -> "json",
          "supportNullValues" -> false,
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      )

      an[Exception] should be thrownBy transform.generateNewRow(input)
    }

    "parse when input is not found and return null" in {
      val JSON =
        """{ "store": {
          |    "bicycle": {
          |      "color": "red"
          |    }
          |  }
          |}""".stripMargin

      val schema = StructType(Seq(StructField("json", StringType)))
      val input = new GenericRowWithSchema(Array(JSON), schema)
      val outputOptions = OutputWriterOptions.defaultOutputOptions("stepName", None, Option("tableName"))
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

      val transform = new JsonPathTransformStepStreaming(
        "json",
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("queries" -> queries.asInstanceOf[JSerializable],
          "whenFieldError" -> WhenFieldError.Null,
          "inputField" -> "json",
          "supportNullValues" -> false,
          "fieldsPreservationPolicy" -> "APPEND")
      )
      val result = transform.generateNewRow(input)
      val expected = Row(JSON, "red", null)

      assertResult(expected)(result)

    }
  }
}