/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
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
package com.stratio.sparkta.plugin.bucketer.datetime

import java.io
import java.util.Date

import com.stratio.sparkta.plugin.bucketer.datetime.DateTimeBucketer._
import com.stratio.sparkta.sdk.{BucketType, Bucketer}
import org.joda.time.DateTime

/**
 * Created by ajnavarro on 9/10/14.
 */
case class DateTimeBucketer() extends Bucketer {

  override val bucketTypes: Seq[BucketType] = Seq(seconds, minutes, hours, days, months, years)

  override def bucket(value: io.Serializable): Map[BucketType, io.Serializable] =
    bucketTypes.map(bucketType =>
      bucketType -> DateTimeBucketer.bucket(value.asInstanceOf[Date], bucketType)
    ).toMap

}

object DateTimeBucketer {
  private def bucket(value: Date, bucketType: BucketType): io.Serializable = {
    val secondsDate = new DateTime(value).withMillisOfSecond(0)
    val minutesDate = secondsDate.withSecondOfMinute(0)
    val hourDate = minutesDate.withMinuteOfHour(0)
    val dayDate = hourDate.withHourOfDay(0)
    val monthDate = dayDate.withDayOfMonth(1)
    val yearDate = monthDate.withMonthOfYear(1)

    (bucketType match {
      case s if s == seconds => secondsDate
      case m if m == minutes => minutesDate
      case h if h == hours => hourDate
      case d if d == days => dayDate
      case mo if mo == months => monthDate
      case y if y == years => yearDate
    }).toDate.asInstanceOf[io.Serializable]
  }

  val seconds = new BucketType("second")
  val minutes = new BucketType("minute")
  val hours = new BucketType("hour")
  val days = new BucketType("day")
  val months = new BucketType("month")
  val years = new BucketType("year")

}
