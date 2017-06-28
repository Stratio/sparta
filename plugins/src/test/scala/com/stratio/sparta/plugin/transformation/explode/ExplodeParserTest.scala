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

package com.stratio.sparta.plugin.transformation.explode

import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{ArrayType, DoubleType, MapType, StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class ExplodeParserTest extends WordSpecLike with Matchers {

  val inputField = Some("explode")
  val schema = StructType(Seq(
    StructField("field1", StringType),
    StructField("field2", StringType),
    StructField(inputField.get, ArrayType(MapType(StringType, StringType))),
    StructField("color", StringType),
    StructField("price", DoubleType))
  )
  val red = "red"
  val blue = "blue"
  val redPrice = 19.95d
  val bluePrice = 10d
  val explodeField = Seq(Map("color" -> red, "price" -> redPrice))
  val explodeFieldMap = Map("color" -> red, "price" -> redPrice)
  val explodeFieldMoreFields = Seq(Map("color" -> red, "price" -> redPrice), Map("color" -> blue, "price" -> bluePrice))
  val field1 = "field1"
  val field2 = "field2"

  "A ExplodeParser" should {
    "explode with only one value" in {
      val input = Row(field1, field2, explodeField)
      val outputsFields = Seq("color", "price")
      val result = new ExplodeParser(1, inputField, outputsFields, schema, Map()).parse(input)
      val expected = Seq(Row(field1, field2, explodeField, red, redPrice))

      assertResult(result)(expected)
    }

    "explode with only one value in map field" in {
      val input = Row(field1, field2, explodeFieldMap)
      val outputsFields = Seq("color", "price")
      val result = new ExplodeParser(1, inputField, outputsFields, schema, Map()).parse(input)
      val expected = Seq(Row(field1, field2, explodeFieldMap, red, redPrice))

      assertResult(result)(expected)
    }

    "explode with two values" in {
      val input = Row(field1, field2, explodeFieldMoreFields)
      val outputsFields = Seq("color", "price")
      val result = new ExplodeParser(1, inputField, outputsFields, schema, Map()).parse(input)
      val expected = Seq(
        Row(field1, field2, explodeFieldMoreFields, red, redPrice),
        Row(field1, field2, explodeFieldMoreFields, blue, bluePrice)
      )

      assertResult(result)(expected)
    }

    "not parse anything if the field does not match" in {
      val input = Row(field1, field2, explodeFieldMoreFields)
      val schema = StructType(Seq(StructField("wrongfield", ArrayType(MapType(StringType, StringType)))))
      val outputsFields = Seq("color", "price")
      an[RuntimeException] should be thrownBy new ExplodeParser(
        1, inputField, outputsFields, schema, Map()).parse(input)
    }
  }
}