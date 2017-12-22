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
package com.stratio.sparta.sdk.properties

import com.github.mustachejava.DefaultMustacheFactory
import com.twitter.mustache.ScalaObjectHandler
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write
import org.json4s.{DefaultFormats, _}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class JsoneyStringTest extends WordSpecLike
with Matchers {

  "A JsoneyString" should {
    "have toString equivalent to its internal string" in {
      assertResult("foo")(JsoneyString("foo").toString)
    }

    "be deserialized if its JSON" in {
      implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
      val result = parse( """{ "foo": "bar" }""").extract[JsoneyString]
      assertResult(JsoneyString( """{"foo":"bar"}"""))(result)
    }

    "be deserialized if it's a String" in {
      implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
      val result = parse("\"foo\"").extract[JsoneyString]
      assertResult(JsoneyString("foo"))(result)
    }

    "be deserialized if it's an Int" in {
      implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
      val result = parse("1").extract[JsoneyString]
      assertResult(JsoneyString("1"))(result)
    }

    "be serialized as JSON" in {
      implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()

      var result = write(JsoneyString("foo"))
      assertResult("\"foo\"")(result)

      result = write(JsoneyString("{\"foo\":\"bar\"}"))
      assertResult("\"{\\\"foo\\\":\\\"bar\\\"}\"")(result)
    }

    "be deserialized if it's an JBool" in {
      implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()
      val result = parse("true").extract[JsoneyString]
      assertResult(JsoneyString("true"))(result)
    }

    "have toSeq equivalent to its internal string" in {
      val result = JsoneyString("o").toSeq
      val expected = Seq("o")

      expected should be (result)
    }

    "string with environment should substitute keys" in {
      val moustacheFactory = new DefaultMustacheFactory
      moustacheFactory.setObjectHandler(new ScalaObjectHandler)
      val environmentContext = Option(EnvironmentContext(moustacheFactory, Map("APP" -> "SPARTA")))
      val result = JsoneyString("Hi I'm {{APP}}", environmentContext).toString
      val expected = "Hi I'm SPARTA"

      expected should be (result)
    }

    "string with environment shouldn't substitute keys if not present in environment" in {
      val moustacheFactory = new DefaultMustacheFactory
      moustacheFactory.setObjectHandler(new ScalaObjectHandler)
      val environmentContext = Option(EnvironmentContext(moustacheFactory, Map("APP" -> "SPARTA")))
      val result = JsoneyString("Hi I'm {{COMPANY}}", environmentContext).toString
      val expected = "Hi I'm "

      expected should be (result)
    }
  }
}
