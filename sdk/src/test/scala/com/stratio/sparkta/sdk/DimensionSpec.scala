/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.sdk

import com.stratio.sparkta.sdk.test.DimensionTypeTest
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DimensionSpec extends WordSpec with Matchers {

  "DimensionSpec" should {
    val defaultDimensionType = new DimensionTypeTest(Map())
    val dimension = Dimension("dim1", "eventKey", "identity", defaultDimensionType)

    "Return the associated precision name" in {
      dimension.getNamePrecision should be("eventKey")
    }

    "Compare function with other dimension must be less" in {
      val dimension2 = Dimension("dim2", "eventKey", "identity", defaultDimensionType)
      dimension.compare(dimension2) should be (-1)
    }

    "Compare function with other dimension must be equal" in {
      val dimension2 = Dimension("dim1", "eventKey", "identity", defaultDimensionType)
      dimension.compare(dimension2) should be (0)
    }

    "Compare function with other dimension must be higher" in {
      val dimension2 = Dimension("dim0", "eventKey", "identity", defaultDimensionType)
      dimension.compare(dimension2) should be (1)
    }

    "The string value must be the name" in {
      dimension.toString should be ("dim1")
    }
  }
}
