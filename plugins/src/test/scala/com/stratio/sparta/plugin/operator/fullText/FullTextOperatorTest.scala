/**
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
package com.stratio.sparta.plugin.operator.fullText

import com.stratio.sparta.plugin.operator.fullText.FullTextOperator
import com.stratio.sparta.sdk.Operator
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class FullTextOperatorTest extends WordSpec with Matchers {

  "FullText operator" should {

    val initSchema = StructType(Seq(
      StructField("field1", IntegerType, false),
      StructField("field2", IntegerType, false),
      StructField("field3", IntegerType, false)
    ))

    val initSchemaFail = StructType(Seq(
      StructField("field2", IntegerType, false)
    ))

    "processMap must be " in {
      val inputField = new FullTextOperator("fullText", initSchema, Map())
      inputField.processMap(Row(1, 2)) should be(None)

      val inputFields2 = new FullTextOperator("fullText", initSchemaFail, Map("inputField" -> "field1"))
      inputFields2.processMap(Row(1, 2)) should be(None)

      val inputFields3 = new FullTextOperator("fullText", initSchema, Map("inputField" -> "field1"))
      inputFields3.processMap(Row(1, 2)) should be(Some(1))

      val inputFields4 = new FullTextOperator("fullText", initSchema,
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2}]"))
      inputFields4.processMap(Row(1, 2)) should be(Some(1L))

      val inputFields5 = new FullTextOperator("fullText", initSchema,
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \">\", \"value\":\"2\"}]"))
      inputFields5.processMap(Row(1, 2)) should be(None)

      val inputFields6 = new FullTextOperator("fullText", initSchema,
        Map("inputField" -> "field1", "filters" -> {
          "[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\"}," +
            "{\"field\":\"field2\", \"type\": \"<\", \"value\":\"2\"}]"
        }))
      inputFields6.processMap(Row(1, 2)) should be(None)
    }

    "processReduce must be " in {
      val inputFields = new FullTextOperator("fullText", initSchema, Map())
      inputFields.processReduce(Seq()) should be(Some(""))

      val inputFields2 = new FullTextOperator("fullText", initSchema, Map())
      inputFields2.processReduce(Seq(Some(1), Some(1))) should be(Some(s"1${Operator.SpaceSeparator}1"))

      val inputFields3 = new FullTextOperator("fullText", initSchema, Map())
      inputFields3.processReduce(Seq(Some("a"), Some("b"))) should be(Some(s"a${Operator.SpaceSeparator}b"))
    }

    "associative process must be " in {
      val inputFields = new FullTextOperator("fullText", initSchema, Map())
      val resultInput = Seq((Operator.OldValuesKey, Some(2)), (Operator.NewValuesKey, None))
      inputFields.associativity(resultInput) should be(Some("2"))

      val inputFields2 = new FullTextOperator("fullText", initSchema, Map("typeOp" -> "arraystring"))
      val resultInput2 = Seq((Operator.OldValuesKey, Some(2)),
        (Operator.NewValuesKey, Some(1)))
      inputFields2.associativity(resultInput2) should be(Some(Seq(s"2${Operator.SpaceSeparator}1")))

      val inputFields3 = new FullTextOperator("fullText", initSchema, Map("typeOp" -> null))
      val resultInput3 = Seq((Operator.OldValuesKey, Some(2)), (Operator.OldValuesKey, Some(3)))
      inputFields3.associativity(resultInput3) should be(Some(s"2${Operator.SpaceSeparator}3"))
    }
  }
}