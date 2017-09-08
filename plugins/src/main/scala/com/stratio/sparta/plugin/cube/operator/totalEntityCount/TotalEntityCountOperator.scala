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
package com.stratio.sparta.plugin.cube.operator.totalEntityCount

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.plugin.cube.operator.entityCount.OperatorEntityCount
import com.stratio.sparta.sdk.pipeline.schema.TypeOp._
import com.stratio.sparta.sdk._
import com.stratio.sparta.sdk.pipeline.aggregation.operator.Associative
import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import org.apache.spark.sql.types.StructType

import scala.util.Try

class TotalEntityCountOperator(name: String,
                               schema: StructType,
                               properties: Map[String, JSerializable])
  extends OperatorEntityCount(name, schema, properties) with Associative {

  final val Some_Empty = Some(0)

  override val defaultTypeOperation = TypeOp.Int

  override def processReduce(values: Iterable[Option[Any]]): Option[Int] =
    Try(Option(values.flatten.map(value => {
      value match {
        case value if value.isInstanceOf[Seq[_]] => getDistinctValues(value.asInstanceOf[Seq[_]]).size
        case _ => TypeOp.castingToSchemaType(TypeOp.Int, value).asInstanceOf[Int]
      }
    }).sum)).getOrElse(Some_Empty)

  def associativity(values: Iterable[(String, Option[Any])]): Option[Int] = {
    val newValues =
      extractValues(values, None).map(value => TypeOp.castingToSchemaType(TypeOp.Int, value).asInstanceOf[Int]).sum

    Try(Option(castingToSchemaType(returnType, newValues)))
      .getOrElse(Some_Empty)
  }
}


