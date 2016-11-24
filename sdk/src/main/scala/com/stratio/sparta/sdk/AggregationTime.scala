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

package com.stratio.sparta.sdk

import akka.event.slf4j.SLF4JLogging
import com.github.nscala_time.time.Imports._
import org.joda.time.Duration

object AggregationTime extends SLF4JLogging {

  val DefaultGranularity = "second"
  val GranularityPropertyName = "granularity"
  val Precisions = Seq(
    "[1-9][0-9]*ms", "[1-9][0-9]*s", "[1-9][0-9]*m", "[1-9][0-9]*h", "[1-9][0-9]*d",
    "undefinedms", "undefineds", "undefinedm", "undefinedh", "undefinedd",
    "millisecond", "second", "minute", "hour", "day", "week", "month", "year",
    "[1-9][0-9]*millisecond", "[1-9][0-9]*second", "[1-9][0-9]*minute", "[1-9][0-9]*hour", "[1-9][0-9]*day",
    "[1-9][0-9]*week", "[1-9][0-9]*month", "[1-9][0-9]*year",
    "undefinedmillisecond", "undefinedsecond", "undefinedminute", "undefinedhour", "undefinedday", "undefinedweek",
    "undefinedmonth", "undefinedyear")

  def truncateDate(date: DateTime, granularity: String): Long =
    selectGranularity(precisionsMatches(granularity), granularity, date)

  def precisionsMatches(granularity: String): Seq[String] =
    Precisions.flatMap(precision => if (granularity.matches(precision)) Option(precision) else None)

  //noinspection ScalaStyle
  private def selectGranularity(prefix: Seq[String], granularity: String, date: DateTime): Long = {
    if (prefix.nonEmpty) {
      prefix.head match {
        case "[1-9][0-9]*ms" =>
          roundDateTime(date, Duration.millis(granularity.replace("ms", "").toLong))
        case "undefinedms" =>
          roundDateTime(date, Duration.millis(1L))
        case "[1-9][0-9]*s" =>
          roundDateTime(date, Duration.standardSeconds(granularity.replace("s", "").toLong))
        case "undefineds" =>
          roundDateTime(date, Duration.standardSeconds(1L))
        case "[1-9][0-9]*m" =>
          roundDateTime(date, Duration.standardMinutes(granularity.replace("m", "").toLong))
        case "undefinedm" =>
          roundDateTime(date, Duration.standardMinutes(1L))
        case "[1-9][0-9]*h" =>
          roundDateTime(date, Duration.standardHours(granularity.replace("h", "").toLong))
        case "undefinedh" =>
          roundDateTime(date, Duration.standardHours(1L))
        case "[1-9][0-9]*d" =>
          roundDateTime(date, Duration.standardDays(granularity.replace("d", "").toLong))
        case "undefinedd" =>
          roundDateTime(date, Duration.standardDays(1L))
        case "[1-9][0-9]*millisecond" | "undefinedmillisecond" =>
          log.debug(s"Cannot aggregate by $granularity. Aggregating by millisecond...")
          roundDateTime(date, genericDates(date, "millisecond"))
        case "[1-9][0-9]*second" | "undefinedsecond" =>
          log.debug(s"Cannot aggregate by $granularity. Aggregating by second...")
          roundDateTime(date, genericDates(date, "second"))
        case "[1-9][0-9]*minute" | "undefinedminute" =>
          log.debug(s"Cannot aggregate by $granularity. Aggregating by minute...")
          roundDateTime(date, genericDates(date, "minute"))
        case "[1-9][0-9]*hour" | "undefinedhour" =>
          log.debug(s"Cannot aggregate by $granularity. Aggregating by hour...")
          roundDateTime(date, genericDates(date, "hour"))
        case "[1-9][0-9]*day" | "undefinedday" =>
          log.debug(s"Cannot aggregate by $granularity. Aggregating by day...")
          roundDateTime(date, genericDates(date, "day"))
        case "[1-9][0-9]*week" | "undefinedweek" =>
          log.debug(s"Cannot aggregate by $granularity. Aggregating by week...")
          genericDates(date, "week").getMillis
        case "[1-9][0-9]*month" | "undefinedmonth" =>
          log.debug(s"Cannot aggregate by $granularity. Aggregating by month...")
          roundDateTime(date, genericDates(date, "month"))
        case "[1-9][0-9]*year" | "undefinedyear" =>
          log.debug(s"Cannot aggregate by $granularity. Aggregating by year...")
          roundDateTime(date, genericDates(date, "year"))
        case _ =>
          roundDateTime(date, genericDates(date, granularity))
      }
    } else {
      throw new IllegalArgumentException("The granularity value is not valid")
    }
  }

  private def genericDates(date: DateTime, granularity: String): Duration = {

    val secondsDate = date.withMillisOfSecond(0)
    val minutesDate = secondsDate.withSecondOfMinute(0)
    val hourDate = minutesDate.withMinuteOfHour(0)
    val dayDate = hourDate.withHourOfDay(0)
    val weekDate = dayDate.weekOfWeekyear.get()
    val monthDate = dayDate.withDayOfMonth(1)
    val yearDate = monthDate.withMonthOfYear(1)

    granularity.toLowerCase match {
      case "millisecond" => Duration.millis(date.getMillis)
      case "second" => Duration.millis(secondsDate.getMillis)
      case "minute" => Duration.millis(minutesDate.getMillis)
      case "hour" => Duration.millis(hourDate.getMillis)
      case "day" => Duration.millis(dayDate.getMillis)
      case "week" => Duration.millis(weekDate)
      case "month" => Duration.millis(monthDate.getMillis)
      case "year" => Duration.millis(yearDate.getMillis)
      case _ => throw new IllegalArgumentException("The granularity value is not valid")
    }
  }

  private def roundDateTime(t: DateTime, d: Duration): Long =
    (t minus (t.getMillis - (t.getMillis.toDouble / d.getMillis).round * d.getMillis)).getMillis
}
