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

import com.stratio.sparkta.sdk.{TypeOp, WriteOp, Operator}
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import breeze.stats._
import breeze.linalg._


class StddevOperator(properties: Map[String, JSerializable]) extends Operator(properties) {

  override val typeOp = Some(TypeOp.Double)

  private val inputField = if(properties.contains("inputField")) Some(properties.getString("inputField")) else None

  override val key : String = "stddev_" + {
    if(inputField.isDefined) inputField.get else "undefined"
  }

  override val writeOperation = WriteOp.Stddev

  override def processMap(inputFields: Map[String, JSerializable]): Option[Number] = {
    if ((inputField.isDefined) && (inputFields.contains(inputField.get))) {
      Some(inputFields.get(inputField.get).get.asInstanceOf[Number])
    } else StddevOperator.SOME_ZERO_NUMBER
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
