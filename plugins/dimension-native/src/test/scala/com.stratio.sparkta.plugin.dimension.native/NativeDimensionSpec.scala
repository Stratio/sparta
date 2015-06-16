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

package com.stratio.sparkta.plugin.dimension.native

import java.io.{Serializable => JSerializable}

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

import com.stratio.sparkta.sdk.{DimensionType, TypeOp}

@RunWith(classOf[JUnitRunner])
class NativeDimensionSpec extends WordSpecLike with Matchers {

  val nativeDimension: NativeDimension = new NativeDimension(Map("typeOp" -> "int"))

  "A NativeDimension" should {
    "In default implementation, get one precisions for a specific time" in {
      val precisions = nativeDimension.dimensionValues("foo".asInstanceOf[JSerializable]).map(_._1.id)

      precisions.size should be(1)

      precisions should contain(DimensionType.IdentityName)
    }

    "The precision must be int" in {
      nativeDimension.precisions(DimensionType.IdentityName).typeOp should be(TypeOp.Int)
    }
  }
}
