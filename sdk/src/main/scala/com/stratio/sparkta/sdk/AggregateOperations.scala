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

import java.sql.{Timestamp, Date}

import org.apache.spark.sql._

object AggregateOperations {

  def toString(dimensionValuesT: DimensionValuesTime,
               aggregations: Map[String, Option[Any]],
               timeDimension: String,
               fixedDimensions: Seq[String]): String =
    keyString(dimensionValuesT, timeDimension, fixedDimensions) +
      " DIMENSIONS: " + dimensionValuesT.dimensionValues.mkString("|") +
      " AGGREGATIONS: " + aggregations +
      " TIME: " + dimensionValuesT.time

  def keyString(dimensionValuesT: DimensionValuesTime, timeDimension: String, fixedDimensions: Seq[String]): String = {
    val dimensionsNames = dimensionValuesNamesSorted(dimensionValuesT.dimensionValues)
      .filter(dimName => dimName.nonEmpty && dimName != timeDimension && !fixedDimensions.contains(dimName)) ++
      fixedDimensions ++ Seq(timeDimension)
    dimensionsNames.mkString(Output.Separator)
  }

  /*
  * By default transform an UpdateMetricOperation in a Row with description.
  * If fixedDimensions is defined add fields and values to the original values and fields.
  * Id field we need calculate the value with all other values
  */
  def toKeyRow(dimensionValuesT: DimensionValuesTime,
               aggregations: Map[String, Option[Any]],
               fixedAggregation: Map[String, Option[Any]],
               fixedDimensions: Option[Seq[(String, Any)]],
               idCalculated: Boolean,
               dateType: TypeOp.Value): (Option[String], Row) = {
    val timeDimension = dimensionValuesT.timeDimension
    val dimensionValuesFiltered =
      filterDimensionValuesByName(dimensionValuesT.dimensionValues,
      if (timeDimension.isEmpty) None else Some(timeDimension))

    val namesDim = dimensionValuesNames(dimensionValuesFiltered.sorted)

    val (valuesDim, valuesAgg) = toSeq(dimensionValuesFiltered, aggregations ++ fixedAggregation)

    val (namesFixed, valuesFixed) = if (fixedDimensions.isDefined) {
      val fixedDimensionsSorted = fixedDimensions.get
        .filter(fb => fb._1 != timeDimension)
        .sortWith((dimension1, dimension2) => dimension1._1 < dimension2._1)
      (
        namesDim ++ fixedDimensionsSorted.map(_._1) ++ Seq(timeDimension),
        valuesDim ++ fixedDimensionsSorted.map(_._2) ++ Seq(getTimeFromDateType(dimensionValuesT.time, dateType))
      )
    } else
      (
        namesDim ++ Seq(timeDimension),
        valuesDim ++ Seq(getTimeFromDateType(dimensionValuesT.time, dateType))
      )

    val (keysId, rowId) = getNamesValues(namesFixed, valuesFixed, idCalculated)

    if (keysId.nonEmpty)
      (Some(keysId.mkString(Output.Separator)), Row.fromSeq(rowId ++ valuesAgg))
    else
      (None, Row.fromSeq(rowId ++ valuesAgg))
  }

  def toSeq(dimensionValues: Seq[DimensionValue], aggregations: Map[String, Option[Any]]): (Seq[Any], Seq[Any]) =
    (dimensionValues.sorted.map(dimVal => dimVal.value),
      aggregations.toSeq.sortWith(_._1 < _._1).map(aggregation => aggregation._2.getOrElse(0)))

  def getNamesValues(names: Seq[String], values: Seq[Any], idCalculated: Boolean): (Seq[String], Seq[Any]) =
    if (idCalculated && !names.contains(Output.Id))
      (Seq(Output.Id) ++ names, Seq(values.mkString(Output.Separator)) ++ values)
    else (names, values)

  def dimensionValuesNames(dimensionValues: Seq[DimensionValue]): Seq[String] = dimensionValues.map(_.dimension.name)

  def dimensionValuesNamesSorted(dimensionValues: Seq[DimensionValue]): Seq[String] =
    dimensionValuesNames(dimensionValues.sorted)

  def filterDimensionValuesByName(dimensionValues: Seq[DimensionValue], dimensionName: Option[String])
  : Seq[DimensionValue] =
    dimensionName match {
      case None => dimensionValues
      case Some(name) => dimensionValues.filter(cube => cube.dimension.name != name)
    }

  def getTimeFromDateType[T](time : Long, dateType : TypeOp.Value) : Any = {
    dateType match {
        case TypeOp.Date | TypeOp.DateTime => new Date(time)
        case TypeOp.Long => time
        case TypeOp.Timestamp => new Timestamp(time)
        case _ => time.toString
      }
  }
}