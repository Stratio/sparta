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
package com.stratio.sparta.plugin.cube.operator.sum

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.pipeline.schema.TypeOp._
import com.stratio.sparta.sdk._
import com.stratio.sparta.sdk.pipeline.aggregation.operator.{Associative, Operator, OperatorProcessMapAsNumber}
import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import org.apache.spark.sql.types.StructType

import scala.util.Try

class SumOperator(name: String,
                  val schema: StructType,
                  properties: Map[String, JSerializable]) extends Operator(name, schema, properties)
with OperatorProcessMapAsNumber with Associative {

  val inputSchema = schema

  override val defaultTypeOperation = TypeOp.Double

  override def processReduce(values: Iterable[Option[Any]]): Option[Double] = {
    Try(Option(getDistinctValues(values.flatten.map(value =>
      TypeOp.transformValueByTypeOp(TypeOp.Double, value).asInstanceOf[Double])).sum))
      .getOrElse(Some(Operator.Zero.toDouble))
  }

  def associativity(values: Iterable[(String, Option[Any])]): Option[Double] = {
    val newValues = extractValues(values, None)

    Try(Option(transformValueByTypeOp(returnType, newValues.map(value =>
      TypeOp.transformValueByTypeOp(TypeOp.Double, value).asInstanceOf[Double]).sum)))
      .getOrElse(Some(Operator.Zero.toDouble))
  }
}
