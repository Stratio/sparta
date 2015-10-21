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

package com.stratio.sparkta.plugin.operator.median

import java.io.{Serializable => JSerializable}

import breeze.linalg._
import breeze.stats._

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.{TypeOp, _}

class MedianOperator(name: String, properties: Map[String, JSerializable]) extends Operator(name, properties)
with ProcessMapAsNumber {

  override val defaultTypeOperation = TypeOp.Double

  override val writeOperation = WriteOp.Median

  override val castingFilterType = TypeOp.Number

  override def processReduce(values: Iterable[Option[Any]]): Option[Double] = {
    val valuesFiltered = getDistinctValues(values.flatten)
    valuesFiltered.size match {
      case (nz) if (nz != 0) => Some(transformValueByTypeOp(returnType,
        median(DenseVector(valuesFiltered.map(_.asInstanceOf[Number].doubleValue()).toArray))))
      case _ => Some(OperatorConstants.Zero.toDouble)
    }
  }
}
