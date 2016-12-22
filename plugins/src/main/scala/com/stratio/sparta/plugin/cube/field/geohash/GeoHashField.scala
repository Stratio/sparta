/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
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
package com.stratio.sparta.plugin.cube.field.geohash

import java.io.{Serializable => JSerializable}

import akka.event.slf4j.SLF4JLogging
import com.github.davidmoten.geo.{GeoHash, LatLong}
import com.stratio.sparta.plugin.cube.field.geohash.GeoHashField._
import com.stratio.sparta.sdk.pipeline.schema.TypeOp._
import com.stratio.sparta.sdk._
import com.stratio.sparta.sdk.pipeline.aggregation.cube.{DimensionType, Precision}
import com.stratio.sparta.sdk.pipeline.schema.TypeOp

import scala.util.Try

/**
 *
 * Aggregation by geoposition
 *
 * Supported dimensions by geoHash length:
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
case class GeoHashField(props: Map[String, JSerializable], override val defaultTypeOperation : TypeOp)
  extends DimensionType with JSerializable with SLF4JLogging {

  def this(defaultTypeOperation : TypeOp) {
    this(Map(), defaultTypeOperation)
  }

  def this(props: Map[String, JSerializable]) {
    this(props, {
      val coordinate = props.get("coordinate")

      coordinate match {
        case Some(coord) =>
          if (coord.asInstanceOf[String] == "latitude") TypeOp.Double
          else {
            if (coord.asInstanceOf[String] == "longitude") TypeOp.Double
            else TypeOp.ArrayDouble
          }
        case None => TypeOp.ArrayDouble
      }
    })
  }

  def this() {
    this(Map(), TypeOp.ArrayDouble)
  }

  override val properties: Map[String, JSerializable] = props

  override val operationProps: Map[String, JSerializable] = props

  val coordinate = properties.get("coordinate")
  
  //scalastyle:off
  def precision(keyName: String): Precision = keyName match {
    case Precision1Name => getPrecision(Precision1Name, getTypeOperation(Precision1Name))
    case Precision2Name => getPrecision(Precision2Name, getTypeOperation(Precision2Name))
    case Precision3Name => getPrecision(Precision3Name, getTypeOperation(Precision3Name))
    case Precision4Name => getPrecision(Precision4Name, getTypeOperation(Precision4Name))
    case Precision5Name => getPrecision(Precision5Name, getTypeOperation(Precision5Name))
    case Precision6Name => getPrecision(Precision6Name, getTypeOperation(Precision6Name))
    case Precision7Name => getPrecision(Precision7Name, getTypeOperation(Precision7Name))
    case Precision8Name => getPrecision(Precision8Name, getTypeOperation(Precision8Name))
    case Precision9Name => getPrecision(Precision9Name, getTypeOperation(Precision9Name))
    case Precision10Name => getPrecision(Precision10Name, getTypeOperation(Precision10Name))
    case Precision11Name => getPrecision(Precision11Name, getTypeOperation(Precision11Name))
    case Precision12Name => getPrecision(Precision12Name, getTypeOperation(Precision12Name))
  }


  def precisionValue(keyName: String, value: Any): (Precision, Any) =
    Try {
      Option(value) match {
        case Some(latLongValue: String) => extractPrecision(keyName, latLongValue)
        case None => defaultPrecisionTuple
        case _ =>
          log.error("Error trying to extract the latitude and longitude values")
          defaultPrecisionTuple
      }
    }.recover{
      case cce: ClassCastException =>
        log.error(s"Error parsing $value.")
        throw cce
    }.get

  def extractPrecision(keyName: String, latLongValue: String): (Precision, Any) = {
    val precisionKey = precision(keyName)
    val latLongArray =
      latLongValue.split(
        properties.getOrElse(GeoHashField.LatLongKey, GeoHashField.LatLongSeparator)
          .toString
      )

    latLongArray match {
      case Array(lat, long) =>
        (precisionKey, getPrecision(lat.toDouble, long.toDouble, precisionKey))
      case _ => (precisionKey, "")
    }
  }

  def defaultPrecisionTuple = {
    val defaultPrecision = getPrecision(Precision3Name, getTypeOperation(Precision3Name))
    (defaultPrecision, getPrecision(0, 0, defaultPrecision))
  }

  //scalastyle:off
  def getPrecision(lat: Double, long: Double, precision: Precision): Any =
    TypeOp.transformValueByTypeOp(precision.typeOp, precision.id match {
      case Precision1Name => decodeHash(GeoHash.encodeHash(lat, long, 1))
      case Precision2Name => decodeHash(GeoHash.encodeHash(lat, long, 2))
      case Precision3Name => decodeHash(GeoHash.encodeHash(lat, long, 3))
      case Precision4Name => decodeHash(GeoHash.encodeHash(lat, long, 4))
      case Precision5Name => decodeHash(GeoHash.encodeHash(lat, long, 5))
      case Precision6Name => decodeHash(GeoHash.encodeHash(lat, long, 6))
      case Precision7Name => decodeHash(GeoHash.encodeHash(lat, long, 7))
      case Precision8Name => decodeHash(GeoHash.encodeHash(lat, long, 8))
      case Precision9Name => decodeHash(GeoHash.encodeHash(lat, long, 9))
      case Precision10Name => decodeHash(GeoHash.encodeHash(lat, long, 10))
      case Precision11Name => decodeHash(GeoHash.encodeHash(lat, long, 11))
      case Precision12Name => decodeHash(GeoHash.encodeHash(lat, long, 12))
    })

  def decodeHash(geoLocHash: String): Any = {
    val geoDecoded: LatLong = GeoHash.decodeHash(geoLocHash)
    val (latitude, longitude) = (geoDecoded.getLat, geoDecoded.getLon)
    coordinate match {
      case Some("latitude") => latitude.asInstanceOf[Any]
      case Some("longitude") => longitude.asInstanceOf[Any]
      case _ => Seq(longitude, latitude).asInstanceOf[Any]
    }
  }
}

object GeoHashField {

  final val Precision1Name = "precision1"
  final val Precision2Name = "precision2"
  final val Precision3Name = "precision3"
  final val Precision4Name = "precision4"
  final val Precision5Name = "precision5"
  final val Precision6Name = "precision6"
  final val Precision7Name = "precision7"
  final val Precision8Name = "precision8"
  final val Precision9Name = "precision9"
  final val Precision10Name = "precision10"
  final val Precision11Name = "precision11"
  final val Precision12Name = "precision12"
  final val LatLongSeparator = "__"
  final val LatLongKey = "separator"

}
