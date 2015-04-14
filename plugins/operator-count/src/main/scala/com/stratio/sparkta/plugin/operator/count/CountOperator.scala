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
package com.stratio.sparkta.plugin.operator.count

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk._

import scala.util.Try

class CountOperator(properties: Map[String, JSerializable]) extends Operator(properties) {

  override val key: String = "count"

  override val writeOperation = WriteOp.Inc

  override def processMap(inputFields: Map[String, JSerializable]): Option[Long] = {
    CountOperator.SOME_ONE
  }

  override def processReduce(values : Iterable[Option[Any]]): Option[Long] =
    Try(Some(values.map(_.get.asInstanceOf[Number].longValue()).reduce(_ + _)))
      .getOrElse(CountOperator.SOME_ZERO)
}

private object CountOperator {
  val SOME_ONE = Some(1L)
  val SOME_ZERO = Some(0L)
}
