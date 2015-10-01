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

package com.stratio.sparkta.plugin.operator.accumulator

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class AccumulatorOperatorTestSpec extends WordSpec with Matchers {

  "Accumulator operator" should {

    "processMap must be " in {
      val inputField = new AccumulatorOperator("accumulator", Map())
      inputField.processMap(Map("field1" -> 1, "field2" -> 2)) should be(None)

      val inputFields2 = new AccumulatorOperator("accumulator", Map("inputField" -> "field1"))
      inputFields2.processMap(Map("field3" -> 1, "field2" -> 2)) should be(None)

      val inputFields3 = new AccumulatorOperator("accumulator", Map("inputField" -> "field1"))
      inputFields3.processMap(Map("field1" -> 1, "field2" -> 2)) should be(Some(1))

      val inputFields4 = new AccumulatorOperator("accumulator",
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2}]"))
      inputFields4.processMap(Map("field1" -> 1, "field2" -> 2)) should be(Some(1L))

      val inputFields5 = new AccumulatorOperator("accumulator",
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \">\", \"value\":\"2\"}]"))
      inputFields5.processMap(Map("field1" -> 1, "field2" -> 2)) should be(None)

      val inputFields6 = new AccumulatorOperator("accumulator",
        Map("inputField" -> "field1", "filters" -> {"[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\"}," +
          "{\"field\":\"field2\", \"type\": \"<\", \"value\":\"2\"}]"}))
      inputFields6.processMap(Map("field1" -> 1, "field2" -> 2)) should be(None)
    }

    "processReduce must be " in {
      val inputFields = new AccumulatorOperator("accumulator", Map())
      inputFields.processReduce(Seq()) should be(Some(Seq()))

      val inputFields2 = new AccumulatorOperator("accumulator", Map())
      inputFields2.processReduce(Seq(Some(1), Some(1))) should be(Some(Seq("1", "1")))

      val inputFields3 = new AccumulatorOperator("accumulator", Map())
      inputFields3.processReduce(Seq(Some("a"), Some("b"))) should be(Some(Seq("a", "b")))

      val inputFields4 = new AccumulatorOperator("accumulator", Map("typeOp" -> "string"))
      inputFields4.processReduce(Seq(Some(1), Some(1))) should be(Some("1_1"))

      val inputFields5 = new AccumulatorOperator("accumulator", Map("typeOp" -> "string"))

      //I know, null sucks, but it's the only way test the "Try" inner
      //scalastyle:off
      inputFields5.processReduce(null) should be(Some(""))
      //scalastyle:on
    }

    "processReduce distinct must be " in {
      val inputFields = new AccumulatorOperator("accumulator", Map("distinct" -> "true"))
      inputFields.processReduce(Seq()) should be(Some(Seq()))

      val inputFields2 = new AccumulatorOperator("accumulator", Map("distinct" -> "true"))
      inputFields2.processReduce(Seq(Some(1), Some(1))) should be(Some(Seq("1")))

      val inputFields3 = new AccumulatorOperator("accumulator", Map("distinct" -> "true"))
      inputFields3.processReduce(Seq(Some("a"), Some("b"))) should be(Some(Seq("a", "b")))

      val inputFields4 = new AccumulatorOperator("accumulator", Map("typeOp" -> "string", "distinct" -> "true"))
      inputFields4.processReduce(Seq(Some(1), Some(1))) should be(Some("1"))
    }
  }
}