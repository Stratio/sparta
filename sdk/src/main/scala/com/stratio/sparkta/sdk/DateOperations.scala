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

import com.github.nscala_time.time.Imports._

object DateOperations {

  final val Slash: String = "/"

  def getTimeFromGranularity(timeBucket: Option[String], granularity: Option[String]): Long =
    (timeBucket, granularity) match {
      case (Some(time), Some(granularity)) => dateFromGranularity(DateTime.now, granularity)
      case _ => 0L
    }

  def dateFromGranularity(value: DateTime, granularity: String): Long = {
    val secondsDate = value.withMillisOfSecond(0)
    val minutesDate = secondsDate.withSecondOfMinute(0)
    val hourDate = minutesDate.withMinuteOfHour(0)
    val dayDate = hourDate.withHourOfDay(0)
    val monthDate = dayDate.withDayOfMonth(1)
    val yearDate = monthDate.withMonthOfYear(1)

    granularity.toLowerCase match {
      case "minute" => minutesDate.getMillis
      case "hour" => hourDate.getMillis
      case "day" => dayDate.getMillis
      case "month" => monthDate.getMillis
      case "year" => yearDate.getMillis
      case "second" => secondsDate.getMillis
      case _ => 0L
    }
  }

  def millisToTimeStamp(date: Long): Timestamp = new Timestamp(date)

  def subPath(granularity: String, datePattern: Option[String]): String = {
    val suffix = dateFromGranularity(DateTime.now, granularity)
    if (!datePattern.isDefined || suffix.equals(0L)) Slash + suffix
    else Slash + DateTimeFormat.forPattern(datePattern.get).print(new DateTime(suffix)) + Slash + suffix
  }
}
