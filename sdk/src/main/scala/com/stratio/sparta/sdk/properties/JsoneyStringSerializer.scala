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

import org.json4s._
import org.json4s.ext.DateTimeSerializer
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write

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
          parse(x.toString)
        } else if (x.toString.equals("true") || x.toString.equals("false")) {
          new JBool(x.toString.toBoolean)
        } else {
          new JString(x.toString)
        }
    }
    )
  })