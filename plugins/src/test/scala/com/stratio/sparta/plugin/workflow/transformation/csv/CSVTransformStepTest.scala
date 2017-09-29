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
import org.scalatest.{Matchers, WordSpecLike}


class CSVTransformStepTest extends WordSpecLike
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
      val result = new CSVTransformStep(
        inputField,
        outputOptions,
        null,
        null,
        Map("fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "addAllInputFields" -> false)
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

      val result = new CSVTransformStep(
        inputField,
        outputOptions,
        null,
        null,
        Map("fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "addAllInputFields" -> true)
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

      an[IllegalArgumentException] should be thrownBy new CSVTransformStep(
        inputField,
        outputOptions,
        null,
        null,
        Map("fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "addAllInputFields" -> false)
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

      an[IllegalStateException] should be thrownBy new CSVTransformStep(
        inputField,
        outputOptions,
        null,
        null,
        Map("fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "addAllInputFields" -> false)
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

      an[Exception] should be thrownBy new CSVTransformStep(
        inputField,
        outputOptions,
        null,
        null,
        Map("fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "addAllInputFields" -> false)
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

      an[IllegalStateException] should be thrownBy new CSVTransformStep(
        inputField,
        outputOptions,
        null,
        null,
        Map("fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "addAllInputFields" -> false)
      ).parse(input)
    }
  }

}
