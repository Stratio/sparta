/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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
