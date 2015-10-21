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

import com.stratio.sparkta.sdk.{Precision, TypeOp}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}

import scala.util.{Failure, Try}

@RunWith(classOf[JUnitRunner])
class GeoHashFieldSpec extends WordSpecLike with Matchers {

  val geoHashDimension: GeoHashField = new GeoHashField(Map("precision1" -> "int", "typeOp" -> "long"))
  val geoHashDimensionDefault: GeoHashField = new GeoHashField()

  "A GeoHashDimension" should {
    "In default implementation, get 12 precisions for all precision sizes" in {
      val precision1 =
        geoHashDimension.precisionValue(GeoHashField.Precision1Name, Some("40.1__30.2").asInstanceOf[JSerializable])
      val precision2 =
        geoHashDimension.precisionValue(GeoHashField.Precision2Name, Some("40.1__30.2").asInstanceOf[JSerializable])
      val precision3 =
        geoHashDimension.precisionValue(GeoHashField.Precision3Name, Some("40.1__30.2").asInstanceOf[JSerializable])
      val precision4 =
        geoHashDimension.precisionValue(GeoHashField.Precision4Name, Some("40.1__30.2").asInstanceOf[JSerializable])
      val precision5 =
        geoHashDimension.precisionValue(GeoHashField.Precision5Name, Some("40.1__30.2").asInstanceOf[JSerializable])
      val precision6 =
        geoHashDimension.precisionValue(GeoHashField.Precision6Name, Some("40.1__30.2").asInstanceOf[JSerializable])
      val precision7 =
        geoHashDimension.precisionValue(GeoHashField.Precision7Name, Some("40.1__30.2").asInstanceOf[JSerializable])
      val precision8 =
        geoHashDimension.precisionValue(GeoHashField.Precision8Name, Some("40.1__30.2").asInstanceOf[JSerializable])
      val precision9 =
        geoHashDimension.precisionValue(GeoHashField.Precision9Name, Some("40.1__30.2").asInstanceOf[JSerializable])
      val precision10 =
        geoHashDimension.precisionValue(GeoHashField.Precision10Name, Some("40.1__30.2").asInstanceOf[JSerializable])
      val precision11 =
        geoHashDimension.precisionValue(GeoHashField.Precision11Name, Some("40.1__30.2").asInstanceOf[JSerializable])
      val precision12 =
        geoHashDimension.precisionValue(GeoHashField.Precision12Name, Some("40.1__30.2").asInstanceOf[JSerializable])


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
        geoHashDimension.precisionValue(GeoHashField.Precision12Name, "40.1__30.2".asInstanceOf[JSerializable])

      precision13._1.id should be(GeoHashField.Precision3Name)

      val precision14 =
        geoHashDimension.precisionValue(GeoHashField.Precision12Name, None.asInstanceOf[JSerializable])
      precision14._1.id should be(GeoHashField.Precision3Name)

      val precision15 =
        geoHashDimension.precisionValue(GeoHashField.Precision12Name, Some("40.1").asInstanceOf[JSerializable])

      precision15._1.id should be(GeoHashField.Precision12Name)
      val precision16 = Try(
        geoHashDimension.precisionValue(GeoHashField.Precision12Name, Some(1).asInstanceOf[JSerializable]))
      match {
        case Failure(ex) => ex
      }

      precision16.isInstanceOf[ClassCastException] should be(true)

    }

    "Each precision have their output type, precision1 must be integer and the others long" in {
      geoHashDimension.precision(GeoHashField.Precision1Name).typeOp should be(TypeOp.Int)
      geoHashDimension.precision(GeoHashField.Precision2Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precision(GeoHashField.Precision3Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precision(GeoHashField.Precision4Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precision(GeoHashField.Precision5Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precision(GeoHashField.Precision6Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precision(GeoHashField.Precision7Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precision(GeoHashField.Precision8Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precision(GeoHashField.Precision9Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precision(GeoHashField.Precision10Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precision(GeoHashField.Precision11Name).typeOp should be(TypeOp.Long)
      geoHashDimension.precision(GeoHashField.Precision12Name).typeOp should be(TypeOp.Long)
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
      val latitude = new GeoHashField(Map("precision1" -> "int", "typeOp" -> "long", "coordinate" -> "latitude"))
      latitude.precision(GeoHashField.Precision12Name).typeOp should be(TypeOp.Long)
      val longitude = new GeoHashField(Map("precision1" -> "int", "typeOp" -> "long", "coordinate" -> "longitude"))
      longitude.precision(GeoHashField.Precision12Name).typeOp should be(TypeOp.Long)
      val other = new GeoHashField(Map("precision1" -> "int", "typeOp" -> "long", "coordinate" -> "other"))
      other.precision(GeoHashField.Precision12Name).typeOp should be(TypeOp.Long)

      val latResult: (Precision, JSerializable) =
        latitude.precisionValue(GeoHashField.Precision12Name, Some("40.1__30.2").asInstanceOf[JSerializable])

      latResult._1.id should be(GeoHashField.Precision12Name)

      val lonResult: (Precision, JSerializable) =
        longitude.precisionValue(GeoHashField.Precision12Name, Some("40.1__30.2").asInstanceOf[JSerializable])

      lonResult._1.id should be(GeoHashField.Precision12Name)

      val otherResult: (Precision, JSerializable) =
        other.precisionValue(GeoHashField.Precision12Name, Some("40.1__30.2").asInstanceOf[JSerializable])

      otherResult._1.id should be(GeoHashField.Precision12Name)
    }
  }
}
