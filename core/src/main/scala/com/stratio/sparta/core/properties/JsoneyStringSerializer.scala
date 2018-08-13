/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.properties

import org.json4s._
import org.json4s.ext.DateTimeSerializer
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write

import scala.util.Try

//scalastyle:off

class JsoneyStringSerializer()
  extends CustomSerializer[JsoneyString](_ => {

    implicit val json4sJacksonFormats: Formats =
      DefaultFormats + DateTimeSerializer + new JsoneyStringSerializer()

    ( {
      case obj: JObject =>
        JsoneyString(write(obj))
      case _: org.json4s.JsonAST.JNull.type =>
        JsoneyString(null)
      case arr: JArray =>
        JsoneyString(write(arr))
      case s: JString =>
        JsoneyString(s.s)
      case i: JInt =>
        JsoneyString(i.num.toString())
      case i: JDouble =>
        JsoneyString(i.num.toString)
      case b: JBool =>
        JsoneyString(b.value.toString)
    }, {
      case x: JsoneyString =>
        if (x.toString == null) {
          new JString("")
        } else if (x.toString.contains("[") && x.toString.contains("{")) {
          Try(parse(x.toString)).getOrElse(new JString(x.toString))
        } else if (x.toString.equals("true") || x.toString.equals("false")) {
          new JBool(x.toString.toBoolean)
        } else {
          new JString(x.toString)
        }
    }
    )
  })