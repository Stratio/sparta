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

package com.stratio.sparkta.plugin.operator.lastValue

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class LastValueOperatorSpec extends WordSpec with Matchers {

  "LastValue operator" should {

    "processMap must be " in {
      val inputField = new LastValueOperator("lastValue", Map())
      inputField.processMap(Map("field1" -> 1, "field2" -> 2)) should be(None)

      val inputFields2 = new LastValueOperator("lastValue", Map("inputField" -> "field1"))
      inputFields2.processMap(Map("field3" -> 1, "field2" -> 2)) should be(None)

      val inputFields3 = new LastValueOperator("lastValue", Map("inputField" -> "field1"))
      inputFields3.processMap(Map("field1" -> 1, "field2" -> 2)) should be(Some(1))

      val inputFields4 = new LastValueOperator("lastValue",
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2}]"))
      inputFields4.processMap(Map("field1" -> 1, "field2" -> 2)) should be(Some(1L))

      val inputFields5 = new LastValueOperator("lastValue",
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \">\", \"value\":\"2\"}]"))
      inputFields5.processMap(Map("field1" -> 1, "field2" -> 2)) should be(None)

      val inputFields6 = new LastValueOperator("lastValue",
        Map("inputField" -> "field1", "filters" -> {"[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\"}," +
          "{\"field\":\"field2\", \"type\": \"<\", \"value\":\"2\"}]"}))
      inputFields6.processMap(Map("field1" -> 1, "field2" -> 2)) should be(None)
    }

    "processReduce must be " in {
      val inputFields = new LastValueOperator("lastValue", Map())
      inputFields.processReduce(Seq()) should be(Some(""))

      val inputFields2 = new LastValueOperator("lastValue", Map("typeOp" -> "int"))
      inputFields2.processReduce(Seq(Some(1), Some(2))) should be(Some(2))

      val inputFields4 = new LastValueOperator("lastValue", Map("typeOp" -> "string"))
      inputFields4.processReduce(Seq(Some(1), Some(2))) should be(Some("2"))

      val inputFields3 = new LastValueOperator("lastValue", Map())
      inputFields3.processReduce(Seq(Some("a"), Some("b"))) should be(Some("b"))
    }
  }
}