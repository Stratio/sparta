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
package com.stratio.sparta.plugin.cube.operator.range

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class RangeOperatorTest extends WordSpec with Matchers {

  "Range operator" should {

    val initSchema = StructType(Seq(
      StructField("field1", IntegerType, false),
      StructField("field2", IntegerType, false),
      StructField("field3", IntegerType, false)
    ))

    val initSchemaFail = StructType(Seq(
      StructField("field2", IntegerType, false)
    ))

    "processMap must be " in {
      val inputField = new RangeOperator("range", initSchema, Map())
      inputField.processMap(Row(1, 2)) should be(None)

      val inputFields2 = new RangeOperator("range", initSchemaFail, Map("inputField" -> "field1"))
      inputFields2.processMap(Row(1, 2)) should be(None)

      val inputFields3 = new RangeOperator("range", initSchema, Map("inputField" -> "field1"))
      inputFields3.processMap(Row(1, 2)) should be(Some(1))

      val inputFields4 = new RangeOperator("range", initSchema, Map("inputField" -> "field1"))
      inputFields3.processMap(Row("1", 2)) should be(Some(1))

      val inputFields6 = new RangeOperator("range", initSchema, Map("inputField" -> "field1"))
      inputFields6.processMap(Row(1.5, 2)) should be(Some(1.5))

      val inputFields7 = new RangeOperator("range", initSchema, Map("inputField" -> "field1"))
      inputFields7.processMap(Row(5L, 2)) should be(Some(5L))

      val inputFields8 = new RangeOperator("range", initSchema,
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2}]"))
      inputFields8.processMap(Row(1, 2)) should be(Some(1L))

      val inputFields9 = new RangeOperator("range", initSchema,
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \">\", \"value\":\"2\"}]"))
      inputFields9.processMap(Row(1, 2)) should be(None)

      val inputFields10 = new RangeOperator("range", initSchema,
        Map("inputField" -> "field1", "filters" -> {
          "[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\"}," +
            "{\"field\":\"field2\", \"type\": \"<\", \"value\":\"2\"}]"
        }))
      inputFields10.processMap(Row(1, 2)) should be(None)
    }

    "processReduce must be " in {
      val inputFields = new RangeOperator("range", initSchema, Map())
      inputFields.processReduce(Seq()) should be(Some(0d))

      val inputFields2 = new RangeOperator("range", initSchema, Map())
      inputFields2.processReduce(Seq(Some(1), Some(1))) should be(Some(0))

      val inputFields3 = new RangeOperator("range", initSchema, Map())
      inputFields3.processReduce(Seq(Some(1), Some(2), Some(4))) should be(Some(3))

      val inputFields4 = new RangeOperator("range", initSchema, Map())
      inputFields4.processReduce(Seq(None)) should be(Some(0d))

      val inputFields5 = new RangeOperator("range", initSchema, Map("typeOp" -> "string"))
      inputFields5.processReduce(Seq(Some(1), Some(2), Some(3), Some(7), Some(7))) should be(Some("6.0"))
    }

    "processReduce distinct must be " in {
      val inputFields = new RangeOperator("range", initSchema, Map("distinct" -> "true"))
      inputFields.processReduce(Seq()) should be(Some(0d))

      val inputFields2 = new RangeOperator("range", initSchema, Map("distinct" -> "true"))
      inputFields2.processReduce(Seq(Some(1), Some(1))) should be(Some(0))

      val inputFields3 = new RangeOperator("range", initSchema, Map("distinct" -> "true"))
      inputFields3.processReduce(Seq(Some(1), Some(2), Some(4))) should be(Some(3))

      val inputFields4 = new RangeOperator("range", initSchema, Map("distinct" -> "true"))
      inputFields4.processReduce(Seq(None)) should be(Some(0d))

      val inputFields5 = new RangeOperator("range", initSchema, Map("typeOp" -> "string", "distinct" -> "true"))
      inputFields5.processReduce(Seq(Some(1), Some(2), Some(3), Some(7), Some(7))) should be(Some("6.0"))
    }
  }
}
