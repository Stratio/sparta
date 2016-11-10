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

package com.stratio.sparta.sdk

import akka.event.slf4j.SLF4JLogging
import com.github.nscala_time.time.Imports._
import org.joda.time.Duration

object AggregationTime extends SLF4JLogging{
  val Prefix: Seq[String] = Seq(
    "[1-9][0-9]*s","[1-9][0-9]*m","[1-9][0-9]*h", "[1-9][0-9]*d", "[1-9][0-9]*month", "second", "minute", "hour",
    "day", "month", "year", "[1-9][0-9]*second","[1-9][0-9]*minute","[1-9][0-9]*hour","[1-9][0-9]*day",
    "[1-9][0-9]*month", "[1-9][0-9]*year")

  def truncateDate(date: DateTime, granularity: String): Long = {
    val duration: Duration = parseDate(date, granularity)
    roundDateTime(date, duration)
  }

  private def parseDate(date: DateTime, aggregationTime: String): Duration = {
    val prefix = for {
      prefix <- Prefix
      if(aggregationTime.matches(prefix))
    } yield (prefix)
  selectGranularity(prefix, aggregationTime, date)
  }

  //noinspection ScalaStyle
  private def selectGranularity(prefix: Seq[String], aggregationTime: String, date: DateTime): Duration = {
    if(prefix.headOption.isDefined) {
      prefix(0) match {
        case "[1-9][0-9]*s" =>
          Duration.standardSeconds(aggregationTime.replace("s","").toLong)
        case "[1-9][0-9]*m" =>
          Duration.standardMinutes(aggregationTime.replace("m","").toLong)
        case "[1-9][0-9]*h" =>
          Duration.standardHours(aggregationTime.replace("h","").toLong)
        case "[1-9][0-9]*d" =>
          Duration.standardDays(aggregationTime.replace("d","").toLong)
        case "[1-9][0-9]*second" =>
          log.error(s"Cannot aggregate by ${aggregationTime}. Aggregating by second...")
          genericDates(date, "second")
        case "[1-9][0-9]*minute" =>
          log.error(s"Cannot aggregate by ${aggregationTime}. Aggregating by minute...")
          genericDates(date, "minute")
        case "[1-9][0-9]*hour" =>
          log.error(s"Cannot aggregate by ${aggregationTime}. Aggregating by hour...")
          genericDates(date, "hour")
        case "[1-9][0-9]*day" =>
          log.error(s"Cannot aggregate by ${aggregationTime}. Aggregating by day...")
          genericDates(date, "day")
        case "[1-9][0-9]*month" =>
          log.error(s"Cannot aggregate by ${aggregationTime}. Aggregating by month...")
          genericDates(date, "month")
        case "[1-9][0-9]*year" =>
          log.error(s"Cannot aggregate by ${aggregationTime}. Aggregating by year...")
          genericDates(date, "year")
        case _ =>
          genericDates(date, aggregationTime)
      }
    } else {
      throw new IllegalArgumentException("The value for the granularity is not valid")
    }
  }

  private def genericDates(date: DateTime, granularity: String): Duration = {

    val secondsDate = date.withMillisOfSecond(0)
    val minutesDate = secondsDate.withSecondOfMinute(0)
    val hourDate = minutesDate.withMinuteOfHour(0)
    val dayDate = hourDate.withHourOfDay(0)
    val monthDate = dayDate.withDayOfMonth(1)
    val yearDate = monthDate.withMonthOfYear(1)

    granularity.toLowerCase match {
      case "second" => Duration.millis(secondsDate.getMillis)
      case "minute" => Duration.millis(minutesDate.getMillis)
      case "hour" => Duration.millis(hourDate.getMillis)
      case "day" => Duration.millis(dayDate.getMillis)
      case "month" => Duration.millis(monthDate.getMillis)
      case "year" => Duration.millis(yearDate.getMillis)
      case _ => throw new IllegalArgumentException("The value for the granularity is not valid")
    }
  }

  private def roundDateTime(t: DateTime, d: Duration): Long =
    (t minus (t.getMillis - (t.getMillis.toDouble / d.getMillis).round * d.getMillis)).getMillis
}

