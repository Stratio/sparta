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
package com.stratio.sparta.plugin.parser.geo

import java.io.{Serializable => JSerializable}
import com.stratio.sparta.sdk.Parser
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.StructType

import scala.util.Try

class GeoParser(
  order: Integer,
  inputField: String,
  outputFields: Seq[String],
  schema: StructType,
  properties: Map[String, JSerializable]
) extends Parser(order, inputField, outputFields, schema, properties) {

  val defaultLatitudeField = "latitude"
  val defaultLongitudeField = "longitude"
  val separator = "__"

  val latitudeField = properties.getOrElse("latitude", defaultLatitudeField).toString
  val longitudeField = properties.getOrElse("longitude", defaultLongitudeField).toString

  def parse(data: Row, removeRaw: Boolean): Row = {

    val newData = addGeoField(data).toSeq
    val prevData = if (removeRaw) data.toSeq.drop(1) else data.toSeq

    Row.fromSeq(prevData ++ newData)
  }

  private def getLatitude(row: Row): Option[String] =
    Try(row.get(schema.fieldIndex(latitudeField)))
      .toOption
      .map(_.toString)

  private def getLongitude(row: Row): Option[String] =
    Try(row.get(schema.fieldIndex(longitudeField)))
      .toOption
      .map(_.toString)

  private def addGeoField(row: Row): Option[String] =
    for {
      lat   <- getLatitude(row)
      long  <- getLongitude(row)
    } yield lat + separator + long

}
