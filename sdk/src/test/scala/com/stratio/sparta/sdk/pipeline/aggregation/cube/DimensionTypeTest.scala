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
package com.stratio.sparta.sdk.pipeline.aggregation.cube

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class DimensionTypeTest extends WordSpec with Matchers {

  val prop = Map("hello" -> "bye")

  "DimensionType" should {

    "the return operations properties" in {
      val dimensionTypeTest = new DimensionTypeMock(prop)
      val result = dimensionTypeTest.operationProps
      result should be(prop)
    }

    "the return properties" in {
      val dimensionTypeTest = new DimensionTypeMock(prop)
      val result = dimensionTypeTest.properties
      result should be(prop)
    }

    "the return precisionValue" in {
      val dimensionTypeTest = new DimensionTypeMock(prop)
      val expected = (DimensionType.getIdentity(None, dimensionTypeTest.defaultTypeOperation), "hello")
      val result = dimensionTypeTest.precisionValue("", "hello")
      result should be(expected)
    }

    "the return precision" in {
      val dimensionTypeTest = new DimensionTypeMock(prop)
      val expected = (DimensionType.getIdentity(None, dimensionTypeTest.defaultTypeOperation))
      val result = dimensionTypeTest.precision("")
      result should be(expected)
    }
  }

  "DimensionType object" should {

    "getIdentity must be " in {
      val identity = DimensionType.getIdentity(None, TypeOp.Int)
      identity.typeOp should be(TypeOp.Int)
      identity.id should be(DimensionType.IdentityName)
      val identity2 = DimensionType.getIdentity(Some(TypeOp.String), TypeOp.Int)
      identity2.typeOp should be(TypeOp.String)
    }

    "getIdentityField must be " in {
      val identity = DimensionType.getIdentityField(None, TypeOp.Int)
      identity.typeOp should be(TypeOp.Int)
      identity.id should be(DimensionType.IdentityFieldName)
      val identity2 = DimensionType.getIdentityField(Some(TypeOp.String), TypeOp.Int)
      identity2.typeOp should be(TypeOp.String)
    }

    "getTimestamp must be " in {
      val identity = DimensionType.getTimestamp(None, TypeOp.Int)
      identity.typeOp should be(TypeOp.Int)
      identity.id should be(DimensionType.TimestampName)
      val identity2 = DimensionType.getTimestamp(Some(TypeOp.String), TypeOp.Int)
      identity2.typeOp should be(TypeOp.String)
    }
  }
}
