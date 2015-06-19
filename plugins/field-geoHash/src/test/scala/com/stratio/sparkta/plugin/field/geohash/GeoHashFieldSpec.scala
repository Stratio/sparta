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

package com.stratio.sparkta.plugin.field.geohash

import java.io.{Serializable => JSerializable}

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

import com.stratio.sparkta.sdk.TypeOp

@RunWith(classOf[JUnitRunner])
class GeoHashFieldSpec extends WordSpecLike with Matchers {

  val geoHashDimension: GeoHashField = new GeoHashField(Map("precision1" -> "int", "typeOp" -> "long"))
  val geoHashDimensionDefault: GeoHashField = new GeoHashField()

  "A GeoHashDimension" should {
    "In default implementation, get 12 precisions for all precision sizes" in {
      val precisions = geoHashDimension.dimensionValues(Some("40.1__30.2").asInstanceOf[JSerializable]).map(_._1.id)

      precisions.size should be(12)

      precisions should contain(GeoHashField.Precision1Name)
      precisions should contain(GeoHashField.Precision2Name)
      precisions should contain(GeoHashField.Precision3Name)
      precisions should contain(GeoHashField.Precision4Name)
      precisions should contain(GeoHashField.Precision5Name)
      precisions should contain(GeoHashField.Precision6Name)
      precisions should contain(GeoHashField.Precision7Name)
      precisions should contain(GeoHashField.Precision8Name)
      precisions should contain(GeoHashField.Precision9Name)
      precisions should contain(GeoHashField.Precision10Name)
      precisions should contain(GeoHashField.Precision11Name)
      precisions should contain(GeoHashField.Precision12Name)
    }

    "Each precision have their output type, precision1 must be integer and the others long" in {
      geoHashDimension.precisions(GeoHashField.Precision1Name).typeOp should be(TypeOp.Int)
      geoHashDimension.precisions(GeoHashField.Precision2Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashField.Precision3Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashField.Precision4Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashField.Precision5Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashField.Precision6Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashField.Precision7Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashField.Precision8Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashField.Precision9Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashField.Precision10Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashField.Precision11Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precisions(GeoHashField.Precision12Name).typeOp should be(TypeOp.Long)
    }

    "Each precision have their output type, all precisions must be ArrayDouble by default" in {
      geoHashDimensionDefault.precisions(GeoHashField.Precision1Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashField.Precision2Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashField.Precision3Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashField.Precision4Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashField.Precision5Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashField.Precision6Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashField.Precision7Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashField.Precision8Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashField.Precision9Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashField.Precision10Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashField.Precision11Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precisions(GeoHashField.Precision12Name).typeOp should be(TypeOp.ArrayDouble)
    }
  }
}
