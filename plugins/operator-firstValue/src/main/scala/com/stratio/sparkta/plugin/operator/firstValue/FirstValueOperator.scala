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

package com.stratio.sparkta.plugin.operator.firstValue

import java.io.{Serializable => JSerializable}
import scala.util.Try

import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk._

class FirstValueOperator(properties: Map[String, JSerializable]) extends Operator(properties) {

  override val typeOp = Some(TypeOp.String)

  private val inputField = if (properties.contains("inputField")) properties.getString("inputField", None) else None

  override val key: String = "first_" + {
    if(inputField.isDefined) inputField.get else "undefined"
  }

  override val writeOperation = WriteOp.Set

  override def processMap(inputFields: Map[String, JSerializable]): Option[Any] =
    if ((inputField.isDefined) && (inputFields.contains(inputField.get))) {
      Some(inputFields.get(inputField.get).get)
    } else FirstValueOperator.SOME_EMPTY

  override def processReduce(values: Iterable[Option[Any]]): Option[Any] =
    Try(values.head.orElse(FirstValueOperator.SOME_EMPTY)).getOrElse(FirstValueOperator.SOME_EMPTY)
}

private object FirstValueOperator {
  val SOME_EMPTY = Some("")
}
