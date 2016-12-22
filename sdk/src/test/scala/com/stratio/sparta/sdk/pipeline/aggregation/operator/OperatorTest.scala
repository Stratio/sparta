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
package com.stratio.sparta.sdk.pipeline.aggregation.operator

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.pipeline.filter.FilterModel
import com.stratio.sparta.sdk.pipeline.aggregation.operator.Operator
import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class OperatorTest extends WordSpec with Matchers {

  "Operator" should {

    "Distinct must be " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("any", StringType))), Map())
      val distinct = operator.distinct
      distinct should be(false)

      val operator2 = new OperatorMock("opTest",
        StructType(Seq(StructField("field1",StringType))),
        Map("distinct" -> "true")
      )
      val distinct2 = operator2.distinct
      distinct2 should be equals Some(true)
    }

    "Filters must be " in {
      val operator = new OperatorMock("opTest",
        StructType(Seq(StructField("field1",StringType))),
        Map("filters" -> """[{"field":"field1", "type": "<", "value":2}]"""))
      val filters = operator.filters
      filters should be equals Array(new FilterModel("field1", "<", Some("2"), None))
      filters.size should be(1)

      val operator2 = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val filters2 = operator2.filters
      filters2.size should be(0)
      filters2 should be equals Array()

      val operator3 = new OperatorMock("opTest",
        StructType(Seq(StructField("field1",StringType))),
        Map("filters" -> """[{"field":"field1", "type":"=", "value": "1"}]""""))
      val filters3 = operator2.filters
      filters3.size should be(0)
      filters3 should be equals Array()

      val operator4 = new OperatorMock("opTest",
        StructType(Seq(StructField("field1",StringType))),
        Map("filters" -> """[{"field":"field1", "type": "<", "value":"2"}]"""))
      val filters4 = operator4.filters
      filters4.size should be(1)
      filters4 should be equals Array(new FilterModel("field1", "<", Some("2"), None))

      val operator5 = new OperatorMock("opTest",
        StructType(Seq(StructField("field1",StringType))),
        Map("filters" ->
          """
             [{"field":"field1", "type": "<", "value":"2"}, {"field":"field2", "type": "<", "value":"2"}]"""
        ))
      val filters5 = operator5.filters
      filters5.size should be(2)
      filters5 should be equals Array(new FilterModel("field1", "<", Some("2"), None),
        new FilterModel("field2", "<", Some("2"), None))
    }

    "getNumberFromSerializable must be " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("any", StringType))), Map())
      operator.getNumberFromAny(1.asInstanceOf[JSerializable]) should be(1.asInstanceOf[Number])
      operator.getNumberFromAny("1".asInstanceOf[JSerializable]) should be(1.asInstanceOf[Number])
    }

    "getDistinctValues must be " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      operator.getDistinctValues(List(1, 2, 1)) should be(List(1, 2, 1))

      val operator2 = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))),
        Map("distinct" -> "true"))
      operator2.getDistinctValues(List(1, 2, 1)) should be(List(1, 2))

      val operator3 = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      operator3.getDistinctValues(List()) should be(List())

      val operator4 = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))),
        Map("distinct" -> "true"))
      operator4.getDistinctValues(List()) should be(List())
    }

    "applyFilters must be " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val inputFields = Row(2,1)
      operator.applyFilters(inputFields) should be(Some(Map("field1" -> 2)))

      val operator2 = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      operator2.applyFilters(Row()) should be(Some(Map()))

      val operator3 = new OperatorMock("opTest",
        StructType(Seq(StructField("field1", StringType))),
        Map("filters" -> """[{"field":"field1", "type": "<", "value":2}]"""))
      val inputFields3 = Row(2)
      operator3.applyFilters(inputFields3) should be(None)

      val operator4 = new OperatorMock("opTest",
        StructType(Seq(
          StructField("field1", StringType),
          StructField("field2",StringType))),
        Map("filters" -> """[{"field":"field1", "type": "<", "value":2}]"""))
      val inputFields4 = Row(1,1)
      operator4.applyFilters(inputFields4) should be(Some(Map(
        "field1" -> 1,
        "field2" -> 1
      )))

      val operator5 = new OperatorMock("opTest",
        StructType(Seq(
          StructField("field1", StringType),
          StructField("field2", StringType)
        )),
        Map("filters" -> """[{"field":"field1", "type": ">", "value":2}]"""))
      operator5.applyFilters(Row(3,1)) should be(Option(Map(
        "field1" -> 3,
        "field2" -> 1
      )))

      val operator6 = new OperatorMock("opTest",
        StructType(Seq(StructField("field1", StringType),
        StructField("field2", StringType)
        )),
        Map("filters" -> """[{"field":"field1", "type": ">", "value":2}]"""))
      operator6.applyFilters(Row(1,1)) should be(None)

      val operator7 = new OperatorMock("opTest",
        StructType(Seq(
          StructField("field1",StringType),
          StructField("field2",StringType)
        )),
        Map("filters" -> """[{"field":"field1", "type": ">=", "value":2}]"""))
      val inputFields7 = Row(2,1)
      operator7.applyFilters(inputFields7) should be(Some(Map(
        "field1" -> 2,
        "field2" -> 1
      )))

      val operator8 = new OperatorMock("opTest",
        StructType(Seq(StructField("field1",StringType))),
        Map("filters" -> """[{"field":"field1", "type": ">=", "value":2}]"""))
      operator8.applyFilters(Row(1,1)) should be(None)

      val operator9 = new OperatorMock("opTest",
        StructType(Seq(
          StructField("field1", StringType),
          StructField("field2", StringType)
        )),
        Map("filters" -> """[{"field":"field1", "type": "<=", "value":2}]"""))
      val inputFields9 = Row(2,1)
      operator9.applyFilters(inputFields9) should be(Some(Map(
        "field1" -> 2,
        "field2" -> 1
      )))

      val operator10 = new OperatorMock("opTest",
        StructType(Seq(StructField("field1",StringType))),
        Map("filters" -> """[{"field":"field1", "type": "<=", "value":2}]"""))
      val inputFields10 = Row(3,1)
      operator10.applyFilters(inputFields10) should be(None)

      val operator11 = new OperatorMock("opTest",
        StructType(Seq(StructField("field1",StringType))),
        Map("filters" -> """[{"field":"field1", "type": "=", "value":2}]"""))
      val inputFields11 = Row(1,1)
      operator11.applyFilters(inputFields11) should be(None)

      val operator12 = new OperatorMock("opTest",
        StructType(Seq(
          StructField("field1",StringType),
          StructField("field2",StringType)
        )),
        Map("filters" -> """[{"field":"field1", "type": "=", "value":2}]"""))
      val inputFields12 = Row(2,1)
      operator12.applyFilters(inputFields12) should be(Some(Map(
        "field1" -> 2,
        "field2" -> 1
      )))

      val operator13 = new OperatorMock("opTest",
        StructType(Seq(
          StructField("field1",StringType),
          StructField("field2",StringType)
        )),
        Map("filters" -> """[{"field":"field1", "type": "!=", "value":2}]"""))
      val inputFields13 = Row(1,1)
      operator13.applyFilters(inputFields13) should be(Some(Map(
        "field1" -> 1,
        "field2" -> 1
      )))


      val operator14 = new OperatorMock("opTest",
        StructType(Seq(StructField("field1",StringType))),
        Map("filters" -> """[{"field":"field1", "type": "!=", "value":2}]"""))
      val inputFields14 = Row(2,1)
      operator14.applyFilters(inputFields14) should be(None)

      val operator15 = new OperatorMock("opTest",
        StructType(Seq(
          StructField("field1",StringType),
          StructField("field2",StringType)
        )),
        Map("filters" -> """[{"field":"field1", "type": "<", "fieldValue":"field2"}]"""))
      val inputFields15 = Row(2,1)
      operator15.applyFilters(inputFields15) should be(None)

      val operator16 = new OperatorMock("opTest",
        StructType(Seq(
          StructField("field1",StringType),
          StructField("field2",StringType)
        )),
        Map("filters" -> """[{"field":"field1", "type": "<", "fieldValue":"field2"}]"""))
      val inputFields16 = Row(2,3)
      operator16.applyFilters(inputFields16) should be(Some(Map(
        "field1" -> 2,
        "field2" -> 3
      )))

      val operator17 = new OperatorMock("opTest",
        StructType(Seq(
          StructField("field1",StringType),
          StructField("field2",StringType)
        )),
        Map("filters" -> """[{"field":"field1", "type": "<", "fieldValue":"field2", "value":2 }]"""))
      val inputFields17 = Row(1,4)
      operator17.applyFilters(inputFields17) should be(Some(Map(
        "field1" -> 1,
        "field2" -> 4
      )))

      val operator18 = new OperatorMock("opTest",
        StructType(Seq(StructField("field1",StringType))),
        Map("filters" -> """[{"field":"field1", "type": "<", "fieldValue":"field2", "value":2 }]"""))
      val inputFields18 = Row(2,1)
      operator18.applyFilters(inputFields18) should be(None)

      val operator19 = new OperatorMockString("opTest",
        StructType(Seq(
          StructField("field1", StringType),
          StructField("field2", StringType)
        )),
        Map("filters" -> """[{"field":"field1", "type": "=", "value":2}]"""))
      val inputFields19 = Row(2,1)
      operator19.applyFilters(inputFields19) should be(Some(Map(
        "field1" -> 2,
        "field2" -> 1
      )))

      val operator20 = new OperatorMockString("opTest",
        StructType(Seq(
          StructField("field1", StringType),
          StructField("field2", StringType)
        )),
        Map("filters" -> """[{"field":"field1", "type": "=","fieldValue":"field2"}]"""))
      val inputFields20 = Row(2,1)
      operator20.applyFilters(inputFields20) should be(None)
    }

    "Operation compare with other operator must be less " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val operator2 = new OperatorMock("upTest", StructType(Seq(StructField("field1",StringType))), Map())
      val expected = -6
      val result = operator.compare(operator2)
      result should be(expected)
    }

    "Operation compare with other operator must be equals " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val operator2 = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val expected = 0
      val result = operator.compare(operator2)
      result should be(expected)
    }

    "Operation compare with other operator must be higher " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val operator2 = new OperatorMock("apTest", StructType(Seq(StructField("field1",StringType))), Map())
      val expected = 14
      val result = operator.compare(operator2)
      result should be(expected)
    }

    "Operation properties must be " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val expected = Map()
      val result = operator.operationProps
      result should be(expected)
    }

    "Operation type must be " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val expected = TypeOp.Long
      val result = operator.defaultTypeOperation
      result should be(expected)
    }

    "Operation key must be " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val expected = "opTest"
      val result = operator.key
      result should be(expected)
    }

    "Operation casting filter must be " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val expected = TypeOp.Number
      val result = operator.defaultCastingFilterType
      result should be(expected)
    }

    "Operation return type must be " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val expected = TypeOp.Long
      val result = operator.returnType
      result should be(expected)
    }

    "Operation associativity must be " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val expected = false
      val result = operator.isAssociative
      result should be(expected)
    }

    "Operation extract old values must be " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val expected = Seq(1)
      val result = operator.extractValues(Seq((Operator.OldValuesKey, Some(1))), Some(Operator.OldValuesKey))
      result should be(expected)
    }

    "Operation extract new values must be " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val expected = Seq(1)
      val result = operator.extractValues(Seq((Operator.NewValuesKey, Some(1))), Some(Operator.NewValuesKey))
      result should be(expected)
    }

    "Operation extract old values must be empty " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val expected = Seq()
      val result = operator.extractValues(Seq((Operator.NewValuesKey, Some(1))), Some(Operator.OldValuesKey))
      result should be(expected)
    }

    "Operation extract new values must be empty " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val expected = Seq()
      val result = operator.extractValues(Seq((Operator.OldValuesKey, Some(1))), Some(Operator.NewValuesKey))
      result should be(expected)
    }

    "Operation extract values must be " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      val expected = Seq(1)
      val result = operator.extractValues(Seq((Operator.OldValuesKey, Some(1))), None)
      result should be(expected)
    }

    "Operation number casting must be " in {
      val operator = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      operator.getNumberFromAny(2) should be(2)
      operator.getNumberFromAny(2L) should be(2)
      operator.getNumberFromAny(2d) should be(2)
      operator.getNumberFromAny(2.asInstanceOf[Byte]) should be(2)
      operator.getNumberFromAny(2.asInstanceOf[Short]) should be(2)
    }

    "classSuffix must be " in {
      val expected = "Operator"
      val result = Operator.ClassSuffix
      result should be(expected)
    }

    "processMap must be " in {
      val inputFields = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))), Map())
      inputFields.processMap(Row(1, 2)) should be(None)

      val inputFields2 = new OperatorMock("opTest", StructType(Seq(StructField("field1",StringType))),
        Map("inputField" -> "field1"))
      inputFields2.processMap(Row(1, 2)) should be(Some(1))
    }
  }
}
