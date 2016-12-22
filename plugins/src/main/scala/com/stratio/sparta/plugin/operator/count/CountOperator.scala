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
package com.stratio.sparta.plugin.operator.count

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.TypeOp._
import com.stratio.sparta.sdk.properties.ValidatingPropertyMap._
import com.stratio.sparta.sdk.{TypeOp, _}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

import scala.util.Try

class CountOperator(name: String, schema: StructType, properties: Map[String, JSerializable])
  extends Operator(name, schema, properties) with Associative {

  val distinctFields = parseDistinctFields

  override val defaultTypeOperation = TypeOp.Long

  override val defaultCastingFilterType = TypeOp.Number

  override def processMap(inputFieldsValues: Row): Option[Any] = {
    applyFilters(inputFieldsValues).flatMap(filteredFields => distinctFields match {
      case None => Option(CountOperator.One.toLong)
      case Some(fields) => Option(fields.map(field => filteredFields.getOrElse(field, CountOperator.NullValue))
        .mkString(Operator.UnderscoreSeparator).toString)
    })
  }

  override def processReduce(values: Iterable[Option[Any]]): Option[Long] = {
    Try {
      val longList = distinctFields match {
        case None => values.flatten.map(value => value.asInstanceOf[Number].longValue())
        case Some(fields) => values.flatten.toList.distinct.map(value => CountOperator.One.toLong)
      }
      Option(longList.sum)
    }.getOrElse(Option(Operator.Zero.toLong))
  }

  def associativity(values: Iterable[(String, Option[Any])]): Option[Long] = {
    val newValues = extractValues(values, None)
      .map(value => TypeOp.transformValueByTypeOp(TypeOp.Long, value).asInstanceOf[Long]).sum

    Try(Option(transformValueByTypeOp(returnType, newValues)))
      .getOrElse(Option(Operator.Zero.toLong))
  }

  //FIXME: We should refactor this code
  private def parseDistinctFields: Option[Seq[String]] = {
    val distinct = properties.getString("distinctFields", None)
    if (distinct.isDefined && !distinct.get.isEmpty)
      Option(distinct.get.split(Operator.UnderscoreSeparator))
    else None
  }
}

object CountOperator {

  final val NullValue = "None"
  final val One = 1
}
