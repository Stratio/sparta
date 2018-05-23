/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.plugin.workflow.transformation.csv

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.models.{OutputOptions, TransformationStepManagement}
import com.stratio.sparta.sdk.enumerators.SaveModeEnum
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
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
      val result = new CsvTransformStepStreaming(
        inputField,
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("schema.fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "schema.inputMode" -> "FIELDS",
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      ).generateNewRow(input)

      val expected = Option(Row("red", 19.95))
      expected should be eq result
    }

    "parse a CSV string adding also the input fields" in {
      val input = new GenericRowWithSchema(Array(CSV), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
      val fields =
        """[
          |{
          |   "name":"color"
          |},
          |{
          |   "name":"price"
          |}]
          |""".stripMargin

      val result = new CsvTransformStepStreaming(
        inputField,
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("schema.fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "schema.inputMode" -> "FIELDS",
          "fieldsPreservationPolicy" -> "APPEND")
      ).generateNewRow(input)

      val expected = Option(Row(CSV,"red", 19.95))
      expected should be eq result
    }

    "parse a CSV string adding also the input fields with header schema" in {
      val input = new GenericRowWithSchema(Array(CSV), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
      val header = "name,price"
      val result = new CsvTransformStepStreaming(
        inputField,
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("schema.header" -> header,
          "inputField" -> inputField,
          "schema.inputMode" -> "HEADER",
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      ).generateNewRow(input)

      val expected = Option(Row(CSV,"red", "19.95"))
      expected should be eq result
    }

    "parse a CSV string adding the input fields with header schema and change the input field" in {
      val input = new GenericRowWithSchema(Array("var", CSV),
        StructType(Seq(StructField("foo", StringType), StructField(inputField, StringType))))
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
      val header = "name,price"
      val result = new CsvTransformStepStreaming(
        inputField,
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("schema.header" -> header,
          "inputField" -> inputField,
          "schema.inputMode" -> "HEADER",
          "fieldsPreservationPolicy" -> "REPLACE")
      ).generateNewRow(input)

      val expected = Option(Row("var", "red", "19.95"))
      expected should be eq result
    }

    "parse a CSV string adding the input fields with header schema and change the input field with null values" in {
      val input = new GenericRowWithSchema(Array("var", "red,19.95,,var"),
        StructType(Seq(StructField("foo", StringType), StructField(inputField, StringType))))
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
      val header = "name,price,age,address"
      val result = new CsvTransformStepStreaming(
        inputField,
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("schema.header" -> header,
          "inputField" -> inputField,
          "schema.inputMode" -> "HEADER",
          "fieldsPreservationPolicy" -> "REPLACE")
      ).generateNewRow(input)

      val expected = Option(Row("var", "red", "19.95", null, "var"))
      expected should be eq result
    }

    "parse a CSV string adding also the input fields with spark schema" in {
      val input = new GenericRowWithSchema(Array(CSV), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
      val sparkSchema = "StructType((StructField(name,StringType,true),StructField(age,DoubleType,true)))"
      val result = new CsvTransformStepStreaming(
        inputField,
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("schema.sparkSchema" -> sparkSchema,
          "inputField" -> inputField,
          "schema.inputMode" -> "SPARKFORMAT",
          "fieldsPreservationPolicy" -> "APPEND")
      ).generateNewRow(input)

      val expected = Option(Row(CSV,"red", 19.95))
      expected should be eq result
    }

    "not parse anything if a field does not match" in {
      val schema = StructType(Seq(StructField("wrongField", StringType)))
      val input = new GenericRowWithSchema(Array(CSV), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
      val fields =
        """[
          |{
          |   "name":"color"
          |},
          |{
          |   "name":"price"
          |}]
          |""".stripMargin

      an[Exception] should be thrownBy new CsvTransformStepStreaming(
        inputField,
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("schema.fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "schema.inputMode" -> "FIELDS",
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      ).generateNewRow(input)
    }

    "not parse anything if the number of fields do not match with the values parsed" in {
      val input = new GenericRowWithSchema(Array(CSV), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
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

      an[Exception] should be thrownBy new CsvTransformStepStreaming(
        inputField,
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("schema.fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "schema.inputMode" -> "FIELDS",
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      ).generateNewRow(input)
    }

    "not parse anything if the input is wrong" in {
      val input = Row("{}")
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
      val fields =
        """[
          |{
          |   "name":"color"
          |},
          |{
          |   "name":"price"
          |}]
          |""".stripMargin

      an[Exception] should be thrownBy new CsvTransformStepStreaming(
        inputField,
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("schema.fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "schema.inputMode" -> "FIELDS",
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      ).generateNewRow(input)
    }

    "not parse anything if the values parsed are greated than the fields stated" in {
      val CSV = "red,19.95,3"
      val input = new GenericRowWithSchema(Array(CSV), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
      val fields =
        """[
          |{
          |   "name":"color"
          |},
          |{
          |   "name":"price"
          |}]
          |""".stripMargin

      an[Exception] should be thrownBy new CsvTransformStepStreaming(
        inputField,
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("schema.fields" -> fields.asInstanceOf[JSerializable],
          "inputField" -> inputField,
          "schema.inputMode" -> "FIELDS",
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      ).generateNewRow(input)
    }

    "discard the header if specified so" in {
      val header = "color,price"
      val input = new GenericRowWithSchema(Array(header), schema)
      val outputOptions = OutputOptions(SaveModeEnum.Append, "stepName", "tableName", None, None)
      val result = new CsvTransformStepStreaming(
        inputField,
        outputOptions,
        TransformationStepManagement(),
        null,
        null,
        Map("schema.header" -> header,
          "headerRemoval" -> true,
          "inputField" -> inputField,
          "schema.inputMode" -> "HEADER",
          "fieldsPreservationPolicy" -> "JUST_EXTRACTED")
      ).generateNewRow(input)

      val expected = Seq.empty[Row]
      expected should be eq result
    }
  }

}
