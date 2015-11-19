/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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

package com.stratio.sparkta.plugin.operator.fullText

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.{TypeOp, _}

import scala.util.Try

class FullTextOperator(name: String, properties: Map[String, JSerializable]) extends Operator(name, properties)
with ProcessMapAsAny with Associative {

  override val defaultTypeOperation = TypeOp.String

  override val writeOperation = WriteOp.FullText

  override def processReduce(values: Iterable[Option[Any]]): Option[String] = {
    Try(Option(values.flatten.map(_.toString).mkString(OperatorConstants.SpaceSeparator)))
      .getOrElse(Some(OperatorConstants.EmptyString))
  }

  def associativity(values: Iterable[(String, Option[Any])]): Option[String] = {
    val newValues = extractValues(values, None).map(_.toString).mkString(OperatorConstants.SpaceSeparator)

    Try(Option(transformValueByTypeOp(returnType, newValues)))
      .getOrElse(Some(OperatorConstants.EmptyString))
  }
}
