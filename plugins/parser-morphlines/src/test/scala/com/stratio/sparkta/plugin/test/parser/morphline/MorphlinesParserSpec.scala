/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.plugin.test.parser.morphline

import java.io.Serializable

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.plugin.parser.morphline.MorphlinesParser
import com.stratio.sparkta.sdk.{Event, Input}
import com.typesafe.config.ConfigRenderOptions
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

/**
 * Created by ajnavarro on 24/10/14.
 */
@RunWith(classOf[JUnitRunner])
class MorphlinesParserSpec extends WordSpecLike with Matchers with BeforeAndAfter with BeforeAndAfterAll {
  var parser: MorphlinesParser = null

  before {
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
    val props: Map[String, Serializable] = Map("morphline" -> morphlineConfig)
    parser = new MorphlinesParser(props)
  }

  after {
    val parser = null
  }

  "A MorphlinesParser" should {
    "parse a simple json" in {
      val simpleJson =
        """{
            "col1":"hello",
            "col2":"word"
            }
        """.getBytes("UTF-8")
      val e1 = new Event(Map(Input.RAW_DATA_KEY -> simpleJson.asInstanceOf[Serializable]))
      val e2 = parser.parse(e1)
      e2.keyMap should contain(("col1", "hello"))
    }
  }
}
