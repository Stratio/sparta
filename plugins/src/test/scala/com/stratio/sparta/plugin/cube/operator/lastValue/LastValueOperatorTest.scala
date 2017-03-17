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
package com.stratio.sparta.plugin.cube.operator.lastValue

import java.util.Date

import com.stratio.sparta.sdk.pipeline.aggregation.operator.Operator
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class LastValueOperatorTest extends WordSpec with Matchers {

  "LastValue operator" should {

    val initSchema = StructType(Seq(
      StructField("field1", IntegerType, false),
      StructField("field2", IntegerType, false),
      StructField("field3", IntegerType, false)
    ))

    val initSchemaFail = StructType(Seq(
      StructField("field2", IntegerType, false)
    ))

    "processMap must be " in {
      val inputField = new LastValueOperator("lastValue", initSchema, Map())
      inputField.processMap(Row(1, 2)) should be(None)

      val inputFields2 = new LastValueOperator("lastValue", initSchemaFail, Map("inputField" -> "field1"))
      inputFields2.processMap(Row(1, 2)) should be(None)

      val inputFields3 = new LastValueOperator("lastValue", initSchema, Map("inputField" -> "field1"))
      inputFields3.processMap(Row(1, 2)) should be(Some(1))

      val inputFields4 = new LastValueOperator("lastValue", initSchema,
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2}]"))
      inputFields4.processMap(Row(1, 2)) should be(Some(1L))

      val inputFields5 = new LastValueOperator("lastValue", initSchema,
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \">\", \"value\":\"2\"}]"))
      inputFields5.processMap(Row(1, 2)) should be(None)

      val inputFields6 = new LastValueOperator("lastValue", initSchema,
        Map("inputField" -> "field1", "filters" -> {
          "[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\"}," +
            "{\"field\":\"field2\", \"type\": \"<\", \"value\":\"2\"}]"
        }))
      inputFields6.processMap(Row(1, 2)) should be(None)
    }

    "processReduce must be " in {
      val inputFields = new LastValueOperator("lastValue", initSchema, Map())
      inputFields.processReduce(Seq()) should be(None)

      val inputFields2 = new LastValueOperator("lastValue", initSchema, Map())
      inputFields2.processReduce(Seq(Some(1), Some(2))) should be(Some(2))

      val inputFields3 = new LastValueOperator("lastValue", initSchema, Map())
      inputFields3.processReduce(Seq(Some("a"), Some("b"))) should be(Some("b"))
    }

    "associative process must be " in {
      val inputFields = new LastValueOperator("lastValue", initSchema, Map())
      val resultInput = Seq((Operator.OldValuesKey, Some(1L)),
        (Operator.NewValuesKey, Some(1L)),
        (Operator.NewValuesKey, None))
      inputFields.associativity(resultInput) should be(Some(1L))

      val inputFields2 = new LastValueOperator("lastValue", initSchema, Map("typeOp" -> "int"))
      val resultInput2 = Seq((Operator.OldValuesKey, Some(1L)),
        (Operator.NewValuesKey, Some(1L)))
      inputFields2.associativity(resultInput2) should be(Some(1))

      val inputFields3 = new LastValueOperator("lastValue", initSchema, Map("typeOp" -> null))
      val resultInput3 = Seq((Operator.OldValuesKey, Some(1)),
        (Operator.NewValuesKey, Some(2)))
      inputFields3.associativity(resultInput3) should be(Some(2))

      val inputFields4 = new LastValueOperator("lastValue", initSchema, Map())
      val resultInput4 = Seq()
      inputFields4.associativity(resultInput4) should be(None)

      val inputFields5 = new LastValueOperator("lastValue", initSchema, Map())
      val date = new Date()
      val resultInput5 = Seq((Operator.NewValuesKey, Some(date)))
      inputFields5.associativity(resultInput5) should be(Some(date))
    }
  }
}
