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
import java.io.{Serializable => JSerializable}

case class UpdateMetricOperation(rollupKey: Seq[DimensionValue],
                                 var aggregations: Map[String, Option[Any]]) {

  if (rollupKey == null) {
    throw new NullPointerException("rollupKey")
  }

  if (aggregations == null) {
    throw new NullPointerException("aggregations")
  }

  override def toString: String = {
    this.keyString + " DIMENSIONS: " + rollupKey.mkString("|") + " AGGREGATIONS: " + aggregations
  }

  def keyString: String = {
    UpdateMetricOperation.sortedNamesDimensionsValues(rollupKey)
      .filter(dimName => dimName.nonEmpty).mkString(Output.SEPARATOR)
  }

  /*
   * By default transform an UpdateMetricOperation in a Row with the description.
   * If fixedBuckets is defined add fields and values to the original values and fields.
   * Id field we need calculate the value with all other values
   */
  def toKeyRow(fixedBuckets: Option[Seq[(String, Any)]], idCalculated: Boolean): (Option[String], Row) = {
    val namesValues = UpdateMetricOperation.getNamesValues(
      UpdateMetricOperation.namesDimensionValues(UpdateMetricOperation.sortDimensionValues(rollupKey)),
      toSeq,
      idCalculated)
    val keysRow = if (fixedBuckets.isDefined) {
      val fixedBucketsFiltered = fixedBuckets.get.filter(bucket => !namesValues._1.contains(bucket._1))
      (namesValues._1 ++ fixedBucketsFiltered.map(_._1),
        Row.fromSeq(namesValues._2 ++ fixedBucketsFiltered.map(_._2)))
    } else (namesValues._1, Row.fromSeq(namesValues._2))

    if (keysRow._1.length > 0) {
      (Some(keysRow._1.mkString(Output.SEPARATOR)), keysRow._2)
    } else (None, keysRow._2)
  }

  def toSeq: (Seq[Any], Seq[Any]) = {
    (UpdateMetricOperation.sortDimensionValues(rollupKey).map(dimVal => dimVal.value),
      aggregations.toSeq.map(aggregation => aggregation._2.getOrElse(0)))
  }
}

object UpdateMetricOperation {

  def getNamesValues(sortedNames: Seq[String],
                     seqOfvalues: (Seq[Any], Seq[Any]),
                     idCalculated: Boolean): (Seq[String], Seq[Any]) = {
    if (idCalculated && !sortedNames.contains(Output.ID)) {
      (sortedNames ++ Seq(Output.ID),
        seqOfvalues._1 ++ seqOfvalues._2 ++ Seq(seqOfvalues._1.mkString(Output.SEPARATOR)))
    } else (sortedNames, seqOfvalues._1 ++ seqOfvalues._2)
  }

  def sortDimensionValues(dimValues: Seq[DimensionValue]): Seq[DimensionValue] = {
    dimValues.sortWith((dim1, dim2) =>
      (dim1.dimension.name + dim1.bucketType.id) < (dim2.dimension.name + dim2.bucketType.id)
    )
  }

  def namesDimensionValues(dimValues: Seq[DimensionValue]): Seq[String] = {
    dimValues.map(dimVal => {
      dimVal.bucketType match {
        case Bucketer.identity => dimVal.dimension.name
        case _ => dimVal.bucketType.id
      }
    })
  }

  def sortedNamesDimensionsValues(dimValues: Seq[DimensionValue]): Seq[String] = {
    namesDimensionValues(sortDimensionValues(dimValues))
  }
}