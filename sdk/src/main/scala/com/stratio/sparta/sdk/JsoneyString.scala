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
package com.stratio.sparta.sdk

import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write

case class JsoneyString(string : String) {
  override def toString : String = string
  def toSeq : Seq[String] = {
    // transfors string of the form "[\"prop1\",\"prop2\"]" in a Seq
    string.drop(1).dropRight(1).replaceAll("\"","").split(",").toSeq
  }
}

class JsoneyStringSerializer extends CustomSerializer[JsoneyString](format => (
  {
    case obj : JObject => {
      new JsoneyString(write(obj)(implicitly(DefaultFormats + new JsoneyStringSerializer)))
    }
    case obj : JArray => {
      new JsoneyString(write(obj)(implicitly(DefaultFormats + new JsoneyStringSerializer)))
    }
    case s: JString =>
      new JsoneyString(s.s)
    case i : JInt =>
      new JsoneyString(i.num.toString())
    case b : JBool =>
      new JsoneyString(b.value.toString())
  },
  {
    case x: JsoneyString =>
      if(x.string.contains("[") && x.string.contains("{")) {
        parse(x.string)
      } else if(x.string.equals("true") || x.string.equals("false")) {
        new JBool(x.string.toBoolean)
      } else {
        new JString(x.string)
      }
  }
  )) {
}