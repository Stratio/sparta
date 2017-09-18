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
package com.stratio.sparta.sdk.pipeline.schema

import java.sql.{Date, Timestamp}

import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class TypeOpTest extends WordSpec with Matchers {

  "TypeOp" should {

    "valueToSparkType Int must be IntegerType" in {
      val expected = IntegerType
      val result = TypeOp.valueToSparkType(1.asInstanceOf[java.lang.Integer])
      result should be(expected)
    }

    "valueToSparkType Integer must be IntegerType" in {
      val expected = IntegerType
      val result = TypeOp.valueToSparkType(1)
      result should be(expected)
    }

    "valueToSparkType long must be LongType" in {
      val expected = LongType
      val result = TypeOp.valueToSparkType(1L)
      result should be(expected)
    }

    "valueToSparkType java long must be LongType" in {
      val expected = LongType
      val result = TypeOp.valueToSparkType(1L.asInstanceOf[java.lang.Long])
      result should be(expected)
    }

    "valueToSparkType double must be DoubleType" in {
      val expected = DoubleType
      val result = TypeOp.valueToSparkType(1d)
      result should be(expected)
    }

    "valueToSparkType java double must be DoubleType" in {
      val expected = DoubleType
      val result = TypeOp.valueToSparkType(1d.asInstanceOf[java.lang.Double])
      result should be(expected)
    }

    "valueToSparkType byte must be ByteType" in {
      val expected = ByteType
      val result = TypeOp.valueToSparkType(1.toByte)
      result should be(expected)
    }

    "valueToSparkType java byte must be ByteType" in {
      val expected = ByteType
      val result = TypeOp.valueToSparkType(1.toByte.asInstanceOf[java.lang.Byte])
      result should be(expected)
    }

    "valueToSparkType short must be ShortType" in {
      val expected = ShortType
      val result = TypeOp.valueToSparkType(1.toShort)
      result should be(expected)
    }

    "valueToSparkType java short must be ShortType" in {
      val expected = ShortType
      val result = TypeOp.valueToSparkType(1.toShort.asInstanceOf[java.lang.Short])
      result should be(expected)
    }

    "valueToSparkType float must be FloatType" in {
      val expected = FloatType
      val result = TypeOp.valueToSparkType(1.toFloat)
      result should be(expected)
    }

    "valueToSparkType java float must be FloatType" in {
      val expected = FloatType
      val result = TypeOp.valueToSparkType(1.toFloat.asInstanceOf[java.lang.Float])
      result should be(expected)
    }

    "valueToSparkType sql date must be DateType" in {
      val expected = DateType
      val result = TypeOp.valueToSparkType(new java.sql.Date(1))
      result should be(expected)
    }

    "valueToSparkType date must be DateType" in {
      val expected = DateType
      val result = TypeOp.valueToSparkType(new Date(1))
      result should be(expected)
    }

    "valueToSparkType timestamp must be TimestampType" in {
      val expected = TimestampType
      val result = TypeOp.valueToSparkType(new Timestamp(1))
      result should be(expected)
    }

    "valueToSparkType array byte must be BinaryType" in {
      val expected = BinaryType
      val result = TypeOp.valueToSparkType(new Array[Byte]('1'))
      result should be(expected)
    }

    "valueToSparkType string must be StringType" in {
      val expected = StringType
      val result = TypeOp.valueToSparkType("1")
      result should be(expected)
    }

    "valueToSparkType boolean must be BooleanType" in {
      val expected = BooleanType
      val result = TypeOp.valueToSparkType(true)
      result should be(expected)
    }

    "valueToSparkType java boolean must be BooleanType" in {
      val expected = BooleanType
      val result = TypeOp.valueToSparkType(true.asInstanceOf[java.lang.Boolean])
      result should be(expected)
    }

    "valueToSparkType row must be schema in GenericRowWithSchema" in {
      val schema = StructType(Seq(StructField("a", IntegerType)))
      val row = new GenericRowWithSchema(Array(1), schema)
      val result = TypeOp.valueToSparkType(row)
      result should be(schema)
    }

    "valueToSparkType Seq of string must be Array of IntegerType" in {
      val expected = ArrayType(StringType)
      val result = TypeOp.valueToSparkType(Seq("a", "b"))
      result should be(expected)
    }

    "valueToSparkType Seq of map must be Array of StructType" in {
      val expected = ArrayType(StructType(Seq(StructField("a", StringType))))
      val result = TypeOp.valueToSparkType(Seq(Map("a" -> "b")))
      result should be(expected)
    }

    "valueToSparkType Map of string must be Map of StringType" in {
      val expected = StructType(Seq(StructField("b", StringType)))
      val result = TypeOp.valueToSparkType(Map("b" -> "b"))
      result should be(expected)
    }

    "valueToSparkType Map of map must be Map of StructType" in {
      val expected = StructType(Seq(StructField("b", StructType(Seq(StructField("a", StringType))))))
      val result = TypeOp.valueToSparkType(Map("b" -> Map("a" -> "b")))
      result should be(expected)
    }

    "checkAnyStructType Map must be GenericRowWithSchema" in {
      val schema = StructType(Seq(StructField("b", StructType(Seq(StructField("a", StringType))))))
      val row = new GenericRowWithSchema(Array("b"), schema)
      val result = TypeOp.checkAnyStructType(Map("b" -> "b"))
      result should be(row)
    }

    "checkAnyStructType Map of map must be GenericRowWithSchema" in {
      val schema1 = StructType(Seq(StructField("c", StringType)))
      val schema2 = StructType(Seq(StructField("b", schema1)))
      val row1 = new GenericRowWithSchema(Array("a"), schema1)
      val row2 = new GenericRowWithSchema(Array(row1), schema2)
      val result = TypeOp.checkAnyStructType(Map("b" -> Map("c" -> "a")))
      result should be(row2)
    }

    "typeOperation String must be " in {
      val expected = "String"
      val result = TypeOp.castingToSchemaType(TypeOp.String, "String")
      result should be(expected)
    }

    "typeOperation ArrayDouble from any must be " in {
      val expected = Seq(1d)
      val result = TypeOp.castingToSchemaType(TypeOp.ArrayDouble, Seq("1"))
      result should be(expected)
    }

    "typeOperation ArrayMapStringString from any must be " in {
      val expected = Seq(Map("key" -> "1"))
      val result = TypeOp.castingToSchemaType(TypeOp.ArrayMapStringString, Seq(Map("key" -> 1)))
      result should be(expected)
    }

    "typeOperation ArrayDouble must be " in {
      val expected = Seq(1d)
      val result = TypeOp.castingToSchemaType(TypeOp.ArrayDouble, Seq(1d))
      result should be(expected)
    }

    "typeOperation ArrayString must be " in {
      val expected = Seq("String")
      val result = TypeOp.castingToSchemaType(TypeOp.ArrayString, Seq("String"))
      result should be(expected)
    }

    "typeOperation ArrayString from any must be " in {
      val expected = Seq("1.0")
      val result = TypeOp.castingToSchemaType(TypeOp.ArrayString, Seq(1d))
      result should be(expected)
    }

    "typeOperation MapStringString from any must be " in {
      val expected = Map("key" -> "1")
      val result = TypeOp.castingToSchemaType(TypeOp.MapStringString, Map("key" -> 1))
      result should be(expected)
    }

    "typeOperation MapStringLong from any must be " in {
      val expected = Map("key" -> 1L)
      val result = TypeOp.castingToSchemaType(TypeOp.MapStringLong, Map("key" -> 1))
      result should be(expected)
    }

    "typeOperation MapStringDouble from any must be " in {
      val expected = Map("key" -> 1d)
      val result = TypeOp.castingToSchemaType(TypeOp.MapStringDouble, Map("key" -> "1"))
      result should be(expected)
    }

    "typeOperation MapStringInt from any must be " in {
      val expected = Map("key" -> 1)
      val result = TypeOp.castingToSchemaType(TypeOp.MapStringInt, Map("key" -> "1"))
      result should be(expected)
    }

    "typeOperation Timestamp must be " in {
      val expected = new Timestamp(1L)
      val result = TypeOp.castingToSchemaType(TypeOp.Timestamp, new Timestamp(1L))
      result should be(expected)
    }

    "typeOperation Date must be " in {
      val expected = new Date(1L)
      val result = TypeOp.castingToSchemaType(TypeOp.Date, new Date(1L))
      result should be(expected)
    }

    "typeOperation DateTime must be " in {
      val expected = new DateTime(1L)
      val result = TypeOp.castingToSchemaType(TypeOp.DateTime, new DateTime(1L))
      result should be(expected)
    }

    "typeOperation MapStringLong must be " in {
      val expected = Map("a" -> 1L)
      val result = TypeOp.castingToSchemaType(TypeOp.MapStringLong, Map("a" -> "1"))
      result should be(expected)
    }

    "typeOperation MapStringLong from number must be " in {
      val expected = Map("a" -> 1L)
      val result = TypeOp.castingToSchemaType(TypeOp.MapStringLong, Map("a" -> 1L))
      result should be(expected)
    }

    "typeOperation Long must be " in {
      val expected = 1L
      val result = TypeOp.castingToSchemaType(TypeOp.Long, 1L)
      result should be(expected)
    }

    "typeOperation Binary must be " in {
      val expected = "Binary"
      val result = TypeOp.castingToSchemaType(TypeOp.Binary, "Binary")
      result should be(expected)
    }

    "operation by name Binary must be " in {
      val expected = TypeOp.Binary
      val result = TypeOp.getTypeOperationByName("Binary", TypeOp.Binary)
      result should be(expected)
    }

    "operation by name Long must be " in {
      val expected = TypeOp.Long
      val result = TypeOp.getTypeOperationByName("Long", TypeOp.Long)
      result should be(expected)
    }

    "operation by name Int must be " in {
      val expected = TypeOp.Int
      val result = TypeOp.getTypeOperationByName("Int", TypeOp.Int)
      result should be(expected)
    }

    "operation by name String must be " in {
      val expected = TypeOp.String
      val result = TypeOp.getTypeOperationByName("String", TypeOp.String)
      result should be(expected)
    }

    "operation by name Double must be " in {
      val expected = TypeOp.Double
      val result = TypeOp.getTypeOperationByName("Double", TypeOp.String)
      result should be(expected)
    }

    "operation by name Boolean must be " in {
      val expected = TypeOp.Boolean
      val result = TypeOp.getTypeOperationByName("Boolean", TypeOp.String)
      result should be(expected)
    }

    "operation by name Date must be " in {
      val expected = TypeOp.Date
      val result = TypeOp.getTypeOperationByName("Date", TypeOp.String)
      result should be(expected)
    }

    "operation by name DateTime must be " in {
      val expected = TypeOp.DateTime
      val result = TypeOp.getTypeOperationByName("DateTime", TypeOp.String)
      result should be(expected)
    }

    "operation by name Timestamp must be " in {
      val expected = TypeOp.Timestamp
      val result = TypeOp.getTypeOperationByName("Timestamp", TypeOp.String)
      result should be(expected)
    }

    "operation by name ArrayDouble must be " in {
      val expected = TypeOp.ArrayDouble
      val result = TypeOp.getTypeOperationByName("ArrayDouble", TypeOp.String)
      result should be(expected)
    }

    "operation by name ArrayString must be " in {
      val expected = TypeOp.ArrayString
      val result = TypeOp.getTypeOperationByName("ArrayString", TypeOp.String)
      result should be(expected)
    }

    "operation by name ArrayMapStringString must be " in {
      val expected = TypeOp.ArrayMapStringString
      val result = TypeOp.getTypeOperationByName("ArrayMapStringString", TypeOp.String)
      result should be(expected)
    }

    "operation by name MapStringLong must be " in {
      val expected = TypeOp.MapStringLong
      val result = TypeOp.getTypeOperationByName("MapStringLong", TypeOp.String)
      result should be(expected)
    }

    "operation by name MapStringInt must be " in {
      val expected = TypeOp.MapStringInt
      val result = TypeOp.getTypeOperationByName("MapStringInt", TypeOp.String)
      result should be(expected)
    }

    "operation by name MapStringString must be " in {
      val expected = TypeOp.MapStringString
      val result = TypeOp.getTypeOperationByName("MapStringString", TypeOp.String)
      result should be(expected)
    }
  }
}
