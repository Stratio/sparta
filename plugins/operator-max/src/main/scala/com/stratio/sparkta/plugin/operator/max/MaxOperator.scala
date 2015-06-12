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

package com.stratio.sparkta.plugin.operator.max

import java.io.{Serializable => JSerializable}
import com.stratio.sparkta.sdk.TypeOp
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._

import scala.util.Try

class MaxOperator(properties: Map[String, JSerializable]) extends Operator(properties) {

  override val defaultTypeOperation = TypeOp.Double

  private val inputField = if(properties.contains("inputField")) Some(properties.getString("inputField")) else None

  override val key : String = "max_" + {
    if(inputField.isDefined) inputField.get else "undefined"
  }

  override val writeOperation = WriteOp.Max

  override def processMap(inputFields: Map[String, JSerializable]): Option[Number] = {
    if ((inputField.isDefined) && (inputFields.contains(inputField.get)))
      getNumberFromSerializable(inputFields.get(inputField.get).get)
    else None
  }

  override def processReduce(values : Iterable[Option[Any]]): Option[Double] = {
    Try(Some(transformValueByTypeOp(returnType, values.flatten.map(_.asInstanceOf[Number].doubleValue()).max)))
      .getOrElse(MaxOperator.SOME_ZERO)
  }
}

private object MaxOperator {
  val SOME_ZERO = Some(0d)
}