/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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

import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.WriteOp.WriteOp
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}

import scala.collection.immutable.StringOps
import scala.language.reflectiveCalls
import scala.runtime.RichDouble
import scala.util._

abstract class Operator(name: String, properties: Map[String, JSerializable]) extends Parameterizable(properties)
with Ordered[Operator] with TypeConversions {

  @transient
  implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()

  override def operationProps: Map[String, JSerializable] = properties

  override def defaultTypeOperation: TypeOp = TypeOp.Binary

  def key: String = name

  def distinct: Boolean = Try(properties.getString("distinct").toBoolean).getOrElse(false)

  def writeOperation: WriteOp

  val inputField = properties.getString("inputField", None)

  val filters = properties.getString("filters", None) match {
    case Some(jsonFilters) => parse(jsonFilters).extract[Seq[FilterModel]]
    case None => Seq()
  }

  def processReduce(values: Iterable[Option[Any]]): Option[Any]

  def defaultCastingFilterType: TypeOp = TypeOp.String

  def returnType: TypeOp = getTypeOperation.getOrElse(defaultTypeOperation)

  def compare(operator: Operator): Int = key compareTo operator.key

  def getDistinctValues[T](values: Iterable[T]): List[T] =
    if (distinct)
      values.toList.distinct
    else values.toList

  def applyFilters(inputFields: Map[String, JSerializable]): Option[Map[String, JSerializable]] =
    if (inputFields.map(inputField => doFiltering(inputField, inputFields)).forall(result => result))
      Some(inputFields)
    else None

  def processMap(inputFieldsValues: InputFieldsValues): Option[Any]

  private def doFiltering(inputField: (String, JSerializable),
                          inputFields: Map[String, JSerializable]): Boolean = {
    filters.map(filter =>
      if (inputField._1 == filter.field && (filter.fieldValue.isDefined || filter.value.isDefined)) {
        filterCastingType(filter.fieldType) match {
          case TypeOp.Number | TypeOp.Long | TypeOp.Double | TypeOp.Int => {
            val doubleValues = getDoubleValues(inputField._2, filter, inputFields)
            applyfilterCauses(filter, doubleValues._1, doubleValues._2, doubleValues._3)
          }
          case _ => {
            val stringValues = getStringValues(inputField._2, filter, inputFields)
            applyfilterCauses(filter, stringValues._1, stringValues._2, stringValues._3)
          }
        }
      }
      else true
    ).forall(result => result)
  }

  private def filterCastingType(fieldType: Option[String]): TypeOp =
    fieldType match {
      case Some(typeName) => getTypeOperationByName(typeName, defaultCastingFilterType)
      case None => defaultCastingFilterType
    }

  private def applyfilterCauses[T <: Ordered[U], U](filter: FilterModel,
                                                    value: T,
                                                    filterValue: Option[U],
                                                    dimensionValue: Option[U]): Boolean = {
    Seq(
      if (filter.value.isDefined && filterValue.isDefined)
        doFilteringType(filter.`type`, value, filterValue.get)
      else true,
      if (filter.fieldValue.isDefined && dimensionValue.isDefined)
        doFilteringType(filter.`type`, value, dimensionValue.get)
      else true
    ).forall(result => result)
  }

  private def getDoubleValues(inputValue: JSerializable,
                              filter: FilterModel,
                              inputFields: Map[String, JSerializable]): (RichDouble, Option[Double], Option[Double]) = {

    (new RichDouble(inputValue.toString.toDouble),
      if (filter.value.isDefined) Some(filter.value.get.toString.toDouble) else None,
      if (filter.fieldValue.isDefined && inputFields.contains(filter.fieldValue.get))
        Some(inputFields.get(filter.fieldValue.get).get.toString.toDouble)
      else None)
  }

  private def getStringValues(inputValue: JSerializable,
                              filter: FilterModel,
                              inputFields: Map[String, JSerializable])
  : (StringOps, Option[String], Option[String]) = {

    (new StringOps(inputValue.toString), if (filter.value.isDefined) Some(filter.value.get.toString) else None,
      if (filter.fieldValue.isDefined && inputFields.contains(filter.fieldValue.get))
        Some(inputFields.get(filter.fieldValue.get).get.toString)
      else None)
  }

  private def doFilteringType[T <: Ordered[U], U](filterType: String, value: T, filterValue: U): Boolean =
    filterType match {
      case "=" => value.compare(filterValue) == 0
      case "!=" => value.compare(filterValue) != 0
      case "<" => value < filterValue
      case "<=" => value <= filterValue
      case ">" => value > filterValue
      case ">=" => value >= filterValue
    }

  def isAssociative: Boolean = this.isInstanceOf[Associative]

  def extractValues(values: Iterable[(String, Option[Any])], filterKey: Option[String]): Iterable[Any] =
    values.flatMap { case (key, value) =>
      filterKey match {
        case Some(filter) => if (key == filter) value else None
        case None => value
      }
    }
}

object Operator {

  final val ClassSuffix = "Operator"
  final val OldValuesKey = "old"
  final val NewValuesKey = "new"

  /**
   * This method tries to cast a value to Number, if it's possible.
   *
   * Serializable -> String -> Number
   * Serializable -> Number
   *
   */
  def getNumberFromSerializable(value: JSerializable): Option[Number] =
    Try(value.asInstanceOf[String].toDouble.asInstanceOf[Number]) match {
      case Success(number) => Some(number)
      case Failure(ex) => Try(value.asInstanceOf[Number]) match {
        case Success(number) => Some(number)
        case Failure(ex) => None
      }
    }
}