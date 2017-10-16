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
import org.scalatest.{Matchers, WordSpec}

import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class RowDeserializerTest  extends WordSpec with Matchers {

  "RowDeserializer" should {

    "return a correct values when intput format is string" in {
      val properties = Map(
        "value.deserializer.inputFormat" -> "STRING",
        "value.deserializer.outputField" -> "rawField"
      )
      val desSerializer = new RowDeserializer
      desSerializer.configure(properties, false)
      val dataToSerialize = "Stratio Sparta"
      val result = desSerializer.deserialize("topic", dataToSerialize.getBytes)
      val expectedSchema = StructType(Seq(StructField("rawField", StringType)))
      val expected = new GenericRowWithSchema(Array(dataToSerialize), expectedSchema)

      result should be(expected)
    }

    "return a correct values when intput format is json and the schema is not provided" in {
      val properties = Map(
        "value.deserializer.inputFormat" -> "JSON",
        "value.deserializer.json.schema.fromRow" -> "true"
      )
      val desSerializer = new RowDeserializer
      desSerializer.configure(properties, false)
      val message = "Stratio Sparta"
      val dataToSerialize = s"""{"rawField": "$message"}"""
      val result = desSerializer.deserialize("topic", dataToSerialize.getBytes)
      val expectedSchema = StructType(Seq(StructField("rawField", StringType)))
      val expected = new GenericRowWithSchema(Array(message), expectedSchema)

      result should be(expected)
    }

    "return a correct values when intput format is json and the schema is provided in spark format" in {
      val properties = Map(
        "value.deserializer.inputFormat" -> "JSON",
        "value.deserializer.json.schema.fromRow" -> "false",
        "value.deserializer.json.schema.inputMode" -> "SPARKFORMAT",
        "value.deserializer.schema.provided" -> "StructType((StructField(rawField,StringType,true)))"
      )
      val desSerializer = new RowDeserializer
      desSerializer.configure(properties, false)
      val message = "Stratio Sparta"
      val dataToSerialize = s"""{"rawField": "$message"}"""
      val result = desSerializer.deserialize("topic", dataToSerialize.getBytes)
      val expectedSchema = StructType(Seq(StructField("rawField", StringType)))
      val expected = new GenericRowWithSchema(Array(message), expectedSchema)

      result should be(expected)
    }

    "return a correct values when intput format is json and the schema is provided in example" in {
      val properties = Map(
        "value.deserializer.inputFormat" -> "JSON",
        "value.deserializer.json.schema.fromRow" -> "false",
        "value.deserializer.json.schema.inputMode" -> "EXAMPLE",
        "value.deserializer.schema.provided" -> """{"rawField":"foo"}"""
      )
      val desSerializer = new RowDeserializer
      desSerializer.configure(properties, false)
      val message = "Stratio Sparta"
      val dataToSerialize = s"""{"rawField": "$message"}"""
      val result = desSerializer.deserialize("topic", dataToSerialize.getBytes)
      val expectedSchema = StructType(Seq(StructField("rawField", StringType)))
      val expected = new GenericRowWithSchema(Array(message), expectedSchema)

      result should be(expected)
    }
  }
}
