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

package com.stratio.sparkta.plugin.parser.typeparser

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.{Event, Parser}

class TypeParser(name: String,
                 order: Integer,
                 inputField: String,
                 outputFields: Seq[String],
                 properties: Map[String, JSerializable])
  extends Parser(name, order, inputField, outputFields, properties) {

  override def parse(data: Event): Event = {

    val typeField = properties.getString("type")
    val targetField = properties.getString("newField")

    new Event(data.keyMap.map {
      case (key, value) =>
        if (inputField.equals(key) && outputFields.contains(targetField))
          stringToSerializable(typeField, targetField, value)
        else (key, value)
    })
  }

  private def stringToSerializable(typeField: String, targetField: String, value: JSerializable) = {
    typeField.toLowerCase match {
      case "byte" => (targetField, value.toString.toByte.asInstanceOf[JSerializable])
      case "short" => (targetField, value.toString.toShort.asInstanceOf[JSerializable])
      case "int" => (targetField, value.toString.toInt.asInstanceOf[JSerializable])
      case "long" => (targetField, value.toString.toLong.asInstanceOf[JSerializable])
      case "float" => (targetField, value.toString.toFloat.asInstanceOf[JSerializable])
      case "double" => (targetField, value.toString.toDouble.asInstanceOf[JSerializable])
      case _ =>
        throw new IllegalArgumentException("Possible values for property type are: Byte, Short, Int, Long, " +
          "Float and Double")
    }
  }
}
