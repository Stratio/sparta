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

  "MultiplexerSpec" should {

    "Return the combinations with the complex input" in {
      val input = (Seq("hola", "holo", "adios"), "fixed1", "fixed2")

      val expected = Vector(
        (Seq("hola"), "fixed1", "fixed2"),
        (Seq("holo"), "fixed1", "fixed2"),
        (Seq("adios"), "fixed1", "fixed2"),
        (Seq("hola", "holo"), "fixed1", "fixed2"),
        (Seq("hola", "adios"), "fixed1", "fixed2"),
        (Seq("holo", "adios"), "fixed1", "fixed2"),
        (Seq("hola", "holo", "adios"), "fixed1", "fixed2"))

      Multiplexer.combine(input) should be(expected)
    }

    "Return the combinations with the simple input" in {
      val input = Seq("hola", "holo", "adios")

      val expected = Vector(
        Seq("hola"),
        Seq("holo"),
        Seq("adios"),
        Seq("hola", "holo"),
        Seq("hola", "adios"),
        Seq("holo", "adios"),
        Seq("hola", "holo", "adios"))

      Multiplexer.combine(input) should be(expected)
    }
  }
}
