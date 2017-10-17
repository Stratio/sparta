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

package com.stratio.sparta.plugin.workflow.transformation.csv

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class CsvTransformStepTest extends WordSpecLike
  with Matchers{

  val inputField = "csv"
  val schema = StructType(Seq(StructField(inputField, StringType)))
  val CSV = "red,19.95"

  //scalastyle:off
  "A CSV transformation" should {
    "parse a CSV string" in {
       val fields =
         """[
           |{
           |   "name":"color"
           |},
           |{
           |   "name":"price"
           |}]
           |""".stripMargin

      val input = new GenericRowWithSchema(Array(CSV), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
      val result = new CsvTransformStep(
        inputField,
        outputOptions,
        null,
        null,
        Map("schema.fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "schema.inputMode" -> "FIELDS",
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      ).parse(input)

      val expected = Seq(Row("red", 19.95))
      expected should be eq result
    }

    "parse a CSV string adding also the input fields" in {
      val input = new GenericRowWithSchema(Array(CSV), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tablename", None, None)
      val fields =
        """[
          |{
          |   "name":"color"
          |},
          |{
          |   "name":"price"
          |}]
          |""".stripMargin

      val result = new CsvTransformStep(
        inputField,
        outputOptions,
        null,
        null,
        Map("schema.fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "schema.inputMode" -> "FIELDS",
          "fieldsPreservationPolicy" -> "APPEND")
      ).parse(input)

      val expected = Seq(Row(CSV,"red", 19.95))
      expected should be eq result
    }

    "parse a CSV string adding also the input fields with header schema" in {
      val input = new GenericRowWithSchema(Array(CSV), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tablename", None, None)
      val header = "name,price"
      val result = new CsvTransformStep(
        inputField,
        outputOptions,
        null,
        null,
        Map("schema.header" -> header,
          "inputField" -> inputField,
          "schema.inputMode" -> "HEADER",
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      ).parse(input)

      val expected = Seq(Row(CSV,"red", "19.95"))
      expected should be eq result
    }

    "parse a CSV string adding the input fields with header schema and change the input field" in {
      val input = new GenericRowWithSchema(Array("var", CSV),
        StructType(Seq(StructField("foo", StringType), StructField(inputField, StringType))))
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tablename", None, None)
      val header = "name,price"
      val result = new CsvTransformStep(
        inputField,
        outputOptions,
        null,
        null,
        Map("schema.header" -> header,
          "inputField" -> inputField,
          "schema.inputMode" -> "HEADER",
          "fieldsPreservationPolicy" -> "REPLACE")
      ).parse(input)

      val expected = Seq(Row("var", "red", "19.95"))
      expected should be eq result
    }

    "parse a CSV string adding also the input fields with spark schema" in {
      val input = new GenericRowWithSchema(Array(CSV), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tablename", None, None)
      val sparkSchema = "StructType((StructField(name,StringType,true),StructField(age,DoubleType,true)))"
      val result = new CsvTransformStep(
        inputField,
        outputOptions,
        null,
        null,
        Map("schema.sparkSchema" -> sparkSchema,
          "inputField" -> inputField,
          "schema.inputMode" -> "SPARKFORMAT",
          "fieldsPreservationPolicy" -> "APPEND")
      ).parse(input)

      val expected = Seq(Row(CSV,"red", 19.95))
      expected should be eq result
    }

    "not parse anything if a field does not match" in {
      val schema = StructType(Seq(StructField("wrongField", StringType)))
      val input = new GenericRowWithSchema(Array(CSV), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
      val fields =
        """[
          |{
          |   "name":"color"
          |},
          |{
          |   "name":"price"
          |}]
          |""".stripMargin

      an[Exception] should be thrownBy new CsvTransformStep(
        inputField,
        outputOptions,
        null,
        null,
        Map("schema.fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "schema.inputMode" -> "FIELDS",
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      ).parse(input)
    }

    "not parse anything if the number of fields do not match with the values parsed" in {
      val input = new GenericRowWithSchema(Array(CSV), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
      val fields =
        """[
          |{
          |   "name":"color"
          |},
          |{
          |   "name":"price"
          |},
          |{
          |   "name":"quantity"
          |}]
          |""".stripMargin

      an[Exception] should be thrownBy new CsvTransformStep(
        inputField,
        outputOptions,
        null,
        null,
        Map("schema.fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "schema.inputMode" -> "FIELDS",
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      ).parse(input)
    }

    "not parse anything if the input is wrong" in {
      val input = Row("{}")
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
      val fields =
        """[
          |{
          |   "name":"color"
          |},
          |{
          |   "name":"price"
          |}]
          |""".stripMargin

      an[Exception] should be thrownBy new CsvTransformStep(
        inputField,
        outputOptions,
        null,
        null,
        Map("schema.fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "schema.inputMode" -> "FIELDS",
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      ).parse(input)
    }

    "not parse anything if the values parsed are greated than the fields stated" in {
      val CSV = "red,19.95,3"
      val input = new GenericRowWithSchema(Array(CSV), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
      val fields =
        """[
          |{
          |   "name":"color"
          |},
          |{
          |   "name":"price"
          |}]
          |""".stripMargin

      an[Exception] should be thrownBy new CsvTransformStep(
        inputField,
        outputOptions,
        null,
        null,
        Map("schema.fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "schema.inputMode" -> "FIELDS",
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      ).parse(input)
    }
  }

}
