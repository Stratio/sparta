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

package com.stratio.sparta.plugin.transformation.split

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.properties.JsoneyString
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import com.stratio.sparta.plugin.transformation.split.SplitParser._

@RunWith(classOf[JUnitRunner])
class SplitParserTest extends WordSpecLike with Matchers {

  "SplitParser BYINDEX" when {

    val inputField = Some("stringSubfamily")
    val outputsFields = Seq("sector", "section", "familyGroup", "family", "subFamily")
    val schema = StructType(Seq(
      StructField("sector", StringType),
      StructField("section", StringType),
      StructField("familyGroup", StringType),
      StructField("family", StringType),
      StructField("subFamily", StringType))
    )
    val subFamily = 333210
    val input = Row(subFamily)

    "given a valid list of indexes" should {
      val listIndexes = "1,3,4,5"
      "split a field according to them" in {

        val result = new SplitParser(1,
          inputField,
          outputsFields,
          schema,
          Map("splitMethod" -> "BYINDEX", "byIndexPattern" -> listIndexes,
            "removeInputField" -> JsoneyString.apply("true"),
            "excludeIndexes" -> JsoneyString.apply("false"))
        ).parse(input)

        val expected = Seq(Row("3", "33", "2", "1", "0"))
        assertResult(expected)(result)

        val resultNoRemoved = new SplitParser(1,
          inputField,
          outputsFields,
          schema,
          Map("splitMethod" -> "BYINDEX", "byIndexPattern" -> listIndexes)
        ).parse(input)

        val expectedNoRemoved = Seq(Row(subFamily, "3", "33", "2", "1", "0"))
        assertResult(expectedNoRemoved)(resultNoRemoved)

        val inputFieldExcluded = Some("stringExclude")
        val outputsFieldsExcluded = Seq("year", "month", "day")
        val schemaExcluded = StructType(Seq(
          StructField("year", StringType),
          StructField("month", StringType),
          StructField("day", StringType))
        )
        val excluded = "2017-12-12"
        val inputExcluded = Row(excluded)
        val resultExcludedIndexes= new SplitParser(1,
          inputFieldExcluded,
          outputsFieldsExcluded,
          schemaExcluded,
          Map("splitMethod" -> "BYINDEX", "byIndexPattern" -> "4,7",
            "removeInputField" -> JsoneyString.apply("true"),
            "excludeIndexes" -> JsoneyString.apply("true"))
        ).parse(inputExcluded)
        val expectedExcludedNoRemoved = Seq(Row("2017","12","12"))
        assertResult(expectedExcludedNoRemoved)(resultExcludedIndexes)
      }
    }
    "given an invalid list of indexes" should {
      val listIndexesNegative = "-1,3,4,5"
      val listIndexesNotIncreasing = "1,3,8,4"
      val listIndexesGreaterOrEqualThanEndOfString = "1,3,4,6"
      val listIndexesUnaryIncrease= "1,3,4,7"

      "throw an IllegalStateException" in {
        an[IllegalStateException] should be thrownBy new SplitParser(1,
          inputField,
          outputsFields,
          schema,
          Map("splitMethod" -> "BYINDEX",
            "byIndexPattern" -> listIndexesNegative,
            "removeInputField" -> JsoneyString.apply("true"))
        ).parse(input)

        an[IllegalStateException] should be thrownBy new SplitParser(1,
          inputField,
          outputsFields,
          schema,
          Map("splitMethod" -> "BYINDEX",
            "byIndexPattern" -> listIndexesNotIncreasing,
            "removeInputField" -> JsoneyString.apply("true"))
        ).parse(input)

        an[IllegalStateException] should be thrownBy new SplitParser(1,
          inputField,
          outputsFields,
          schema,
          Map("splitMethod" -> "BYINDEX",
            "byIndexPattern" -> listIndexesGreaterOrEqualThanEndOfString,
            "removeInputField" -> JsoneyString.apply("true"))
        ).parse(input)

        an[IllegalStateException] should be thrownBy new SplitParser(1,
          inputField,
          outputsFields,
          schema,
          Map("splitMethod" -> "BYINDEX",
            "byIndexPattern" -> listIndexesUnaryIncrease,
            "removeInputField" -> JsoneyString.apply("true"),
            "excludeIndexes" -> JsoneyString.apply("true"))
        ).parse(input)
      }
    }
    "the number of OutputField is different from number of split values" should {
      val outputsFieldsDifferent = Seq("sector", "section", "familyGroup", "family")
      val listIndexes = "1,3,4,5"
      "throw an IllegalStateException" in {
        val exceptionThrown = the[IllegalStateException] thrownBy {
          new
              SplitParser(1,
                inputField,
                outputsFieldsDifferent,
                schema,
                Map("splitMethod" -> "BYINDEX",
                  "byIndexPattern" -> listIndexes,
                  "removeInputField" -> JsoneyString.apply("true"))
              ).parse(input)
        }
        exceptionThrown.getMessage should include ("is greater or lower than the output fields")
      }
    }
  }

  "SplitParser BYREGEX" when {

    val inputField = Some("stringField")
    val schema = StructType(Seq(
      StructField("IP1", StringType),
      StructField("IP2", StringType),
      StructField("IP3", StringType),
      StructField("IP4", StringType))
    )
    val outputsFields = Seq("IP1", "IP2", "IP3", "IP4")
    val expected = Seq(Row("172", "0", "0", "1"))

    "a valid regex string is provided" should {
      val regexPoints= "[.]"

      "correctly split into values" in {
        val ipString= Row("172.0.0.1")

        val result = new SplitParser(1,
          inputField,
          outputsFields,
          schema,
          Map("splitMethod" -> "BYREGEX",
            "byRegexPattern" -> regexPoints,
            "removeInputField" -> JsoneyString.apply("true"))
        ).parse(ipString)

        assertResult(expected)(result)

        val ipStringEmpty = Row("172...")
        val resultEmpty= new SplitParser(1,
          inputField,
          outputsFields,
          schema,
          Map("splitMethod" -> "BYREGEX",
            "byRegexPattern" -> regexPoints,
            "removeInputField" -> JsoneyString.apply("true"))
        ).parse(ipStringEmpty)

        val expectedEmpty= Seq(Row("172", "", "", ""))
        assertResult(expectedEmpty)(resultEmpty)
      }
    }
    "the provided regex is not valid" should{
      "throw an IllegalStateException" in {
        val regexNotValid = """[\]"""
        val ipString= Row("""172\0\0\1""")

        val exceptionThrown = the[IllegalStateException] thrownBy {
          new SplitParser(1,
            inputField,
            outputsFields,
            schema,
            Map("splitMethod" -> "BYREGEX",
              "byRegexPattern" -> regexNotValid,
              "removeInputField" -> JsoneyString.apply("true"))
          ).parse(ipString)
        }
        exceptionThrown.getMessage should include ("provided regex")
      }
    }
    "the number of OutputField is different from number of split values" should {
      val regexDollar= "[$]"
      val ipStringDollar= Row("172$0$0$1")
      val outputsFieldsDifferent = Seq("IP1", "IP2", "IP3")

      "throw an IllegalStateException" in {
        val exceptionThrown = the[IllegalStateException] thrownBy {
          new SplitParser(1,
            inputField,
            outputsFieldsDifferent,
            schema,
            Map("splitMethod" -> "BYREGEX",
              "byRegexPattern" -> regexDollar,
              "removeInputField" -> JsoneyString.apply("true"))
          ).parse(ipStringDollar)
        }
        exceptionThrown.getMessage should include ("is greater or lower than the output fields")
      }
    }
  }
  "SplitParser BYCHAR" when {
    val outputsFields = Seq("IP1", "IP2", "IP3", "IP4")
    val schema = StructType(Seq(
      StructField("IP1", StringType),
      StructField("IP2", StringType),
      StructField("IP3", StringType),
      StructField("IP4", StringType))
    )
    val inputField = Some("stringField")
    val expected = Seq(Row("172", "0", "0", "1"))
    "a valid char is provided" should {
      "correctly split into values" in {
        val regexPoints= """\"""
        val ipString= Row("""172\0\0\1""")
        val result = new SplitParser(1,
          inputField,
          outputsFields,
          schema,
          Map("splitMethod" -> "BYCHAR",
            "byCharPattern" -> regexPoints,
            "removeInputField" -> JsoneyString.apply("true"))
        ).parse(ipString)

        assertResult(expected)(result)

        val regexDollar= "$"
        val ipStringDollar= Row("172$0$0$1")
        val resultDollar = new SplitParser(1,
          inputField,
          outputsFields,
          schema,
          Map("splitMethod" -> "BYCHAR",
            "byCharPattern" -> regexDollar,
            "removeInputField" -> JsoneyString.apply("true"))
        ).parse(ipStringDollar)
        assertResult(expected)(resultDollar)

      }
    }
    "the number of OutputField is different from number of split values" should {
      val regexDollar= "$"
      val ipStringDollar= Row("172$0$0$1")
      val outputsFieldsDifferent = Seq("IP1", "IP2", "IP3")
      "throw an IllegalStateException" in {
        val exceptionThrown = the[IllegalStateException] thrownBy {
          new SplitParser(1,
            inputField,
            outputsFieldsDifferent,
            schema,
            Map("splitMethod" -> "BYCHAR",
              "byCharPattern" -> regexDollar,
              "removeInputField" -> JsoneyString.apply("true"))
          ).parse(ipStringDollar)
        }
        exceptionThrown.getMessage should include ("is greater or lower than the output fields")
      }
    }
  }
}
