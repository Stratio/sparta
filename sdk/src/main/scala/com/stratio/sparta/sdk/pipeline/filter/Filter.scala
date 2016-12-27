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

package com.stratio.sparta.sdk.pipeline.filter

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparta.sdk.pipeline.schema.TypeOp._
import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

trait Filter extends SLF4JLogging {

  @transient
  implicit val json4sJacksonFormats: Formats = DefaultFormats + new JsoneyStringSerializer()

  def filterInput: Option[String]

  val schema: StructType

  def defaultCastingFilterType: TypeOp

  val filters = filterInput match {
    case Some(jsonFilters) => parse(jsonFilters).extract[Seq[FilterModel]]
    case None => Seq()
  }

  def applyFilters(row: Row): Option[Map[String, Any]] = {
    val mapRow = schema.fieldNames.zip(row.toSeq).toMap

    if (mapRow.map(inputField => doFiltering(inputField, mapRow)).forall(result => result))
      Option(mapRow)
    else None
  }

  private def doFiltering(inputField: (String, Any),
                          inputFields: Map[String, Any]): Boolean = {
    filters.map(filter =>
      if (inputField._1 == filter.field && (filter.fieldValue.isDefined || filter.value.isDefined)) {

        val filterType = filterCastingType(filter.fieldType)
        val inputValue = TypeOp.transformAnyByTypeOp(filterType, inputField._2)
        val filterValue = filter.value.map(value => TypeOp.transformAnyByTypeOp(filterType, value))
        val fieldValue = filter.fieldValue.flatMap(fieldValue =>
          inputFields.get(fieldValue).map(value => TypeOp.transformAnyByTypeOp(filterType, value)))

        applyFilterCauses(filter, inputValue, filterValue, fieldValue)
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
      case "=" => value equiv filterValue
      case "!=" => !(value equiv filterValue)
      case "<" => value < filterValue
      case "<=" => value <= filterValue
      case ">" => value > filterValue
      case ">=" => value >= filterValue
    }
  }
}
