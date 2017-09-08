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

package com.stratio.sparta.plugin.transformation.geo

import java.io.{Serializable => JSerializable}

import com.stratio.sparta.sdk.pipeline.schema.TypeOp
import com.stratio.sparta.sdk.pipeline.transformation.{Parser, WhenError}
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

import scala.util.{Failure, Success, Try}

class GeoParser(
                 order: Integer,
                 inputField: Option[String],
                 outputFields: Seq[String],
                 schema: StructType,
                 properties: Map[String, JSerializable]
               ) extends Parser(order, inputField, outputFields, schema, properties) {

  val defaultLatitudeField = "latitude"
  val defaultLongitudeField = "longitude"
  val separator = "__"

  val latitudeField = properties.getOrElse("latitude", defaultLatitudeField).toString
  val longitudeField = properties.getOrElse("longitude", defaultLongitudeField).toString

  def parse(row: Row): Seq[Row] = {
    val newData = Try {
      val geoValue = geoField(getLatitude(row), getLongitude(row))
      outputFields.map(outputField => {
        val outputSchemaValid = outputFieldsSchema.find(field => field.name == outputField)
        outputSchemaValid match {
          case Some(outSchema) =>
            TypeOp.castingToSchemaType(outSchema.dataType, geoValue)
          case None =>
            returnWhenError(
              throw new IllegalStateException(s"Impossible to parse outputField: $outputField in the schema"))
        }
      })
    }

    returnData(newData, removeInputField(row))
  }

  private def getLatitude(row: Row): String = {
    val latitude = Try(row.get(schema.fieldIndex(latitudeField)))
      .getOrElse(throw new RuntimeException(s"Impossible to parse $latitudeField in the event: ${row.mkString(",")}"))

    latitude match {
      case valueCast: String => valueCast
      case valueCast: Array[Byte] => new Predef.String(valueCast)
      case _ => latitude.toString
    }
  }

  private def getLongitude(row: Row): String = {
    val longitude = Try(row.get(schema.fieldIndex(longitudeField)))
      .getOrElse(throw new RuntimeException(s"Impossible to parse $latitudeField in the event: ${row.mkString(",")}"))

    longitude match {
      case valueCast: String => valueCast
      case valueCast: Array[Byte] => new Predef.String(valueCast)
      case _ => longitude.toString
    }
  }

  private def geoField(latitude: String, longitude: String): String = latitude + separator + longitude
}
