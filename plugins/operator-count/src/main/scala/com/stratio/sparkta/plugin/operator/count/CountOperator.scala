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

package com.stratio.sparkta.plugin.operator.count

import java.io.{Serializable => JSerializable}
import scala.util.Try

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.{TypeOp, _}

class CountOperator(name: String, properties: Map[String, JSerializable])
  extends Operator(name, properties) {

  val distinctFields = parseDistinctFields

  override val defaultTypeOperation = TypeOp.Long

  override val writeOperation = WriteOp.Inc

  override val castingFilterType = TypeOp.Number

  override def processMap(inputFields: Map[String, JSerializable]): Option[Any] = {
    applyFilters(inputFields).flatMap(filteredFields => distinctFields match {
      case None => Some(CountOperator.One.toLong)
      case Some(fields) => Some(fields.map(field => filteredFields.getOrElse(field, CountOperator.NullValue))
        .mkString(OperatorConstants.UnderscoreSeparator).toString)
    })
  }

  override def processReduce(values: Iterable[Option[Any]]): Option[Long] = {
    Try {
      val longList = distinctFields match {
        case None => values.flatten.map(value => value.asInstanceOf[Number].longValue())
        case Some(fields) => values.flatten.toList.distinct.map(value => CountOperator.One.toLong)
      }
      Some(transformValueByTypeOp(returnType, longList.sum))
    }.getOrElse(Some(OperatorConstants.Zero.toLong))
  }

  //FIXME: We should refactor this code
  private def parseDistinctFields: Option[Seq[String]] = {
    val distinct = properties.getString("distinctFields", None)
    if (distinct.isDefined && !distinct.get.isEmpty)
      Some(distinct.get.split(OperatorConstants.UnderscoreSeparator))
    else None
  }
}

object CountOperator {

  final val NullValue = "None"
  final val One = 1
}
