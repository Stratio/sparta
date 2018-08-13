/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.properties

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
      val result = JsoneyString("[o]").toSeq
      val expected = Seq("o")

      expected should be (result)
    }
  }
}
