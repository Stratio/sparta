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

/**
 * Created by jcgarcia on 16/04/15.
 */
object ExtractOp {

  def sortDimensionValues(rollupKey : Seq[DimensionValue]): Seq[DimensionValue] = {
    rollupKey.sortWith((dim1, dim2) =>
      (dim1.dimension.name + dim1.bucketType.id) < (dim2.dimension.name + dim2.bucketType.id)
    )
  }

  def sortRollups(rollups : Seq[(Dimension, BucketType)]) : Seq[(Dimension, BucketType)] = {
    rollups.sortWith((rollup1, rollup2) =>
      (rollup1._1.name + rollup1._2.id) < (rollup1._1.name + rollup1._2.id))
  }

  def namesDimensionValues(dimValues : Seq[DimensionValue]) : Seq[String] = {
    dimValues.map(dimVal => {
      dimVal.bucketType match {
        case Bucketer.identity => dimVal.dimension.name
        case _ => dimVal.bucketType.id
      }
    })
  }

  def namesRollups(dimValues : Seq[(Dimension, BucketType)]) : Seq[String] = {
    dimValues.map(dimVal => {
      dimVal._2 match {
        case Bucketer.identity => dimVal._1.name
        case _ => dimVal._2.id
      }
    })
  }

  def sortedNamesDimensionsValues(dimValues : Seq[DimensionValue]) : Seq[String] = {
    namesDimensionValues(sortDimensionValues(dimValues))
  }

  def sortedNamesRollups(dimValues : Seq[(Dimension, BucketType)]) : Seq[String] = {
    namesRollups(sortRollups(dimValues))
  }
}
