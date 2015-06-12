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

package com.stratio.sparkta.plugin.dimension.geohash

import java.io.{Serializable => JSerializable}

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

import com.stratio.sparkta.sdk.TypeOp

@RunWith(classOf[JUnitRunner])
class GeoHashDimensionSpec extends WordSpecLike with Matchers {

  val geoHashDimension: GeoHashDimension = new GeoHashDimension(Map("precision1" -> "int", "typeOp" -> "long"))
  val geoHashDimensionDefault: GeoHashDimension = new GeoHashDimension()

  "A GeoHashDimension" should {
    "In default implementation, get 12 precisions for all precision sizes" in {
      val precisions = geoHashDimension.dimensionValues(Some("40.1__30.2").asInstanceOf[JSerializable]).map(_._1.id)

      precisions.size should be(12)

      precisions should contain(GeoHashDimension.Precision1Name)
      precisions should contain(GeoHashDimension.Precision2Name)
      precisions should contain(GeoHashDimension.Precision3Name)
      precisions should contain(GeoHashDimension.Precision4Name)
      precisions should contain(GeoHashDimension.Precision5Name)
      precisions should contain(GeoHashDimension.Precision6Name)
      precisions should contain(GeoHashDimension.Precision7Name)
      precisions should contain(GeoHashDimension.Precision8Name)
      precisions should contain(GeoHashDimension.Precision9Name)
      precisions should contain(GeoHashDimension.Precision10Name)
      precisions should contain(GeoHashDimension.Precision11Name)
      precisions should contain(GeoHashDimension.Precision12Name)
    }

    "Each precision have their output type, precision1 must be integer and the others long" in {
      geoHashDimension.precisions(GeoHashDimension.Precision1Name).typeOp should be(TypeOp.Int)
      geoHashDimension.precisions(GeoHashDimension.Precision2Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashDimension.Precision3Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashDimension.Precision4Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashDimension.Precision5Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashDimension.Precision6Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashDimension.Precision7Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashDimension.Precision8Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashDimension.Precision9Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashDimension.Precision10Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashDimension.Precision11Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashDimension.Precision12Name).typeOp should be(TypeOp.Long)
    }

    "Each precision have their output type, all precisions must be ArrayDouble by default" in {
      geoHashDimensionDefault.precisions(GeoHashDimension.Precision1Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashDimension.Precision2Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashDimension.Precision3Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashDimension.Precision4Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashDimension.Precision5Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashDimension.Precision6Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashDimension.Precision7Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashDimension.Precision8Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashDimension.Precision9Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashDimension.Precision10Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashDimension.Precision11Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashDimension.Precision12Name).typeOp should be(TypeOp.ArrayDouble)
    }
  }
}
