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

package com.stratio.sparkta.sdk

import java.io.{Serializable => JSerializable}

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import com.stratio.sparkta.sdk.test.{OperatorMock, OperatorMockString}

@RunWith(classOf[JUnitRunner])
class OperatorSpec extends WordSpec with Matchers {

  "Operator" should {

    "It throw an exception when properties is null" in {
      an[IllegalArgumentException] should be thrownBy new OperatorMock("opTest", null)
    }

    "Distinct must be " in {
      val operator = new OperatorMock("opTest", Map())
      val distinct = operator.distinct
      distinct should be(false)

      val operator2 = new OperatorMock("opTest", Map("distinct" -> "true"))
      val distinct2 = operator2.distinct
      distinct2 should be equals Some(true)
    }

    "Filters must be " in {
      val operator = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2}]"))
      val filters = operator.filters
      filters should be equals Array(new FilterModel("field1", "<", Some("2"), None))
      filters.size should be(1)

      val operator2 = new OperatorMock("opTest", Map())
      val filters2 = operator2.filters
      filters2.size should be(0)
      filters2 should be equals Array()

      val operator3 = new OperatorMock("opTest", Map("filters" -> "[\"field\":\"]"))
      val filters3 = operator2.filters
      filters3.size should be(0)
      filters3 should be equals Array()

      val operator4 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\"}]"))
      val filters4 = operator4.filters
      filters4.size should be(1)
      filters4 should be equals Array(new FilterModel("field1", "<", Some("2"), None))

      val operator5 = new OperatorMock("opTest",
        Map("filters" -> {
          "[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\"}," +
            "{\"field\":\"field2\", \"type\": \"<\", \"value\":\"2\"}]"
        }))
      val filters5 = operator5.filters
      filters5.size should be(2)
      filters5 should be equals Array(new FilterModel("field1", "<", Some("2"), None),
        new FilterModel("field2", "<", Some("2"), None))
    }

    "getNumberFromSerializable must be " in {
      val operator = new OperatorMock("opTest", Map())
      operator.getNumberFromSerializable(1.asInstanceOf[JSerializable]) should be(Some(1.asInstanceOf[Number]))

      val operator2 = new OperatorMock("opTest", Map())
      operator2.getNumberFromSerializable("1".asInstanceOf[JSerializable]) should be(Some(1.asInstanceOf[Number]))
    }

    "getDistinctValues must be " in {
      val operator = new OperatorMock("opTest", Map())
      operator.getDistinctValues(List(1, 2, 1)) should be(List(1, 2, 1))

      val operator2 = new OperatorMock("opTest", Map("distinct" -> "true"))
      operator2.getDistinctValues(List(1, 2, 1)) should be(List(1, 2))

      val operator3 = new OperatorMock("opTest", Map())
      operator3.getDistinctValues(List()) should be(List())

      val operator4 = new OperatorMock("opTest", Map("distinct" -> "true"))
      operator4.getDistinctValues(List()) should be(List())
    }

    "applyFilters must be " in {
      val operator = new OperatorMock("opTest", Map())
      val inputFields = Map("field1" -> 2.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator.applyFilters(inputFields) should be(Some(inputFields))

      val operator2 = new OperatorMock("opTest", Map())
      operator2.applyFilters(Map()) should be(Some(Map()))

      val operator3 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2}]"))
      val inputFields3 = Map("field1" -> 2.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator3.applyFilters(inputFields3) should be(None)

      val operator4 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2}]"))
      val inputFields4 = Map("field1" -> 1.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator4.applyFilters(inputFields4) should be(Some(inputFields4))

      val operator5 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \">\", \"value\":2}]"))
      val inputFields5 = Map("field1" -> 3.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator5.applyFilters(inputFields5) should be(Some(inputFields5))

      val operator6 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \">\", \"value\":2}]"))
      val inputFields6 = Map("field1" -> 1.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator6.applyFilters(inputFields6) should be(None)

      val operator7 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \">=\", \"value\":2}]"))
      val inputFields7 = Map("field1" -> 2.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator7.applyFilters(inputFields7) should be(Some(inputFields7))

      val operator8 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \">=\", \"value\":2}]"))
      val inputFields8 = Map("field1" -> 1.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator8.applyFilters(inputFields8) should be(None)

      val operator9 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<=\", \"value\":2}]"))
      val inputFields9 = Map("field1" -> 2.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator9.applyFilters(inputFields9) should be(Some(inputFields9))

      val operator10 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<=\", \"value\":2}]"))
      val inputFields10 = Map("field1" -> 3.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator10.applyFilters(inputFields10) should be(None)

      val operator11 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"=\", \"value\":2}]"))
      val inputFields11 = Map("field1" -> 1.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator11.applyFilters(inputFields11) should be(None)

      val operator12 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"=\", \"value\":2}]"))
      val inputFields12 = Map("field1" -> 2.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator12.applyFilters(inputFields12) should be(Some(inputFields12))

      val operator13 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"!=\", \"value\":2}]"))
      val inputFields13 = Map("field1" -> 1.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator13.applyFilters(inputFields13) should be(Some(inputFields13))

      val operator14 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"!=\", \"value\":2}]"))
      val inputFields14 = Map("field1" -> 2.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator14.applyFilters(inputFields14) should be(None)

      val operator15 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"fieldValue\":\"field2\"}]"))
      val inputFields15 = Map("field1" -> 2.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator15.applyFilters(inputFields15) should be(None)

      val operator16 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"fieldValue\":\"field2\"}]"))
      val inputFields16 = Map("field1" -> 2.asInstanceOf[JSerializable], "field2" -> 3.asInstanceOf[JSerializable])
      operator16.applyFilters(inputFields16) should be(Some(inputFields16))

      val operator17 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"fieldValue\":\"field2\", \"value\":2 }]"))
      val inputFields17 = Map("field1" -> 1.asInstanceOf[JSerializable], "field2" -> 4.asInstanceOf[JSerializable])
      operator17.applyFilters(inputFields17) should be(Some(inputFields17))

      val operator18 = new OperatorMock("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"fieldValue\":\"field2\", \"value\":2 }]"))
      val inputFields18 = Map("field1" -> 2.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator18.applyFilters(inputFields18) should be(None)

      val operator19 = new OperatorMockString("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"=\", \"value\":2}]"))
      val inputFields19 = Map("field1" -> 2.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator19.applyFilters(inputFields19) should be(Some(inputFields19))

      val operator20 = new OperatorMockString("opTest",
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"=\",\"fieldValue\":\"field2\"}]"))
      val inputFields20 = Map("field1" -> 2.asInstanceOf[JSerializable], "field2" -> 1.asInstanceOf[JSerializable])
      operator20.applyFilters(inputFields20) should be(None)
    }

    "Operation compare with other operator must be less " in {
      val operator = new OperatorMock("opTest", Map())
      val operator2 = new OperatorMock("upTest", Map())
      val expected = -6
      val result = operator.compare(operator2)
      result should be(expected)
    }

    "Operation compare with other operator must be equals " in {
      val operator = new OperatorMock("opTest", Map())
      val operator2 = new OperatorMock("opTest", Map())
      val expected = 0
      val result = operator.compare(operator2)
      result should be(expected)
    }

    "Operation compare with other operator must be higher " in {
      val operator = new OperatorMock("opTest", Map())
      val operator2 = new OperatorMock("apTest", Map())
      val expected = 14
      val result = operator.compare(operator2)
      result should be(expected)
    }

    "Operation properties must be " in {
      val operator = new OperatorMock("opTest", Map())
      val expected = Map()
      val result = operator.operationProps
      result should be(expected)
    }

    "Operation type must be " in {
      val operator = new OperatorMock("opTest", Map())
      val expected = TypeOp.Long
      val result = operator.defaultTypeOperation
      result should be(expected)
    }

    "Operation key must be " in {
      val operator = new OperatorMock("opTest", Map())
      val expected = "opTest"
      val result = operator.key
      result should be(expected)
    }

    "Operation casting filter must be " in {
      val operator = new OperatorMock("opTest", Map())
      val expected = TypeOp.Number
      val result = operator.castingFilterType
      result should be(expected)
    }

    "Operation return type must be " in {
      val operator = new OperatorMock("opTest", Map())
      val expected = TypeOp.Long
      val result = operator.returnType
      result should be(expected)
    }

    "Operation number casting must be " in {
      val operator = new OperatorMock("opTest", Map())

      operator.getNumberFromSerializable(2) should be(Some(2))
      operator.getNumberFromSerializable(2L) should be(Some(2))
      operator.getNumberFromSerializable(2d) should be(Some(2))
      operator.getNumberFromSerializable(2.asInstanceOf[Byte]) should be(Some(2))
      operator.getNumberFromSerializable(2.asInstanceOf[Short]) should be(Some(2))
      operator.getNumberFromSerializable(Option(2)) should be(None)
    }

    "classSuffix must be " in {
      val expected = "Operator"
      val result = Operator.ClassSuffix
      result should be(expected)
    }
  }
}
