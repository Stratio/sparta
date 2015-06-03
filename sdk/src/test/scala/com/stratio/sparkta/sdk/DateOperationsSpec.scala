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

import com.github.nscala_time.time.Imports._
import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DateOperationsSpec extends FlatSpec with ShouldMatchers {

  trait CommonValues {

    val granularity = "day"
    val datePattern = "yyyy/MM/dd"
    val expectedPath = "/" + DateTimeFormat.forPattern(datePattern) +
      DateOperations.dateFromGranularity(DateTime.now, granularity)
    val dt = DateTime.now
    val minuteDT = dt.withMillisOfSecond(0).withSecondOfMinute(0)
    val hourDT = minuteDT.withMinuteOfHour(0)
    val dayDT = hourDT.withHourOfDay(0)
    val monthDT = dayDT.withDayOfMonth(1)
    val yearDT = monthDT.withMonthOfYear(1)
    val wrongDT = 0L
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

  "DateOperationsSpec" should "return timestamp with correct parameters" in new CommonValues {
    DateOperations.getTimeFromGranularity(Some(""), Some("minute")) should be(minuteDT.getMillis)
    DateOperations.getTimeFromGranularity(Some(""), Some("hour")) should be(hourDT.getMillis)
    DateOperations.getTimeFromGranularity(Some(""), Some("day")) should be(dayDT.getMillis)
    DateOperations.getTimeFromGranularity(Some(""), Some("month")) should be(monthDT.getMillis)
    DateOperations.getTimeFromGranularity(Some(""), Some("year")) should be(yearDT.getMillis)
    DateOperations.getTimeFromGranularity(Some("asdasd"), Some("year")) should be(yearDT.getMillis)
    DateOperations.getTimeFromGranularity(Some(""), Some("bad")) should be(wrongDT)
  }

  it should "return parsed timestamp with granularity" in new CommonValues {
    DateOperations.dateFromGranularity(dt, "minute") should be(minuteDT.getMillis)
    DateOperations.dateFromGranularity(dt, "hour") should be(hourDT.getMillis)
    DateOperations.dateFromGranularity(dt, "day") should be(dayDT.getMillis)
    DateOperations.dateFromGranularity(dt, "month") should be(monthDT.getMillis)
    DateOperations.dateFromGranularity(dt, "year") should be(yearDT.getMillis)
    DateOperations.dateFromGranularity(dt, "bad") should be(wrongDT)
  }

  it should "format path ignoring pattern" in new FailValues {
    DateOperations.subPath(badGranularity, datePattern) should be(expectedPath)
    DateOperations.subPath(badGranularity, datePattern) should be(expectedPath)
    DateOperations.subPath(badGranularity, emptyPattern) should be(expectedPath)
    DateOperations.subPath(granularity, emptyPattern) should be(expectedGranularityPath)
    DateOperations.subPath(granularity, datePattern) should be(expectedGranularityWithPattern)
  }
}
