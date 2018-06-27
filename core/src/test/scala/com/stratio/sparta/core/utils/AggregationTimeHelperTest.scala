/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.core.utils

import java.io.{Serializable => JSerializable}
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import com.github.nscala_time.time.Imports._
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import com.stratio.sparta.core.helpers.AggregationTimeHelper._

@RunWith(classOf[JUnitRunner])
class AggregationTimeHelperTest extends FlatSpec with ShouldMatchers {

  trait CommonValues {
    
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.ZZZ")
      .withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")))
    val dateTime = formatter.parseDateTime("2016-01-19 14:49:19.UTC")
      .withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")))
    val simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ZZZ")
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
    val granularity = "day"
    val datePattern = "yyyy/MM/dd"
    val expectedPath = "/" + DateTimeFormat.forPattern(datePattern) +
      truncateDate(DateTime.now.withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC"))), granularity)
    val dt = DateTime.now
      .withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")))
    val date = new Date(dt.getMillis)
    val timestamp = new Timestamp(dt.getMillis)
    val minuteDT = dt.withMillisOfSecond(0).withSecondOfMinute(0)
    val hourDT = minuteDT.withMinuteOfHour(0)
    val dayDT = hourDT.withHourOfDay(0)
    val monthDT = dayDT.withDayOfMonth(1)
    val yearDT = monthDT.withMonthOfYear(1)
    val s15DT = roundDateTime(dt, Duration.standardSeconds(15))
    val s10DT = roundDateTime(dt, Duration.standardSeconds(10))
    val s5DT = roundDateTime(dt, Duration.standardSeconds(5))
    val wrongDT = 0L
    val expectedRawPath = "/year=1984/month=03/day=17/hour=13/minute=13/second=13"
  }

  trait FailValues {

    val emptyGranularity = ""
    val badGranularity = "minutely"
    val granularity = "minute"
    val datePattern = Some("yyyy/MM/dd")
    val emptyPattern = None
    val expectedPath = "/0"
    val dt = DateTime.now
    val expectedGranularityPath = "/" + dt.withMillisOfSecond(0).withSecondOfMinute(0).getMillis
    val expectedGranularityWithPattern = "/" + DateTimeFormat.forPattern(datePattern.get).print(dt) + "/" +
      dt.withMillisOfSecond(0).withSecondOfMinute(0).getMillis
  }

  trait ParquetPath {

    val yearStr = "year"
    val monthStr = "month"
    val dayStr = "day"
    val hourStr = "hour"
    val minuteStr = "minute"
    val weekStr = "week"
    val defaultStr = "whatever"

    val yearPattern = "/'year='yyyy/'"
    val monthPattern = "/'year='yyyy/'month='MM/'"
    val dayPattern = "/'year='yyyy/'month='MM/'day='dd/'"
    val hourPattern = "/'year='yyyy/'month='MM/'day='dd/'hour='HH/'"
    val minutePattern = "/'year='yyyy/'month='MM/'day='dd/'hour='HH/'minute='mm/'"
    val defaultPattern = "/'year='yyyy/'month='MM/'day='dd/'hour='HH/'minute='mm/'second='ss"

    val yearPatternResult = DateTimeFormat.forPattern(yearPattern).print(DateTime.now())
    val monthPatternResult = DateTimeFormat.forPattern(monthPattern).print(DateTime.now())
    val dayPatternResult = DateTimeFormat.forPattern(dayPattern).print(DateTime.now())
    val hourPatternResult = DateTimeFormat.forPattern(hourPattern).print(DateTime.now())
    val minutePatternResult = DateTimeFormat.forPattern(minutePattern).print(DateTime.now())
    val defaultPatternResult = DateTimeFormat.forPattern(defaultPattern).print(DateTime.now())

  }

  it should "return the date in millis rounded to 15s" in new CommonValues {

    val result = truncateDate(dateTime,"15s")

    simpleDateFormat.format(new Date(result)) should be("2016-01-19 14:49:15.+0000")
  }

  it should "return the date in millis rounded to 15m" in new CommonValues {

    val result = truncateDate(dateTime,"15m")

    simpleDateFormat.format(new Date(result)) should be("2016-01-19 14:45:00.+0000")
  }

  it should "return the date in millis rounded to 15h" in new CommonValues {

    val result = truncateDate(dateTime,"15h")

    simpleDateFormat.format(new Date(result)) should be("2016-01-19 09:00:00.+0000")
  }

  it should "return the date in millis rounded to 2w" in new CommonValues {

    val result = truncateDate(dateTime,"2w")

    simpleDateFormat.format(new Date(result)) should be("2016-01-14 00:00:00.+0000")
  }

  it should "return the date in millis rounded to 15d" in new CommonValues {

    val result = truncateDate(dateTime,"15d")

    simpleDateFormat.format(new Date(result)) should be("2016-01-15 00:00:00.+0000")
  }

  it should "return the date in millis rounded to seconds" in new CommonValues {

    val result = truncateDate(dateTime,"second")

    simpleDateFormat.format(new Date(result)) should be("2016-01-19 14:49:19.+0000")
  }

  it should "return the date in millis rounded to minutes" in new CommonValues {

    val result = truncateDate(dateTime,"minute")

    simpleDateFormat.format(new Date(result)) should be("2016-01-19 14:49:00.+0000")
  }

  "AggregationTime with 45minute" should "return the date in millis rounded to minutes" in new CommonValues {

    val result = truncateDate(dateTime,"45minute")

    simpleDateFormat.format(new Date(result)) should be("2016-01-19 14:49:00.+0000")
  }

  it should "return the date in millis rounded to hours" in new CommonValues {

    val result = truncateDate(dateTime,"hour")

    simpleDateFormat.format(new Date(result)) should be("2016-01-19 14:00:00.+0000")
  }

  "AggregationTime with 3hour" should "return the date in millis rounded to hours" in new CommonValues {

    val result = truncateDate(dateTime,"3hour")

    simpleDateFormat.format(new Date(result)) should be("2016-01-19 14:00:00.+0000")
  }

  it should "return the date in millis rounded to days" in new CommonValues {

    val result = truncateDate(dateTime,"day")

    simpleDateFormat.format(new Date(result)) should be("2016-01-19 00:00:00.+0000")
  }

  it should "return the date in millis rounded to weeks" in new CommonValues {

    val result = truncateDate(dateTime,"week")

    simpleDateFormat.format(new Date(result)) should be("2016-01-18 00:00:00.+0000")
  }

  "AggregationTime with 34day" should "return the date in millis rounded to days" in new CommonValues {

    val result = truncateDate(dateTime,"34day")

    simpleDateFormat.format(new Date(result)) should be("2016-01-19 00:00:00.+0000")
  }

  it should "return the date in millis rounded to year" in new CommonValues {

    val result = truncateDate(dateTime,"year")

    simpleDateFormat.format(new Date(result)) should be("2016-01-01 00:00:00.+0000")
  }

  "AggregationTime with 2month" should "return the date in millis rounded to month" in new CommonValues {

    val result = truncateDate(dateTime,"2month")

    simpleDateFormat.format(new Date(result)) should be("2016-01-01 00:00:00.+0000")
  }

  "AggregationTime with 2year" should "return the date in millis rounded to year" in new CommonValues {

    val result = truncateDate(dateTime,"2year")

    simpleDateFormat.format(new Date(result)) should be("2016-01-01 00:00:00.+0000")
  }

  it should "return parsed timestamp with granularity" in new CommonValues {
    truncateDate(dt, "5s") should be(s5DT)
    truncateDate(dt, "15s") should be(s15DT)
    truncateDate(dt, "10s") should be(s10DT)
    truncateDate(dt, "minute") should be(minuteDT.getMillis)
    truncateDate(dt, "hour") should be(hourDT.getMillis)
    truncateDate(dt, "day") should be(dayDT.getMillis)
    truncateDate(dt, "month") should be(monthDT.getMillis)
    truncateDate(dt, "year") should be(yearDT.getMillis)
    an[IllegalArgumentException] should be thrownBy truncateDate(dt, "bad")
  }

  it should "format path ignoring pattern" in new FailValues {
    an[IllegalArgumentException] should be thrownBy  subPath(badGranularity, datePattern)
    an[IllegalArgumentException] should be thrownBy  subPath(badGranularity, datePattern)
    an[IllegalArgumentException] should be thrownBy  subPath(badGranularity, emptyPattern)
    subPath(granularity, emptyPattern) should be(expectedGranularityPath)
    subPath(granularity, datePattern) should be(expectedGranularityWithPattern)
  }

  it should "round to 15 seconds" in new CommonValues {
    val now = formatter.parseDateTime("1984-03-17 13:13:17.UTC")
    truncateDate(now, "15s") should be(448377195000L)
  }
  it should "round to 10 seconds" in new CommonValues {
    val now = formatter.parseDateTime("1984-03-17 13:13:17.UTC")
    truncateDate(now, "10s") should be(448377200000L)
  }
  it should "round to 5 seconds" in new CommonValues {
    val now = formatter.parseDateTime("1984-03-17 13:13:17.UTC")
    truncateDate(now, "5s") should be(448377195000L)
  }

  it should "parseValueToMilliSeconds should return 1 second when precision is second" in new CommonValues {
    val res = parseValueToMilliSeconds("second")
    res should be(1000)
  }

  it should "parseValueToMilliSeconds should return 1 second" in new CommonValues {
    val res = parseValueToMilliSeconds("1s")
    res should be(1000)
  }

  it should "parseValueToMilliSeconds should return 1 second when precision is minute" in new CommonValues {
    val res = parseValueToMilliSeconds("minute")
    res should be(60000)
  }

  it should "parseValueToMilliSeconds should return a value in seconds of 1 minute" in new CommonValues {
    val res = parseValueToMilliSeconds("1m")
    res should be(60000)
  }

  it should "parseValueToMilliSeconds should return a value in seconds of 1 hour" in new CommonValues {
    val res = parseValueToMilliSeconds("1h")
    res should be(3600000)
  }

  it should "parseValueToMilliSeconds should return 1 second when precision is hour" in new CommonValues {
    val res = parseValueToMilliSeconds("hour")
    res should be(3600000)
  }

  it should "parseValueToMilliSeconds should return 1 second when precision is day" in new CommonValues {
    val res = parseValueToMilliSeconds("day")
    res should be(86400000)
  }
  
  it should "parseValueToMilliSeconds should return a value in seconds of 1 day" in new CommonValues {
    val res = parseValueToMilliSeconds("1d")
    res should be(86400000)
  }

  it should "parseValueToMilliSeconds should return a value in seconds of 1 month" in new CommonValues {
    val res = parseValueToMilliSeconds("1M")
    res should be(2628000000L)
  }

  it should "parseValueToMilliSeconds should return a value in seconds of year" in new CommonValues {
    val res = parseValueToMilliSeconds("year")
    res should be(31557600000L)
  }

  it should "parseValueToMilliSeconds should return a value in seconds of 1 year" in new CommonValues {
    val res = parseValueToMilliSeconds("1y")
    res should be(31557600000L)
  }

  it should "parseValueToMilliSeconds should return 30 second" in new CommonValues {
    val res = parseValueToMilliSeconds("30s")
    res should be(30000)
  }

  it should "parseValueToMilliSeconds should return 30000 second" in new CommonValues {
    val res = parseValueToMilliSeconds("30000")
    res should be(30000)
  }

  it should "parseValueToMilliSeconds should return 2 days" in new CommonValues {
    val res = parseValueToMilliSeconds("2d")
    res should be(172800000)
  }

  it should "parseValueToMilliSeconds should return 6 milliseconds" in new CommonValues {
    val res = parseValueToMilliSeconds("6ms")
    res should be(6)
  }

  it should "parseValueToMilliSeconds should return 6 seconds" in new CommonValues {
    val res = parseValueToMilliSeconds("6000ms")
    res should be(6000)
  }

  it should "parseValueToMilliSeconds should return 6500 milliseconds" in new CommonValues {
    val res = parseValueToMilliSeconds("6500")
    res should be(6500)
  }

}
