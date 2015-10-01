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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}

@RunWith(classOf[JUnitRunner])
class MultiplexerSpec extends WordSpec with Matchers {

  "Multiplexer" should {

    "Return the combinations with the complex input" in {
      val input = (Seq("hello", "bye", "die"), "fixed1", "fixed2")

      val expected = Vector(
        (Seq("hello"), "fixed1", "fixed2"),
        (Seq("bye"), "fixed1", "fixed2"),
        (Seq("die"), "fixed1", "fixed2"),
        (Seq("hello", "bye"), "fixed1", "fixed2"),
        (Seq("hello", "die"), "fixed1", "fixed2"),
        (Seq("bye", "die"), "fixed1", "fixed2"),
        (Seq("hello", "bye", "die"), "fixed1", "fixed2"))

      val result = Multiplexer.combine(input)

      result should be(expected)
    }

    "Return the combinations with the simple input" in {
      val input = Seq("hello", "bye", "die")

      val expected = Vector(
        Seq("hello"),
        Seq("bye"),
        Seq("die"),
        Seq("hello", "bye"),
        Seq("hello", "die"),
        Seq("bye", "die"),
        Seq("hello", "bye", "die"))

      val result = Multiplexer.combine(input)

      result should be(expected)
    }
  }
}
