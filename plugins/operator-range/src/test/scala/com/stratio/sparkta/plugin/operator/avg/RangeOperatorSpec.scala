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

package com.stratio.sparkta.plugin.operator.range

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class RangeOperatorSpec extends WordSpec with Matchers {

  "Avg operator" should {

    "processMap must be " in {
      val inputField = new RangeOperator(Map())
      inputField.processMap(Map("field1" -> 1, "field2" -> 2)) should be(Some(0d))

      val inputFields2 = new RangeOperator(Map("inputField" -> "field1"))
      inputFields2.processMap(Map("field3" -> 1, "field2" -> 2)) should be(Some(0d))

      val inputFields3 = new RangeOperator(Map("inputField" -> "field1"))
      inputFields3.processMap(Map("field1" -> 1, "field2" -> 2)) should be(Some(1))

      val inputFields4 = new RangeOperator(Map("inputField" -> "field1"))
      inputFields3.processMap(Map("field1" -> "1", "field2" -> 2)) should be(Some(1))

      val inputFields5 = new RangeOperator(Map("inputField" -> "field1"))
      inputFields5.processMap(Map("field1" -> "foo", "field2" -> 2)) should be(Some(0))

      val inputFields6 = new RangeOperator(Map("inputField" -> "field1"))
      inputFields6.processMap(Map("field1" -> 1.5, "field2" -> 2)) should be(Some(1.5))

      val inputFields7 = new RangeOperator(Map("inputField" -> "field1"))
      inputFields7.processMap(Map("field1" -> 5L, "field2" -> 2)) should be(Some(5L))
    }

    "processReduce must be " in {
      val inputFields = new RangeOperator(Map())
      inputFields.processReduce(Seq()) should be(Some(0d))

      val inputFields2 = new RangeOperator(Map())
      inputFields2.processReduce(Seq(Some(1), Some(1))) should be(Some(0))

      val inputFields3 = new RangeOperator(Map())
      inputFields3.processReduce(Seq(Some(1), Some(2), Some(4))) should be(Some(3))
    }
  }
}