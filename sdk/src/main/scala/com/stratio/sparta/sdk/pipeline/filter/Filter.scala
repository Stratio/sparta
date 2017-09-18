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

import java.sql.Timestamp
import java.util.Date

import akka.event.slf4j.SLF4JLogging
import com.github.nscala_time.time.Imports._
import com.stratio.sparta.sdk.utils.AggregationTime._
import com.stratio.sparta.sdk.pipeline.schema.TypeOp._
import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import com.stratio.sparta.sdk.properties.JsoneyStringSerializer
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType
import org.joda.time.DateTime
import org.json4s.JsonAST.{JBool, JDouble, JInt, JString}
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}

import scala.util.{Failure, Success, Try}

//TODO remove when refactor cubes

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
        val inputValue = transformAnyByTypeOp(filterType, inputField._2)
        val filterValue = filter.value.map(value => transformAnyByTypeOp(filterType, value))
        val fieldValue = filter.fieldValue.flatMap(fieldValue =>
          inputFields.get(fieldValue).map(value => transformAnyByTypeOp(filterType, value)))

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

  def transformAnyByTypeOp(typeOp: TypeOp, origValue: Any): Any = {
    typeOp match {
      case TypeOp.String => checkAnyStringType(origValue)
      case TypeOp.Double | TypeOp.Number => checkAnyDoubleType(origValue)
      case TypeOp.Int => checkAnyIntType(origValue)
      case TypeOp.Long => checkAnyLongType(origValue)
      case TypeOp.Boolean => checkAnyBooleanType(origValue)
      case TypeOp.Timestamp => checkAnyTimestampType(origValue)
      case TypeOp.Date => checkAnyDateType(origValue)
      case TypeOp.DateTime => checkAnyDateTimeType(origValue)
      case TypeOp.ArrayDouble => checkAnyArrayDoubleType(origValue)
      case TypeOp.ArrayString => checkAnyArrayStringType(origValue)
      case TypeOp.ArrayMapStringString => checkAnyArrayMapStringStringType(origValue)
      case TypeOp.MapStringLong => checkAnyMapStringLongType(origValue)
      case TypeOp.MapStringDouble => checkAnyMapStringDoubleType(origValue)
      case TypeOp.MapStringInt => checkAnyMapStringIntType(origValue)
      case TypeOp.MapStringString => checkAnyMapStringStringType(origValue)
      case TypeOp.RowWithSchema => checkAnyStructType(origValue)
      case TypeOp.Any => origValue
      case _ => origValue
    }
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

  //TODO remove when refactor cubes
  implicit object OrderingAny extends Ordering[Any] {

    import math.Ordering

    override def compare(x: Any, y: Any): Int = (x, y) match {
      case (x: Int, y: Int) => Ordering[Int].compare(x, y)
      case (x: Int, y: Double) => Ordering[Int].compare(x, y.toInt)
      case (x: Int, y: Long) => Ordering[Int].compare(x, y.toInt)
      case (x: Int, y: JInt) => Ordering[Int].compare(x, y.num.intValue())
      case (x: Int, y: JDouble) => Ordering[Int].compare(x, y.num.intValue())
      case (x: Int, y: JString) => Try(Ordering[Int].compare(x, y.s.toInt))
        .getOrElse(Ordering[String].compare(x.toString, y.s))
      case (x: Double, y: Double) => Ordering[Double].compare(x, y)
      case (x: Double, y: Long) => Ordering[Double].compare(x, y.toDouble)
      case (x: Double, y: JDouble) => Ordering[Double].compare(x, y.num)
      case (x: Double, y: JInt) => Ordering[Double].compare(x, y.num.doubleValue())
      case (x: Double, y: JString) => Try(Ordering[Double].compare(x, y.s.toDouble))
        .getOrElse(Ordering[String].compare(x.toString, y.s))
      case (x: Array[Byte], y: Array[Byte]) => Ordering[String].compare(new Predef.String(x), new Predef.String(y))
      case (x: Array[Byte], y: JString) => Ordering[String].compare(new Predef.String(x), y.s)
      case (x: Array[Byte], y: JInt) => Ordering[Int].compare(new Predef.String(x).toInt, y.num.intValue())
      case (x: Array[Byte], y: JDouble) => Ordering[Double].compare(new Predef.String(x).toDouble, y.num.toDouble)
      case (x: Array[Byte], y: JBool) => Ordering[Boolean].compare(new Predef.String(x).toBoolean, y.value)
      case (x: Array[Byte], y: Int) => Ordering[Int].compare(new Predef.String(x).toInt, y)
      case (x: Array[Byte], y: Long) => Ordering[Long].compare(new Predef.String(x).toLong, y)
      case (x: Array[Byte], y: Double) => Ordering[Double].compare(new Predef.String(x).toDouble, y)
      case (x: Array[Byte], y: Boolean) => Ordering[Boolean].compare(new Predef.String(x).toBoolean, y)
      case (x: String, y: Array[Byte]) => Ordering[String].compare(x, new Predef.String(y))
      case (x: String, y: String) => Ordering[String].compare(x, y)
      case (x: String, y: JString) => Ordering[String].compare(x, y.s)
      case (x: String, y: JInt) => Try(Ordering[Int].compare(x.toInt, y.num.intValue()))
        .getOrElse(Ordering[String].compare(x, y.num.toString()))
      case (x: String, y: JDouble) => Try(Ordering[Double].compare(x.toDouble, y.num.toDouble))
        .getOrElse(Ordering[String].compare(x, y.num.toString))
      case (x: String, y: Int) => Try(Ordering[Int].compare(x.toInt, y))
        .getOrElse(Ordering[String].compare(x, y.toString))
      case (x: String, y: Long) => Try(Ordering[Long].compare(x.toLong, y))
        .getOrElse(Ordering[String].compare(x, y.toString))
      case (x: String, y: Double) => Try(Ordering[Double].compare(x.toDouble, y))
        .getOrElse(Ordering[String].compare(x, y.toString))
      case (x: String, y: Boolean) => Try(Ordering[Boolean].compare(x.toBoolean, y))
        .getOrElse(Ordering[String].compare(x, y.toString))
      case (x: Long, y: Long) => Ordering[Long].compare(x, y)
      case (x: Long, y: JInt) => Ordering[Long].compare(x, y.num.longValue())
      case (x: Long, y: JDouble) => Ordering[Long].compare(x, y.num.longValue())
      case (x: Long, y: JString) => Try(Ordering[Long].compare(x, y.s.toLong))
        .getOrElse(Ordering[String].compare(x.toString, y.s))
      case (x: Boolean, y: Boolean) => Ordering[Boolean].compare(x, y)
      case (x: Boolean, y: JBool) => Ordering[Boolean].compare(x, y.value)
      case (x: Boolean, y: JString) => Try(Ordering[Boolean].compare(x, y.s.toBoolean))
        .getOrElse(Ordering[String].compare(x.toString, y.s))
      case (x: Date, y: Date) => Ordering[Date].compare(x, y)
      case (x: Date, y: Long) => Ordering[Long].compare(x.getTime, y)
      case (x: Date, y: JInt) => Ordering[Long].compare(x.getTime, y.num.longValue())
      case (x: Date, y: String) => Try {
        val dateParsed = com.github.nscala_time.time.StaticDateTime.parse(y)
        Ordering[Date].compare(x, dateParsed.date)
      }.getOrElse(Ordering[String].compare(x.toString, y))
      case (x: Date, y: Array[Byte]) => Try {
        val dateParsed = com.github.nscala_time.time.StaticDateTime.parse(new Predef.String(y))
        Ordering[Date].compare(x, dateParsed.date)
      }.getOrElse(Ordering[String].compare(x.toString, new Predef.String(y)))
      case (x: Date, y: JString) => Try {
        val dateParsed = com.github.nscala_time.time.StaticDateTime.parse(y.s)
        Ordering[Date].compare(x, dateParsed.date)
      }.getOrElse(Ordering[String].compare(x.toString, y.s))
      case (x: Timestamp, y: Timestamp) => Ordering[Long].compare(x.getTime, y.getTime)
      case (x: Timestamp, y: Long) => Ordering[Long].compare(x.getTime, y)
      case (x: Timestamp, y: JInt) => Ordering[Long].compare(x.getTime, y.num.longValue())
      case (x: Timestamp, y: String) => Try {
        val dateParsed = com.github.nscala_time.time.StaticDateTime.parse(y)
        Ordering[Long].compare(x.getTime, millisToTimeStamp(dateParsed.toDateTime.getMillis).getTime)
      }.getOrElse(Ordering[String].compare(x.toString, y))
      case (x: Timestamp, y: Array[Byte]) => Try {
        val dateParsed = com.github.nscala_time.time.StaticDateTime.parse(new Predef.String(y))
        Ordering[Long].compare(x.getTime, millisToTimeStamp(dateParsed.toDateTime.getMillis).getTime)
      }.getOrElse(Ordering[String].compare(x.toString, new Predef.String(y)))
      case (x: Timestamp, y: JString) => Try {
        val dateParsed = com.github.nscala_time.time.StaticDateTime.parse(y.s)
        Ordering[Long].compare(x.getTime, millisToTimeStamp(dateParsed.toDateTime.getMillis).getTime)
      }.getOrElse(Ordering[String].compare(x.toString, y.s))
      case (x: DateTime, y: DateTime) => Ordering[DateTime].compare(x, y)
      case (x: DateTime, y: Long) => Ordering[DateTime].compare(x, new DateTime(y))
      case (x: DateTime, y: JInt) => Ordering[DateTime].compare(x, new DateTime(y.num.longValue()))
      case (x: DateTime, y: String) => Try {
        val dateParsed = com.github.nscala_time.time.StaticDateTime.parse(y)
        Ordering[DateTime].compare(x, dateParsed.toDateTime)
      }.getOrElse(Ordering[String].compare(x.toString, y))
      case (x: DateTime, y: Array[Byte]) => Try {
        val dateParsed = com.github.nscala_time.time.StaticDateTime.parse(new Predef.String(y))
        Ordering[DateTime].compare(x, dateParsed.toDateTime)
      }.getOrElse(Ordering[String].compare(x.toString, new Predef.String(y)))
      case (x: DateTime, y: JString) => Try {
        val dateParsed = com.github.nscala_time.time.StaticDateTime.parse(y.s)
        Ordering[DateTime].compare(x, dateParsed.toDateTime)
      }.getOrElse(Ordering[String].compare(x.toString, y.s))
      case _ => throw new Exception(s"Incompatible types when comparing: ${x.toString} and ${y.toString}")
    }
  }



}
