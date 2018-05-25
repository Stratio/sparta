/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.utils

import java.sql.{Date, Timestamp}

import com.stratio.sparta.sdk.helpers.CastingHelper
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.types._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class CastingHelperTest extends WordSpec with Matchers {

  "CastingUtils" should {

    "valueToSparkType Int must be IntegerType" in {
      val expected = IntegerType
      val result = CastingHelper.valueToSparkType(1.asInstanceOf[java.lang.Integer])
      result should be(expected)
    }

    "valueToSparkType Integer must be IntegerType" in {
      val expected = IntegerType
      val result = CastingHelper.valueToSparkType(1)
      result should be(expected)
    }

    "valueToSparkType long must be LongType" in {
      val expected = LongType
      val result = CastingHelper.valueToSparkType(1L)
      result should be(expected)
    }

    "valueToSparkType java long must be LongType" in {
      val expected = LongType
      val result = CastingHelper.valueToSparkType(1L.asInstanceOf[java.lang.Long])
      result should be(expected)
    }

    "valueToSparkType double must be DoubleType" in {
      val expected = DoubleType
      val result = CastingHelper.valueToSparkType(1d)
      result should be(expected)
    }

    "valueToSparkType java double must be DoubleType" in {
      val expected = DoubleType
      val result = CastingHelper.valueToSparkType(1d.asInstanceOf[java.lang.Double])
      result should be(expected)
    }

    "valueToSparkType byte must be ByteType" in {
      val expected = ByteType
      val result = CastingHelper.valueToSparkType(1.toByte)
      result should be(expected)
    }

    "valueToSparkType java byte must be ByteType" in {
      val expected = ByteType
      val result = CastingHelper.valueToSparkType(1.toByte.asInstanceOf[java.lang.Byte])
      result should be(expected)
    }

    "valueToSparkType short must be ShortType" in {
      val expected = ShortType
      val result = CastingHelper.valueToSparkType(1.toShort)
      result should be(expected)
    }

    "valueToSparkType java short must be ShortType" in {
      val expected = ShortType
      val result = CastingHelper.valueToSparkType(1.toShort.asInstanceOf[java.lang.Short])
      result should be(expected)
    }

    "valueToSparkType float must be FloatType" in {
      val expected = FloatType
      val result = CastingHelper.valueToSparkType(1.toFloat)
      result should be(expected)
    }

    "valueToSparkType java float must be FloatType" in {
      val expected = FloatType
      val result = CastingHelper.valueToSparkType(1.toFloat.asInstanceOf[java.lang.Float])
      result should be(expected)
    }

    "valueToSparkType sql date must be DateType" in {
      val expected = DateType
      val result = CastingHelper.valueToSparkType(new java.sql.Date(1))
      result should be(expected)
    }

    "valueToSparkType date must be DateType" in {
      val expected = DateType
      val result = CastingHelper.valueToSparkType(new Date(1))
      result should be(expected)
    }

    "valueToSparkType timestamp must be TimestampType" in {
      val expected = TimestampType
      val result = CastingHelper.valueToSparkType(new Timestamp(1))
      result should be(expected)
    }

    "valueToSparkType array byte must be BinaryType" in {
      val expected = BinaryType
      val result = CastingHelper.valueToSparkType(new Array[Byte]('1'))
      result should be(expected)
    }

    "valueToSparkType string must be StringType" in {
      val expected = StringType
      val result = CastingHelper.valueToSparkType("1")
      result should be(expected)
    }

    "valueToSparkType boolean must be BooleanType" in {
      val expected = BooleanType
      val result = CastingHelper.valueToSparkType(true)
      result should be(expected)
    }

    "valueToSparkType java boolean must be BooleanType" in {
      val expected = BooleanType
      val result = CastingHelper.valueToSparkType(true.asInstanceOf[java.lang.Boolean])
      result should be(expected)
    }

    "valueToSparkType row must be schema in GenericRowWithSchema" in {
      val schema = StructType(Seq(StructField("a", IntegerType)))
      val row = new GenericRowWithSchema(Array(1), schema)
      val result = CastingHelper.valueToSparkType(row)
      result should be(schema)
    }

    "valueToSparkType Seq of string must be Array of IntegerType" in {
      val expected = ArrayType(StringType)
      val result = CastingHelper.valueToSparkType(Seq("a", "b"))
      result should be(expected)
    }

    "valueToSparkType Seq of map must be Array of StructType" in {
      val expected = ArrayType(StructType(Seq(StructField("a", StringType))))
      val result = CastingHelper.valueToSparkType(Seq(Map("a" -> "b")))
      result should be(expected)
    }

    "valueToSparkType Map of string must be Map of StringType" in {
      val expected = StructType(Seq(StructField("b", StringType)))
      val result = CastingHelper.valueToSparkType(Map("b" -> "b"))
      result should be(expected)
    }

    "valueToSparkType Map of map must be Map of StructType" in {
      val expected = StructType(Seq(StructField("b", StructType(Seq(StructField("a", StringType))))))
      val result = CastingHelper.valueToSparkType(Map("b" -> Map("a" -> "b")))
      result should be(expected)
    }

    "checkAnyStructType Map must be GenericRowWithSchema" in {
      val schema = StructType(Seq(StructField("b", StructType(Seq(StructField("a", StringType))))))
      val row = new GenericRowWithSchema(Array("b"), schema)
      val result = CastingHelper.checkStructType(Map("b" -> "b"))
      result should be(row)
    }

    "checkAnyStructType Map of map must be GenericRowWithSchema" in {
      val schema1 = StructType(Seq(StructField("c", StringType)))
      val schema2 = StructType(Seq(StructField("b", schema1)))
      val row1 = new GenericRowWithSchema(Array("a"), schema1)
      val row2 = new GenericRowWithSchema(Array(row1), schema2)
      val result = CastingHelper.checkStructType(Map("b" -> Map("c" -> "a")))
      result should be(row2)
    }

  }
}
