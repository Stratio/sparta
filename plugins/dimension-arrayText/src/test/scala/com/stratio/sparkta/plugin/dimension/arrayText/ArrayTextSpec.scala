package com.stratio.sparkta.plugin.dimension.arrayText

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

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.{BucketType, TypeOp}
import org.scalatest.{Matchers, WordSpecLike}


class ArrayTextSpec extends WordSpecLike with Matchers {

  val dimType = new ArrayTextDimension()

  "ArrayTextDimType " should {

    "process the input" in {
      val result = dimType.bucket(input.asInstanceOf[Serializable])
      result should be(expectedOutput)
    }

    "process other input" in {
      val result = dimType.bucket(otherInput.asInstanceOf[Serializable])
      result should be(otherExpectedOutput)
    }

  }
  val input: Seq[String] = Seq("hola", "holo")

  val otherInput = Seq("hola","hola")

  val expectedOutput: Map[BucketType, JSerializable] =
    Map(BucketType("hola" + 0, TypeOp.String, Map()) -> "hola".asInstanceOf[JSerializable],
      BucketType("holo" + 1, TypeOp.String, Map()) -> "holo".asInstanceOf[JSerializable])

  val otherExpectedOutput: Map[BucketType, JSerializable] =
    Map(BucketType("hola" + 0, TypeOp.String, Map()) -> "hola".asInstanceOf[JSerializable],
      BucketType("hola" + 1, TypeOp.String, Map()) -> "hola".asInstanceOf[JSerializable])

}
