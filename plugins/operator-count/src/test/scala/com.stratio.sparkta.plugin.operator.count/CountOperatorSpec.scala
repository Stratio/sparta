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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import com.stratio.sparkta.sdk.OperatorConstants

@RunWith(classOf[JUnitRunner])
class CountOperatorSpec extends WordSpec with Matchers {

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
      inputFields.processMap(Map("field1" -> 1, "field2" -> 2)) should be(Some(CountOperator.One.toLong))

      val inputFields2 =
        new CountOperator("count", Map("distinctFields" -> s"field1${OperatorConstants.UnderscoreSeparator}field2"))
      inputFields2.processMap(Map("field1" -> 1, "field2" -> 2)).get.toString should be
      s"field1${OperatorConstants.UnderscoreSeparator}field2"

      val inputFields3 = new CountOperator("count", Map("distinctFields" -> ""))
      inputFields3.processMap(Map("field1" -> 1, "field2" -> 2)) should be(Some(CountOperator.One))

      val inputFields4 = new CountOperator("count",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2}]"))
      inputFields4.processMap(Map("field1" -> 1, "field2" -> 2)) should be(Some(CountOperator.One.toLong))

      val inputFields5 = new CountOperator("count",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \">\", \"value\":\"2\"}]"))
      inputFields5.processMap(Map("field1" -> 1, "field2" -> 2)) should be(None)

      val inputFields6 = new CountOperator("count",
        Map("filters" -> {
          "[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\"}," +
            "{\"field\":\"field2\", \"type\": \"<\", \"value\":\"2\"}]"
        }))
      inputFields6.processMap(Map("field1" -> 1, "field2" -> 2)) should be(None)
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
      inputFields5.processReduce(Seq(Some(1), Some(1))) should be(Some("2"))

      val inputFields6 =
        new CountOperator("count", Map("distinctFields" -> s"field1${OperatorConstants.UnderscoreSeparator}field2"))
      inputFields6.processReduce(Seq(Some(s"field1${OperatorConstants.UnderscoreSeparator}field2"),
        Some(s"field1${OperatorConstants.UnderscoreSeparator}field3"),
        Some(s"field1${OperatorConstants.UnderscoreSeparator}field3"))) should be(Some(2L))

      val inputFields7 = new CountOperator("count", Map("typeOp" -> null))
      inputFields7.processReduce(Seq(Some(1), Some(1))) should be(Some(0))
    }
  }
}
