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
package com.stratio.sparta.sdk

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.TypeOp._
import com.stratio.sparta.sdk.ValidatingPropertyMap._
import org.apache.spark.Logging
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}

import scala.language.reflectiveCalls
import scala.util._

abstract class Operator(name: String,
                        schema: StructType,
                        properties: Map[String, JSerializable]) extends Parameterizable(properties)
with Ordered[Operator] with TypeConversions with SLF4JLogging {

  @transient
  implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()

  override def operationProps: Map[String, JSerializable] = properties

  override def defaultTypeOperation: TypeOp = TypeOp.String

  def key: String = name

  def distinct: Boolean = Try(properties.getString("distinct").toBoolean).getOrElse(false)

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

  def applyFilters(row: Row): Option[Map[String, Any]] = {
    val mapRow = schema.fieldNames.zip(row.toSeq).toMap

    if (mapRow.map(inputField => doFiltering(inputField, mapRow)).forall(result => result))
      Option(mapRow)
    else None
  }

  def processMap(inputFieldsValues: Row): Option[Any]

  private def doFiltering(inputField: (String, Any),
                          inputFields: Map[String, Any]): Boolean = {
    filters.map(filter =>
      if (inputField._1 == filter.field && (filter.fieldValue.isDefined || filter.value.isDefined)) {

        val filterType = filterCastingType(filter.fieldType)
        val inputValue = TypeOp.transformAnyByTypeOp(filterType, inputField._2)
        val filterValue = filter.value.map(value => TypeOp.transformAnyByTypeOp(filterType, value))
        val dimensionValue = filter.fieldValue.flatMap(fieldValue =>
          inputFields.get(fieldValue).map(value => TypeOp.transformAnyByTypeOp(filterType, value)))

        applyFilterCauses(filter, inputValue, filterValue, dimensionValue)
      }
      else true
    ).forall(result => result)
  }

  private def filterCastingType(fieldType: Option[String]): TypeOp =
    fieldType match {
      case Some(typeName) => getTypeOperationByName(typeName, defaultCastingFilterType)
      case None => defaultCastingFilterType
    }

  //scalastyle:off
  private def applyFilterCauses(filter: FilterModel,
                                                    value: Any,
                                                    filterValue: Option[Any],
                                                    dimensionValue: Option[Any]): Boolean = {
    val valueOrdered = value
    val filterValueOrdered = filterValue.map(filterVal => filterVal)
    val dimensionValueOrdered = dimensionValue.map(dimensionVal => dimensionVal)

    Seq(
      if (filter.value.isDefined && filterValue.isDefined && filterValueOrdered.isDefined)
        Try(doFilteringType(filter.`type`, valueOrdered, filterValueOrdered.get)) match {
          case Success(filterResult) =>
            filterResult
          case Failure(e) =>
            log.error(e.getLocalizedMessage)
            true
        }
      else true,
      if (filter.fieldValue.isDefined && dimensionValue.isDefined && dimensionValueOrdered.isDefined)
        Try(doFilteringType(filter.`type`, valueOrdered, dimensionValueOrdered.get)) match {
          case Success(filterResult) =>
            filterResult
          case Failure(e) =>
            log.error(e.getLocalizedMessage)
            true
        }
      else true
    ).forall(result => result)
  }

  private def doFilteringType(filterType: String, value: Any, filterValue: Any): Boolean = {
    import OrderingAny._
    filterType match {
      case "=" => value == filterValue
      case "!=" => value != filterValue
      case "<" => value < filterValue
      case "<=" => value <= filterValue
      case ">" => value > filterValue
      case ">=" => value >= filterValue
    }
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

object Operator extends Logging {

  final val ClassSuffix = "Operator"
  final val OldValuesKey = "old"
  final val NewValuesKey = "new"
  final val EmptyString = ""
  final val SpaceSeparator = " "
  final val Zero = 0
  final val UnderscoreSeparator = "_"

  /**
   * This method tries to cast a value to Number, if it's possible.
   *
   * Serializable -> String -> Number
   * Serializable -> Number
   *
   */
  def getNumberFromAny(value: Any): Number =
    Try(TypeOp.transformValueByTypeOp(TypeOp.Double, value).asInstanceOf[Number]) match {
      case Success(number) => number
      case Failure(ex) => log.info(s"Impossible to parse as double number inside operator: ${value.toString}")
        throw new Exception(ex.getMessage)
      }
}