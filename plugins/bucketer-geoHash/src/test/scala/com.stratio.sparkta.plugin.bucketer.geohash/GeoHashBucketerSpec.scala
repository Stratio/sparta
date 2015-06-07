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

package com.stratio.sparkta.plugin.bucketer.geohash

import java.io.{Serializable => JSerializable}

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import com.stratio.sparkta.sdk.TypeOp

@RunWith(classOf[JUnitRunner])
class GeoHashBucketerSpec extends WordSpecLike with Matchers with BeforeAndAfter with BeforeAndAfterAll {

  var geoHashBucketer: GeoHashBucketer = null
  var geoHashBucketerDefault: GeoHashBucketer = null
  before {
    geoHashBucketer = new GeoHashBucketer(Map("precision1" -> "int", "typeOp" -> "long"))
    geoHashBucketerDefault = new GeoHashBucketer()
  }

  after {
    geoHashBucketer = null
    geoHashBucketerDefault = null
  }

  "A GeoHashBucketer" should {
    "In default implementation, get 12 buckets for all precision sizes" in {
      val buckets = geoHashBucketer.bucket(Some("40.1__30.2").asInstanceOf[JSerializable]).map(_._1.id)

      buckets.size should be(12)

      buckets should contain(GeoHashBucketer.Precision1Name)
      buckets should contain(GeoHashBucketer.Precision2Name)
      buckets should contain(GeoHashBucketer.Precision3Name)
      buckets should contain(GeoHashBucketer.Precision4Name)
      buckets should contain(GeoHashBucketer.Precision5Name)
      buckets should contain(GeoHashBucketer.Precision6Name)
      buckets should contain(GeoHashBucketer.Precision7Name)
      buckets should contain(GeoHashBucketer.Precision8Name)
      buckets should contain(GeoHashBucketer.Precision9Name)
      buckets should contain(GeoHashBucketer.Precision10Name)
      buckets should contain(GeoHashBucketer.Precision11Name)
      buckets should contain(GeoHashBucketer.Precision12Name)
    }

    "Each precision bucket have their output type, precision1 must be integer and the others long" in {
      geoHashBucketer.bucketTypes(GeoHashBucketer.Precision1Name).typeOp should be(TypeOp.Int)
      geoHashBucketer.bucketTypes(GeoHashBucketer.Precision2Name).typeOp should be(TypeOp.Long)
      geoHashBucketer.bucketTypes(GeoHashBucketer.Precision3Name).typeOp should be(TypeOp.Long)
      geoHashBucketer.bucketTypes(GeoHashBucketer.Precision4Name).typeOp should be(TypeOp.Long)
      geoHashBucketer.bucketTypes(GeoHashBucketer.Precision5Name).typeOp should be(TypeOp.Long)
      geoHashBucketer.bucketTypes(GeoHashBucketer.Precision6Name).typeOp should be(TypeOp.Long)
      geoHashBucketer.bucketTypes(GeoHashBucketer.Precision7Name).typeOp should be(TypeOp.Long)
      geoHashBucketer.bucketTypes(GeoHashBucketer.Precision8Name).typeOp should be(TypeOp.Long)
      geoHashBucketer.bucketTypes(GeoHashBucketer.Precision9Name).typeOp should be(TypeOp.Long)
      geoHashBucketer.bucketTypes(GeoHashBucketer.Precision10Name).typeOp should be(TypeOp.Long)
      geoHashBucketer.bucketTypes(GeoHashBucketer.Precision11Name).typeOp should be(TypeOp.Long)
      geoHashBucketer.bucketTypes(GeoHashBucketer.Precision12Name).typeOp should be(TypeOp.Long)
    }

    "Each precision bucket have their output type, all precisions must be ArrayDouble by default" in {
      geoHashBucketerDefault.bucketTypes(GeoHashBucketer.Precision1Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashBucketerDefault.bucketTypes(GeoHashBucketer.Precision2Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashBucketerDefault.bucketTypes(GeoHashBucketer.Precision3Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashBucketerDefault.bucketTypes(GeoHashBucketer.Precision4Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashBucketerDefault.bucketTypes(GeoHashBucketer.Precision5Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashBucketerDefault.bucketTypes(GeoHashBucketer.Precision6Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashBucketerDefault.bucketTypes(GeoHashBucketer.Precision7Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashBucketerDefault.bucketTypes(GeoHashBucketer.Precision8Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashBucketerDefault.bucketTypes(GeoHashBucketer.Precision9Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashBucketerDefault.bucketTypes(GeoHashBucketer.Precision10Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashBucketerDefault.bucketTypes(GeoHashBucketer.Precision11Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashBucketerDefault.bucketTypes(GeoHashBucketer.Precision12Name).typeOp should be(TypeOp.ArrayDouble)
    }
  }
}
