/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.plugin.operator.max

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk._
import ValidatingPropertyMap._

class MaxOperator(properties: Map[String, JSerializable]) extends Operator(properties) {

  private val inputField = properties.getString("inputField")

  override val key : String = "max_" + inputField

  override val writeOperation = WriteOp.Max

  override def processMap(inputFields: Map[String, JSerializable]): Option[Long] =
    inputFields.contains(inputField) match {
      case false => None
      case true => Some(inputFields.getLong(inputField))
    }

  override def processReduce(values: Iterable[Long]): Long =
    values.max

}
