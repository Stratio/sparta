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

package com.stratio.sparkta.sdk

import java.io.{Serializable => JSerializable}

import com.stratio.sparkta.sdk.TypeOp.TypeOp
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable.StringOps
import scala.language.reflectiveCalls
import scala.runtime.RichDouble
import scala.util.Try

abstract class Operator(name: String, properties: Map[String, JSerializable]) extends Parameterizable(properties)
with Ordered[Operator] with TypeConversions {

  implicit val formats = DefaultFormats

  override def operationProps: Map[String, JSerializable] = properties

  override def defaultTypeOperation: TypeOp = TypeOp.Binary

  def key: String = name

  def distinct: Boolean = Try(properties.getString("distinct").toBoolean).getOrElse(false)

  def filters: Array[FilterModel] = properties.getString("filters", None) match {
    case Some(filters) => parse(filters).extract[Array[FilterModel]]
    case None => Array()
  }

  def writeOperation: WriteOp

  def processMap(inputFields: Map[String, JSerializable]): Option[Any]

  def processReduce(values: Iterable[Option[Any]]): Option[Any]

  def castingFilterType: TypeOp = TypeOp.String

  def returnType: TypeOp = getTypeOperation.getOrElse(defaultTypeOperation)

  def compare(operator: Operator): Int = key compareTo operator.key

  //scalastyle:off
  def getNumberFromSerializable(value: JSerializable): Option[Number] =
    value match {
      case value if value.isInstanceOf[String] =>
        Try(Some(value.asInstanceOf[String].toDouble.asInstanceOf[Number])).getOrElse(None)
      case value if value.isInstanceOf[Int] ||
        value.isInstanceOf[Double] ||
        value.isInstanceOf[Float] ||
        value.isInstanceOf[Long] ||
        value.isInstanceOf[Short] ||
        value.isInstanceOf[Byte] => Some(value.asInstanceOf[Number])
      case _ => None
    }

  //scalastyle:on

  protected def getDistinctValues[T](values: Iterable[T]): List[T] =
    if (distinct) {
      values.toList.distinct
    }
    else values.toList

  protected def applyFilters(inputFields: Map[String, JSerializable]): Option[Map[String, JSerializable]] =
    if (inputFields.map(inputfield => applyFiltering(inputfield)).forall(result => result)) Some(inputFields) else None

  protected def applyFiltering(inputField: (String, JSerializable)): Boolean = {
    filters.map(filter => if (inputField._1 == filter.field) {
      castingFilterType match {
        case TypeOp.Number =>
          applyFilteringType(filter.`type`,
            new RichDouble(inputField._2.toString.toDouble),
            filter.value.toString.toDouble)
        case _ =>
          applyFilteringType(filter.`type`, new StringOps(inputField._2.toString), filter.value.toString)
      }
    } else true).forall(result => result)
  }

  protected def applyFilteringType[T <: Ordered[U], U](filterType: String, value: T, filterValue: U): Boolean =
    filterType match {
      case "=" => value == filterValue
      case "!=" => value != filterValue
      case "<" => value < filterValue
      case "<=" => value <= filterValue
      case ">" => value > filterValue
      case ">=" => value >= filterValue
    }
}

object Operator {

  final val ClassSuffix = "Operator"
}