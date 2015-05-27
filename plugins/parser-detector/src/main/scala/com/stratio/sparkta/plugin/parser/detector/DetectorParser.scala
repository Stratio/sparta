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
package com.stratio.sparkta.plugin.parser.detector

import java.io.Serializable
import java.util.Date

import com.stratio.sparkta.sdk.ValidatingPropertyMap._
import com.stratio.sparkta.sdk.{Input, Event, Parser}
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import net.liftweb.json._

import scala.util.parsing.json.JSON

class DetectorParser(properties: Map[String, Serializable]) extends Parser(properties) {



  override def parse(data: Event): Event = {
    var event: Option[Event] = None
    data.keyMap.foreach(e => {
      if (Input.RAW_DATA_KEY.equals(e._1)) {
        val json = JSON.parseFull(e._2.toString)
        event = Some(new Event(json.get.asInstanceOf[Map[String, Serializable]],Some(e._2)))
        val columns = event.get.keyMap.get("columns").get.asInstanceOf[List[Map[String,String]]]
        val columnMap = columns.map(c => c.get("column").get-> c.get("value").getOrElse("")).toMap

        event = Some(new Event(columnMap.filter(m=> m._2!=""),None))


      }
    })

    val parsedEvent = event.getOrElse(data).asInstanceOf[Event]
    if (hasAlarms(parsedEvent.keyMap).contains(true))
     new Event(Map(),None)
    else
     parsedEvent
  }
  private def hasAlarms (map :Map[String, Serializable]):Set[Boolean] ={
     map.keySet.map(isAlarm(_))
    }
  def isAlarm(key:String): Boolean = {
    key match {
      case "alarm_timestamp" => true
      case "alarm_code" => true
      case "alarm_imei" => true
      case "alarm_lat" => true
      case "alarm_lon" => true
      case "alarm_sat_number" => true
      case "alarm_speed" => true
      case "alarm_direction" => true
      case "alarm_ignition" => true
      case "alarm_batt_tension" => true
      case "alarm_detl" => true
      case _ => false
    }
  }

}

