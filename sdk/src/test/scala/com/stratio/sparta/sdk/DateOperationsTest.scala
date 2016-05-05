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

import java.io.{Serializable => JSerializable}
import java.sql.Timestamp
import java.util.Date

import com.github.nscala_time.time.Imports._
import com.stratio.sparta.sdk.DateOperations
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DateOperationsTest extends FlatSpec with ShouldMatchers {

  trait CommonValues {

    val granularity = "day"
    val datePattern = "yyyy/MM/dd"
    val expectedPath = "/" + DateTimeFormat.forPattern(datePattern) +
      DateOperations.dateFromGranularity(DateTime.now, granularity)
    val dt = DateTime.now
    val date = new Date(dt.getMillis)
    val timestamp = new Timestamp(dt.getMillis)
    val minuteDT = dt.withMillisOfSecond(0).withSecondOfMinute(0)
    val hourDT = minuteDT.withMinuteOfHour(0)
    val dayDT = hourDT.withHourOfDay(0)
    val monthDT = dayDT.withDayOfMonth(1)
    val yearDT = monthDT.withMonthOfYear(1)
    val s15DT = DateOperations.roundDateTime(dt, Duration.standardSeconds(15))
    val s10DT = DateOperations.roundDateTime(dt, Duration.standardSeconds(10))
    val s5DT = DateOperations.roundDateTime(dt, Duration.standardSeconds(5))
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

  "DateOperations" should "return timestamp with correct parameters" in new CommonValues {
    DateOperations.getTimeFromGranularity(Some(""), Some("5s")) should not be (wrongDT)
    DateOperations.getTimeFromGranularity(Some(""), Some("10s")) should not be (wrongDT)
    DateOperations.getTimeFromGranularity(Some(""), Some("15s")) should not be (wrongDT)
    DateOperations.getTimeFromGranularity(Some(""), Some("minute")) should not be (wrongDT)
    DateOperations.getTimeFromGranularity(Some(""), Some("hour")) should not be (wrongDT)
    DateOperations.getTimeFromGranularity(Some(""), Some("day")) should not be (wrongDT)
    DateOperations.getTimeFromGranularity(Some(""), Some("month")) should not be (wrongDT)
    DateOperations.getTimeFromGranularity(Some(""), Some("year")) should not be (wrongDT)
    DateOperations.getTimeFromGranularity(Some("asdasd"), Some("year")) should not be (wrongDT)
    an[IllegalArgumentException] should be thrownBy DateOperations.getTimeFromGranularity(Some(""), Some("bad"))
  }

  it should "return parsed timestamp with granularity" in new CommonValues {
    DateOperations.dateFromGranularity(dt, "5s") should be(s5DT.getMillis)
    DateOperations.dateFromGranularity(dt, "15s") should be(s15DT.getMillis)
    DateOperations.dateFromGranularity(dt, "10s") should be(s10DT.getMillis)
    DateOperations.dateFromGranularity(dt, "minute") should be(minuteDT.getMillis)
    DateOperations.dateFromGranularity(dt, "hour") should be(hourDT.getMillis)
    DateOperations.dateFromGranularity(dt, "day") should be(dayDT.getMillis)
    DateOperations.dateFromGranularity(dt, "month") should be(monthDT.getMillis)
    DateOperations.dateFromGranularity(dt, "year") should be(yearDT.getMillis)
    an[IllegalArgumentException] should be thrownBy DateOperations.dateFromGranularity(dt, "bad")
  }

  it should "format path ignoring pattern" in new FailValues {
    an[IllegalArgumentException] should be thrownBy  DateOperations.subPath(badGranularity, datePattern)
    an[IllegalArgumentException] should be thrownBy  DateOperations.subPath(badGranularity, datePattern)
    an[IllegalArgumentException] should be thrownBy  DateOperations.subPath(badGranularity, emptyPattern)
    DateOperations.subPath(granularity, emptyPattern) should be(expectedGranularityPath)
    DateOperations.subPath(granularity, datePattern) should be(expectedGranularityWithPattern)
  }

  it should "round to 15 seconds" in new CommonValues {
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.ZZZ")
    val now = formatter.parseDateTime("1984-03-17 13:13:17.CET")
    DateOperations.dateFromGranularity(now, "15s") should be(448373595000L)
  }
  it should "round to 10 seconds" in new CommonValues {
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.ZZZ")
    val now = formatter.parseDateTime("1984-03-17 13:13:17.CET")
    DateOperations.dateFromGranularity(now, "10s") should be(448373600000L)
  }
  it should "round to 5 seconds" in new CommonValues {
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.ZZZ")
    val now = formatter.parseDateTime("1984-03-17 13:13:17.CET")
    DateOperations.dateFromGranularity(now, "5s") should be(448373595000L)
  }

  it should "return millis from a Serializable date" in new CommonValues {
    DateOperations.getMillisFromSerializable(dt.asInstanceOf[JSerializable]) should be(dt.getMillis)
    DateOperations.getMillisFromSerializable(timestamp.asInstanceOf[JSerializable]) should be(dt.getMillis)
    DateOperations.getMillisFromSerializable(date.asInstanceOf[JSerializable]) should be(dt.getMillis)
    DateOperations.getMillisFromSerializable(1L.asInstanceOf[JSerializable]) should be(1L)
    DateOperations.getMillisFromSerializable("1".asInstanceOf[JSerializable]) should be(1L)
  }

  it should "return millis from a Serializable dateTime" in new CommonValues {
    DateOperations.getMillisFromDateTime(dt.asInstanceOf[JSerializable]) should be(dt.getMillis)
    DateOperations.getMillisFromDateTime(timestamp.asInstanceOf[JSerializable]) should be(dt.getMillis)
    DateOperations.getMillisFromDateTime(date.asInstanceOf[JSerializable]) should be(dt.getMillis)
  }
}
