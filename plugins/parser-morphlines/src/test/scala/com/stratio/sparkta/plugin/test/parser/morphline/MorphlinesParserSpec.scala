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

package com.stratio.sparkta.plugin.test.parser.morphline

import java.io.Serializable

import com.stratio.sparkta.plugin.parser.morphline.MorphlinesParser
import com.stratio.sparkta.sdk.{Event, Input}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}


@RunWith(classOf[JUnitRunner])
class MorphlinesParserSpec extends WordSpecLike with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  val morphlineConfig = """
          id : test1
          importCommands : ["org.kitesdk.**"]
          commands: [
          {
              readJson {},
          }
          {
              extractJsonPaths {
                  paths : {
                      col1 : /col1
                      col2 : /col2
                  }
              }
          }
          {
            java {
              code : "return child.process(record);"
            }
          }
          {
              removeFields {
                  blacklist:["literal:_attachment_body"]
              }
          }
          ]
                        """
  val inputField = Input.RawDataKey
  val wrongInputField = "_wrong_attachment_body"
  val outputsFields = Seq("col1", "col2")
  val props: Map[String, Serializable] = Map("morphline" -> morphlineConfig)
  val parser = new MorphlinesParser("name", 1, inputField, outputsFields, props)

  "A MorphlinesParser" should {

    "parse a simple json" in {
      val simpleJson =
        """{
            "col1":"hello",
            "col2":"word"
            }
        """.getBytes("UTF-8")
      val e1 = new Event(Map(inputField -> simpleJson.asInstanceOf[Serializable]))

      val e2 = parser.parse(e1)

      e2.keyMap should contain(("col1", "hello"))
      e2.keyMap.size should be(3)
    }

    "exclude not configured fields" in {
      val simpleJson =
        """{
            "col1":"hello",
            "col2":"word",
            "col3":"!"
            }
        """.getBytes("UTF-8")
      val e1 = new Event(Map(inputField -> simpleJson.asInstanceOf[Serializable]))

      val e2 = parser.parse(e1)

      e2.keyMap should contain(("col1", "hello"))
      e2.keyMap should not contain(("col3", "!"))
      e2.keyMap.size should be(3)
    }

    "not cast input event to ByteArrayInputStream when _attachment_body is not coming" in {
      val wrongParser = new MorphlinesParser("name", 1, wrongInputField, outputsFields, props)
      val simpleJson =
        """{
            "col1":"hello",
            "col2":"word",
            "col3":"!"
            }
        """.getBytes("UTF-8")
      val e1 = new Event(Map(inputField -> simpleJson.asInstanceOf[Serializable]))

      val attachmentBodyValue = wrongParser
        .parse(e1)
        .keyMap
        .get("_attachment_body")
        .get

      attachmentBodyValue.isInstanceOf[Array[Byte]] should be(true)
    }
  }
}
