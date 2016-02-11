/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
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
import com.stratio.sparkta.plugin.field.datetime.DateTimeField._
import com.stratio.sparkta.sdk._
import org.joda.time.DateTime

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

  override def precision(keyName: String): Precision = {
    if (DateTimeField.Precisions.contains(keyName)) getPrecision(keyName, getTypeOperation(keyName))
    else timestamp
  }

  @throws(classOf[ClassCastException])
  override def precisionValue(keyName: String, value: JSerializable): (Precision, JSerializable) =
    try {
      val precisionKey = precision(keyName)
      (precisionKey, DateTimeField.getPrecision(TypeOp.transformValueByTypeOp(TypeOp.Date, value).asInstanceOf[Date],
        precisionKey, properties))
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
  final val s15Name = "15s"
  final val s10Name = "10s"
  final val s5Name = "5s"
  final val Precisions = Seq(SecondName, MinuteName, HourName, DayName, MonthName, YearName, s15Name, s10Name, s5Name)
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
