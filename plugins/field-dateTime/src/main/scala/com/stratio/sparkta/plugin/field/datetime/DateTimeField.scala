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

package com.stratio.sparkta.plugin.field.datetime

import java.io.{Serializable => JSerializable}
import java.util.Date

import akka.event.slf4j.SLF4JLogging
import org.joda.time.DateTime

import DateTimeField._
import com.stratio.sparkta.sdk._

case class DateTimeField(props: Map[String, JSerializable])
  extends DimensionType with JSerializable with SLF4JLogging {

  def this() {
    this(Map())
  }

  override val defaultTypeOperation = TypeOp.Timestamp

  override val operationProps: Map[String, JSerializable] = props

  override val properties: Map[String, JSerializable] = props ++ {
    if (!props.contains(GranularityPropertyName)) Map(GranularityPropertyName -> DefaultGranularity) else Map()
  }

  override def precision(keyName: String): Precision = keyName match {
    case SecondName => getPrecision(SecondName, getTypeOperation(SecondName))
    case MinuteName => getPrecision(MinuteName, getTypeOperation(MinuteName))
    case HourName => getPrecision(HourName, getTypeOperation(HourName))
    case DayName => getPrecision(DayName, getTypeOperation(DayName))
    case MonthName => getPrecision(MonthName, getTypeOperation(MonthName))
    case YearName => getPrecision(YearName, getTypeOperation(YearName))
    case s15Name => getPrecision(s15Name, getTypeOperation(s15Name))
    case s10Name => getPrecision(s10Name, getTypeOperation(s10Name))
    case s5Name => getPrecision(s5Name, getTypeOperation(s5Name))
    case _ => timestamp
  }

  @throws(classOf[ClassCastException])
  override def precisionValue(keyName: String, value: JSerializable): (Precision, JSerializable) =
    try {
      val precisionKey = precision(keyName)
      (precisionKey, DateTimeField.getPrecision(value.asInstanceOf[Date], precisionKey, properties))
    }
    catch {
      case cce: ClassCastException => {
        log.error("Error parsing " + value + " .")
        throw cce
      }
    }
}

object DateTimeField {

  final val DefaultGranularity = "second"
  final val GranularityPropertyName = "granularity"
  final val SecondName = "second"
  final val MinuteName = "minute"
  final val HourName = "hour"
  final val DayName = "day"
  final val MonthName = "month"
  final val YearName = "year"
  final val s15Name = "s15"
  final val s10Name = "s10"
  final val s5Name = "s5"
  final val timestamp = DimensionType.getTimestamp(Some(TypeOp.Timestamp), TypeOp.Timestamp)

  def getPrecision(value: Date, precision: Precision, properties: Map[String, JSerializable]): JSerializable = {
    TypeOp.transformValueByTypeOp(precision.typeOp,
      DateOperations.dateFromGranularity(new DateTime(value), precision match {
        case t if t == timestamp => if (properties.contains(GranularityPropertyName))
          properties.get(GranularityPropertyName).get.toString
        else DefaultGranularity
        case _ => precision.id
      })).asInstanceOf[JSerializable]
  }
}
