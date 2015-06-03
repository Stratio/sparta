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
package com.stratio.sparkta.plugin.parser.split

import com.stratio.sparkta.sdk.Event
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpec}


@RunWith(classOf[JUnitRunner])
class SplitParserSpec extends WordSpec with Matchers {


  val inputEvent: Event = new Event(Map("text" -> "hola mundo hola mundito"))

  "The SplitParser" should {
    "add a tuple with a list" in {
      val properties: Map[String, Serializable] = Map("textField" -> "text", "splitter" -> " ", "resultField" -> "parsedText").asInstanceOf[Map[String, Serializable]]
      val parser = new SplitParser(properties)
      val resultEvent = parser.parse(inputEvent)
      resultEvent.keyMap.get("parsedText") should be(Some(List("hola", "mundo", "hola", "mundito")))
    }
  }

}
