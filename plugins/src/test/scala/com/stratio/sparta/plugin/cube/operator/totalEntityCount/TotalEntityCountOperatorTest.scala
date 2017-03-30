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
package com.stratio.sparta.plugin.cube.operator.totalEntityCount

import com.stratio.sparta.sdk.pipeline.aggregation.operator.Operator
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class TotalEntityCountOperatorTest extends WordSpec with Matchers {

  "Entity Count Operator" should {

    val initSchema = StructType(Seq(
      StructField("field1", IntegerType, false),
      StructField("field2", IntegerType, false),
      StructField("field3", IntegerType, false)
    ))

    val initSchemaFail = StructType(Seq(
      StructField("field2", IntegerType, false)
    ))

    "processMap must be " in {
      val inputField = new TotalEntityCountOperator("totalEntityCount", initSchema, Map())
      inputField.processMap(Row(1, 2)) should be(None)

      val inputFields2 = new TotalEntityCountOperator("totalEntityCount", initSchemaFail, Map("inputField" -> "field1"))
      inputFields2.processMap(Row(1, 2)) should be(None)

      val inputFields3 = new TotalEntityCountOperator("totalEntityCount", initSchema, Map("inputField" -> "field1"))
      inputFields3.processMap(Row("hola holo", 2)) should be(Some(Seq("hola holo")))

      val inputFields4 =
        new TotalEntityCountOperator("totalEntityCount", initSchema, Map("inputField" -> "field1", "split" -> ","))
      inputFields4.processMap(Row("hola holo", 2)) should be(Some(Seq("hola holo")))

      val inputFields5 =
        new TotalEntityCountOperator("totalEntityCount", initSchema, Map("inputField" -> "field1", "split" -> "-"))
      inputFields5.processMap(Row("hola-holo", 2)) should be(Some(Seq("hola", "holo")))

      val inputFields6 =
        new TotalEntityCountOperator("totalEntityCount", initSchema, Map("inputField" -> "field1", "split" -> ","))
      inputFields6.processMap(Row("hola,holo adios", 2)) should be(Some(Seq("hola", "holo " + "adios")))

      val inputFields7 = new TotalEntityCountOperator("totalEntityCount", initSchema,
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \"!=\", \"value\":\"hola\"}]"))
      inputFields7.processMap(Row("hola", 2)) should be(None)

      val inputFields8 = new TotalEntityCountOperator("totalEntityCount", initSchema,
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \"!=\", \"value\":\"hola\"}]",
          "split" -> " "))
      inputFields8.processMap(Row("hola holo", 2)) should be
      (Some(Seq("hola", "holo")))
    }

    "processReduce must be " in {
      val inputFields = new TotalEntityCountOperator("totalEntityCount", initSchema, Map())
      inputFields.processReduce(Seq()) should be(Some(0L))

      val inputFields2 = new TotalEntityCountOperator("totalEntityCount", initSchema, Map())
      inputFields2.processReduce(Seq(Some(Seq("hola", "holo")))) should be(Some(2L))

      val inputFields3 = new TotalEntityCountOperator("totalEntityCount", initSchema, Map())
      inputFields3.processReduce(Seq(Some(Seq("hola", "holo", "hola")))) should be(Some(3L))

      val inputFields4 = new TotalEntityCountOperator("totalEntityCount", initSchema, Map())
      inputFields4.processReduce(Seq(None)) should be(Some(0L))
    }

    "processReduce distinct must be " in {
      val inputFields = new TotalEntityCountOperator("totalEntityCount", initSchema, Map("distinct" -> "true"))
      inputFields.processReduce(Seq()) should be(Some(0L))

      val inputFields2 = new TotalEntityCountOperator("totalEntityCount", initSchema, Map("distinct" -> "true"))
      inputFields2.processReduce(Seq(Some(Seq("hola", "holo", "hola")))) should be(Some(2L))
    }

    "associative process must be " in {
      val inputFields = new TotalEntityCountOperator("totalEntityCount", initSchema, Map())
      val resultInput = Seq((Operator.OldValuesKey, Some(2)), (Operator.NewValuesKey, None))
      inputFields.associativity(resultInput) should be(Some(2))

      val inputFields2 = new TotalEntityCountOperator("totalEntityCount", initSchema, Map("typeOp" -> "int"))
      val resultInput2 = Seq((Operator.OldValuesKey, Some(2)),
        (Operator.NewValuesKey, Some(1)))
      inputFields2.associativity(resultInput2) should be(Some(3))

      val inputFields3 = new TotalEntityCountOperator("totalEntityCount", initSchema, Map("typeOp" -> null))
      val resultInput3 = Seq((Operator.OldValuesKey, Some(2)))
      inputFields3.associativity(resultInput3) should be(Some(2))
    }
  }
}
