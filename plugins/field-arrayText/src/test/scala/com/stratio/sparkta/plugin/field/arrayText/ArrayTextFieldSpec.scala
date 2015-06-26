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
import org.scalatest.{Matchers, WordSpecLike}


class ArrayTextFieldSpec extends WordSpecLike with Matchers {

  val dimType = new ArrayTextField()

  "ArrayTextDimType " should {

    "process the input" in {
      val result = dimType.dimensionValues(input.asInstanceOf[JSerializable])
      result should be(expectedOutput)
    }

    "process other input" in {
      val result = dimType.dimensionValues(otherInput.asInstanceOf[JSerializable])
      result should be(otherExpectedOutput)
    }

  }
  val input: Seq[String] = Seq("hola", "holo")

  val otherInput = Seq("hola","hola")

  val expectedOutput: Map[Precision, JSerializable] =
    Map(Precision("hola" + 0, TypeOp.String, Map()) -> "hola".asInstanceOf[JSerializable],
      Precision("holo" + 1, TypeOp.String, Map()) -> "holo".asInstanceOf[JSerializable])

  val otherExpectedOutput: Map[Precision, JSerializable] =
    Map(Precision("hola" + 0, TypeOp.String, Map()) -> "hola".asInstanceOf[JSerializable],
      Precision("hola" + 1, TypeOp.String, Map()) -> "hola".asInstanceOf[JSerializable])

}
