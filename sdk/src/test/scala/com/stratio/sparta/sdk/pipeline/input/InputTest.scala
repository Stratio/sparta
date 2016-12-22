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
package com.stratio.sparta.sdk.pipeline.input

import org.apache.spark.storage.StorageLevel
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class InputTest extends WordSpec with Matchers {

  "Input" should {
    val input = new InputMock(Map())
    val expected = StorageLevel.DISK_ONLY
    val result = input.storageLevel("DISK_ONLY")

    "Return the associated storageLevel" in {
      result should be(expected)
    }
  }

  "classSuffix must be " in {
    val expected = "Input"
    val result = Input.ClassSuffix
    result should be(expected)
  }
}
