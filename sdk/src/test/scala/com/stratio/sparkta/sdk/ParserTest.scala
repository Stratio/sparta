/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

import java.io.{Serializable => JSerializable}

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

import com.stratio.sparkta.sdk.test.ParserMock

@RunWith(classOf[JUnitRunner])
class ParserTest extends WordSpec with Matchers {

  "Parser" should {

    val parserTest = new ParserMock("parser", 1, "input", Seq("output"), Map())

    "Order must be " in {
      val expected = 1
      val result = parserTest.getOrder
      result should be(expected)
    }

    "Parse must be " in {
      val event = new Event(Map("field" -> "value"))
      val expected = event
      val result = parserTest.parse(event)
      result should be(expected)
    }

    "checked fields not be contained in outputs must be " in {
      val keyMap = Map("field" -> "value")
      val expected = Map()
      val result = parserTest.checkFields(keyMap)
      result should be(expected)
    }

    "checked fields are contained in outputs must be " in {
      val keyMap = Map("output" -> "value")
      val expected = keyMap
      val result = parserTest.checkFields(keyMap)
      result should be(expected)
    }

    "classSuffix must be " in {
      val expected = "Parser"
      val result = Parser.ClassSuffix
      result should be(expected)
    }
  }
}
