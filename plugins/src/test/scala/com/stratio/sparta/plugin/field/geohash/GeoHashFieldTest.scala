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
package com.stratio.sparta.plugin.field.geohash

import com.stratio.sparta.plugin.field.geohash.GeoHashField
import com.stratio.sparta.sdk.{Precision, TypeOp}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import java.io.{Serializable => JSerializable}

import scala.util.{Success, Failure, Try}

@RunWith(classOf[JUnitRunner])
class GeoHashFieldTest extends WordSpecLike with Matchers {

  val geoHashDimension: GeoHashField = new GeoHashField(Map.empty[String, JSerializable])
  val geoHashDimensionDefault: GeoHashField = new GeoHashField()

  "A GeoHashDimension" should {
    "In default implementation, get 12 precisions for all precision sizes" in {

      val precisionLatLong = "40.1__30.2"

      val precision1 =
        geoHashDimension.precisionValue(GeoHashField.Precision1Name, precisionLatLong.asInstanceOf[Any])
      val precision2 =
        geoHashDimension.precisionValue(GeoHashField.Precision2Name, precisionLatLong.asInstanceOf[Any])
      val precision3 =
        geoHashDimension.precisionValue(GeoHashField.Precision3Name, precisionLatLong.asInstanceOf[Any])
      val precision4 =
        geoHashDimension.precisionValue(GeoHashField.Precision4Name, precisionLatLong.asInstanceOf[Any])
      val precision5 =
        geoHashDimension.precisionValue(GeoHashField.Precision5Name, precisionLatLong.asInstanceOf[Any])
      val precision6 =
        geoHashDimension.precisionValue(GeoHashField.Precision6Name, precisionLatLong.asInstanceOf[Any])
      val precision7 =
        geoHashDimension.precisionValue(GeoHashField.Precision7Name, precisionLatLong.asInstanceOf[Any])
      val precision8 =
        geoHashDimension.precisionValue(GeoHashField.Precision8Name, precisionLatLong.asInstanceOf[Any])
      val precision9 =
        geoHashDimension.precisionValue(GeoHashField.Precision9Name, precisionLatLong.asInstanceOf[Any])
      val precision10 =
        geoHashDimension.precisionValue(GeoHashField.Precision10Name, precisionLatLong.asInstanceOf[Any])
      val precision11 =
        geoHashDimension.precisionValue(GeoHashField.Precision11Name, precisionLatLong.asInstanceOf[Any])
      val precision12 =
        geoHashDimension.precisionValue(GeoHashField.Precision12Name, precisionLatLong.asInstanceOf[Any])

      precision1._1.id should be(GeoHashField.Precision1Name)
      precision2._1.id should be(GeoHashField.Precision2Name)
      precision3._1.id should be(GeoHashField.Precision3Name)
      precision4._1.id should be(GeoHashField.Precision4Name)
      precision5._1.id should be(GeoHashField.Precision5Name)
      precision6._1.id should be(GeoHashField.Precision6Name)
      precision7._1.id should be(GeoHashField.Precision7Name)
      precision8._1.id should be(GeoHashField.Precision8Name)
      precision9._1.id should be(GeoHashField.Precision9Name)
      precision10._1.id should be(GeoHashField.Precision10Name)
      precision11._1.id should be(GeoHashField.Precision11Name)
      precision12._1.id should be(GeoHashField.Precision12Name)

      val precision13 =
        geoHashDimension.precisionValue(GeoHashField.Precision12Name, Option("40.1__30.2").asInstanceOf[Any])

      precision13._1.id should be(GeoHashField.Precision3Name)

      val precision14 =
        geoHashDimension.precisionValue(GeoHashField.Precision12Name, None.orNull.asInstanceOf[Any])
      precision14._1.id should be(GeoHashField.Precision3Name)

      val precision15 =
        geoHashDimension.precisionValue(GeoHashField.Precision12Name, "40.1".asInstanceOf[Any])

      precision15._1.id should be(GeoHashField.Precision12Name)

    }

    "Each precision have their output type must be must be ArrayDouble" in {
      geoHashDimension.precision(GeoHashField.Precision1Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimension.precision(GeoHashField.Precision2Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimension.precision(GeoHashField.Precision3Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimension.precision(GeoHashField.Precision4Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimension.precision(GeoHashField.Precision5Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimension.precision(GeoHashField.Precision6Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimension.precision(GeoHashField.Precision7Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimension.precision(GeoHashField.Precision8Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimension.precision(GeoHashField.Precision9Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimension.precision(GeoHashField.Precision10Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimension.precision(GeoHashField.Precision11Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimension.precision(GeoHashField.Precision12Name).typeOp should be(TypeOp.ArrayDouble)
    }

    "Each precision have their output type, all precisions must be ArrayDouble by default" in {
      geoHashDimensionDefault.precision(GeoHashField.Precision1Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precision(GeoHashField.Precision2Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precision(GeoHashField.Precision3Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precision(GeoHashField.Precision4Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precision(GeoHashField.Precision5Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precision(GeoHashField.Precision6Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precision(GeoHashField.Precision7Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precision(GeoHashField.Precision8Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precision(GeoHashField.Precision9Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precision(GeoHashField.Precision10Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precision(GeoHashField.Precision11Name).typeOp should be(TypeOp.ArrayDouble)
      geoHashDimensionDefault.precision(GeoHashField.Precision12Name).typeOp should be(TypeOp.ArrayDouble)
    }
    "latitude and longitude splited  tests " in {
      val latitude = new GeoHashField(Map("coordinate" -> "latitude"))
      latitude.precision(GeoHashField.Precision12Name).typeOp should be(TypeOp.Double)
      val longitude = new GeoHashField(Map("coordinate" -> "longitude"))
      longitude.precision(GeoHashField.Precision12Name).typeOp should be(TypeOp.Double)
      val other = new GeoHashField(Map("coordinate" -> "other"))
      other.precision(GeoHashField.Precision12Name).typeOp should be(TypeOp.ArrayDouble)

      val latResult: (Precision, Any) =
        latitude.precisionValue(GeoHashField.Precision12Name, "40.1__30.2".asInstanceOf[Any])

      latResult._1.id should be(GeoHashField.Precision12Name)

      val lonResult: (Precision, Any) =
        longitude.precisionValue(GeoHashField.Precision12Name, "40.1__30.2".asInstanceOf[Any])

      lonResult._1.id should be(GeoHashField.Precision12Name)

      val otherResult: (Precision, Any) =
        other.precisionValue(GeoHashField.Precision12Name, "40.1__30.2".asInstanceOf[Any])

      otherResult._1.id should be(GeoHashField.Precision12Name)
    }
  }
}
