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

import java.io.Serializable

import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.{Event, Parser}

class TypeParser(properties: Map[String, Serializable]) extends Parser(properties) {

  override def parse(data: Event): Event = {

    val sourceField = properties.getString("sourceField")
    val typeField = properties.getString("type")
    val targetField = properties.getString("newField")

    new Event(data.keyMap.map({
      case (key, value) =>
        if (sourceField.equals(key)) {
          typeField.toLowerCase match {
            case "byte" => (targetField, value.toString.toByte.asInstanceOf[Serializable])
            case "short" => (targetField, value.toString.toShort.asInstanceOf[Serializable])
            case "int" => (targetField, value.toString.toInt.asInstanceOf[Serializable])
            case "long" => (targetField, value.toString.toLong.asInstanceOf[Serializable])
            case "float" => (targetField, value.toString.toFloat.asInstanceOf[Serializable])
            case "double" => (targetField, value.toString.toDouble.asInstanceOf[Serializable])
            case _ =>
              throw new IllegalArgumentException("Possible values for property type are: Byte, Short, Int, Long, " +
                "Float " +
                "and " +
                "Double")
          }
        } else {
          (key, value)
        }
    }))
  }
}
