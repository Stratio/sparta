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
package com.stratio.sparta.sdk

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

import com.stratio.sparta.sdk.test.DimensionTypeMock

@RunWith(classOf[JUnitRunner])
class DimensionTest extends WordSpec with Matchers {

  "Dimension" should {
    val defaultDimensionType = new DimensionTypeMock(Map())
    val dimension = Dimension("dim1", "eventKey", "identity", defaultDimensionType)
    val dimensionIdentity = Dimension("dim1", "identity", "identity", defaultDimensionType)
    val dimensionNotIdentity = Dimension("dim1", "key", "key", defaultDimensionType)

    "Return the associated identity precision name" in {
      val expected = "identity"
      val result = dimensionIdentity.getNamePrecision
      result should be(expected)
    }

    "Return the associated name precision name" in {
      val expected = "key"
      val result = dimensionNotIdentity.getNamePrecision
      result should be(expected)
    }

    "Return the associated precision name" in {
      val expected = "eventKey"
      val result = dimension.getNamePrecision
      result should be(expected)
    }

    "Compare function with other dimension must be less" in {
      val dimension2 = Dimension("dim2", "eventKey", "identity", defaultDimensionType)
      val expected = -1
      val result = dimension.compare(dimension2)
      result should be(expected)
    }

    "Compare function with other dimension must be equal" in {
      val dimension2 = Dimension("dim1", "eventKey", "identity", defaultDimensionType)
      val expected = 0
      val result = dimension.compare(dimension2)
      result should be(expected)
    }

    "Compare function with other dimension must be higher" in {
      val dimension2 = Dimension("dim0", "eventKey", "identity", defaultDimensionType)
      val expected = 1
      val result = dimension.compare(dimension2)
      result should be(expected)
    }

    "classSuffix must be " in {
      val expected = "Field"
      val result = Dimension.FieldClassSuffix
      result should be(expected)
    }
  }
}
