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

package com.stratio.sparkta.plugin.operator.stddev

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class StddevOperatorSpec extends WordSpec with Matchers {

  "Std dev operator" should {

    "processMap must be " in {
      val inputField = new StddevOperator("stdev", Map())
      inputField.processMap(Map("field1" -> 1, "field2" -> 2)) should be(None)

      val inputFields2 = new StddevOperator("stdev", Map("inputField" -> "field1"))
      inputFields2.processMap(Map("field3" -> 1, "field2" -> 2)) should be(None)

      val inputFields3 = new StddevOperator("stdev", Map("inputField" -> "field1"))
      inputFields3.processMap(Map("field1" -> 1, "field2" -> 2)) should be(Some(1))

      val inputFields4 = new StddevOperator("stdev", Map("inputField" -> "field1"))
      inputFields3.processMap(Map("field1" -> "1", "field2" -> 2)) should be(Some(1))

      val inputFields5 = new StddevOperator("stdev", Map("inputField" -> "field1"))
      inputFields5.processMap(Map("field1" -> "foo", "field2" -> 2)) should be(None)

      val inputFields6 = new StddevOperator("stdev", Map("inputField" -> "field1"))
      inputFields6.processMap(Map("field1" -> 1.5, "field2" -> 2)) should be(Some(1.5))

      val inputFields7 = new StddevOperator("stdev", Map("inputField" -> "field1"))
      inputFields7.processMap(Map("field1" -> 5L, "field2" -> 2)) should be(Some(5L))

      val inputFields8 = new StddevOperator("stdev",
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2}]"))
      inputFields8.processMap(Map("field1" -> 1, "field2" -> 2)) should be(Some(1L))

      val inputFields9 = new StddevOperator("stdev",
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \">\", \"value\":\"2\"}]"))
      inputFields9.processMap(Map("field1" -> 1, "field2" -> 2)) should be(None)

      val inputFields10 = new StddevOperator("stdev",
        Map("inputField" -> "field1", "filters" -> {"[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\"}," +
          "{\"field\":\"field2\", \"type\": \"<\", \"value\":\"2\"}]"}))
      inputFields10.processMap(Map("field1" -> 1, "field2" -> 2)) should be(None)
    }

    "processReduce must be " in {
      val inputFields = new StddevOperator("stdev", Map())
      inputFields.processReduce(Seq()) should be(Some(0d))

      val inputFields2 = new StddevOperator("stdev", Map())
      inputFields2.processReduce(Seq(Some(1), Some(2), Some(3), Some(7), Some(7))) should be
      (Some(2.8284271247461903))

      val inputFields3 = new StddevOperator("stdev", Map())
      inputFields3.processReduce(Seq(Some(1), Some(2), Some(3), Some(6.5), Some(7.5))) should be
      (Some(2.850438562747845))

      val inputFields4 = new StddevOperator("stdev", Map())
      inputFields4.processReduce(Seq(None)) should be(Some(0d))

      val inputFields5 = new StddevOperator("stdev", Map("typeOp" -> "string"))
      inputFields5.processReduce(
        Seq(Some(1), Some(2), Some(3), Some(6.5), Some(7.5))) should be(Some("2.850438562747845"))

    }

    "processReduce distinct must be " in {
      val inputFields = new StddevOperator("stdev", Map("distinct" -> "true"))
      inputFields.processReduce(Seq()) should be(Some(0d))

      val inputFields2 = new StddevOperator("stdev", Map("distinct" -> "true"))
      inputFields2.processReduce(Seq(Some(1), Some(2), Some(3), Some(7), Some(7))) should be
      (Some(2.8284271247461903))

      val inputFields3 = new StddevOperator("stdev", Map("distinct" -> "true"))
      inputFields3.processReduce(Seq(Some(1), Some(1), Some(2), Some(3), Some(6.5), Some(7.5))) should be
      (Some(2.850438562747845))

      val inputFields4 = new StddevOperator("stdev", Map("distinct" -> "true"))
      inputFields4.processReduce(Seq(None)) should be(Some(0d))

      val inputFields5 = new StddevOperator("stdev", Map("typeOp" -> "string", "distinct" -> "true"))
      inputFields5.processReduce(
        Seq(Some(1), Some(1), Some(2), Some(3), Some(6.5), Some(7.5))) should be(Some("2.850438562747845"))

    }
  }
}