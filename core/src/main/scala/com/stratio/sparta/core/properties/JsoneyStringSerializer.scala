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

class JsoneyStringSerializer(environmentContext: Option[EnvironmentContext] = None)
  extends CustomSerializer[JsoneyString](_ => {

    implicit val json4sJacksonFormats: Formats =
      DefaultFormats + DateTimeSerializer + new JsoneyStringSerializer(environmentContext)

    ( {
      case obj: JObject =>
        JsoneyString(write(obj), environmentContext)
      case _: org.json4s.JsonAST.JNull.type =>
        JsoneyString(null, environmentContext)
      case arr: JArray =>
        JsoneyString(write(arr), environmentContext)
      case s: JString =>
        JsoneyString(s.s, environmentContext)
      case i: JInt =>
        JsoneyString(i.num.toString(), environmentContext)
      case i: JDouble =>
        JsoneyString(i.num.toString, environmentContext)
      case b: JBool =>
        JsoneyString(b.value.toString, environmentContext)
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