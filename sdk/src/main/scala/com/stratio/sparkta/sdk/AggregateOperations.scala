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

import java.sql.{Date, Timestamp}

import org.apache.spark.sql._

object AggregateOperations {

  /*
  * By default transform an UpdateMetricOperation in a Row with description.
  * If fixedDimensions is defined add fields and values to the original values and fields.
  * Id field we need calculate the value with all other values
  */
  def toKeyRowWithTime(dimensionValuesT: DimensionValuesTime,
                       measures: MeasuresValues,
                       fixedMeasures: MeasuresValues,
                       fixedDimensions: Option[Seq[(String, Any)]],
                       idCalculated: Boolean,
                       dateType: TypeOp.Value): (Option[String], Row) = {
    val timeDimension = dimensionValuesT.timeDimension
    val dimensionValuesFiltered =
      filterDimensionValuesByName(dimensionValuesT.dimensionValues,
        if (timeDimension.isEmpty) None else Some(timeDimension))
    val namesDim = dimensionValuesT.cube
    val (valuesDim, valuesAgg) = toSeq(dimensionValuesFiltered, measures.values ++ fixedMeasures.values)
    val (namesFixed, valuesFixed) = if (fixedDimensions.isDefined) {
      val fixedDimensionsSorted = fixedDimensions.get
        .filter(fb => fb._1 != timeDimension)
        .sortWith((dimension1, dimension2) => dimension1._1 < dimension2._1)
      (namesDim,
        valuesDim ++ fixedDimensionsSorted.map(_._2) ++ Seq(getTimeFromDateType(dimensionValuesT.time, dateType)))
    } else
      (namesDim,
        valuesDim ++ Seq(getTimeFromDateType(dimensionValuesT.time, dateType)))
    val (keysId, rowId) = getNamesValues(namesFixed, valuesFixed, idCalculated)

    (Some(keysId), Row.fromSeq(rowId ++ valuesAgg))
  }

  /*
  * By default transform an UpdateMetricOperation in a Row with description.
  * If fixedDimensions is defined add fields and values to the original values and fields.
  * Id field we need calculate the value with all other values
  */
  def toKeyRowWithoutTime(dimensionValuesT: DimensionValuesWithoutTime,
               measures: MeasuresValues,
               fixedMeasures: MeasuresValues,
               fixedDimensions: Option[Seq[(String, Any)]],
               idCalculated: Boolean,
               dateType: TypeOp.Value): (Option[String], Row) = {
    val dimensionValuesFiltered = dimensionValuesT.dimensionValues
    val namesDim = dimensionValuesT.cube
    val (valuesDim, valuesAgg) = toSeq(dimensionValuesFiltered, measures.values ++ fixedMeasures.values)
    val (namesFixed, valuesFixed) = if (fixedDimensions.isDefined) {
      val fixedDimensionsSorted = fixedDimensions.get
        .sortWith((dimension1, dimension2) => dimension1._1 < dimension2._1)
      (namesDim,
        valuesDim ++ fixedDimensionsSorted.map(_._2))
    } else
      (namesDim, valuesDim)
    val (keysId, rowId) = getNamesValues(namesFixed, valuesFixed, idCalculated)

    (Some(keysId), Row.fromSeq(rowId ++ valuesAgg))
  }


  def toSeq(dimensionValues: Seq[DimensionValue], aggregations: Map[String, Option[Any]]): (Seq[Any], Seq[Any]) =
    (dimensionValues.sorted.map(dimVal => dimVal.value),
      aggregations.toSeq.sortWith(_._1 < _._1).map(aggregation => aggregation._2.getOrElse(0)))

  def getNamesValues(names: String, values: Seq[Any], idCalculated: Boolean): (String, Seq[Any]) =
    if (idCalculated)
      (names, Seq(values.mkString(Output.Separator)) ++ values)
    else (names, values)

  def dimensionValuesNamesSorted(dimensionValues: Seq[DimensionValue]): Seq[String] =
    dimensionValues.sorted.map(_.dimension.name)

  def filterDimensionValuesByName(dimensionValues: Seq[DimensionValue], dimensionName: Option[String])
  : Seq[DimensionValue] =
    dimensionName match {
      case None => dimensionValues
      case Some(name) => dimensionValues.filter(cube => cube.dimension.name != name)
    }

  def getTimeFromDateType[T](time: Long, dateType: TypeOp.Value): Any = {
    dateType match {
      case TypeOp.Date | TypeOp.DateTime => new Date(time)
      case TypeOp.Long => time
      case TypeOp.Timestamp => new Timestamp(time)
      case _ => time.toString
    }
  }
}