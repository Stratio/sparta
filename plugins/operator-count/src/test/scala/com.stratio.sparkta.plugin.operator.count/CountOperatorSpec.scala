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

@RunWith(classOf[JUnitRunner])
class CountOperatorSpec extends WordSpec with Matchers {

  "Count operator" should {

    "Distinct fields must be " in {
      val inputFields = new CountOperator(Map())
      val distinctFields = inputFields.distinctFields
      distinctFields should be(None)

      val inputFields2 = new CountOperator(Map("distinctFields" -> "field1"))
      val distinctFields2 = inputFields2.distinctFields
      distinctFields2 should be equals (Some(Array[String]("field1")))
    }

    "processMap must be " in {
      val inputFields = new CountOperator(Map())
      inputFields.processMap(Map("field1" -> 1, "field2" -> 2)) should be(Some(1L))

      val inputFields2 = new CountOperator(Map("distinctFields" -> s"field1${CountOperator.Separator}field2"))
      inputFields2.processMap(Map("field1" -> 1, "field2" -> 2)).get.toString should be
      (s"field1${CountOperator.Separator}field2")

      val inputFields3 = new CountOperator(Map("distinctFields" -> ""))
      inputFields3.processMap(Map("field1" -> 1, "field2" -> 2)).get.toString should be("")
    }

    "processReduce must be " in {
      val inputFields = new CountOperator(Map())
      inputFields.processReduce(Seq(Some(1L), Some(1L))) should be(Some(2L))

      val inputFields2 = new CountOperator(Map("distinctFields" -> s"field1${CountOperator.Separator}field2"))
      inputFields2.processReduce(Seq(Some("field1_field2" -> 1))) should be(Some(1L))

      val inputFields3 = new CountOperator(Map("distinctFields" -> s"field1${CountOperator.Separator}field2"))
      inputFields3.processReduce(Seq(Some(s"field1${CountOperator.Separator}field2" -> 1),
        Some(s"field1${CountOperator.Separator}field2" -> 1))) should be(Some(1L))

      val inputFields5 = new CountOperator(Map("typeOp" -> "string"))
      inputFields5.processReduce(Seq(Some(1), Some(1))) should be(Some("2"))
    }
  }
}