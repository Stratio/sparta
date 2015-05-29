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

import akka.event.slf4j.SLF4JLogging
import com.github.davidmoten.geo.{LatLong, GeoHash}
import GeoHashDimension._
import com.stratio.sparkta.sdk.{BucketType, Bucketer}
import com.stratio.sparkta.sdk.TypeOp._
import com.stratio.sparkta.sdk.{TypeOp, _}

/**
 *
 * Aggregation by geoposition
 *
 * Supported buckets by geoHash length:
 * 1 - 5,009.4km x 4,992.6km
 * 2 - 51,252.3km x 624.1km
 * 3 - 156.5km x 156km
 * 4 - 39.1km x 19.5km
 * 5 - 4.9km x 4.9km
 * 6 - 1.2km x 609.4m
 * 7 - 152.9m x 152.4m
 * 8 - 38.2m x 19m
 * 9 - 4.8m x 4.8m
 * 10 - 1.2m x 59.5cm
 * 11 - 14.9cm x 14.9cm
 * 12 - 3.7cm x 1.9cm
 *
 */
case class GeoHashDimension(props: Map[String, JSerializable]) extends Bucketer with SLF4JLogging {

  def this() {
    this(Map())
  }

  override val properties: Map[String, JSerializable] = props

  override val bucketTypes: Seq[BucketType] =
    Seq(
      getPrecision(Precision1, getTypeOperation(Precision1), defaultTypeOperation),
      getPrecision(Precision2, getTypeOperation(Precision2), defaultTypeOperation),
      getPrecision(Precision3, getTypeOperation(Precision3), defaultTypeOperation),
      getPrecision(Precision4, getTypeOperation(Precision4), defaultTypeOperation),
      getPrecision(Precision5, getTypeOperation(Precision5), defaultTypeOperation),
      getPrecision(Precision6, getTypeOperation(Precision6), defaultTypeOperation),
      getPrecision(Precision7, getTypeOperation(Precision7), defaultTypeOperation),
      getPrecision(Precision8, getTypeOperation(Precision8), defaultTypeOperation),
      getPrecision(Precision9, getTypeOperation(Precision9), defaultTypeOperation),
      getPrecision(Precision10, getTypeOperation(Precision10), defaultTypeOperation),
      getPrecision(Precision11, getTypeOperation(Precision11), defaultTypeOperation),
      getPrecision(Precision12, getTypeOperation(Precision12), defaultTypeOperation))

  override def bucket(value: JSerializable): Map[BucketType, JSerializable] = {
    //TODO temporal data treatment

    try {
      if (value.asInstanceOf[Option[_]] != None) {
        bucketTypes.map(bucketType => {
          //TODO temporal data treatment
          val latLongString = value.asInstanceOf[Option[_]].get.asInstanceOf[String].split("__")
          if (latLongString.size != 0) {
            val latDouble = latLongString(0).toDouble
            val longDouble = latLongString(1).toDouble
            bucketType -> GeoHashDimension.bucket(latDouble, longDouble, bucketType)
          } else (bucketType -> "")
        }).toMap
      } else {
        val defaultPrecision = getPrecision(Precision3, getTypeOperation(Precision3), defaultTypeOperation)
        Map(defaultPrecision -> GeoHashDimension.bucket(0, 0, defaultPrecision))
      }
    }
    catch {
      case aobe: ArrayIndexOutOfBoundsException => log.error("geo problem"); Map()

      case cce: ClassCastException => log.error("Error parsing " + value + " ."); throw cce;
    }
  }

  override val defaultTypeOperation = TypeOp.ArrayDouble
}

object GeoHashDimension {

  val Precision1 = "precision1"
  val Precision2 = "precision2"
  val Precision3 = "precision3"
  val Precision4 = "precision4"
  val Precision5 = "precision5"
  val Precision6 = "precision6"
  val Precision7 = "precision7"
  val Precision8 = "precision8"
  val Precision9 = "precision9"
  val Precision10 = "precision10"
  val Precision11 = "precision11"
  val Precision12 = "precision12"

  //scalastyle:off
  private def bucket(lat: Double, long: Double, bucketType: BucketType): JSerializable = {
    bucketType match {
      case p if p.id == Precision1 => decodeHash(GeoHash.encodeHash(lat, long, 1))
      case p if p.id == Precision2 => decodeHash(GeoHash.encodeHash(lat, long, 2))
      case p if p.id == Precision3 => decodeHash(GeoHash.encodeHash(lat, long, 3))
      case p if p.id == Precision4 => decodeHash(GeoHash.encodeHash(lat, long, 4))
      case p if p.id == Precision5 => decodeHash(GeoHash.encodeHash(lat, long, 5))
      case p if p.id == Precision6 => decodeHash(GeoHash.encodeHash(lat, long, 6))
      case p if p.id == Precision7 => decodeHash(GeoHash.encodeHash(lat, long, 7))
      case p if p.id == Precision8 => decodeHash(GeoHash.encodeHash(lat, long, 8))
      case p if p.id == Precision9 => decodeHash(GeoHash.encodeHash(lat, long, 9))
      case p if p.id == Precision10 => decodeHash(GeoHash.encodeHash(lat, long, 10))
      case p if p.id == Precision11 => decodeHash(GeoHash.encodeHash(lat, long, 11))
      case p if p.id == Precision12 => decodeHash(GeoHash.encodeHash(lat, long, 12))
    }
  }

  //scalastyle:on
  private def decodeHash(geoLocHash: String) = {
    val geoDecoded: LatLong = GeoHash.decodeHash(geoLocHash)
    val (latitude, longitude) = (geoDecoded.getLat, geoDecoded.getLon)
    Seq(longitude, latitude).asInstanceOf[JSerializable]
  }

  def getPrecision(precision: String, typeOperation: Option[TypeOp], default: TypeOp): BucketType =
    new BucketType(precision, typeOperation.orElse(Some(default)))

}
