/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.sdk

import org.apache.spark.sql._
import java.io.{Serializable => JSerializable}

case class UpdateMetricOperation(rollupKey: Seq[DimensionValue],
                                  var aggregations: Map[String, Option[Any]]) {

  final val SEPARATOR = "_"

  if (rollupKey == null) {
    throw new NullPointerException("rollupKey")
  }

  if (aggregations == null) {
    throw new NullPointerException("aggregations")
  }

  //TODO is not necesary filter??
  def keyString: String = {
    sortedNamesDimensionsValues
      .filter(dimName => dimName.nonEmpty).mkString(SEPARATOR)
  }

  override def toString: String = {
    this.keyString + " DIMENSIONS: " + rollupKey.mkString("|") + " AGGREGATIONS: " + aggregations
  }

  def sortDimensionValues: Seq[DimensionValue] = {
    rollupKey.sortWith((dim1, dim2) =>
      (dim1.dimension.name + dim1.bucketType.id) < (dim2.dimension.name + dim2.bucketType.id)
    )
  }

  def namesDimensionValues(dimValues: Seq[DimensionValue]) : Seq[String] = {
    dimValues.map(dimVal => {
      dimVal.bucketType match {
        case Bucketer.identity => dimVal.dimension.name
        case _ => dimVal.bucketType.id
      }
    })
  }

  def sortedNamesDimensionsValues: Seq[String] = {
    namesDimensionValues(sortDimensionValues)
  }

  def toKeyRow: (Option[String], Row) = {
    val sortedRollup = sortDimensionValues
    val sortedNames = namesDimensionValues(sortDimensionValues)
    val row = toRow
    //TODO read from broadcast, we can check??
    /*val schema : StructType = StructType(
      sortedNames.map(fieldName => StructField(fieldName, StringType, false)) ++
        aggregations.map(fieldName =>
          StructField(fieldName._1, rowTypeFromOption(fieldName._2), true)
        )
    )*/
    if (sortedNames.length > 0) (Some(sortedNames.mkString(SEPARATOR)), row) else (None, row)
  }

  def toRow: Row = {
    Row.fromSeq(
      sortDimensionValues.map(dimVal => dimVal.value) ++ aggregations.toSeq.map(aggregation => aggregation._2.get))
  }
}
