/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.plugin.operator.count

import com.stratio.sparkta.sdk._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class CountOperatorTest extends WordSpec with Matchers {

  "Count operator" should {

    "Distinct fields must be " in {
      val inputFields = new CountOperator("count", Map())
      val distinctFields = inputFields.distinctFields
      distinctFields should be(None)

      val inputFields2 = new CountOperator("count", Map("distinctFields" -> "field1"))
      val distinctFields2 = inputFields2.distinctFields
      distinctFields2 should be equals Some(Array[String]("field1"))

      val inputFields3 = new CountOperator("count", Map("distinctFields" -> ""))
      val distinctFields3 = inputFields3.distinctFields
      distinctFields3 should be(None)
    }

    "processMap must be " in {
      val inputFields = new CountOperator("count", Map())
      inputFields.processMap(InputFieldsValues(Map("field1" -> 1, "field2" -> 2))) should be
      (Some(CountOperator.One.toLong))

      val inputFields2 =
        new CountOperator("count", Map("distinctFields" -> s"field1${OperatorConstants.UnderscoreSeparator}field2"))
      inputFields2.processMap(InputFieldsValues(Map("field1" -> 1, "field2" -> 2))).get.toString should be
      s"field1${OperatorConstants.UnderscoreSeparator}field2"

      val inputFields3 = new CountOperator("count", Map("distinctFields" -> ""))
      inputFields3.processMap(InputFieldsValues(Map("field1" -> 1, "field2" -> 2))) should be(Some(CountOperator.One))

      val inputFields4 = new CountOperator("count",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2}]"))
      inputFields4.processMap(InputFieldsValues(Map("field1" -> 1, "field2" -> 2))) should be
      (Some(CountOperator.One.toLong))

      val inputFields5 = new CountOperator("count",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \">\", \"value\":\"2\"}]"))
      inputFields5.processMap(InputFieldsValues(Map("field1" -> 1, "field2" -> 2))) should be(None)

      val inputFields6 = new CountOperator("count",
        Map("filters" -> {
          "[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\"}," +
            "{\"field\":\"field2\", \"type\": \"<\", \"value\":\"2\"}]"
        }))
      inputFields6.processMap(InputFieldsValues(Map("field1" -> 1, "field2" -> 2))) should be(None)

      val inputFields7 = new CountOperator("count",
        Map("filters" -> {
          "[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\", \"fieldType\":\"int\"}," +
            "{\"field\":\"field2\", \"type\": \"<\", \"value\":\"2\", \"fieldType\":\"int\"}]"
        }))
      inputFields7.processMap(InputFieldsValues(Map("field1" -> 1L, "field2" -> 2L))) should be(None)

      val inputFields8 = new CountOperator("count",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2, \"fieldType\":\"int\"}]"))
      inputFields8.processMap(InputFieldsValues(Map("field1" -> 1, "field2" -> 2))) should be
      (Some(CountOperator.One.toLong))

      val inputFields9 = new CountOperator("count",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2, \"fieldType\":\"int\"}]"))
      inputFields9.processMap(InputFieldsValues(Map("field1" -> 1L, "field2" -> 2L))) should be
      (Some(CountOperator.One.toLong))

      val inputFields10 = new CountOperator("count",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2, \"fieldType\":\"int\"}]"))
      inputFields10.processMap(InputFieldsValues(Map("field1" -> 1d, "field2" -> 2d))) should be
      (Some(CountOperator.One.toLong))

      val inputFields11 = new CountOperator("count",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2, \"fieldType\":\"int\"}]"))
      inputFields11.processMap(InputFieldsValues(Map("field1" -> "1", "field2" -> "2"))) should be
      (Some(CountOperator.One.toLong))

      val inputFields12 = new CountOperator("count",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"=\", \"value\":1, \"fieldType\":\"int\"}]"))
      inputFields12.processMap(InputFieldsValues(Map("field1" -> "1", "field2" -> "2"))) should be(Some(CountOperator
        .One.toLong))

      val inputFields13 = new CountOperator("count",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"=\", \"value\":\"1\", \"fieldType\":\"string\"}]"))
      inputFields13.processMap(InputFieldsValues(Map("field1" -> "1", "field2" -> "2"))) should be
      (Some(CountOperator.One.toLong))

      val inputFields14 = new CountOperator("count",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"!=\", \"value\":\"1\", \"fieldType\":\"string\"}]"))
      inputFields14.processMap(InputFieldsValues(Map("field1" -> "1", "field2" -> "2"))) should be(None)

      val inputFields15 = new CountOperator("count",
        Map("filters" -> ("[{\"field\":\"field1\", \"type\": \"=\", \"value\":\"1\", \"fieldType\":\"string\"}," +
          "{\"field\":\"field2\", \"type\": \"=\", \"value\":\"1\", \"fieldType\":\"string\"}]")))
      inputFields15.processMap(InputFieldsValues(Map("field1" -> "1", "field2" -> "2"))) should be(None)

      val inputFields16 = new CountOperator("count",
        Map("filters" -> ("[{\"field\":\"field1\", \"type\": \"=\", \"value\":\"1\", \"fieldType\":\"string\"}," +
          "{\"field\":\"field2\", \"type\": \"!=\", \"value\":\"1\", \"fieldType\":\"string\"}]")))
      inputFields16.processMap(InputFieldsValues(Map("field1" -> "1", "field2" -> "2"))) should be
      (Some(CountOperator.One.toLong))

      val inputFields17 = new CountOperator("count",
        Map("filters" -> ("[{\"field\":\"hashtag\", \"type\": \"!=\", \"value\":\"\", \"fieldType\":\"string\"}," +
          "{\"field\":\"unique\", \"type\": \"=\", \"value\":\"false\", \"fieldType\":\"string\"}]")))
      inputFields17.processMap(InputFieldsValues(Map("hashtag" -> "sparkta", "unique" -> "false"))) should be
      (Some(CountOperator.One.toLong))

      val inputFields18 = new CountOperator("count",
        Map("filters" -> ("[{\"field\":\"hashtag\", \"type\": \"!=\", \"value\":\"\", \"fieldType\":\"string\"}," +
          "{\"field\":\"unique\", \"type\": \"=\", \"value\":\"false\", \"fieldType\":\"string\"}]")))
      inputFields18.processMap(InputFieldsValues(Map("hashtag" -> "", "unique" -> "false"))) should be(None)
    }

    "processReduce must be " in {
      val inputFields = new CountOperator("count", Map())
      inputFields.processReduce(Seq(Some(1L), Some(1L), None)) should be(Some(2L))

      val inputFields2 =
        new CountOperator("count", Map("distinctFields" -> s"field1${OperatorConstants.UnderscoreSeparator}field2"))
      inputFields2.processReduce(Seq(Some("field1_field2"))) should be(Some(CountOperator.One.toLong))

      val inputFields3 =
        new CountOperator("count", Map("distinctFields" -> s"field1${OperatorConstants.UnderscoreSeparator}field2"))
      inputFields3.processReduce(Seq(Some(s"field1${OperatorConstants.UnderscoreSeparator}field2"),
        Some(s"field1${OperatorConstants.UnderscoreSeparator}field2"))) should be(Some(CountOperator.One.toLong))

      val inputFields4 =
        new CountOperator("count", Map("distinctFields" -> s"field1${OperatorConstants.UnderscoreSeparator}field2"))
      inputFields4.processReduce(Seq(Some(s"field1${OperatorConstants.UnderscoreSeparator}field2"),
        Some(s"field1${OperatorConstants.UnderscoreSeparator}field3"))) should be(Some(2L))

      val inputFields5 = new CountOperator("count", Map("typeOp" -> "string"))
      inputFields5.processReduce(Seq(Some(1), Some(1))) should be(Some(2))

      val inputFields6 =
        new CountOperator("count", Map("distinctFields" -> s"field1${OperatorConstants.UnderscoreSeparator}field2"))
      inputFields6.processReduce(Seq(Some(s"field1${OperatorConstants.UnderscoreSeparator}field2"),
        Some(s"field1${OperatorConstants.UnderscoreSeparator}field3"),
        Some(s"field1${OperatorConstants.UnderscoreSeparator}field3"))) should be(Some(2L))

      val inputFields7 = new CountOperator("count", Map("typeOp" -> null))
      inputFields7.processReduce(Seq(Some(1), Some(1))) should be(Some(2))

    }

    "associative process must be " in {
      val inputFields = new CountOperator("count", Map())
      val resultInput = Seq((Operator.OldValuesKey, Some(1L)),
        (Operator.NewValuesKey, Some(1L)),
        (Operator.NewValuesKey, None))
      inputFields.associativity(resultInput) should be(Some(2L))

      val inputFields2 = new CountOperator("count", Map("typeOp" -> "string"))
      val resultInput2 = Seq((Operator.OldValuesKey, Some(1L)), (Operator.NewValuesKey, Some(1L)))
      inputFields2.associativity(resultInput2) should be(Some("2"))

      val inputFields3 = new CountOperator("count", Map("typeOp" -> null))
      val resultInput3 = Seq((Operator.OldValuesKey, Some(1L)), (Operator.NewValuesKey, Some(1L)))
      inputFields3.associativity(resultInput3) should be(Some(2))
    }
  }
}
