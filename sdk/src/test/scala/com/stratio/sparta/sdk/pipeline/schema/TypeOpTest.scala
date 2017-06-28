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

import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class TypeOpTest extends WordSpec with Matchers {

  "TypeOp" should {
    "typeOperation String must be " in {
      val expected = "String"
      val result = TypeOp.transformValueByTypeOp(TypeOp.String, "String")
      result should be(expected)
    }

    "typeOperation ArrayDouble from any must be " in {
      val expected = Seq(1d)
      val result = TypeOp.transformValueByTypeOp(TypeOp.ArrayDouble, Seq("1"))
      result should be(expected)
    }

    "typeOperation ArrayMapStringString from any must be " in {
      val expected = Seq(Map("key" -> "1"))
      val result = TypeOp.transformValueByTypeOp(TypeOp.ArrayMapStringString, Seq(Map("key" -> 1)))
      result should be(expected)
    }

    "typeOperation ArrayDouble must be " in {
      val expected = Seq(1d)
      val result = TypeOp.transformValueByTypeOp(TypeOp.ArrayDouble, Seq(1d))
      result should be(expected)
    }

    "typeOperation ArrayString must be " in {
      val expected = Seq("String")
      val result = TypeOp.transformValueByTypeOp(TypeOp.ArrayString, Seq("String"))
      result should be(expected)
    }

    "typeOperation ArrayString from any must be " in {
      val expected = Seq("1.0")
      val result = TypeOp.transformValueByTypeOp(TypeOp.ArrayString, Seq(1d))
      result should be(expected)
    }

    "typeOperation MapStringString from any must be " in {
      val expected = Map("key" -> "1")
      val result = TypeOp.transformValueByTypeOp(TypeOp.MapStringString, Map("key" -> 1))
      result should be(expected)
    }

    "typeOperation MapStringLong from any must be " in {
      val expected = Map("key" -> 1L)
      val result = TypeOp.transformValueByTypeOp(TypeOp.MapStringLong, Map("key" -> 1))
      result should be(expected)
    }

    "typeOperation MapStringDouble from any must be " in {
      val expected = Map("key" -> 1d)
      val result = TypeOp.transformValueByTypeOp(TypeOp.MapStringDouble, Map("key" -> "1"))
      result should be(expected)
    }

    "typeOperation MapStringInt from any must be " in {
      val expected = Map("key" -> 1)
      val result = TypeOp.transformValueByTypeOp(TypeOp.MapStringInt, Map("key" -> "1"))
      result should be(expected)
    }

    "typeOperation Timestamp must be " in {
      val expected = new Timestamp(1L)
      val result = TypeOp.transformValueByTypeOp(TypeOp.Timestamp, new Timestamp(1L))
      result should be(expected)
    }

    "typeOperation Date must be " in {
      val expected = new Date(1L)
      val result = TypeOp.transformValueByTypeOp(TypeOp.Date, new Date(1L))
      result should be(expected)
    }

    "typeOperation DateTime must be " in {
      val expected = new DateTime(1L)
      val result = TypeOp.transformValueByTypeOp(TypeOp.DateTime, new DateTime(1L))
      result should be(expected)
    }

    "typeOperation MapStringLong must be " in {
      val expected = Map("a" -> 1L)
      val result = TypeOp.transformValueByTypeOp(TypeOp.MapStringLong, Map("a" -> "1"))
      result should be(expected)
    }

    "typeOperation MapStringLong from number must be " in {
      val expected = Map("a" -> 1L)
      val result = TypeOp.transformValueByTypeOp(TypeOp.MapStringLong, Map("a" -> 1L))
      result should be(expected)
    }

    "typeOperation Long must be " in {
      val expected = 1L
      val result = TypeOp.transformValueByTypeOp(TypeOp.Long, 1L)
      result should be(expected)
    }

    "typeOperation Binary must be " in {
      val expected = "Binary"
      val result = TypeOp.transformValueByTypeOp(TypeOp.Binary, "Binary")
      result should be(expected)
    }

    "operation by name Binary must be " in {
      val expected = TypeOp.Binary
      val result = TypeOp.getTypeOperationByName("Binary", TypeOp.Binary)
      result should be(expected)
    }

    "operation by name BigDecimal must be " in {
      val expected = TypeOp.BigDecimal
      val result = TypeOp.getTypeOperationByName("BigDecimal", TypeOp.BigDecimal)
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
