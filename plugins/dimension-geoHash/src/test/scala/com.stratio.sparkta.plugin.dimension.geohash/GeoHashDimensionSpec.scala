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
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import com.stratio.sparkta.sdk.TypeOp

@RunWith(classOf[JUnitRunner])
class GeoHashDimensionSpec extends WordSpecLike with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  var geoHashDimension: GeoHashDimension = null
  var geoHashDimensionDefault: GeoHashDimension = null
  before {
    geoHashDimension = new GeoHashDimension(Map("precision1" -> "int", "typeOp" -> "long"))
    geoHashDimensionDefault = new GeoHashDimension()
  }

  after {
    geoHashDimension = null
    geoHashDimensionDefault = null
  }

  "A GeoHashDimension" should {
    "In default implementation, get 12 buckets for all precision sizes" in {
      val buckets = geoHashDimension.bucket(Some("40.1__30.2").asInstanceOf[JSerializable]).map(_._1.id)

      buckets.size should be(12)

      buckets should contain(GeoHashDimension.Precision1Name)
      buckets should contain(GeoHashDimension.Precision2Name)
      buckets should contain(GeoHashDimension.Precision3Name)
      buckets should contain(GeoHashDimension.Precision4Name)
      buckets should contain(GeoHashDimension.Precision5Name)
      buckets should contain(GeoHashDimension.Precision6Name)
      buckets should contain(GeoHashDimension.Precision7Name)
      buckets should contain(GeoHashDimension.Precision8Name)
      buckets should contain(GeoHashDimension.Precision9Name)
      buckets should contain(GeoHashDimension.Precision10Name)
      buckets should contain(GeoHashDimension.Precision11Name)
      buckets should contain(GeoHashDimension.Precision12Name)
    }

    "Each precision bucket have their output type, precision1 must be integer and the others long" in {
      geoHashDimension.bucketTypes(GeoHashDimension.Precision1Name).typeOp should be(TypeOp.Int)
      geoHashDimension.bucketTypes(GeoHashDimension.Precision2Name).typeOp should be(TypeOp.Long)
      geoHashDimension.bucketTypes(GeoHashDimension.Precision3Name).typeOp should be(TypeOp.Long)
      geoHashDimension.bucketTypes(GeoHashDimension.Precision4Name).typeOp should be(TypeOp.Long)
      geoHashDimension.bucketTypes(GeoHashDimension.Precision5Name).typeOp should be(TypeOp.Long)
      geoHashDimension.bucketTypes(GeoHashDimension.Precision6Name).typeOp should be(TypeOp.Long)
      geoHashDimension.bucketTypes(GeoHashDimension.Precision7Name).typeOp should be(TypeOp.Long)
      geoHashDimension.bucketTypes(GeoHashDimension.Precision8Name).typeOp should be(TypeOp.Long)
      geoHashDimension.bucketTypes(GeoHashDimension.Precision9Name).typeOp should be(TypeOp.Long)
      geoHashDimension.bucketTypes(GeoHashDimension.Precision10Name).typeOp should be(TypeOp.Long)
      geoHashDimension.bucketTypes(GeoHashDimension.Precision11Name).typeOp should be(TypeOp.Long)
      geoHashDimension.bucketTypes(GeoHashDimension.Precision12Name).typeOp should be(TypeOp.Long)
    }

    "Each precision bucket have their output type, all precisions must be ArrayDouble by default" in {
      geoHashDimensionDefault.bucketTypes(GeoHashDimension.Precision1Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.bucketTypes(GeoHashDimension.Precision2Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.bucketTypes(GeoHashDimension.Precision3Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.bucketTypes(GeoHashDimension.Precision4Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.bucketTypes(GeoHashDimension.Precision5Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.bucketTypes(GeoHashDimension.Precision6Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.bucketTypes(GeoHashDimension.Precision7Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.bucketTypes(GeoHashDimension.Precision8Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.bucketTypes(GeoHashDimension.Precision9Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.bucketTypes(GeoHashDimension.Precision10Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.bucketTypes(GeoHashDimension.Precision11Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.bucketTypes(GeoHashDimension.Precision12Name).typeOp should be(TypeOp.ArrayDouble)
    }
  }
}
