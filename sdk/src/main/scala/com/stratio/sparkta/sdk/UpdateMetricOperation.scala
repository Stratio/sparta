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

import org.apache.spark.sql._

case class UpdateMetricOperation(rollupKey: Seq[DimensionValue], var aggregations: Map[String, Option[Any]]) {

  if (rollupKey == null) {
    throw new NullPointerException("rollupKey")
  }

  if (aggregations == null) {
    throw new NullPointerException("aggregations")
  }

  override def toString: String =
    this.keyString + " DIMENSIONS: " + rollupKey.mkString("|") + " AGGREGATIONS: " + aggregations

  def keyString: String =
    UpdateMetricOperation.sortedNamesDimVals(rollupKey).filter(dimName => dimName.nonEmpty).mkString(Output.SEPARATOR)

  /*
  * By default transform an UpdateMetricOperation in a Row with description.
  * If fixedBuckets is defined add fields and values to the original values and fields.
  * Id field we need calculate the value with all other values
  */
  def toKeyRow(fixedBuckets: Option[Seq[(String, Option[Any])]], idCalculated: Boolean): (Option[String], Row) = {
    val rollupKeyFiltered = if (fixedBuckets.isDefined) {
      val fixedBucketsNames = fixedBuckets.get.map(_._1)
      rollupKey.filter(dimVal => !fixedBucketsNames.contains(dimVal.getNameDimension))
    } else rollupKey
    val namesDim = UpdateMetricOperation.namesDimVals(rollupKeyFiltered.sorted)
    val (valuesDim, valuesAgg) = UpdateMetricOperation.toSeq(rollupKeyFiltered, aggregations)
    val (namesFixed, valuesFixed) = if (fixedBuckets.isDefined) {
      val fixedBucketsSorted = fixedBuckets.get.sortWith((bucket1, bucket2) => bucket1._1 < bucket2._1)
      (namesDim ++ fixedBucketsSorted.map(_._1), valuesDim ++ valuesAgg ++ fixedBucketsSorted.map(_._2.getOrElse("")))
    } else (namesDim, valuesDim ++ valuesAgg)
    val (keys, row) = UpdateMetricOperation.getNamesValues(namesFixed, valuesFixed, idCalculated)

    if (keys.length > 0) (Some(keys.mkString(Output.SEPARATOR)), Row.fromSeq(row)) else (None, Row.fromSeq(row))
  }
}

object UpdateMetricOperation {

  def toSeq(dimensionValues: Seq[DimensionValue], aggregations: Map[String, Option[Any]]): (Seq[Any], Seq[Any]) =
    (dimensionValues.sorted.map(dimVal => dimVal.value),
      aggregations.toSeq.sortWith((agg1, agg2) => agg1._1 < agg2._1).map(aggregation => aggregation._2.getOrElse(0)))

  def getNamesValues(names: Seq[String],
                     values: Seq[Any],
                     idCalculated: Boolean): (Seq[String], Seq[Any]) = {
    if (idCalculated && !names.contains(Output.ID))
      (names ++ Seq(Output.ID), values ++ Seq(values.mkString(Output.SEPARATOR)))
    else (names, values)
  }

  def namesDimVals(dimValues: Seq[DimensionValue]): Seq[String] = dimValues.map(_.getNameDimension)

  def sortedNamesDimVals(dimValues: Seq[DimensionValue]): Seq[String] = namesDimVals(dimValues.sorted)

  def filterDimVals(dimValues: Seq[DimensionValue], bucketName : Option[String]): Seq[DimensionValue] = {
    bucketName match {
      case None => dimValues
      case Some(bucket) => dimValues.filter(rollup => (rollup.bucketType.id != bucket))
    }
  }
}