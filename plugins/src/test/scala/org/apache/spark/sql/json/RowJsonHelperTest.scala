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
package org.apache.spark.sql.json


import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class RowJsonHelperTest extends WordSpec with Matchers {

  "RowJsonHelper.toJSON" should {
    "correctly produce a JSON string" when {
      "it is passed a row (simple types)" in {
        val structSimple =
          StructType(
            StructField("a", IntegerType, true) ::
              StructField("b", LongType, false) ::
              StructField("c", BooleanType, false) :: Nil)
        val (a, b) = (1, 2222222L)
        val valuesSimple: Array[Any] = Array(a, b, false)
        val test1 = new GenericRowWithSchema(valuesSimple, structSimple)
        val jsonString = """{"a":1,"b":2222222,"c":false}"""
        RowJsonHelper.toJSON(test1, Map.empty[String, String]) should equal(jsonString)
      }

      "it is passed a row (complex types)" in {
        val structComplex = StructType(
          StructField("response", StringType, true) ::
            StructField("members",
              ArrayType(
                StructType(
                  StructField("ID", StringType, true) ::
                    StructField("name", StringType, true) :: Nil), false),
              false) :: Nil)

        val innerSchema = StructType(
          StructField("ID", StringType, true) ::
            StructField("name", StringType, true) :: Nil)
        val innerRow = new GenericRowWithSchema(Array("185778762", "Rodrigo Diaz de Vivar"), innerSchema)
        val valuesComplex: Array[Any] = Array("yes", Seq(innerRow))
        val externalRow = new GenericRowWithSchema(valuesComplex, structComplex)
        val jsonStringComplex =
          """{"response":"yes","members":[{"ID":"185778762","name":"Rodrigo Diaz de Vivar"}]}"""
        RowJsonHelper.toJSON(externalRow, Map.empty[String, String]) should equal(jsonStringComplex)
      }
    }
  }

  "RowJsonHelper.toRow" should {
    "correctly produce a row" when {

      "it is passed a json (simple types)" in {
        val structSimple =
          StructType(
            StructField("a", IntegerType, true) ::
              StructField("b", LongType, false) ::
              StructField("c", BooleanType, false) :: Nil)
        val (a, b) = (1, 2222222L)
        val valuesSimple: Array[Any] = Array(a, b, false)
        val test1 = new GenericRowWithSchema(valuesSimple, structSimple)
        val jsonString = """{"a":1,"b":2222222,"c":false}"""
        RowJsonHelper.toRow(jsonString, Map.empty[String, String], structSimple) should equal(test1)
      }

      "it is passed a json (complex types)" in {
        val structComplex = StructType(
          StructField("response", StringType, true) ::
            StructField("members",
              ArrayType(
                StructType(
                  StructField("ID", StringType, true) ::
                    StructField("name", StringType, true) :: Nil), false),
              false) :: Nil)
        val innerSchema = StructType(
          StructField("ID", StringType, true) ::
            StructField("name", StringType, true) :: Nil)
        val innerRow = new GenericRowWithSchema(Array("185778762", "Rodrigo Diaz de Vivar"), innerSchema)
        val valuesComplex: Array[Any] = Array("yes", Seq(innerRow))
        val externalRow = new GenericRowWithSchema(valuesComplex, structComplex)
        val jsonStringComplex =
          """{"response":"yes","members":[{"ID":"185778762","name":"Rodrigo Diaz de Vivar"}]}"""
        val result = RowJsonHelper.toRow(jsonStringComplex, Map.empty[String, String], structComplex)
        result should equal(externalRow)
      }
    }
  }

  "RowJsonHelper.extractSchemaFromJson" should {
    "correctly extract schema from json" when {

      "it is passed a json (simple types)" in {
        val structSimple =
          StructType(
            StructField("a", LongType, true) ::
              StructField("b", LongType, true) ::
              StructField("c", BooleanType, true) :: Nil)
        val jsonString = """{"a":1,"b":2222222,"c":false}"""
        val result = RowJsonHelper.extractSchemaFromJson(jsonString, Map.empty[String, String])

        result should equal(structSimple)
      }

      "it is passed a json (complex types)" in {
        val structComplex = StructType(Seq(
          StructField("members", ArrayType(StructType(Seq(
            StructField("ID", StringType, true),
            StructField("name", StringType, true)
          )), true)),
          StructField("response", StringType, true)
        ))
        val jsonStringComplex =
          """{"response":"yes","members":[{"ID":"185778762","name":"Rodrigo Diaz de Vivar"}]}"""
        val result = RowJsonHelper.extractSchemaFromJson(jsonStringComplex, Map.empty[String, String])

        result should equal(structComplex)
      }
    }
  }
}

