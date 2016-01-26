/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

package com.stratio.sparkta.plugin.operator.entityCount

import com.stratio.sparkta.sdk.{InputFieldsValues, Operator}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class EntityCountOperatorTest extends WordSpec with Matchers {

  "Entity Count Operator" should {

    "processMap must be " in {
      val inputField = new EntityCountOperator("entityCount", Map())
      inputField.processMap(InputFieldsValues(Map("field1" -> 1, "field2" -> 2))) should be(None)

      val inputFields2 = new EntityCountOperator("entityCount", Map("inputField" -> "field1"))
      inputFields2.processMap(InputFieldsValues(Map("field3" -> 1, "field2" -> 2))) should be(None)

      val inputFields3 = new EntityCountOperator("entityCount", Map("inputField" -> "field1"))
      inputFields3.processMap(InputFieldsValues(Map("field1" -> "hola holo", "field2" -> 2))) should be
      (Some(Seq("hola holo")))

      val inputFields4 = new EntityCountOperator("entityCount", Map("inputField" -> "field1", "split" -> ","))
      inputFields4.processMap(InputFieldsValues(Map("field1" -> "hola holo", "field2" -> 2))) should be
      (Some(Seq("hola holo")))

      val inputFields5 = new EntityCountOperator("entityCount", Map("inputField" -> "field1", "split" -> "-"))
      inputFields5.processMap(InputFieldsValues(Map("field1" -> "hola-holo", "field2" -> 2))) should be
      (Some(Seq("hola", "holo")))

      val inputFields6 = new EntityCountOperator("entityCount", Map("inputField" -> "field1", "split" -> ","))
      inputFields6.processMap(InputFieldsValues(Map("field1" -> "hola,holo adios", "field2" -> 2))) should be
      (Some(Seq("hola", "holo " + "adios")))

      val inputFields7 = new EntityCountOperator("entityCount",
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \"!=\", \"value\":\"hola\"}]"))
      inputFields7.processMap(InputFieldsValues(Map("field1" -> "hola", "field2" -> 2))) should be(None)

      val inputFields8 = new EntityCountOperator("entityCount",
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \"!=\", \"value\":\"hola\"}]",
          "split" -> " "))
      inputFields8.processMap(InputFieldsValues(Map("field1" -> "hola holo", "field2" -> 2))) should be
      (Some(Seq("hola", "holo")))
    }

    "processReduce must be " in {
      val inputFields = new EntityCountOperator("entityCount", Map())
      inputFields.processReduce(Seq()) should be(Some(Seq()))

      val inputFields2 = new EntityCountOperator("entityCount", Map())
      inputFields2.processReduce(Seq(Some(Seq("hola", "holo")))) should be(Some(Seq("hola", "holo")))

      val inputFields3 = new EntityCountOperator("entityCount", Map())
      inputFields3.processReduce(Seq(Some(Seq("hola", "holo", "hola")))) should be(Some(Seq("hola", "holo", "hola")))
    }

    "associative process must be " in {
      val inputFields = new EntityCountOperator("entityCount", Map())
      val resultInput = Seq((Operator.OldValuesKey, Some(Map("hola" -> 1L, "holo" -> 1L))),
        (Operator.NewValuesKey, None))
      inputFields.associativity(resultInput) should be(Some(Map("hola" -> 1L, "holo" -> 1L)))

      val inputFields2 = new EntityCountOperator("entityCount", Map("typeOp" -> "int"))
      val resultInput2 = Seq((Operator.OldValuesKey, Some(Map("hola" -> 1L, "holo" -> 1L))),
        (Operator.NewValuesKey, Some(Seq("hola"))))
      inputFields2.associativity(resultInput2) should be(Some(0))

      val inputFields3 = new EntityCountOperator("entityCount", Map("typeOp" -> null))
      val resultInput3 = Seq((Operator.OldValuesKey, Some(Map("hola" -> 1L, "holo" -> 1L))))
      inputFields3.associativity(resultInput3) should be(Some(Map("hola" -> 1L, "holo" -> 1L)))
    }
  }
}