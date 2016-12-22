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
package com.stratio.sparta.plugin.cube.operator.entityCount

import java.io.{Serializable => JSerializable}

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class OperatorEntityCountTest extends WordSpec with Matchers {

  "EntityCount" should {
    val props = Map(
      "inputField" -> "inputField".asInstanceOf[JSerializable],
      "split" -> ",".asInstanceOf[JSerializable])
    val schema = StructType(Seq(StructField("inputField", StringType)))
    val entityCount = new OperatorEntityCountMock("op1", schema, props)
    val inputFields = Row("hello,bye")

    "Return the associated precision name" in {
      val expected = Option(Seq("hello", "bye"))
      val result = entityCount.processMap(inputFields)
      result should be(expected)
    }

    "Return empty list" in {
      val expected = None
      val result = entityCount.processMap(Row())
      result should be(expected)
    }
  }
}
