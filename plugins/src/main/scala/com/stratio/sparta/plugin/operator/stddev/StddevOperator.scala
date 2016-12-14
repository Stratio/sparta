/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
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
package com.stratio.sparta.plugin.operator.stddev

import java.io.{Serializable => JSerializable}

import breeze.stats._

import com.stratio.sparta.sdk.TypeOp._
import com.stratio.sparta.sdk.{TypeOp, _}
import org.apache.spark.sql.types.StructType

class StddevOperator(name: String,
                     schema: StructType,
                     properties: Map[String, JSerializable]) extends Operator(name, schema, properties)
with OperatorProcessMapAsNumber {

  val inputSchema = schema

  override val defaultTypeOperation = TypeOp.Double

  override val defaultCastingFilterType = TypeOp.Number

  override def processReduce(values: Iterable[Option[Any]]): Option[Double] = {
    val valuesFiltered = getDistinctValues(values.flatten)
    valuesFiltered.size match {
      case (nz) if (nz != 0) =>
        Some(transformValueByTypeOp(returnType, stddev(valuesFiltered.map(value =>
          TypeOp.transformValueByTypeOp(TypeOp.Double, value).asInstanceOf[Double]))))
      case _ => Some(Operator.Zero.toDouble)
    }
  }
}
