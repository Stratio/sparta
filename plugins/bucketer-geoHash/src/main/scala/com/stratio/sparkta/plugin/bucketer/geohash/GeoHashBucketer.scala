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
package com.stratio.sparkta.plugin.bucketer.geohash

import java.io

import com.github.davidmoten.geo.GeoHash
import com.stratio.sparkta.plugin.bucketer.geohash.GeoHashBucketer._
import com.stratio.sparkta.sdk.{BucketType, Bucketer}

/**
 * Created by ajnavarro on 20/10/14.
 *
 * Aggregation by geoposition
 *
 * Supported buckets by geoHash length:
 * 1 -> 5,009.4km x 4,992.6km
 * 2 -> 51,252.3km x 624.1km
 * 3 -> 156.5km x 156km
 * 4 -> 39.1km x 19.5km
 * 5 -> 4.9km x 4.9km
 * 6 -> 1.2km x 609.4m
 * 7 -> 152.9m x 152.4m
 * 8 -> 38.2m x 19m
 * 9 -> 4.8m x 4.8m
 * 10 -> 1.2m x 59.5cm
 * 11 -> 14.9cm x 14.9cm
 * 12 -> 3.7cm x 1.9cm
 *
 */
case class GeoHashBucketer(override val bucketTypes: Seq[BucketType] =
                           Seq(
                             precision1,
                             precision2,
                             precision3,
                             precision4,
                             precision5,
                             precision6,
                             precision7,
                             precision8,
                             precision9)) extends Bucketer {

  override def bucketForWrite(value: io.Serializable): Map[BucketType, Seq[io.Serializable]] = {
    //TODO temporal data treatment
    if (value != null) {
      bucketTypes.map(bucketType => {
        //TODO temporal data treatment
        val latLongString = value.asInstanceOf[String].split("__")
        val latDouble = latLongString(0).toDouble
        val longDouble = latLongString(1).toDouble
        (bucketType -> Seq(bucket(latDouble, longDouble, bucketType)))
      }).toMap
    } else {
      Map()
    }
  }
}

object GeoHashBucketer {
  private def bucket(lat: Double, long: Double, bucketType: BucketType): io.Serializable = {
    (bucketType match {
      case p if p == precision1 => GeoHash.encodeHash(lat, long, 1)
      case p if p == precision2 => GeoHash.encodeHash(lat, long, 2)
      case p if p == precision3 => GeoHash.encodeHash(lat, long, 3)
      case p if p == precision4 => GeoHash.encodeHash(lat, long, 4)
      case p if p == precision5 => GeoHash.encodeHash(lat, long, 5)
      case p if p == precision6 => GeoHash.encodeHash(lat, long, 6)
      case p if p == precision7 => GeoHash.encodeHash(lat, long, 7)
      case p if p == precision8 => GeoHash.encodeHash(lat, long, 8)
      case p if p == precision9 => GeoHash.encodeHash(lat, long, 9)
      case p if p == precision10 => GeoHash.encodeHash(lat, long, 10)
      case p if p == precision11 => GeoHash.encodeHash(lat, long, 11)
      case p if p == precision12 => GeoHash.encodeHash(lat, long, 12)
    }).asInstanceOf[io.Serializable]
  }

  val precision1 = new BucketType("precision1")
  val precision2 = new BucketType("precision2")
  val precision3 = new BucketType("precision3")
  val precision4 = new BucketType("precision4")
  val precision5 = new BucketType("precision5")
  val precision6 = new BucketType("precision6")
  val precision7 = new BucketType("precision7")
  val precision8 = new BucketType("precision8")
  val precision9 = new BucketType("precision9")
  val precision10 = new BucketType("precision10")
  val precision11 = new BucketType("precision11")
  val precision12 = new BucketType("precision12")
}
