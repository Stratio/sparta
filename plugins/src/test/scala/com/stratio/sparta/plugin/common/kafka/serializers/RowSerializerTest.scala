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

package com.stratio.sparta.plugin.common.kafka.serializers

import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, WordSpec}

import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class RowSerializerTest  extends WordSpec with Matchers with MockitoSugar {

  "RowSerializer" should {

    "return a correct values in row string" in {
      val properties = Map("value.serializer.outputFormat" -> "ROW")
      val desSerializer = new RowSerializer
      desSerializer.configure(properties, false)
      val message = "Stratio Sparta"
      val schema = StructType(Seq(StructField("rawField", StringType)))
      val row = new GenericRowWithSchema(Array(message), schema)
      val result = new String(desSerializer.serialize("topic", row))
      val expected = message

      result should be(expected)
    }

    "return a correct values in json string" in {
      val properties = Map("value.serializer.outputFormat" -> "JSON")
      val desSerializer = new RowSerializer
      desSerializer.configure(properties, false)
      val message = "Stratio Sparta"
      val schema = StructType(Seq(StructField("rawField", StringType)))
      val row = new GenericRowWithSchema(Array(message), schema)
      val result = new String(desSerializer.serialize("topic", row))
      val expected = s"""{"rawField":"$message"}"""

      result should be(expected)
    }

  }
}
