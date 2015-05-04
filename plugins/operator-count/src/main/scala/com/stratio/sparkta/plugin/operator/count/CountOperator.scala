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

package com.stratio.sparkta.plugin.operator.count

import java.io.{Serializable => JSerializable}
import scala.util.Try

import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk._

class CountOperator(properties: Map[String, JSerializable]) extends Operator(properties) {

  private val distinctFields = if (properties.contains("distinctFields")) {
    val fields = properties.getString("distinctFields").split(",")
    if (fields.isEmpty) None else Some(fields)
  } else None

  override val typeOp = Some(TypeOp.Long)

  override val key: String = "count" + {
    if (distinctFields.isDefined) "_distinct" else ""
  }

  override val writeOperation = WriteOp.Inc

  override def processMap(inputFields: Map[String, JSerializable]): Option[Any] = {
    distinctFields match {
      case None => CountOperator.SOME_ONE
      case Some(fields) => Some(fields.toString)
    }
  }

  override def processReduce(values: Iterable[Option[Any]]): Option[Long] = {
    Try {
      val longList: Iterable[Long] = distinctFields match {
        case None => values.map(_.get.asInstanceOf[Number].longValue())
        case Some(fields) => values.toList.distinct.map(value => CountOperator.SOME_ONE.get)
      }
      Some(longList.reduce(_ + _))
    }.getOrElse(CountOperator.SOME_ZERO)
  }
}

private object CountOperator {
  val SOME_ONE = Some(1L)
  val SOME_ZERO = Some(0L)
}
