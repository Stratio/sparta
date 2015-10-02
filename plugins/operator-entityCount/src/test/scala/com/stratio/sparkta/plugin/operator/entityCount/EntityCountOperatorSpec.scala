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

package com.stratio.sparkta.plugin.operator.entityCount

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class EntityCountOperatorSpec extends WordSpec with Matchers {

  "Entity Count Operator" should {

    "processMap must be " in {
      val inputField = new EntityCountOperator("entityCount", Map())
      inputField.processMap(Map("field1" -> 1, "field2" -> 2)) should be(None)

      val inputFields2 = new EntityCountOperator("entityCount", Map("inputField" -> "field1"))
      inputFields2.processMap(Map("field3" -> 1, "field2" -> 2)) should be(None)

      val inputFields3 = new EntityCountOperator("entityCount", Map("inputField" -> "field1"))
      inputFields3.processMap(Map("field1" -> "hola holo", "field2" -> 2)) should be(Some(Seq("hola holo")))

      val inputFields4 = new EntityCountOperator("entityCount", Map("inputField" -> "field1", "split" -> ","))
      inputFields4.processMap(Map("field1" -> "hola holo", "field2" -> 2)) should be(Some(Seq("hola holo")))

      val inputFields5 = new EntityCountOperator("entityCount", Map("inputField" -> "field1", "split" -> "-"))
      inputFields5.processMap(Map("field1" -> "hola-holo", "field2" -> 2)) should be(Some(Seq("hola","holo")))

      val inputFields6 = new EntityCountOperator("entityCount", Map("inputField" -> "field1", "split" -> ","))
      inputFields6.processMap(Map("field1" -> "hola,holo adios", "field2" -> 2)) should be(
        Some(Seq("hola","holo " + "adios")))

      val inputFields7 = new EntityCountOperator("entityCount",
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \"!=\", \"value\":\"hola\"}]"))
      inputFields7.processMap(Map("field1" -> "hola", "field2" -> 2)) should be(None)

      val inputFields8 = new EntityCountOperator("entityCount",
        Map("inputField" -> "field1", "filters" -> "[{\"field\":\"field1\", \"type\": \"!=\", \"value\":\"hola\"}]",
          "split" -> " "))
      inputFields8.processMap(Map("field1" -> "hola holo", "field2" -> 2)) should be(Some(Seq("hola","holo")))

    }

    "processReduce must be " in {
      val inputFields = new EntityCountOperator("entityCount", Map())
      inputFields.processReduce(Seq()) should be(Some(Map()))

      val inputFields2 = new EntityCountOperator("entityCount", Map())
      inputFields2.processReduce(Seq(Some(Seq("hola", "holo")))) should be (Some(Map("hola" -> 1, "holo" -> 1)))

      val inputFields3 = new EntityCountOperator("entityCount", Map())
      inputFields3.processReduce(Seq(Some(Seq("hola", "holo", "hola")))) should be (Some(Map("hola" -> 2, "holo" -> 1)))
    }

    "processReduce distinct must be " in {
      val inputFields = new EntityCountOperator("entityCount", Map("distinct" -> "true"))
      inputFields.processReduce(Seq()) should be(Some(Map()))

      val inputFields2 = new EntityCountOperator("entityCount", Map("distinct" -> "true"))
      inputFields2.processReduce(Seq(Some(Seq("hola", "holo", "hola")))) should be (Some(Map("hola" -> 1, "holo" -> 1)))

    }
  }

}