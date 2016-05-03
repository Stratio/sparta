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
package com.stratio.sparta.plugin.operator.variance

import com.stratio.sparta.plugin.operator.variance.VarianceOperator
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class VarianceOperatorTest extends WordSpec with Matchers {

  "Variance operator" should {

    "processMap must be " in {
      val schema1 = StructType(Seq(StructField("field1", IntegerType), StructField("field2", IntegerType)))
      val inputField = new VarianceOperator("variance", schema1, Map())
      inputField.processMap(Row(1, 2)) should be(None)

      val schema2 = StructType(Seq(StructField("field3", IntegerType), StructField("field2", IntegerType)))
      val inputFields2 = new VarianceOperator("variance", schema2, Map("inputField" -> "field1"))
      inputFields2.processMap(Row(1, 2)) should be(None)

      val schema3 = StructType(Seq(StructField("field1", IntegerType), StructField("field2", IntegerType)))
      val inputFields3 = new VarianceOperator("variance", schema3, Map("inputField" -> "field1"))
      inputFields3.processMap(Row(1, 2)) should be(Some(1))

      val schema4 = StructType(Seq(StructField("field1", IntegerType), StructField("field2", IntegerType)))
      val inputFields4 = new VarianceOperator("variance", schema4, Map("inputField" -> "field1"))
      inputFields3.processMap(Row(1, 2)) should be(Some(1))

      val schema6 = StructType(Seq(StructField("field1", FloatType), StructField("field2", IntegerType)))
      val inputFields6 = new VarianceOperator("variance", schema6, Map("inputField" -> "field1"))
      inputFields6.processMap(Row(1.5, 2)) should be(Some(1.5))

      val schema7 = StructType(Seq(StructField("field1", LongType), StructField("field2", IntegerType)))
      val inputFields7 = new VarianceOperator("variance", schema7, Map("inputField" -> "field1"))
      inputFields7.processMap(Row(5L, 2)) should be(Some(5L))


      val schema8 = StructType(Seq(StructField("field1", IntegerType), StructField("field2", IntegerType)))
      val inputFields8 = new VarianceOperator("variance", schema8,
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \"<\", \"value\":2}]"))
      inputFields8.processMap(Row(1, 2)) should be(Some(1L))

      val schema9 = StructType(Seq(StructField("field1", IntegerType), StructField("field2", IntegerType)))
      val inputFields9 = new VarianceOperator("variance", schema9,
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \">\", \"value\":\"2\"}]"))
      inputFields9.processMap(Row(1, 2)) should be(None)

      val schema10 = StructType(Seq(StructField("field1", IntegerType), StructField("field2", IntegerType)))
      val inputFields10 = new VarianceOperator("variance", schema10,
        Map("inputField" -> "field1", "filters" -> {
          "[{\"field\":\"field1\", \"type\": \"<\", \"value\":\"2\"}," +
            "{\"field\":\"field2\", \"type\": \"<\", \"value\":\"2\"}]"
        }))
      inputFields10.processMap(Row(1, 2)) should be(None)
    }

    "processReduce must be " in {
      val schema11 = StructType(Seq(StructField("field1", DoubleType)))
      val inputFields = new VarianceOperator("variance", schema11, Map())
      inputFields.processReduce(Seq()) should be(Some(0d))

      val schema12 = StructType(Seq(
        StructField("field1", IntegerType),
        StructField("field2", IntegerType),
        StructField("field3", IntegerType),
        StructField("field4", IntegerType),
        StructField("field5", IntegerType)
      ))
      val inputFields2 = new VarianceOperator("variance", schema12, Map())
      inputFields2.processReduce(Seq(Some(1), Some(2), Some(3), Some(7), Some(7))) should be(Some(8))

      val schema13 = StructType(Seq(
        StructField("field1", IntegerType),
        StructField("field2", IntegerType),
        StructField("field3", IntegerType),
        StructField("field4", FloatType),
        StructField("field5", FloatType)
      ))
      val inputFields3 = new VarianceOperator("variance", schema13, Map())
      inputFields3.processReduce(Seq(Some(1), Some(2), Some(3), Some(6.5), Some(7.5))) should be(Some(8.125))

      val schema14 = StructType(Seq())
      val inputFields4 = new VarianceOperator("variance", schema14, Map())
      inputFields4.processReduce(Seq(None)) should be(Some(0d))

      val schema15 = StructType(Seq(
        StructField("field1", IntegerType),
        StructField("field2", IntegerType),
        StructField("field3", IntegerType),
        StructField("field4", IntegerType),
        StructField("field5", IntegerType)
      ))
      val inputFields5 = new VarianceOperator("variance", schema15, Map("typeOp" -> "string"))
      inputFields5.processReduce(Seq(Some(1), Some(2), Some(3), Some(7), Some(7))) should be(Some("8.0"))
    }

    "processReduce distinct must be " in {
      val schema16 = StructType(Seq(StructField("field1", IntegerType), StructField("field", IntegerType)))
      val inputFields = new VarianceOperator("variance", schema16, Map("distinct" -> "true"))
      inputFields.processReduce(Seq()) should be(Some(0d))

      val schema17 = StructType(Seq(StructField("field1", IntegerType), StructField("field", IntegerType)))
      val inputFields2 = new VarianceOperator("variance", schema17, Map("distinct" -> "true"))
      inputFields2.processReduce(Seq(Some(1), Some(2), Some(3), Some(7), Some(7))) should be(Some(6.916666666666667))

      val schema18 = StructType(Seq(StructField("field1", IntegerType), StructField("field", IntegerType)))
      val inputFields3 = new VarianceOperator("variance", schema18, Map("distinct" -> "true"))
      inputFields3.processReduce(Seq(Some(1), Some(1), Some(2), Some(3), Some(6.5), Some(7.5))) should be(Some(8.125))

      val schema19 = StructType(Seq(StructField("field1", IntegerType), StructField("field", IntegerType)))
      val inputFields4 = new VarianceOperator("variance", schema19, Map("distinct" -> "true"))
      inputFields4.processReduce(Seq(None)) should be(Some(0d))

      val schema20 = StructType(Seq(StructField("field1", IntegerType), StructField("field", IntegerType)))
      val inputFields5 = new VarianceOperator("variance", schema20, Map("typeOp" -> "string", "distinct" -> "true"))
      inputFields5.processReduce(Seq(Some(1), Some(2), Some(3), Some(7), Some(7))) should be(Some("6.916666666666667"))
    }
  }
}
