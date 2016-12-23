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
package com.stratio.sparta.plugin.transformation.morphline

import java.io.Serializable

import com.stratio.sparta.plugin.transformation.morphline.MorphlinesParser
import com.stratio.sparta.sdk.pipeline.input.Input
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}


@RunWith(classOf[JUnitRunner])
class MorphlinesParserTest extends WordSpecLike with Matchers with BeforeAndAfter with BeforeAndAfterAll {

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
  val outputsFields = Seq("col1", "col2")
  val props: Map[String, Serializable] = Map("morphline" -> morphlineConfig)

  val schema = StructType(Seq(StructField("col1", StringType), StructField("col2", StringType)))

  val parser = new MorphlinesParser(1, inputField, outputsFields, schema, props)

  "A MorphlinesParser" should {

    "parse a simple json" in {
      val simpleJson =
        """{
            "col1":"hello",
            "col2":"word"
            }
        """
      val input = Row(simpleJson)
      val result = parser.parse(input, false)

      val expected = Option(Row(simpleJson, "hello", "world"))

      result should be eq(expected)
    }

    "parse a simple json removing raw" in {
      val simpleJson =
        """{
            "col1":"hello",
            "col2":"word"
            }
        """
      val input = Row(simpleJson)
      val result = parser.parse(input, true)

      val expected = Option(Row("hello", "world"))

      result should be eq(expected)
    }

    "exclude not configured fields" in {
      val simpleJson =
        """{
            "col1":"hello",
            "col2":"word",
            "col3":"!"
            }
        """
      val input = Row(simpleJson)
      val result = parser.parse(input, false)

      val expected = Option(Row(simpleJson, "hello", "world"))

      result should be eq(expected)
    }
  }
}
