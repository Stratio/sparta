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
package com.stratio.sparta.plugin.workflow.transformation.split

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.properties.JsoneyString
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import com.stratio.sparta.sdk.workflow.enumerators.SaveModeEnum
import com.stratio.sparta.sdk.workflow.step.OutputOptions
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema

@RunWith(classOf[JUnitRunner])
class SplitTransformStepStreamTest extends WordSpecLike with Matchers {

  val outputOptions = OutputOptions(SaveModeEnum.Append, "tableName", None, None)
  val inputField = "split"
  val schema = StructType(Seq(
    StructField(inputField, StringType))
  )

  //scalastyle:off
  "SplitParser BYINDEX" when {
    val subFamily = 333210

    val fields =
      """[
        |{
        |   "name":"sector"
        |},
        |{
        |   "name":"section"
        |},
        |{
        |   "name":"familyGroup"
        |},
        |{
        |   "name":"family"
        |},
        |{
        |   "name":"subFamily"
        |}]
        |""".stripMargin
    val input = new GenericRowWithSchema(Array(subFamily), schema)


    "given a valid list of indexes" should {
      val listIndexes = "1,3,4,5"

      "split a field according to them" in {

        val result = new SplitTransformStepStream(
          inputField,
          outputOptions,
          null,
          null,
          Map("splitMethod" -> "BYINDEX",
            "byIndexPattern" -> listIndexes,
            "excludeIndexes" -> JsoneyString.apply("false"),
            "inputField" -> inputField,
            "schema.fields" -> fields.asInstanceOf[JSerializable],
            "schema.inputMode" -> "FIELDS"
          )
        ).parse(input)
        val expected = Seq(Row("3", "33", "2", "1", "0"))
        assertResult(expected)(result)

        val resultNoRemoved = new SplitTransformStepStream(
          inputField,
          outputOptions,
          null,
          null,
          Map("splitMethod" -> "BYINDEX",
            "byIndexPattern" -> listIndexes,
            "inputField" -> inputField,
            "schema.fields" -> fields.asInstanceOf[JSerializable],
            "schema.inputMode" -> "FIELDS",
            "fieldsPreservationPolicy" -> "APPEND")
        ).parse(input)
        val expectedNoRemoved = Seq(Row(subFamily, "3", "33", "2", "1", "0"))
        assertResult(expectedNoRemoved)(resultNoRemoved)

        val fieldsExcluded =
          """[
            |{
            |   "name":"year"
            |},
            |{
            |   "name":"month"
            |},
            |{
            |   "name":"day"
            |}]
            |""".stripMargin
        val excluded = "2017-12-12"
        val inputExcluded = new GenericRowWithSchema(Array(excluded), schema)
        val resultExcludedIndexes = new SplitTransformStepStream(
          inputField,
          outputOptions,
          null,
          null,
          Map("splitMethod" -> "BYINDEX",
            "byIndexPattern" -> "4,7",
            "inputField" -> inputField,
            "schema.fields" -> fieldsExcluded.asInstanceOf[JSerializable],
            "schema.inputMode" -> "FIELDS",
            "excludeIndexes" -> JsoneyString.apply("true"))
        ).parse(inputExcluded)

        val expectedExcludedNoRemoved = Seq(Row("2017", "12", "12"))
        assertResult(expectedExcludedNoRemoved)(resultExcludedIndexes)
      }
    }

    "given an invalid list of indexes" should {
      val listIndexesNegative = "-1,3,4,5"
      val listIndexesNotIncreasing = "1,3,8,4"
      val listIndexesGreaterOrEqualThanEndOfString = "1,3,4,6"
      val listIndexesUnaryIncrease = "1,3,4,7"

      "throw an IllegalStateException" in {
        an[IllegalStateException] should be thrownBy new SplitTransformStepStream(
          inputField,
          outputOptions,
          null,
          null,
          Map("splitMethod" -> "BYINDEX",
            "byIndexPattern" -> listIndexesNegative,
            "inputField" -> inputField,
            "schema.fields" -> fields.asInstanceOf[JSerializable],
            "schema.inputMode" -> "FIELDS")
        ).parse(input)

        an[IllegalStateException] should be thrownBy new SplitTransformStepStream(
          inputField,
          outputOptions,
          null,
          null,
          Map("splitMethod" -> "BYINDEX",
            "byIndexPattern" -> listIndexesNotIncreasing,
            "inputField" -> inputField,
            "schema.fields" -> fields.asInstanceOf[JSerializable],
            "schema.inputMode" -> "FIELDS")
        ).parse(input)

        an[IllegalStateException] should be thrownBy new SplitTransformStepStream(
          inputField,
          outputOptions,
          null,
          null,
          Map("splitMethod" -> "BYINDEX",
            "byIndexPattern" -> listIndexesGreaterOrEqualThanEndOfString,
            "inputField" -> inputField,
            "schema.fields" -> fields.asInstanceOf[JSerializable],
            "schema.inputMode" -> "FIELDS")
        ).parse(input)

        an[IllegalStateException] should be thrownBy new SplitTransformStepStream(
          inputField,
          outputOptions,
          null,
          null,
          Map("splitMethod" -> "BYINDEX",
            "byIndexPattern" -> listIndexesUnaryIncrease,
            "inputField" -> inputField,
            "schema.fields" -> fields.asInstanceOf[JSerializable],
            "schema.inputMode" -> "FIELDS")
        ).parse(input)
      }
    }


    "the number of OutputField is different from number of split values" should {
      val outputsFieldsDifferent =
        """[
          |{
          |   "name":"sector"
          |},
          |{
          |   "name":"section"
          |},
          |{
          |   "name":"familyGroup"
          |},
          |{
          |   "name":"family"
          |}]
          |""".stripMargin

      val listIndexes = "1,3,4,5"
      "throw an IllegalStateException" in {
        val exceptionThrown = the[IllegalStateException] thrownBy {
          new SplitTransformStepStream(
            inputField,
            outputOptions,
            null,
            null,
            Map("splitMethod" -> "BYINDEX",
              "byIndexPattern" -> listIndexes,
              "inputField" -> inputField,
              "schema.fields" -> outputsFieldsDifferent.asInstanceOf[JSerializable],
              "schema.inputMode" -> "FIELDS")
          ).parse(input)
        }
        exceptionThrown.getMessage should include("is greater or lower than the output fields")
      }
    }
  }

  "SplitParser BYREGEX" when {

    val fields =
      """[
        |{
        |   "name":"IP1"
        |},
        |{
        |   "name":"IP2"
        |},
        |{
        |   "name":"IP3"
        |},
        |{
        |   "name":"IP4"
        |}]
        |""".stripMargin

    "a valid regex string is provided" should {
      val regexPoints = "[.]"

      "correctly split into values" in {
        val ipString = new GenericRowWithSchema(Array("172.0.0.1"), schema)
        val result = new SplitTransformStepStream(
          inputField,
          outputOptions,
          null,
          null,
          Map("splitMethod" -> "BYREGEX",
            "byRegexPattern" -> regexPoints,
            "inputField" -> inputField,
            "schema.fields" -> fields.asInstanceOf[JSerializable],
            "schema.inputMode" -> "FIELDS")
        ).parse(ipString)
        val expected = Seq(Row("172", "0", "0", "1"))
        assertResult(expected)(result)

        val ipStringEmpty = new GenericRowWithSchema(Array("172..."), schema)
        val resultEmpty = new SplitTransformStepStream(
          inputField,
          outputOptions,
          null,
          null,
          Map("splitMethod" -> "BYREGEX",
            "byRegexPattern" -> regexPoints,
            "inputField" -> inputField,
            "schema.fields" -> fields.asInstanceOf[JSerializable],
            "schema.inputMode" -> "FIELDS")
        ).parse(ipStringEmpty)

        val expectedEmpty = Seq(Row("172", "", "", ""))
        assertResult(expectedEmpty)(resultEmpty)
      }
    }

    "the provided regex is not valid" should {

      "throw an IllegalStateException" in {
        val regexNotValid = """[\]"""
        val ipString = new GenericRowWithSchema(Array("""172\0\0\1"""), schema)

        val exceptionThrown = the[IllegalStateException] thrownBy {
          new SplitTransformStepStream(
            inputField,
            outputOptions,
            null,
            null,
            Map("splitMethod" -> "BYREGEX",
              "byRegexPattern" -> regexNotValid,
              "inputField" -> inputField,
              "schema.fields" -> fields.asInstanceOf[JSerializable],
              "schema.inputMode" -> "FIELDS")
          ).parse(ipString)
        }
        exceptionThrown.getMessage should include("provided regex")
      }
    }

    "the number of OutputField is different from number of split values" should {
      val regexDollar = "[$]"
      val ipStringDollar = new GenericRowWithSchema(Array("172$0$0$1"), schema)
      val outputsFieldsDifferent =
        """[
          |{
          |   "name":"IP1"
          |},
          |{
          |   "name":"IP2"
          |},
          |{
          |   "name":"IP3"
          |}]
          |""".stripMargin

      "throw an IllegalStateException" in {
        val exceptionThrown = the[IllegalStateException] thrownBy {
          new SplitTransformStepStream(
            inputField,
            outputOptions,
            null,
            null,
            Map("splitMethod" -> "BYREGEX",
              "byRegexPattern" -> regexDollar,
              "inputField" -> inputField,
              "schema.fields" -> outputsFieldsDifferent.asInstanceOf[JSerializable],
              "schema.inputMode" -> "FIELDS")
          ).parse(ipStringDollar)
        }
        exceptionThrown.getMessage should include("is greater or lower than the output fields")
      }
    }
  }

  "SplitParser BYCHAR" when {
    val outputsFields =
      """[
        |{
        |   "name":"IP1"
        |},
        |{
        |   "name":"IP2"
        |},
        |{
        |   "name":"IP3"
        |},
        |{
        |   "name":"IP4"
        |}]
        |""".stripMargin

    val expected = Seq(Row("172", "0", "0", "1"))

    "a valid char is provided" should {
      "correctly split into values" in {
        val regexPoints = """\"""
        val ipString = new GenericRowWithSchema(Array("""172\0\0\1"""), schema)
        val result = new SplitTransformStepStream(
          inputField,
          outputOptions,
          null,
          null,
          Map("splitMethod" -> "BYCHAR",
            "byCharPattern" -> regexPoints,
            "inputField" -> inputField,
            "schema.fields" -> outputsFields.asInstanceOf[JSerializable],
            "schema.inputMode" -> "FIELDS")
        ).parse(ipString)

        assertResult(expected)(result)

        val regexDollar = "$"
        val ipStringDollar = new GenericRowWithSchema(Array("172$0$0$1"), schema)
        val resultDollar = new SplitTransformStepStream(
          inputField,
          outputOptions,
          null,
          null,
          Map("splitMethod" -> "BYCHAR",
            "byCharPattern" -> regexDollar,
            "inputField" -> inputField,
            "schema.fields" -> outputsFields.asInstanceOf[JSerializable],
            "schema.inputMode" -> "FIELDS")
        ).parse(ipStringDollar)
        assertResult(expected)(resultDollar)

      }
    }

    "the number of OutputField is different from number of split values" should {
      val regexDollar = "$"
      val ipStringDollar = new GenericRowWithSchema(Array("172$0$0$1"), schema)
      val outputsFieldsDifferent =
        """[
          |{
          |   "name":"IP1"
          |},
          |{
          |   "name":"IP2"
          |},
          |{
          |   "name":"IP3"
          |}]
          |""".stripMargin
      "throw an IllegalStateException" in {
        val exceptionThrown = the[IllegalStateException] thrownBy {
          new SplitTransformStepStream(
            inputField,
            outputOptions,
            null,
            null,
            Map("splitMethod" -> "BYCHAR",
              "byCharPattern" -> regexDollar,
              "inputField" -> inputField,
              "schema.fields" -> outputsFieldsDifferent.asInstanceOf[JSerializable],
              "schema.inputMode" -> "FIELDS")
          ).parse(ipStringDollar)
        }
        exceptionThrown.getMessage should include("is greater or lower than the output fields")
      }
    }
  }
}