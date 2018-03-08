/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
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

