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

package com.stratio.sparkta.plugin.operator.entityCount

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk._

import scala.util.Try

class EntityCountOperator(name: String, properties: Map[String, JSerializable])
  extends EntityCount(name, properties) with Associative {

  final val Some_Empty = Some(Map("" -> 0L))

  override val defaultTypeOperation = TypeOp.MapStringLong

  override val writeOperation = WriteOp.EntityCount

  override def processReduce(values: Iterable[Option[Any]]): Option[Map[String, Long]] =
    Try(Option(applyCount(getDistinctValues(values.flatten.flatMap(_.asInstanceOf[Seq[String]])))))
      .getOrElse(Option(Map()))

  def associativity(values: Iterable[(String, Option[Any])]): Option[Map[String, Long]] = {
    val newValues = getDistinctValues(extractValues(values, None).flatMap(_.asInstanceOf[Seq[String]]))

    Try(Option(transformValueByTypeOp(returnType, applyCount(newValues)))).getOrElse(Option(Map()))
  }

  private def applyCount(values: List[String]): Map[String, Long] =
    values.groupBy((word: String) => word).mapValues(_.length.toLong)
}


