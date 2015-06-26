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

package com.stratio.sparkta.plugin.field.default

import java.io.{Serializable => JSerializable}

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

import com.stratio.sparkta.sdk.{Precision, DimensionType, TypeOp}

@RunWith(classOf[JUnitRunner])
class DefaultFieldSpec extends WordSpecLike with Matchers {

  val defaultDimension: DefaultField = new DefaultField(Map("typeOp" -> "int"))

  "A DefaultDimension" should {
    "In default implementation, get one precisions for a specific time" in {
      val precision: (Precision, JSerializable) = defaultDimension.precisionValue("", "foo".asInstanceOf[JSerializable])

      precision._2 should be("foo")

      precision._1.id should be(DimensionType.IdentityName)
    }

    "The precision must be int" in {
      defaultDimension.precision(DimensionType.IdentityName).typeOp should be(TypeOp.Int)
    }
  }
}
