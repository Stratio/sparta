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
package com.stratio.sparkta.plugin.operator.stddev

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.{WriteOp, Operator}
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import breeze.stats._
import breeze.linalg._


class StddevOperator(properties: Map[String, JSerializable]) extends Operator(properties) {

  private val inputField = properties.getString("inputField")

  override val key : String = "stddev_" + inputField

  override val writeOperation = WriteOp.Stddev

  override def processMap(inputFields: Map[String, JSerializable]): Option[Number] =
    inputFields.contains(inputField) match {
      case false => StddevOperator.SOME_ZERO_NUMBER
      case true => Some(inputFields.get(inputField).get.asInstanceOf[Number])
    }

  override def processReduce(values: Iterable[Option[Any]]): Option[Double] = {
    values.size match {
      case (nz) if (nz != 0) => Some(stddev(values.map(_.get.asInstanceOf[Number].doubleValue())))
      case _ => StddevOperator.SOME_ZERO
    }
  }
}

private object StddevOperator {
  val SOME_ZERO = Some(0d)
  val SOME_ZERO_NUMBER = Some(0d.asInstanceOf[Number])
}
