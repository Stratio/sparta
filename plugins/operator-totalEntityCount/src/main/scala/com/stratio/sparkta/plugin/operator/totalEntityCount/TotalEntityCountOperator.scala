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

package com.stratio.sparkta.plugin.operator.totalEntityCount

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk._

import scala.util.Try

class TotalEntityCountOperator(name: String, properties: Map[String, JSerializable])
  extends EntityCount(name, properties) {

  final val Some_Empty = Some(0)

  override val defaultTypeOperation = TypeOp.Int

  override val writeOperation = WriteOp.WordCount

  override def processReduce(values: Iterable[Option[Any]]): Option[Int] = {
    Try {
      val wordCounts = getDistinctValues(values.flatten.flatMap(_.asInstanceOf[Seq[String]])).size
      Some(transformValueByTypeOp(returnType, wordCounts))
    }.getOrElse(Some_Empty)
  }
}


