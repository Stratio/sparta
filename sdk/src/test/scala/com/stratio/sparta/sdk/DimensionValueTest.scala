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
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import com.stratio.sparta.sdk.test.DimensionTypeMock

@RunWith(classOf[JUnitRunner])
class DimensionValueTest extends WordSpec with Matchers {

  "DimensionValue" should {
    val defaultDimensionType = new DimensionTypeMock(Map())
    val dimension = Dimension("dim1", "eventKey", "identity", defaultDimensionType)
    val dimensionValue = DimensionValue(dimension, "hola")

    "return the correct name" in {

      val expected = "dim1"

      val result = dimensionValue.getNameDimension

      result should be(expected)
    }
  }
}
