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


package com.stratio.sparkta.sdk

import java.sql.Timestamp

import org.joda.time.DateTime

object DateOperations {

  def getTimeFromGranularity(timeBucket: Option[String], granularity: Option[String]): Option[DateTime] =
    (timeBucket, granularity) match {
      case (Some(time), Some(granularity)) => Some(dateFromGranularity(DateTime.now(), granularity))
      case _ => None
    }

  def dateFromGranularity(value: DateTime, granularity: String): DateTime = {
    val secondsDate = value.withMillisOfSecond(0)
    val minutesDate = secondsDate.withSecondOfMinute(0)
    val hourDate = minutesDate.withMinuteOfHour(0)
    val dayDate = hourDate.withHourOfDay(0)
    val monthDate = dayDate.withDayOfMonth(1)
    val yearDate = monthDate.withMonthOfYear(1)

    granularity.toLowerCase match {
      case "minute" => minutesDate
      case "hour" => hourDate
      case "day" => dayDate
      case "month" => monthDate
      case "year" => yearDate
      case _ => secondsDate
    }
  }

  def dateTimeToTimeStamp(dateOp : Option[DateTime]) : Option[Timestamp] = dateOp match {
    case Some(date) => Some(new Timestamp(date.getMillis))
    case None => None
  }

  def dateTimeToMillis(dateOp : Option[DateTime]) : Long = dateOp match {
    case Some(date) => date.getMillis
    case None => 0L
  }
}
