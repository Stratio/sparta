/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.sdk.helpers

import java.sql.Timestamp

import akka.event.slf4j.SLF4JLogging
import com.github.nscala_time.time.Imports._
import org.joda.time.{DateTimeConstants, Duration}

object AggregationTimeHelper extends SLF4JLogging {

  val DefaultGranularity = "second"
  val GranularityPropertyName = "granularity"
  val Precisions = Seq(
    "[1-9][0-9]*ms", "[1-9][0-9]*s", "[1-9][0-9]*m", "[1-9][0-9]*h", "[1-9][0-9]*d", "[1-9][0-9]*M", "[1-9][0-9]*y",
    "[1-9][0-9]*w", "undefinedms", "undefineds", "undefinedm", "undefinedh", "undefinedd", "undefinedw",
    "millisecond", "second", "minute", "hour", "day", "week", "month", "year",
    "[1-9][0-9]*millisecond", "[1-9][0-9]*second", "[1-9][0-9]*minute", "[1-9][0-9]*hour", "[1-9][0-9]*day",
    "[1-9][0-9]*week", "[1-9][0-9]*month", "[1-9][0-9]*year",
    "undefinedmillisecond", "undefinedsecond", "undefinedminute", "undefinedhour", "undefinedday", "undefinedweek",
    "undefinedmonth", "undefinedyear")

  def truncateDate(date: DateTime, granularity: String): Long =
    selectGranularity(precisionsMatches(granularity), granularity, date)

  def precisionsMatches(granularity: String): Seq[String] =
    Precisions.flatMap(precision => if (granularity.matches(precision)) Option(precision) else None)

  //scalastyle:off

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
        case "[1-9][0-9]*w" =>
          roundDateTime(date, Duration.standardDays(granularity.replace("w", "").toLong * 7))
        case "undefinedw" =>
          roundDateTime(date, Duration.standardDays(7L))
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
          roundDateTime(date, genericDates(date, "week"))
        case "[1-9][0-9]*month" | "undefinedmonth" | "[1-9][0-9]*M" =>
          log.debug(s"Cannot aggregate by $granularity. Aggregating by month...")
          roundDateTime(date, genericDates(date, "month"))
        case "[1-9][0-9]*year" | "undefinedyear" | "[1-9][0-9]*y" =>
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
    val weekDate = dayDate.withDayOfWeek(DateTimeConstants.MONDAY)
    val monthDate = dayDate.withDayOfMonth(1)
    val yearDate = monthDate.withMonthOfYear(1)

    granularity.toLowerCase match {
      case "millisecond" => Duration.millis(date.getMillis)
      case "second" => Duration.millis(secondsDate.getMillis)
      case "minute" => Duration.millis(minutesDate.getMillis)
      case "hour" => Duration.millis(hourDate.getMillis)
      case "day" => Duration.millis(dayDate.getMillis)
      case "week" => Duration.millis(weekDate.getMillis)
      case "month" => Duration.millis(monthDate.getMillis)
      case "year" => Duration.millis(yearDate.getMillis)
      case _ => throw new IllegalArgumentException("The granularity value is not valid")
    }
  }

  def roundDateTime(t: DateTime, d: Duration): Long =
    (t minus (t.getMillis - (t.getMillis.toDouble / d.getMillis).round * d.getMillis)).getMillis

  def millisToTimeStamp(date: Long): Timestamp = new Timestamp(date)

  def subPath(granularity: String, datePattern: Option[String]): String = {
    val suffix = truncateDate(DateTime.now, granularity)
    if (datePattern.isEmpty || suffix.equals(0L)) s"/$suffix"
    else s"/${DateTimeFormat.forPattern(datePattern.get).print(new DateTime(suffix))}/$suffix"
  }

  def parseValueToMilliSeconds(value: String): Long = {
    val prefix = for {
      prefix <- Precisions
      if value.matches(prefix)
    } yield prefix

    prefix.headOption match {
      case Some("[1-9][0-9]*ms") => value.replace("ms", "").toLong
      case Some("[1-9][0-9]*s") => value.replace("s", "").toLong * 1000
      case Some("[1-9][0-9]*m") => value.replace("m", "").toLong * 60000
      case Some("[1-9][0-9]*h") => value.replace("h", "").toLong * 3600000
      case Some("[1-9][0-9]*d") => value.replace("d", "").toLong * 86400000
      case Some("[1-9][0-9]*w") => value.replace("w", "").toLong * 86400000 * 7
      case Some("[1-9][0-9]*M") => value.replace("M", "").toLong * 2628000000L
      case Some("[1-9][0-9]*y") => value.replace("y", "").toLong * 31557600000L
      case Some(value) => parsePrecisionToMillis(value)
      case _ => value.toLong
    }
  }

  private def parsePrecisionToMillis(value: String): Long = {
    value match {
      case "second" => 1000L
      case "minute" => 60000L
      case "hour" => 3600000L
      case "day" => 86400000L
      case "week" => 604800000L
      case "month" => 2628000000L
      case "year" => 31557600000L
      case _ => value.toLong
    }
  }
}
