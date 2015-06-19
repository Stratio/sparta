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

package com.stratio.sparkta.plugin.parser.detector

import java.io.Serializable

import com.stratio.sparkta.sdk.{Event, Input, Parser}

import scala.util.parsing.json.JSON

class DetectorParser(properties: Map[String, Serializable]) extends Parser(properties) {

  def addGeoTo(event: Map[String, Serializable]): Map[String, Serializable] = {
    val lat = event.get("lat") match {
      case (Some(_: Serializable)) => if (event.get("lat") != Some(0.0)) event.get("lat") else None
      case (_) => None
    }
    val lon = event.get("lon") match {
      case (Some(_: Serializable)) => if (event.get("lon") != Some(0.0)) event.get("lon") else None
      case (_) => None
    }
    val mapToReturn = (lat, lon) match {
      case (Some(_), Some(_)) => "geo" -> Some(lat.get + "__" + lon.get)
      case (None, None) => "geo" -> ""
    }

    if (Map(mapToReturn).get("geo") != Some("__")) Map(mapToReturn)
    else Map()
  }

  def stringDimensionToDouble(dimensionName: String, newDimensionName: String, columnMap: Map[String, Any]):
  Map[String, Serializable] = {
    columnMap.get(dimensionName) match {
      case Some(x:Double) => Map(newDimensionName-> x)
      case Some(x:String) => if(x=="") Map() else Map(newDimensionName->x.toDouble)
      case Some(_)=> Map(newDimensionName -> columnMap.get(dimensionName).getOrElse("0").toString.toDouble)
      case None => Map()
    }
  }

  def cloneDimension(dimensionName: String, newDimensionName: String, columnMap: Map[String, String]):
  Map[String, String] = {
    Map(newDimensionName -> columnMap.get(dimensionName).getOrElse("undefined"))
  }

  override def parse(data: Event): Event = {
    var event: Option[Event] = None
    data.keyMap.foreach(e => {
      if (Input.RawDataKey.equals(e._1)) {
        val result = e._2 match {
          case s: String => s
          case b: Array[Byte] => new String(b)
        }

        JSON.globalNumberParser = {input: String => input.toDouble}
        val json = JSON.parseFull(result)
        event = Some(new Event(json.get.asInstanceOf[Map[String, Serializable]], Some(e._2)))
        val columns = event.get.keyMap.get("columns").get.asInstanceOf[List[Map[String, String]]]
        val columnMap = columns.map(c => c.get("column").get -> c.get("value").getOrElse("")).toMap

        val columnMapExtended = columnMap ++
          cloneDimension("company_root", "c_r", columnMap) ++
          cloneDimension("ou_vehicle", "ou_v", columnMap) ++
          cloneDimension("asset", "a", columnMap) ++
          cloneDimension("recorded_at_ms", "r_a_m", columnMap) ++
          cloneDimension("rpm_event_avg", "r_e_a", columnMap) ++
          cloneDimension("odometer", "o", columnMap) ++
          cloneDimension("path_id", "p_i", columnMap)

        val odometerMap = stringDimensionToDouble("odometer", "odometerNum", columnMapExtended)

        val rmpAvgMap = stringDimensionToDouble("rpm_event_avg", "rpmAvgNum", columnMapExtended)

        val resultMap = columnMapExtended ++ odometerMap ++ rmpAvgMap

        event = Some(new Event((resultMap
          .asInstanceOf[Map[String,Serializable]] ++
          addGeoTo(resultMap))
          .filter(m => m
          ._2 !=
          ""), None))
      }
    })

    val parsedEvent = event.getOrElse(data)
    if (!parsedEvent.keyMap.get("alarm_code").getOrElse("1").equals(0.0))
      new Event(Map(), None)
    else
      parsedEvent
  }
}

