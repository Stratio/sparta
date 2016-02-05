/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.{TypeOp, _}

import scala.util.Try

class MaxOperator(name: String, properties: Map[String, JSerializable]) extends Operator(name, properties)
with OperatorProcessMapAsNumber with Associative {

  override val defaultTypeOperation = TypeOp.Double

  override val writeOperation = WriteOp.Max

  override val defaultCastingFilterType = TypeOp.Number

  override def processReduce(values: Iterable[Option[Any]]): Option[Double] = {
    Try(Option(getDistinctValues(values.flatten.map(_.asInstanceOf[Number].doubleValue())).max))
      .getOrElse(Option(Operator.Zero.toDouble))
  }

  def associativity(values: Iterable[(String, Option[Any])]): Option[Double] = {
    val newValues = extractValues(values, None)

    Try(Option(transformValueByTypeOp(returnType, newValues.map(_.asInstanceOf[Number].doubleValue()).max)))
      .getOrElse(Option(Operator.Zero.toDouble))
  }
}
