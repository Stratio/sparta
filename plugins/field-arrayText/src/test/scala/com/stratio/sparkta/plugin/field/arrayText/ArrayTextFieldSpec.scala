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

package com.stratio.sparkta.plugin.field.arrayText

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.plugin.field.arrayText.ArrayTextField
import com.stratio.sparkta.sdk._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class ArrayTextFieldSpec extends WordSpecLike with Matchers {

  val dimType = new ArrayTextField()

  "ArrayTextDimType " should {

    "process the input" in {
      val result = dimType.precisionValue("hola", input.asInstanceOf[JSerializable])
      result should be(expectedOutput)

      val result1 = dimType.precisionValue("holo", input.asInstanceOf[JSerializable])
      result1 should be(noExpectedOutput)

      val result2 = dimType.precisionValue("hola", otherInput.asInstanceOf[JSerializable])
      result2 should be(otherExpectedOutput)

      val result3 = dimType.precisionValue("holo", otherInput.asInstanceOf[JSerializable])
      result3 should be(otherNoExpectedOutput)
    }

  }
  val input: Seq[String] = Seq("hola", "holo")

  val otherInput = Seq("hola","hola")

  val expectedOutput = (Precision("hola" + 0, TypeOp.String, Map()), "0".asInstanceOf[JSerializable])
  val noExpectedOutput = (Precision("holo" + 1, TypeOp.String, Map()), "1".asInstanceOf[JSerializable])

  val otherExpectedOutput = (Precision("hola" + 0, TypeOp.String, Map()), "0".asInstanceOf[JSerializable])
  val otherNoExpectedOutput =
    (Precision(DimensionType.IdentityName, TypeOp.String, Map()), 0.asInstanceOf[JSerializable])

}
