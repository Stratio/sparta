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

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.{Event, Parser}

import scala.util.{Failure, Success, Try}

class SplitParser(name: String,
                  order: Integer,
                  inputField: String,
                  outputFields: Seq[String],
                  properties: Map[String, JSerializable])
  extends Parser(name, order, inputField, outputFields, properties) {

  val splitter = properties.getString(SplitParser.Splitter)
  val resultField = properties.getString(SplitParser.ResultField)

  override def parse(data: Event): Event = {
    val txtValue = data.keyMap.getOrElse(inputField, None)
    val splitted = txtValue match {
      case (x: String) => Try {
        val splittedText = x.toString.split(splitter).toList
        Map(resultField -> splittedText)
      }
      case None => new Failure(new NoSuchElementException)
    }
    val result = new Event(data.keyMap ++ {
      splitted match {
        case Success(x: Map[String, List[String]]) => x.asInstanceOf[Map[String, JSerializable]]
        case Failure(_) => Map()
      }
    })
    result
  }
}

object SplitParser {

  final val TextField = "textField"
  final val Splitter = "splitter"
  final val ResultField = "resultField"
}
