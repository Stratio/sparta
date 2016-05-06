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
package com.stratio.sparta.plugin.operator.mean

import com.stratio.sparta.sdk.Operator
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class MeanAssociativeOperatorTest extends WordSpec with Matchers {

  "Mean operator" should {

    val initSchema = StructType(Seq(
      StructField("field1", IntegerType, false),
      StructField("field2", IntegerType, false),
      StructField("field3", IntegerType, false)
    ))

    val initSchemaFail = StructType(Seq(
      StructField("field2", IntegerType, false)
    ))

    "processMap must be " in {
      val inputField = new MeanAssociativeOperator("avg", initSchema, Map())
      inputField.processMap(Row(1, 2)) should be(None)

      val inputFields2 = new MeanAssociativeOperator("avg", initSchemaFail, Map("inputField" -> "field1"))
      inputFields2.processMap(Row(1, 2)) should be(None)

      val inputFields3 = new MeanAssociativeOperator("avg", initSchema, Map("inputField" -> "field1"))
      inputFields3.processMap(Row(1, 2)) should be(Some(1))

      val inputFields4 = new MeanAssociativeOperator("avg", initSchema, Map("inputField" -> "field1"))
      inputFields4.processMap(Row("1", 2)) should be(Some(1))

      val inputFields6 = new MeanAssociativeOperator("avg", initSchema, Map("inputField" -> "field1"))
      inputFields6.processMap(Row(1.5, 2)) should be(Some(1.5))

      val inputFields7 = new MeanAssociativeOperator("avg", initSchema, Map("inputField" -> "field1"))
      inputFields7.processMap(Row(5L, 2)) should be(Some(5L))

      val inputFields8 = new MeanAssociativeOperator("avg", initSchema,
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2}]"))
      inputFields8.processMap(Row(1, 2)) should be(Some(1L))

      val inputFields9 = new MeanAssociativeOperator("avg", initSchema,
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \">\", \"value\":\"2\"}]"))
      inputFields9.processMap(Row(1, 2)) should be(None)

      val inputFields10 = new MeanAssociativeOperator("avg", initSchema,
        Map("inputField" -> "field1", "filters" -> {
          "[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\"}," +
            "{\"field\":\"field2\", \"type\": \"<\", \"value\":\"2\"}]"
        }))
      inputFields10.processMap(Row(1, 2)) should be(None)
    }

    "processReduce must be " in {
      val inputFields = new MeanAssociativeOperator("avg", initSchema, Map())
      inputFields.processReduce(Seq()) should be(Some(List()))

      val inputFields2 = new MeanAssociativeOperator("avg", initSchema, Map())
      inputFields2.processReduce(Seq(Some(1), Some(1), None)) should be (Some(List(1.0, 1.0)))

      val inputFields3 = new MeanAssociativeOperator("avg", initSchema, Map())
      inputFields3.processReduce(Seq(Some(1), Some(2), Some(3), None)) should be(Some(List(1.0, 2.0, 3.0)))

      val inputFields4 = new MeanAssociativeOperator("avg", initSchema, Map())
      inputFields4.processReduce(Seq(None)) should be(Some(List()))
    }

    "processReduce distinct must be " in {
      val inputFields = new MeanAssociativeOperator("avg", initSchema, Map("distinct" -> "true"))
      inputFields.processReduce(Seq()) should be(Some(List()))

      val inputFields2 = new MeanAssociativeOperator("avg", initSchema, Map("distinct" -> "true"))
      inputFields2.processReduce(Seq(Some(1), Some(1), None)) should be(Some(List(1.0)))

      val inputFields3 = new MeanAssociativeOperator("avg", initSchema, Map("distinct" -> "true"))
      inputFields3.processReduce(Seq(Some(1), Some(3), Some(1), None)) should be(Some(List(1.0, 3.0)))

      val inputFields4 = new MeanAssociativeOperator("avg", initSchema, Map("distinct" -> "true"))
      inputFields4.processReduce(Seq(None)) should be(Some(List()))
    }

    "associative process must be " in {
      val inputFields = new MeanAssociativeOperator("avg", initSchema, Map())
      val resultInput =
        Seq((Operator.OldValuesKey, Some(Map("count" -> 1d, "sum" -> 2d, "mean" -> 2d))), (Operator.NewValuesKey, None))
      inputFields.associativity(resultInput) should be(Some(Map("count" -> 1.0, "sum" -> 2.0, "mean" -> 2.0)))

      val inputFields2 = new MeanAssociativeOperator("avg", initSchema, Map())
      val resultInput2 = Seq((Operator.OldValuesKey, Some(Map("count" -> 1d, "sum" -> 2d, "mean" -> 2d))),
        (Operator.NewValuesKey, Some(Seq(1d))))
      inputFields2.associativity(resultInput2) should be(Some(Map("sum" -> 3.0, "count" -> 2.0, "mean" -> 1.5)))
    }
  }
}
