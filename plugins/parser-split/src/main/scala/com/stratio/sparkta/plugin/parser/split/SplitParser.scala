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

import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.{Event, Parser}

import scala.util.{Failure, Success, Try}

/**
 * @author arincon
 */
class SplitParser(properties: Map[String, Serializable]) extends Parser(properties) {
  val txtField = properties.getString(SplitParser.TextField)
  val splitter = properties.getString(SplitParser.Splitter)
  val resultField = properties.getString(SplitParser.ResultField)

  override def parse(data: Event): Event = {
    val txtValue = data.keyMap.getOrElse(txtField, None)
    val splitted = Try {
      Map(resultField -> txtValue.toString.split(splitter).toList)
    }
    val result = splitted match {
      case Success(x) => new Event(data.keyMap ++ x.asInstanceOf[Map[String, Serializable]])
      case Failure(_) => data
    }
    result
  }
}

object SplitParser {

  final val TextField = "textField"
  final val Splitter = "splitter"
  final val ResultField = "resultField"
}
