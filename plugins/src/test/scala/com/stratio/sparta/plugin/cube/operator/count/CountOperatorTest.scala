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
package com.stratio.sparta.plugin.cube.operator.count

import com.stratio.sparta.plugin.cube.operator.count.CountOperator
import com.stratio.sparta.sdk._
import com.stratio.sparta.sdk.pipeline.aggregation.operator.Operator
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class CountOperatorTest extends WordSpec with Matchers {

  "Count operator" should {

    val initSchema = StructType(Seq(
      StructField("field1", IntegerType, false),
      StructField("field2", IntegerType, false),
      StructField("field3", IntegerType, false)
    ))

    val initSchemaTwitter = StructType(Seq(
      StructField("hashtag", StringType, false),
      StructField("unique", StringType, false),
      StructField("field3", IntegerType, false)
    ))

    "Distinct fields must be " in {
      val inputFields = new CountOperator("count", initSchema, Map())
      val distinctFields = inputFields.distinctFields
      distinctFields should be(None)

      val inputFields2 = new CountOperator("count", initSchema, Map("distinctFields" -> "field1"))
      val distinctFields2 = inputFields2.distinctFields
      distinctFields2 should be equals Some(Array[String]("field1"))

      val inputFields3 = new CountOperator("count", initSchema, Map("distinctFields" -> ""))
      val distinctFields3 = inputFields3.distinctFields
      distinctFields3 should be(None)
    }

    "processMap must be " in {
      val inputFields = new CountOperator("count", initSchema, Map())
      inputFields.processMap(Row(1, 2)) should be (Some(CountOperator.One.toLong))

      val inputFields2 =
        new CountOperator("count", initSchema, Map("distinctFields" -> s"field1${Operator.UnderscoreSeparator}field2"))
      inputFields2.processMap(Row(1, 2)).get.toString should be s"field1${Operator.UnderscoreSeparator}field2"

      val inputFields3 = new CountOperator("count", initSchema, Map("distinctFields" -> ""))
      inputFields3.processMap(Row(1, 2)) should be(Some(CountOperator.One))

      val inputFields4 = new CountOperator("count", initSchema,
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2}]"))
      inputFields4.processMap(Row(1, 2)) should be (Some(CountOperator.One.toLong))

      val inputFields5 = new CountOperator("count", initSchema,
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \">\", \"value\":\"2\"}]"))
      inputFields5.processMap(Row(1, 2)) should be(None)

      val inputFields6 = new CountOperator("count", initSchema,
        Map("filters" -> {
          "[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\"}," +
            "{\"field\":\"field2\", \"type\": \"<\", \"value\":\"2\"}]"
        }))
      inputFields6.processMap(Row(1, 2)) should be(None)

      val inputFields7 = new CountOperator("count", initSchema,
        Map("filters" -> {
          "[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\", \"fieldType\":\"int\"}," +
            "{\"field\":\"field2\", \"type\": \"<\", \"value\":\"2\", \"fieldType\":\"int\"}]"
        }))
      inputFields7.processMap(Row(1L, 2L)) should be(None)

      val inputFields8 = new CountOperator("count", initSchema,
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2, \"fieldType\":\"int\"}]"))
      inputFields8.processMap(Row(1, 2)) should be (Some(CountOperator.One.toLong))

      val inputFields9 = new CountOperator("count", initSchema,
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2, \"fieldType\":\"int\"}]"))
      inputFields9.processMap(Row(1L, 2L)) should be (Some(CountOperator.One.toLong))

      val inputFields10 = new CountOperator("count", initSchema,
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2, \"fieldType\":\"int\"}]"))
      inputFields10.processMap(Row(1d, 2d)) should be (Some(CountOperator.One.toLong))

      val inputFields11 = new CountOperator("count", initSchema,
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2, \"fieldType\":\"int\"}]"))
      inputFields11.processMap(Row("1", "2")) should be (Some(CountOperator.One.toLong))

      val inputFields12 = new CountOperator("count", initSchema,
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"=\", \"value\":1, \"fieldType\":\"int\"}]"))
      inputFields12.processMap(Row("1", "2")) should be(Some(CountOperator
        .One.toLong))

      val inputFields13 = new CountOperator("count", initSchema,
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"=\", \"value\":\"1\", \"fieldType\":\"string\"}]"))
      inputFields13.processMap(Row("1", "2")) should be
      (Some(CountOperator.One.toLong))

      val inputFields14 = new CountOperator("count", initSchema,
        Map("filters" -> "[{\"field\":\"field1\", \"type\": \"!=\", \"value\":\"1\", \"fieldType\":\"string\"}]"))
      inputFields14.processMap(Row("1", "2")) should be(None)

      val inputFields15 = new CountOperator("count", initSchema,
        Map("filters" -> ("[{\"field\":\"field1\", \"type\": \"=\", \"value\":\"1\", \"fieldType\":\"string\"}," +
          "{\"field\":\"field2\", \"type\": \"=\", \"value\":\"1\", \"fieldType\":\"string\"}]")))
      inputFields15.processMap(Row("1", "2")) should be(None)

      val inputFields16 = new CountOperator("count", initSchema,
        Map("filters" -> ("[{\"field\":\"field1\", \"type\": \"=\", \"value\":\"1\", \"fieldType\":\"string\"}," +
          "{\"field\":\"field2\", \"type\": \"!=\", \"value\":\"1\", \"fieldType\":\"string\"}]")))
      inputFields16.processMap(Row("1", "2")) should be (Some(CountOperator.One.toLong))

      val inputFields17 = new CountOperator("count", initSchemaTwitter,
        Map("filters" -> ("[{\"field\":\"hashtag\", \"type\": \"!=\", \"value\":\"\", \"fieldType\":\"string\"}," +
          "{\"field\":\"unique\", \"type\": \"=\", \"value\":\"false\", \"fieldType\":\"string\"}]")))
      inputFields17.processMap(Row("hashtag", "false")) should be (Some(CountOperator.One.toLong))

      val inputFields18 = new CountOperator("count", initSchemaTwitter,
        Map("filters" -> ("[{\"field\":\"hashtag\", \"type\": \"!=\", \"value\":\"\", \"fieldType\":\"string\"}," +
          "{\"field\":\"unique\", \"type\": \"=\", \"value\":\"false\", \"fieldType\":\"string\"}]")))
      inputFields18.processMap(Row("hashtag", "true")) should be(None)
    }

    "processReduce must be " in {
      val inputFields = new CountOperator("count",  initSchema,Map())
      inputFields.processReduce(Seq(Some(1L), Some(1L), None)) should be(Some(2L))

      val inputFields2 =
        new CountOperator("count",  initSchema,Map("distinctFields" -> s"field1${Operator.UnderscoreSeparator}field2"))
      inputFields2.processReduce(Seq(Some("field1_field2"))) should be(Some(CountOperator.One.toLong))

      val inputFields3 =
        new CountOperator("count",  initSchema,Map("distinctFields" -> s"field1${Operator.UnderscoreSeparator}field2"))
      inputFields3.processReduce(Seq(Some(s"field1${Operator.UnderscoreSeparator}field2"),
        Some(s"field1${Operator.UnderscoreSeparator}field2"))) should be(Some(CountOperator.One.toLong))

      val inputFields4 =
        new CountOperator("count",  initSchema,Map("distinctFields" -> s"field1${Operator.UnderscoreSeparator}field2"))
      inputFields4.processReduce(Seq(Some(s"field1${Operator.UnderscoreSeparator}field2"),
        Some(s"field1${Operator.UnderscoreSeparator}field3"))) should be(Some(2L))

      val inputFields5 = new CountOperator("count",  initSchema,Map("typeOp" -> "string"))
      inputFields5.processReduce(Seq(Some(1), Some(1))) should be(Some(2))

      val inputFields6 =
        new CountOperator("count",  initSchema,Map("distinctFields" -> s"field1${Operator.UnderscoreSeparator}field2"))
      inputFields6.processReduce(Seq(Some(s"field1${Operator.UnderscoreSeparator}field2"),
        Some(s"field1${Operator.UnderscoreSeparator}field3"),
        Some(s"field1${Operator.UnderscoreSeparator}field3"))) should be(Some(2L))

      val inputFields7 = new CountOperator("count", initSchema, Map("typeOp" -> null))
      inputFields7.processReduce(Seq(Some(1), Some(1))) should be(Some(2))

    }

    "associative process must be " in {
      val inputFields = new CountOperator("count", initSchema, Map())
      val resultInput = Seq((Operator.OldValuesKey, Some(1L)),
        (Operator.NewValuesKey, Some(1L)),
        (Operator.NewValuesKey, None))
      inputFields.associativity(resultInput) should be(Some(2L))

      val inputFields2 = new CountOperator("count", initSchema, Map("typeOp" -> "string"))
      val resultInput2 = Seq((Operator.OldValuesKey, Some(1L)), (Operator.NewValuesKey, Some(1L)))
      inputFields2.associativity(resultInput2) should be(Some("2"))

      val inputFields3 = new CountOperator("count", initSchema, Map("typeOp" -> null))
      val resultInput3 = Seq((Operator.OldValuesKey, Some(1L)), (Operator.NewValuesKey, Some(1L)))
      inputFields3.associativity(resultInput3) should be(Some(2))
    }
  }
}
